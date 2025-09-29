package dev.fristone.backpack;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EncumbranceSpeedApplier {
    private EncumbranceSpeedApplier() {}

    // 固定 UUID，确保只存在我们这一条减速项
    private static final UUID SLOW_UUID = UUID.fromString("1f6d2a7a-0a0d-49ae-bf6b-9b4f0b3d54c9");

    private static final Map<UUID, Double> LAST_FACTOR = new ConcurrentHashMap<>();
    private static final double ALPHA = 0.2; // 平滑系数

    /** 创造/旁观或未超载时，清理惩罚 */
    public static void clear(ServerPlayer p) {
        LAST_FACTOR.remove(p.getUUID());
        AttributeInstance inst = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst != null) inst.removeModifier(SLOW_UUID);
    }

    /** 每 tick 施加/更新惩罚 */
    public static void tickApply(ServerPlayer p, double carriedMass, double capacity) {
        // 创造/旁观：不受影响
        if (p.isCreative() || p.isSpectator()) {
            clear(p);
            return;
        }

        // 容量异常或未超载：清理并返回
        if (capacity <= 0 || carriedMass <= capacity) {
            clear(p);
            return;
        }

        double r = (carriedMass - capacity) / capacity; // 超载比
        double target = curve(r); // 0.12~1 之间，越大越快（这里小于1表示减速）

        double prev = LAST_FACTOR.getOrDefault(p.getUUID(), 1.0);
        double current = prev + (target - prev) * ALPHA;
        if (current < 0.05) current = 0.05;
        if (current > 1.0)  current = 1.0;

        AttributeInstance inst = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst != null) {
            // 先移除旧的，再加新的瞬态 modifier
            inst.removeModifier(SLOW_UUID);
            inst.addTransientModifier(new AttributeModifier(
                    SLOW_UUID,
                    "fobp_encumbrance_speed",
                    current - 1.0, // MULTIPLY_TOTAL：最终 * (1 + amount)
                    AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }

        LAST_FACTOR.put(p.getUUID(), current);
    }

    private static double curve(double r) {
        if (r <= 0) return 1.0;
        double k = 1.35;
        double p = 1.3;
        double base = 1.0 / Math.pow(1.0 + k * r, p);
        return Math.max(0.12, base);
    }
}