package dev.fristone.backpack;

import net.minecraft.resources.ResourceLocation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeightRegistry {
    private static final Map<ResourceLocation, ItemMetrics> REG = new ConcurrentHashMap<>();

    public static void putOrOverride(ResourceLocation id, ItemMetrics m) {
        if (id != null && m != null) REG.put(id, m);
    }

    public static void putIfAbsent(ResourceLocation id, ItemMetrics m) {
        if (id != null && m != null) REG.putIfAbsent(id, m);
    }

    public static ItemMetrics get(ResourceLocation id) {
        return REG.get(id);
    }

    public static int size() {
        return REG.size();
    }

    public static void clear() {
        REG.clear();
    }

    // === 新增：便捷查询（供融合器使用） ===
    /** 若手工表中存在，返回“单件质量（kg）”；否则返回 null */
    public static Double lookupManualMass(ResourceLocation id) {
        ItemMetrics m = REG.get(id);
        return (m == null ? null : m.mass());
    }

    /** V1 旧体积系数（64=1.0, 16=1.5, 1=2.0）——供 V1 raw 使用 */
    public static double v1StackFactor(int maxStack) {
        if (maxStack >= 64) return 1.0;
        if (maxStack >= 16) return 1.5;
        return 2.0;
    }
}