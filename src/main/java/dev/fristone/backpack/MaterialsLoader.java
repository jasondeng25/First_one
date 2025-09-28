package dev.fristone.backpack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** 加载材料表、别名映射、类型系数 */
public class MaterialsLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    /** 材料密度/体积表（供配方推测累加） */
    public static final Map<String, ItemMetrics> MATERIALS = new HashMap<>();
    /** item -> material */
    public static final Map<ResourceLocation, String> ITEM_TO_MAT = new HashMap<>();
    /** tag  -> material（用 ResourceLocation 表示 tag 名称） */
    public static final Map<ResourceLocation, String> TAG_TO_MAT  = new HashMap<>();
    /** 类型系数 */
    public static final Map<String, Double> TYPE_MUL = new HashMap<>();

    public static void clear() {
        MATERIALS.clear(); ITEM_TO_MAT.clear(); TAG_TO_MAT.clear(); TYPE_MUL.clear();
    }

    public static int load(ResourceManager rm) {
        clear();
        int count = 0;

        // 1) materials/*.json
        var mats = rm.listResources("materials", rl -> rl.getNamespace().equals(WeightsReload.MODID) && rl.getPath().endsWith(".json"));
        for (var entry : mats.entrySet()) {
            try (var in = entry.getValue().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                JsonObject materials = root.has("materials") && root.get("materials").isJsonObject() ? root.getAsJsonObject("materials") : new JsonObject();
                for (var e : materials.entrySet()) {
                    JsonObject v = e.getValue().getAsJsonObject();
                    double mass = v.has("mass") ? v.get("mass").getAsDouble() : 1.0;
                    double vol  = v.has("vol")  ? v.get("vol").getAsDouble()  : 1.0;
                    MATERIALS.put(e.getKey(), new ItemMetrics(vol, mass, "material"));
                    count++;
                }
            } catch (Exception ex) {
                LOGGER.warn("[MaterialsLoader] materials parse fail {}: {}", entry.getKey(), ex.toString());
            }
        }

        // 2) material_alias/*.json
        var alias = rm.listResources("material_alias", rl -> rl.getNamespace().equals(WeightsReload.MODID) && rl.getPath().endsWith(".json"));
        for (var entry : alias.entrySet()) {
            try (var in = entry.getValue().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);

                if (root.has("item_to_material")) {
                    JsonObject it = root.getAsJsonObject("item_to_material");
                    for (var e : it.entrySet()) {
                        ITEM_TO_MAT.put(new ResourceLocation(e.getKey()), e.getValue().getAsString());
                        count++;
                    }
                }
                if (root.has("tag_to_material")) {
                    JsonObject tt = root.getAsJsonObject("tag_to_material");
                    for (var e : tt.entrySet()) {
                        TAG_TO_MAT.put(new ResourceLocation(e.getKey()), e.getValue().getAsString());
                        count++;
                    }
                }
            } catch (Exception ex) {
                LOGGER.warn("[MaterialsLoader] alias parse fail {}: {}", entry.getKey(), ex.toString());
            }
        }

        // 3) type_multipliers/*.json
        var tms = rm.listResources("type_multipliers", rl -> rl.getNamespace().equals(WeightsReload.MODID) && rl.getPath().endsWith(".json"));
        for (var entry : tms.entrySet()) {
            try (var in = entry.getValue().open(); var r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(r, JsonObject.class);
                for (Map.Entry<String, JsonElement> e : root.entrySet()) {
                    TYPE_MUL.put(e.getKey(), e.getValue().getAsDouble());
                    count++;
                }
            } catch (Exception ex) {
                LOGGER.warn("[MaterialsLoader] type multipliers parse fail {}: {}", entry.getKey(), ex.toString());
            }
        }

        return count;
    }
}