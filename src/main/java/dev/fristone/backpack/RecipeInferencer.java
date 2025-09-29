
package dev.fristone.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RecipeInferencer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    public static int generate(ResourceManager rm) {
        int written = 0;
        var all = rm.listResources("recipes", rl -> rl.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> e : all.entrySet()) {
            try (var in = e.getValue().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (!root.has("type")) continue;
                String type = root.get("type").getAsString();

                Out out = parseResult(root);
                if (out == null) continue;

                double massSum = 0.0;
                double volSum  = 0.0;

                if (type.contains("crafting")) {
                    JsonArray ings = gatherIngredients(root);
                    for (JsonElement ing : ings) {
                        massSum += resolveMass(ing);
                        volSum  += resolveVol(ing);
                    }
                } else if (type.endsWith("smelting") || type.endsWith("blasting") || type.endsWith("smoking") || type.endsWith("campfire_cooking")) {
                    if (root.has("ingredient")) {
                        JsonElement ing = root.get("ingredient");
                        massSum += resolveMass(ing);
                        volSum  += resolveVol(ing);
                    }
                } else if (type.endsWith("smithing_transform") || type.endsWith("smithing")) {
                    for (String k : new String[]{"base","addition","template","template_item"}) {
                        if (root.has(k)) {
                            JsonElement ing = root.get(k);
                            massSum += resolveMass(ing);
                            volSum  += resolveVol(ing);
                        }
                    }
                } else if (type.endsWith("stonecutting")) {
                    if (root.has("ingredient")) {
                        JsonElement ing = root.get("ingredient");
                        massSum += resolveMass(ing);
                        volSum  += resolveVol(ing);
                    }
                } else {
                    continue;
                }

                if (out.count <= 0) out.count = 1;
                double mass = massSum / out.count;
                double vol  = volSum  / out.count;

                String typ = TypeDetector.detectType(out.id);
                Double mul = MaterialsLoader.TYPE_MUL.getOrDefault(typ, 1.0);
                mass *= mul; vol *= mul;

                if (mass > 0 || vol > 0) {
                    WeightRegistry.putOrOverride(out.id, new ItemMetrics(vol, mass, typ));
                    written++;
                }
            } catch (Exception ex) {
                LOGGER.debug("[RecipeInferencer] skip {} due to {}", e.getKey(), ex.toString());
            }
        }
        return written;
    }

    private static JsonArray gatherIngredients(JsonObject root) {
        JsonArray arr = new JsonArray();
        if (root.has("ingredients") && root.get("ingredients").isJsonArray()) {
            for (JsonElement el : root.getAsJsonArray("ingredients")) {
                arr.add(el);
            }
        }
        if (root.has("key") && root.get("key").isJsonObject()) {
            for (Map.Entry<String, JsonElement> en : root.getAsJsonObject("key").entrySet()) {
                arr.add(en.getValue());
            }
        }
        return arr;
    }

    private static Out parseResult(JsonObject root) {
        try {
            if (root.has("result")) {
                JsonElement res = root.get("result");
                if (res.isJsonObject()) {
                    JsonObject o = res.getAsJsonObject();
                    String id = o.has("item") ? o.get("item").getAsString() : null;
                    int count = o.has("count") ? o.get("count").getAsInt() : 1;
                    if (id == null) return null;
                    return new Out(ResourceLocation.tryParse(id), count);
                } else if (res.isJsonPrimitive()) {
                    String id = res.getAsString();
                    return new Out(ResourceLocation.tryParse(id), 1);
                }
            } else if (root.has("result_item")) {
                String id = root.get("result_item").getAsString();
                int count = root.has("result_count") ? root.get("result_count").getAsInt() : 1;
                return new Out(ResourceLocation.tryParse(id), count);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static double resolveMass(JsonElement ing) {
        String mat = MaterialsLoader.resolveMaterialName(ing);
        if (mat == null) return 0.0;
        ItemMetrics m = MaterialsLoader.MATERIALS.get(mat);
        return m != null ? m.mass() : 0.0;
    }
    private static double resolveVol(JsonElement ing) {
        String mat = MaterialsLoader.resolveMaterialName(ing);
        if (mat == null) return 0.0;
        ItemMetrics m = MaterialsLoader.MATERIALS.get(mat);
        return m != null ? m.vol() : 0.0;
    }

    private static class Out {
        final ResourceLocation id;
        int count;
        Out(ResourceLocation id, int count) { this.id = id; this.count = count; }
    }
}
