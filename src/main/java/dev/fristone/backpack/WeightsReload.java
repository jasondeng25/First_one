package dev.fristone.backpack;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 单一入口的资源重载监听器：
 * 1) 清空注册表
 * 2) 加载材料/别名/类型系数
 * 3) 扫描配方并推测重量（最高优先级，覆盖）
 * 4) 加载手写条目（仅补洞，不覆盖）
 */
@Mod.EventBusSubscriber(modid = WeightsReload.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WeightsReload implements PreparableReloadListener {

    public static final String MODID = "first_one_backpack";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 将本监听器挂到 Forge 资源重载管线 */
    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(new WeightsReload());
        LOGGER.info("[WeightsReload] attached.");
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                          ResourceManager rm,
                                          ProfilerFiller p1, ProfilerFiller p2,
                                          Executor exec1, Executor exec2) {
        return CompletableFuture.supplyAsync(() -> {
            WeightRegistry.clear();

            int mats = MaterialsLoader.load(rm);
            LOGGER.info("[WeightsReload] materials/aliases/types loaded: {}", mats);

            int inferred = RecipeInferencer.generate(rm);
            LOGGER.info("[WeightsReload] recipe inferred entries: {}", inferred);

            int manual = ManualMetricsLoader.load(rm);
            LOGGER.info("[WeightsReload] manual item_metrics filled: {}", manual);

            LOGGER.info("[WeightsReload] finished. total={}", WeightRegistry.size());
            return null;
        }, exec1).thenCompose(v -> barrier.wait(v));
    }
}