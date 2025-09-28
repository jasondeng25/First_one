# Internal API Contracts (Draft)

## Encumbrance
```java
EncumbranceResult Encumbrance.compute(ServerPlayer player);
record EncumbranceResult(double encVol, double encMass, double penalty, int tier) {}
```
- Runs every 10 ticks (5 when moving). Uses datapack item metrics.

## Status
```java
StatusSample Status.sample(ServerPlayer player);
record StatusSample(float heat, float thirst, float fatigue, int heatTier, int thirstTier, int fatigueTier) {}
```

## UI Hint Bus
```java
void UI.pushHint(ServerPlayer player, String langKey, int priority, int ttlTicks);
```
- Client decides visual/aural effects. Cooldowns per channel.

## Combat
```java
void Combat.applyKnock(Entity e, Vec3 dir, float power, Stance stance);
```

## Scavenge
```java
List<ItemStack> Scavenge.roll(BlockPos pos, ScavengeContext ctx);
```
