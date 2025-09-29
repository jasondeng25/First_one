
package dev.fristone.backpack;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;


@Mod(FirstOne.MODID)
public class FirstOne {
    public static final String MODID = "first_one_backpack";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FirstOne() {
        LOGGER.info("[FOBP] bootstrap OK");
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(new InvEvents());
        MinecraftForge.EVENT_BUS.register(new EncumbranceHandler());
        MinecraftForge.EVENT_BUS.register(new WeightsReload());
        AttributeRegistry.ATTRIBUTES.register(modBus);
        modBus.addListener(FirstOne::onEntityAttributeMod);
    }
    public static void onEntityAttributeMod(EntityAttributeModificationEvent event) {
        // 给玩家实体挂上我们的新属性
        event.add(EntityType.PLAYER, AttributeRegistry.CARRY_CAPACITY.get());
    }
}
