package dev.fristone.backpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

/** 计算质量与施加惩罚 */
public class EncumbranceHandler {
    /** 基础可承重（可按需要改成配置） */
    public static final double MASS_CAP = 40.0;

    /** 每 tick 调用：根据当前负重施加或清理惩罚 */
    public static void tick(ServerPlayer p) {
        // 创造/旁观不受影响；若有旧惩罚，先清理后退出
        if (p.isCreative() || p.isSpectator()) {
            // 用一次“无惩罚”调用来清理：质量=0，容量=无限
            EncumbranceSpeedApplier.tickApply(p, 0.0, Double.MAX_VALUE);
            return;
        }

        double mass = computeCarriedMass(p);
        double cap  = computeCapacity(p);

        // 只调用一次
        EncumbranceSpeedApplier.tickApply(p, mass, cap);
    }

    /** 当前最大可承重（默认 MASS_CAP，可被自定义属性/额外加成覆盖） */
    public static double computeCapacity(ServerPlayer p) {
        double base = MASS_CAP;

        // 若注册了自定义属性，则以属性最终值为准（可被 AttributeModifier 动态调整）
        AttributeInstance inst = p.getAttribute(AttributeRegistry.CARRY_CAPACITY.get());
        if (inst != null) {
            base = inst.getValue();
        }

        // 再叠加你自定义的持久化加成（正负皆可）
        double bonus = p.getPersistentData().getDouble("fobpCarryBonus");
        return Math.max(0.0, base + bonus);
    }

    /** 遍历玩家所有物品，按规则库计算总质量 */
    public static double computeCarriedMass(ServerPlayer p) {
        double sum = 0.0;
        for (ItemStack st : p.getInventory().items)   sum += massOf(st);
        for (ItemStack st : p.getInventory().armor)   sum += massOf(st);
        for (ItemStack st : p.getInventory().offhand) sum += massOf(st);
        return sum;
    }

    /** 统计玩家当前携带体积（主手+护甲+副手），供展示/判定 */
    public static double computeCarriedVolume(ServerPlayer p) {
        double sum = 0.0;
        for (ItemStack st : p.getInventory().items)   sum += volOf(st);
        for (ItemStack st : p.getInventory().armor)   sum += volOf(st);
        for (ItemStack st : p.getInventory().offhand) sum += volOf(st);
        return sum;
    }

    private static double massOf(ItemStack st) {
        if (st == null || st.isEmpty()) return 0.0;
        return MetricsBlender.weightOf(st);
    }

    // 取某个物品栈的体积 = 单件体积 * 数量；查不到就给个很小的兜底
    private static double volOf(ItemStack st) {
        if (st == null || st.isEmpty()) return 0.0;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(st.getItem());
        ItemMetrics m = WeightRegistry.get(id);

        if (m == null) {
            try {
                m = NameHeuristics.estimate(id, st.getHoverName().getString());
                if (m != null) WeightRegistry.putIfAbsent(id, m);
            } catch (Throwable ignored) {}
        }

        double unitVol = (m != null ? m.vol() : 0.1);
        return unitVol * st.getCount();
    }
}