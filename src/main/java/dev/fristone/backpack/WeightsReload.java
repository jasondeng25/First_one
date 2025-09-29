
package dev.fristone.backpack;

import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class WeightsReload implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onReload(AddReloadListenerEvent e) {
        e.addListener(new WeightsReload());
        LOGGER.info("[FOBP] WeightsReload listener attached");
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager rm,
                                          ProfilerFiller p1, ProfilerFiller p2,
                                          Executor exec1, Executor exec2) {
        return CompletableFuture.supplyAsync(() -> {
            WeightRegistry.clear();
            MaterialsLoader.loadAll(rm);
            int nRecipe = RecipeInferencer.generate(rm);
            LOGGER.info("[FOBP] recipe inferred {}", nRecipe);
            ManualMetricsLoader.loadAll(rm, WeightRegistry::putOrOverride);
            LOGGER.info("[FOBP] manual metrics loaded. total={}", WeightRegistry.size());
            return null;
        }, exec1).thenCompose(barrier::wait).thenRunAsync(() -> {}, exec2);
    }
}
