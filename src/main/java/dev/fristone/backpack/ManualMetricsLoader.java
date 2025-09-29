
package dev.fristone.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class ManualMetricsLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final String BASE = "item_metrics";
    private static final String EXT = ".json";

    public static void loadAll(ResourceManager rm, BiConsumer<ResourceLocation, ItemMetrics> out) {
        var found = rm.listResources(BASE, rl -> rl.getPath().startsWith(BASE + "/") && rl.getPath().endsWith(EXT));
        found.forEach((rl, res) -> {
            try (var in = res.open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject o = GSON.fromJson(r, JsonObject.class);
                double vol  = o.has("vol")  ? o.get("vol").getAsDouble()  : 0.1;
                double mass = o.has("mass") ? o.get("mass").getAsDouble() : 0.1;
                String form = o.has("form") ? o.get("form").getAsString() : "misc";

                String full = rl.getPath();
                String rel = full.substring((BASE + "/").length(), full.length() - EXT.length());
                int slash = rel.indexOf('/');
                if (slash <= 0 || slash >= rel.length()-1) return;
                String ns = rel.substring(0, slash);
                String idPath = rel.substring(slash+1);
                var id = ResourceLocation.fromNamespaceAndPath(ns, idPath);
                out.accept(id, new ItemMetrics(vol, mass, form));
            } catch (Exception ex) {
                LOGGER.warn("[FOBP] manual metric parse failed {}: {}", rl, ex.toString());
            }
        });
    }
}
