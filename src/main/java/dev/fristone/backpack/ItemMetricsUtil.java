package dev.fristone.backpack;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemMetricsUtil {
    private ItemMetricsUtil() {}

    // 这些阈值和上限按你项目里现有取值来；先给默认
    private static final double TOOL_MIN = 1.20;
    private static final double TOOL_MAX = 5.00;
    private static final double HELMET_MAX = 4.0, CHEST_MAX = 16.0, LEGS_MAX = 10.0, BOOTS_MAX = 4.0;

    // ===== V1：单件“原值”（不要乘数量；尽量不做 clamp）=====
    public static double v1Raw(ItemStack stack) {
        // 1) 工具/武器：名字启发式（用你已有的 NameHeuristics）
        if (stack.getItem() instanceof TieredItem || stack.getItem() instanceof ProjectileWeaponItem) {
            double base = guessToolMaterialMassByName(stack);
            return base * 0.60;
        }
        // 2) 护甲：名字启发式
        if (stack.getItem() instanceof ArmorItem armor) {
            double base = guessArmorMaterialMassByName(armor);
            return base * 0.70;
        }
        // 3) 容器（沿用旧逻辑）
        Double cont = containerSingle(stack);
        if (cont != null) return cont;
        // 4) 其他未知：0.2 × 旧体积系数（V1）
        return 0.2 * WeightRegistry.v1StackFactor(stack.getMaxStackSize());
    }

    // ===== V2：单件“原值”（修复材料 / 标签 / 容器）=====
    public static double v2Raw(ItemStack stack) {
        var cw = V2.containerWeight(stack);
        if (cw.isPresent()) return cw.get();

        if (stack.getItem() instanceof TieredItem || stack.getItem() instanceof ProjectileWeaponItem) {
            double ingotMass = V2.massFromToolRepair(stack).orElse(2.0);
            double headCoeff = V2.toolHeadCoeff(stack); // pick=3, axe=3, sword=2, shovel=1, hoe=2, bow/crossbow=1.5
            return (ingotMass * headCoeff + 0.1) * 0.60;
        }
        if (stack.getItem() instanceof ArmorItem armor) {
            double ingotMass = V2.massFromArmorRepair(armor).orElse(2.0);
            int ingots = V2.armorIngotCount(armor.getEquipmentSlot()); // 头5/胸8/腿7/靴4
            return ingotMass * ingots * 0.70;
        }
        return V2.massFromTagsOrCategory(stack).orElse(0.2);
    }

    // ===== 保护性限制（融合后再用）=====
    public static double clampTool(double v) {
        return Math.max(TOOL_MIN, Math.min(TOOL_MAX, v));
    }
    public static double capArmor(ItemStack stack, double v) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return v;
        double cap = switch (armor.getEquipmentSlot()) {
            case HEAD -> HELMET_MAX;
            case CHEST -> CHEST_MAX;
            case LEGS -> LEGS_MAX;
            case FEET -> BOOTS_MAX;
            default -> 8.0;
        };
        return Math.min(v, cap);
    }
    private static double guessToolMaterialMassByName(ItemStack stack) {
        String key = String.valueOf(ForgeRegistries.ITEMS.getKey(stack.getItem()));
        // 粗略估一个“原料质量”（单位：kg），与我们之前约定一致
        if (key.contains("wood") || key.contains("wooden")) return 1.0;   // 木
        if (key.contains("stone")) return 2.0;                            // 石
        if (key.contains("iron"))  return 6.0;                            // ≈3锭
        if (key.contains("gold"))  return 6.9;                            // 金更密
        if (key.contains("diamond")) return 2.0;                          // 宝石 + 金属箍
        if (key.contains("netherite")) return 10.0;
        // 未命中：给个温和默认
        return 2.0;
    }

    private static double guessArmorMaterialMassByName(ArmorItem armor) {
        String key = String.valueOf(ForgeRegistries.ITEMS.getKey(armor));
        double ingotKg = 2.0; // 缺省按铁
        if (key.contains("gold"))     ingotKg = 2.3;
        else if (key.contains("diamond"))   ingotKg = 0.7;  // 宝石甲有金属底，粗估
        else if (key.contains("netherite")) ingotKg = 2.6;

        int ingotCount = switch (armor.getEquipmentSlot()) {
            case HEAD -> 5; case CHEST -> 8; case LEGS -> 7; case FEET -> 4;
            default -> 6;
        };
        return ingotKg * ingotCount;
    }
    // ===== 旧容器单件值（V1 用）=====
    private static Double containerSingle(ItemStack s) {
        Item i = s.getItem();
        if (i == Items.BUCKET)       return 0.5;
        if (i == Items.WATER_BUCKET) return 0.5 + 1.0;
        if (i == Items.LAVA_BUCKET)  return 0.5 + 1.0; // 需要更重你再调
        if (i == Items.MILK_BUCKET)  return 0.5 + 1.0;
        if (i == Items.GLASS_BOTTLE) return 0.08;
        if (i == Items.HONEY_BOTTLE) return 0.08 + 0.35;
        if (i == Items.BOWL)         return 0.05;
        if (i == Items.MUSHROOM_STEW || i == Items.RABBIT_STEW
                || i == Items.SUSPICIOUS_STEW || i == Items.BEETROOT_SOUP)
            return 0.05 + 0.30;
        return null;
    }
}