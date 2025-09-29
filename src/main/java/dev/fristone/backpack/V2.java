package dev.fristone.backpack;

import net.minecraft.world.item.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

final class V2 {
    // 由配置或 Loader 填充
    static final Map<String, Double> MATERIALS = new HashMap<>();      // “修复材料/锭”的单件质量
    static final Map<String, Double> CATEGORIES = new HashMap<>();     // 类目质量（stone_like/planks等）
    static final Map<String, String> TAG_TO_CATEGORY = new HashMap<>();// tag -> 类目
    static final Map<String, Double> CONTAINERS = new HashMap<>();
    static final Map<String, Double> CONTENT = new HashMap<>();

    static Optional<Double> containerWeight(ItemStack s) {
        Item i = s.getItem();
        if (i == Items.BUCKET)       return Optional.of(CONTAINERS.getOrDefault("minecraft:bucket",0.5));
        if (i == Items.WATER_BUCKET) return Optional.of(CONTAINERS.getOrDefault("minecraft:bucket",0.5)+CONTENT.getOrDefault("water",1.0));
        if (i == Items.LAVA_BUCKET)  return Optional.of(CONTAINERS.getOrDefault("minecraft:bucket",0.5)+CONTENT.getOrDefault("lava",3.0));
        if (i == Items.MILK_BUCKET)  return Optional.of(CONTAINERS.getOrDefault("minecraft:bucket",0.5)+CONTENT.getOrDefault("milk",1.0));
        if (i == Items.GLASS_BOTTLE) return Optional.of(CONTAINERS.getOrDefault("minecraft:glass_bottle",0.08));
        if (i == Items.HONEY_BOTTLE) return Optional.of(CONTAINERS.getOrDefault("minecraft:glass_bottle",0.08)+CONTENT.getOrDefault("honey",0.35));
        if (i == Items.BOWL)         return Optional.of(CONTAINERS.getOrDefault("minecraft:bowl",0.05));
        if (i == Items.MUSHROOM_STEW || i == Items.RABBIT_STEW || i == Items.SUSPICIOUS_STEW || i == Items.BEETROOT_SOUP)
            return Optional.of(CONTAINERS.getOrDefault("minecraft:bowl",0.05) + 0.30);
        return Optional.empty();
    }

    static Optional<Double> massFromToolRepair(ItemStack s) {
        if (!(s.getItem() instanceof TieredItem ti)) return Optional.empty();
        double best = -1;
        for (ItemStack ex : ti.getTier().getRepairIngredient().getItems()) {
            String key = String.valueOf(ForgeRegistries.ITEMS.getKey(ex.getItem()));
            Double v = MATERIALS.get(key);
            if (v != null) best = Math.max(best, v);
        }
        return best > 0 ? Optional.of(best) : Optional.empty();
    }

    static Optional<Double> massFromArmorRepair(ArmorItem armor) {
        double best = -1;
        for (ItemStack ex : armor.getMaterial().getRepairIngredient().getItems()) {
            String key = String.valueOf(ForgeRegistries.ITEMS.getKey(ex.getItem()));
            Double v = MATERIALS.get(key);
            if (v != null) best = Math.max(best, v);
        }
        return best > 0 ? Optional.of(best) : Optional.empty();
    }

    static int armorIngotCount(net.minecraft.world.entity.EquipmentSlot slot) {
        return switch (slot) { case HEAD -> 5; case CHEST -> 8; case LEGS -> 7; case FEET -> 4; default -> 6; };
    }

    static double toolHeadCoeff(ItemStack s) {
        Item i = s.getItem();
        if (i instanceof PickaxeItem) return 3.0;
        if (i instanceof AxeItem)     return 3.0;
        if (i instanceof SwordItem)   return 2.0;
        if (i instanceof ShovelItem)  return 1.0;
        if (i instanceof HoeItem)     return 2.0;
        if (i instanceof BowItem || i instanceof CrossbowItem) return 1.5;
        return 2.0;
    }

    static Optional<Double> massFromTagsOrCategory(ItemStack s) {
        // 这里给一个保底实现；你可以用 TypeDetector/RecipeInferencer 替换
        String id = String.valueOf(ForgeRegistries.ITEMS.getKey(s.getItem()));
        if (id.contains("planks")) return Optional.of(CATEGORIES.getOrDefault("planks",0.45));
        if (id.contains("stone") || id.contains("deepslate") || id.contains("cobblestone"))
            return Optional.of(CATEGORIES.getOrDefault("stone_like",0.55));
        if (id.contains("sand"))   return Optional.of(CATEGORIES.getOrDefault("sand",0.40));
        if (id.contains("gravel")) return Optional.of(CATEGORIES.getOrDefault("gravel",0.60));
        if (id.contains("dirt") || id.contains("grass")) return Optional.of(CATEGORIES.getOrDefault("dirt",0.30));
        if (id.contains("coal"))   return Optional.of(CATEGORIES.getOrDefault("coal_like",0.20));
        if (id.contains("paper"))  return Optional.of(CATEGORIES.getOrDefault("paper_like",0.02));
        return Optional.empty();
    }
}