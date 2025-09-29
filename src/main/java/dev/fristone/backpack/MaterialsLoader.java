package dev.fristone.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MaterialsLoader {
    private static final Gson GSON = new Gson();

    public static final Map<String, ItemMetrics> MATERIALS = new HashMap<>();
    public static final Map<ResourceLocation, String> ITEM_TO_MAT = new HashMap<>();
    public static final Map<ResourceLocation, String> TAG_TO_MAT  = new HashMap<>();
    public static final Map<String, Double> TYPE_MUL = new HashMap<>();

    public static void loadAll(ResourceManager rm) {
        MATERIALS.clear();
        ITEM_TO_MAT.clear();
        TAG_TO_MAT.clear();
        TYPE_MUL.clear();

        // data/first_one_backpack/materials.json
        var res1 = rm.getResource(ResourceLocation.fromNamespaceAndPath("first_one_backpack", "materials.json"));
        if (res1.isPresent()) {
            try (var in = res1.get().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (root.has("materials") && root.get("materials").isJsonObject()) {
                    for (var e : root.getAsJsonObject("materials").entrySet()) {
                        String name = e.getKey();
                        JsonObject o = e.getValue().getAsJsonObject();
                        double vol  = o.has("vol")  ? o.get("vol").getAsDouble()  : 0.1;
                        double mass = o.has("mass") ? o.get("mass").getAsDouble() : 0.1;
                        String form = o.has("form") ? o.get("form").getAsString() : "misc";
                        MATERIALS.put(name, new ItemMetrics(vol, mass, form));
                    }
                }
                if (root.has("type_mul") && root.get("type_mul").isJsonObject()) {
                    for (var e : root.getAsJsonObject("type_mul").entrySet()) {
                        TYPE_MUL.put(e.getKey(), e.getValue().getAsDouble());
                    }
                }
            } catch (Exception ignored) {}
        }

        // data/first_one_backpack/materials_map.json
        var res2 = rm.getResource(ResourceLocation.fromNamespaceAndPath("first_one_backpack", "materials_map.json"));
        if (res2.isPresent()) {
            try (var in = res2.get().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                if (root.has("item_to_mat") && root.get("item_to_mat").isJsonObject()) {
                    for (var e : root.getAsJsonObject("item_to_mat").entrySet()) {
                        var id = ResourceLocation.tryParse(e.getKey());
                        if (id != null) ITEM_TO_MAT.put(id, e.getValue().getAsString());
                    }
                }
                if (root.has("tag_to_mat") && root.get("tag_to_mat").isJsonObject()) {
                    for (var e : root.getAsJsonObject("tag_to_mat").entrySet()) {
                        var tag = ResourceLocation.tryParse(e.getKey());
                        if (tag != null) TAG_TO_MAT.put(tag, e.getValue().getAsString());
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static String resolveMaterialName(JsonElement ing) {
        if (ing == null || ing.isJsonNull()) return null;
        if (ing.isJsonArray()) {
            for (JsonElement e : ing.getAsJsonArray()) {
                String m = resolveMaterialName(e);
                if (m != null) return m;
            }
            return null;
        }
        if (!ing.isJsonObject()) return null;
        JsonObject o = ing.getAsJsonObject();

        if (o.has("item")) {
            var id = ResourceLocation.tryParse(o.get("item").getAsString());
            if (id != null) {
                String m = ITEM_TO_MAT.get(id);
                if (m != null) return m;
            }
        }
        if (o.has("tag")) {
            var tag = ResourceLocation.tryParse(o.get("tag").getAsString());
            if (tag != null) {
                String m = TAG_TO_MAT.get(tag);
                if (m != null) return m;
            }
        }
        return null;
    }
}