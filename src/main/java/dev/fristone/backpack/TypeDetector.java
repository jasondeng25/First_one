package dev.fristone.backpack;

import net.minecraft.resources.ResourceLocation;

/** 依据物品 id 路径关键字给出大致类型，用于类型系数 */
public class TypeDetector {
    public static String detectType(ResourceLocation id) {
        String p = id.getPath();
        if (p.contains("sword") || p.contains("bow") || p.contains("crossbow") || p.contains("trident")) return "weapon";
        if (p.contains("axe") || p.contains("pickaxe") || p.contains("shovel") || p.contains("hoe") || p.contains("shears") || p.contains("fishing_rod")) return "tool";
        if (p.contains("helmet") || p.contains("chestplate") || p.contains("leggings") || p.contains("boots") || p.contains("shield")) return "armor";
        if (p.contains("apple") || p.contains("bread") || p.contains("potion") || p.contains("food") || p.contains("stew") || p.contains("soup")) return "consumable";
        if (p.contains("block") || p.contains("planks") || p.endsWith("_ore") || p.endsWith("_log") || p.endsWith("_wood")) return "block";
        return "misc";
    }
}