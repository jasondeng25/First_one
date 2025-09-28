package dev.fristone.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 手写条目加载器：只补洞，不覆盖配方推测结果 */
public class ManualMetricsLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    public static int load(ResourceManager rm) {
        int count = 0;
        var found = rm.listResources("item_metrics", rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> e : found.entrySet()) {
            ResourceLocation rl = e.getKey();
            // 跳过我们内部的中转目录（如果有的话）
            try (var in = e.getValue().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject o = GSON.fromJson(r, JsonObject.class);
                double vol  = o.has("vol")  ? o.get("vol").getAsDouble()  : 0.1;
                double mass = o.has("mass") ? o.get("mass").getAsDouble() : 0.1;
                String form = o.has("form") ? o.get("form").getAsString() : "misc";

                // item_metrics/<ns>/<path>.json
                String full = rl.getPath();
                final String prefix = "item_metrics/";
                if (!full.startsWith(prefix)) continue;
                String rel = full.substring(prefix.length(), full.length() - ".json".length());
                int slash = rel.indexOf('/');
                if (slash <= 0 || slash >= rel.length() - 1) continue;

                String ns = rel.substring(0, slash);
                String idPath = rel.substring(slash + 1);
                ResourceLocation id = new ResourceLocation(ns, idPath);

                WeightRegistry.putIfAbsent(id, new ItemMetrics(vol, mass, form));
                count++;
            } catch (Exception ex) {
                LOGGER.warn("[ManualMetricsLoader] parse fail {}: {}", rl, ex.toString());
            }
        }
        return count;
    }
}