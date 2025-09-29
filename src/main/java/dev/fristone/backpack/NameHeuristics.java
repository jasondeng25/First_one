
package dev.fristone.backpack;

import net.minecraft.resources.ResourceLocation;

public class NameHeuristics {
    public static ItemMetrics estimate(ResourceLocation id, String displayName) {
        String s = (id != null ? id.getPath() : "") + " " + (displayName == null ? "" : displayName.toLowerCase());
        s = s.toLowerCase();

        if (s.contains("sword")) return new ItemMetrics(0.8, 3.0, "weapon");
        if (s.contains("pickaxe") || s.contains("axe")) return new ItemMetrics(0.9, 4.0, "tool");
        if (s.contains("shovel") || s.contains("hoe")) return new ItemMetrics(0.8, 3.0, "tool");
        if (s.contains("helmet")) return new ItemMetrics(1.0, 2.0, "armor");
        if (s.contains("chestplate")) return new ItemMetrics(1.4, 6.0, "armor");
        if (s.contains("leggings")) return new ItemMetrics(1.2, 5.0, "armor");
        if (s.contains("boots")) return new ItemMetrics(0.8, 2.0, "armor");
        if (s.contains("ingot")) return new ItemMetrics(0.25, 2.0, "metal");
        if (s.contains("nugget")) return new ItemMetrics(0.05, 0.2, "metal");
        if (s.contains("log")) return new ItemMetrics(1.0, 1.2, "wood");
        if (s.contains("stone")) return new ItemMetrics(1.0, 2.6, "stone");
        return new ItemMetrics(0.5, 1.0, "misc");
    }
}
