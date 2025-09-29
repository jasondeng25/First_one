
package dev.fristone.backpack;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


@Mod.EventBusSubscriber(modid = FirstOne.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class InvEvents {

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {

    }
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent e) {
        if (e.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (!(e.player instanceof net.minecraft.server.level.ServerPlayer p)) return;
        double mass = EncumbranceHandler.computeCarriedMass(p);
        double cap  = EncumbranceHandler.computeCapacity(p);
        EncumbranceSpeedApplier.tickApply(p, mass, cap);
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent e) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("fobp")
                .then(Commands.literal("weight").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    double mass = EncumbranceHandler.computeCarriedMass(p);
                    double vol  = EncumbranceHandler.computeCarriedVolume(p);
                    double cap  = EncumbranceHandler.computeCapacity(p);
                    ctx.getSource().sendSuccess(() -> Component.literal("[FOBP] 当前质量/上限: " + String.format("%.1f/%.1f", mass, cap)
                            + "  体积: " + String.format("%.1f", vol)), false);
                    return 1;
                }));

        e.getDispatcher().register(root);
    }
    @SubscribeEvent
    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p) {
            EncumbranceSpeedApplier.clear(p);
        }
    }
    @SubscribeEvent
    public static void onBreakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed e) {
        if (!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)) return;

        double mass = EncumbranceHandler.computeCarriedMass(p);
        double cap  = EncumbranceHandler.computeCapacity(p);

        double r = 0.0;
        if (cap > 0 && mass > cap) r = (mass - cap) / cap;

        // 复制 EncumbranceSpeedApplier 的曲线，避免反射
        double k = 1.35, pow = 1.3;
        double base = (r <= 0) ? 1.0 : 1.0 / Math.pow(1.0 + k * r, pow);
        double factor = Math.max(0.12, base);

        e.setNewSpeed((float)(e.getNewSpeed() * factor));
    }
}
