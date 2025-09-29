package dev.fristone.backpack;

import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public final class MetricsBlender {
    private MetricsBlender() {}

    // 可配置（先写死，之后用你的 WeightsReload 覆盖）
    public static class Blend {
        public static boolean enable = true;     // 关掉时，只用手工表或 V1
        public static double alphaGlobal   = 0.7;
        public static double alphaTool     = 0.8;
        public static double alphaArmor    = 0.8;
        public static double alphaContainer= 1.0;
        public static double alphaTagged   = 0.7;
        public static double alphaUnknown  = 0.5;
        public static boolean clampTool    = true;
        public static boolean capArmor     = true;
    }

    private static double stackFactorV2(int maxStack) {
        if (maxStack >= 64) return 1.00;
        if (maxStack >= 16) return 1.25;
        return 1.50;
    }

    enum Kind { EXPLICIT, CONTAINER, TOOL, ARMOR, TAGGED, UNKNOWN }

    private static Kind detectKind(ItemStack s) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(s.getItem());
        if (WeightRegistry.lookupManualMass(id) != null) return Kind.EXPLICIT;
        if (isContainer(s)) return Kind.CONTAINER;
        if (s.getItem() instanceof TieredItem || s.getItem() instanceof ProjectileWeaponItem) return Kind.TOOL;
        if (s.getItem() instanceof ArmorItem) return Kind.ARMOR;
        if (V2.massFromTagsOrCategory(s).isPresent()) return Kind.TAGGED;// 你已有
        return Kind.UNKNOWN;
    }

    private static boolean isContainer(ItemStack s) {
        Item i = s.getItem();
        return i == Items.BUCKET || i == Items.WATER_BUCKET || i == Items.LAVA_BUCKET || i == Items.MILK_BUCKET
                || i == Items.GLASS_BOTTLE || i == Items.HONEY_BOTTLE
                || i == Items.BOWL || i == Items.MUSHROOM_STEW || i == Items.RABBIT_STEW
                || i == Items.SUSPICIOUS_STEW || i == Items.BEETROOT_SOUP;
    }

    /** 统一的“算重量”入口：把你原来用到质量的地方都改成调这个 */
    public static double weightOf(ItemStack stack) {
        // 先看手工表（ManualMetricsLoader 塞进来的 mass）
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        Double manualMass = WeightRegistry.lookupManualMass(id);
        if (manualMass != null) {
            boolean gear = stack.getItem() instanceof ArmorItem
                    || stack.getItem() instanceof TieredItem
                    || stack.getItem() instanceof ProjectileWeaponItem;
            double f = gear ? 1.0 : stackFactorV2(stack.getMaxStackSize());
            return manualMass * f * stack.getCount();
        }

        if (!Blend.enable) {
            // 简单回退：只用 V1
            double single = ItemMetricsUtil.v1Raw(stack);
            if (stack.getItem() instanceof ArmorItem || stack.getItem() instanceof TieredItem || stack.getItem() instanceof ProjectileWeaponItem) {
                return single * Math.max(1, stack.getCount());
            }
            return single * stackFactorV2(stack.getMaxStackSize()) * stack.getCount();
        }

        Kind kind = detectKind(stack);

        double v1 = ItemMetricsUtil.v1Raw(stack); // 单件
        double v2 = ItemMetricsUtil.v2Raw(stack); // 单件

        double alpha = switch (kind) {
            case CONTAINER -> Blend.alphaContainer;
            case TOOL      -> Blend.alphaTool;
            case ARMOR     -> Blend.alphaArmor;
            case TAGGED    -> Blend.alphaTagged;
            case UNKNOWN   -> Blend.alphaUnknown;
            default        -> Blend.alphaGlobal;
        };

        double single = alpha * v2 + (1.0 - alpha) * v1;

        if (kind == Kind.TOOL && Blend.clampTool) single = ItemMetricsUtil.clampTool(single);
        if (kind == Kind.ARMOR && Blend.capArmor) single = ItemMetricsUtil.capArmor(stack, single);

        if (kind == Kind.CONTAINER) return single * stack.getCount();
        if (kind == Kind.TOOL || kind == Kind.ARMOR) return single * Math.max(1, stack.getCount());
        return single * stackFactorV2(stack.getMaxStackSize()) * stack.getCount();
    }
}