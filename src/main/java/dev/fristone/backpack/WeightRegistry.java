package dev.fristone.backpack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;

/** 统一查询/写入表 */
public class WeightRegistry {
    private static final Map<ResourceLocation, ItemMetrics> MAP = new ConcurrentHashMap<>();

    public static void clear() { MAP.clear(); }
    public static int size() { return MAP.size(); }

    /** 配方推测写入（最高优先级：覆盖） */
    public static void putRecipe(ResourceLocation id, ItemMetrics m) { MAP.put(id, m); }

    /** 手写条目只补洞（不覆盖） */
    public static void putIfAbsent(ResourceLocation id, ItemMetrics m) { MAP.putIfAbsent(id, m); }

    public static ItemMetrics get(ResourceLocation id) { return MAP.get(id); }

    /** 暴露给其它系统使用 */
    public static Map<ResourceLocation, ItemMetrics> map() { return MAP; }
}