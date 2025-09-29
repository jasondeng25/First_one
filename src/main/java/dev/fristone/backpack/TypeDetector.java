
package dev.fristone.backpack;

import net.minecraft.resources.ResourceLocation;

public class TypeDetector {
    public static String detectType(ResourceLocation id) {
        if (id == null) return "misc";
        String p = id.getPath();
        if (p.contains("sword") || p.contains("bow") || p.contains("crossbow") || p.contains("trident")) return "weapon";
        if (p.contains("pickaxe") || p.contains("axe") || p.contains("shovel") || p.contains("hoe") || p.contains("shears")) return "tool";
        if (p.contains("helmet") || p.contains("chestplate") || p.contains("leggings") || p.contains("boots") || p.contains("armor")) return "armor";
        if (p.contains("ingot") || p.contains("nugget") || p.contains("gem")) return "metal";
        if (p.contains("stone") || p.contains("cobblestone") || p.contains("deepslate")) return "stone";
        if (p.contains("log") || p.contains("planks") || p.contains("stick")) return "wood";
        if (p.contains("wool") || p.contains("leather")) return "fabric";
        return "misc";
    }
}
