package dev.fristone.backpack;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AttributeRegistry {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, FirstOne.MODID);

    // 名称: first_one_backpack:carry_capacity
    public static final RegistryObject<Attribute> CARRY_CAPACITY = ATTRIBUTES.register(
            "carry_capacity",
            () -> new RangedAttribute(
                    "attribute.name.first_one_backpack.carry_capacity",
                    EncumbranceHandler.MASS_CAP,   // 默认值=全局基础上限(40)
                    0.0,                           // 最小
                    4096.0                         // 最大(随意给个足够大的上限)
            ).setSyncable(true) // 客户端同步
    );

    private AttributeRegistry() {}
}