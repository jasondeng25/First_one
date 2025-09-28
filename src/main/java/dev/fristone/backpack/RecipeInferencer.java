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

/** 扫描所有配方并生成重量（覆盖写入） */
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

                // 解析输出
                Out out = parseResult(root);
                if (out == null) continue;

                // 解析输入 -> 累加材料权重
                double massSum = 0.0;
                double volSum  = 0.0;

                if (type.contains("crafting")) {
                    // shaped or shapeless
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
                    // 兼容锻造（有 base/addition/template 等）
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
                    // 其它配方类型先忽略
                    continue;
                }

                if (out.count <= 0) out.count = 1;
                double mass = massSum / out.count;
                double vol  = volSum  / out.count;

                // 类型系数
                String typ = TypeDetector.detectType(out.id);
                Double mul = MaterialsLoader.TYPE_MUL.getOrDefault(typ, 1.0);
                mass *= mul; vol *= mul;

                if (mass > 0 || vol > 0) {
                    WeightRegistry.putRecipe(out.id, new ItemMetrics(vol, mass, typ));
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
                    return new Out(new ResourceLocation(id), count);
                } else if (res.isJsonPrimitive()) {
                    String id = res.getAsString();
                    return new Out(new ResourceLocation(id), 1);
                }
            } else if (root.has("result_item")) { // 一些模组的字段名
                String id = root.get("result_item").getAsString();
                int count = root.has("result_count") ? root.get("result_count").getAsInt() : 1;
                return new Out(new ResourceLocation(id), count);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static double resolveMass(JsonElement ing) {
        String mat = resolveMaterialName(ing);
        if (mat == null) return 0.0;
        ItemMetrics m = MaterialsLoader.MATERIALS.get(mat);
        return m != null ? m.mass() : 0.0;
    }
    private static double resolveVol(JsonElement ing) {
        String mat = resolveMaterialName(ing);
        if (mat == null) return 0.0;
        ItemMetrics m = MaterialsLoader.MATERIALS.get(mat);
        return m != null ? m.vol() : 0.0;
    }

    /** 将配方 ingredient 映射到材料名（优先 item，再 tag） */
    private static String resolveMaterialName(JsonElement ing) {
        if (ing == null || ing.isJsonNull()) return null;
        if (ing.isJsonArray()) {
            // 任一命中则取第一个有材料映射的
            for (JsonElement e : ing.getAsJsonArray()) {
                String m = resolveMaterialName(e);
                if (m != null) return m;
            }
            return null;
        }
        if (!ing.isJsonObject()) return null;
        JsonObject o = ing.getAsJsonObject();

        if (o.has("item")) {
            try {
                ResourceLocation id = new ResourceLocation(o.get("item").getAsString());
                String m = MaterialsLoader.ITEM_TO_MAT.get(id);
                if (m != null) return m;
            } catch (Exception ignored) {}
        }
        if (o.has("tag")) {
            try {
                ResourceLocation tag = new ResourceLocation(o.get("tag").getAsString());
                String m = MaterialsLoader.TAG_TO_MAT.get(tag);
                if (m != null) return m;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private static class Out {
        final ResourceLocation id;
        int count;
        Out(ResourceLocation id, int count) { this.id = id; this.count = count; }
    }
}