# Project Vision & Blueprint (First One)

> Version: MVP Blueprint / Target Minecraft Forge 1.20.1

## One‑Sentence Vision
Remove intrusive, number‑heavy HUDs. Replace them with **diegetic** signals and **subjective** descriptions (e.g., “我有点饿 / I feel a bit hungry,” “头有点晕 / slightly dizzy”), while preserving underlying gameplay depth via hidden stats (nutrition, hydration, fatigue, injuries, etc.).

## Product Vision (from user)
做一个**极简 GUI、强沉浸**的 Minecraft 大体量 Forge 模组（1.20.1），核心是“**玩家以身体为容器**”的生存体验：

* 尽量少的数值条与菜单；更多**体感反馈**（移动、呼吸、视野、听感、镜头抖动、屏边状态）+ **主观文本**（“有点重/很饿/头发晕”）。
* 信息**通过道具**（手持指南针、温度计、怀表、野外笔记等）与**环境**（广播、线索、痕迹）传达，而不是堆 GUI。
* 第一人称为主，有**近战/倒地/大位移**等动作表现；第三人称可用但不优先打磨，以满足联机观感。

## System Packages

### MVP Scope
1. **Encumbrance (Backpack/Load)** — volume+mass penalties; data‑driven per‑item metrics; subjective prompts.
2. **Scavenge** — searchable containers/remnants with noise/aggro weights and timed refresh.
3. **Core States** — temperature, thirst, fatigue with subjective text + light VFX only.
4. **Combat Primitives** — directional knockback, short hit‑stun, sprint collisions; prone/knockdown 1P camera transitions.
5. **Minimal UI** — hide extra bars; show corner hints; numeric readouts only via handheld tools (thermometer, compass, watch).
6. **Onboarding** — 60s gentle tips; handheld info‑card (replaces heavy wiki mods).

### Phase 2 (Enhance)
Stamina (hidden), clothing layers (wet/dry), carry rigs (chest/waist/frame), radio/weather events, survivor NPC prototype.

## UX Principles
- “**Feel it, don’t read it**”: prioritize motion/aural/visual cues over numbers.
- Subjective description **tiers 0–4**: 有点X → 明显X → 非常X → 难以行动.
- Max two corner bubbles; merge/roll overflow.

## Naming & Packages
- ModId: `first_one`
- Packages:
  - `dev.fristone.core` — common data, net, state machines
  - `dev.fristone.backpack` — encumbrance
  - `dev.fristone.scavenge` — lootables
  - `dev.fristone.status` — heat/thirst/fatigue
  - `dev.fristone.combat` — knockback/hitstun/downed
  - `dev.fristone.ui` — Immersive HUD & subjective text

## Data Formats (Datapack‑overridable)
- Item metrics: `data/first_one/item_metrics/<ns>/<id>.json`
  ```json
  { "vol": 0.25, "mass": 0.5, "form": "weapon" }
  ```
- Scavenge loot tables: `data/first_one/loot_tables/scavenge/<tier>/<name>.json`
- Subjective text: `assets/first_one/lang/xx_xx.json`
  ```json
  { "first_one.feel.heavy.2": "感觉身上挺重的…" }
  ```
- State tunings: `data/first_one/tunings/status.json`

## Internal API (subject to change)
- `Encumbrance.compute(player) -> {encVol, encMass, penalty, tier}`
- `Status.sample(player) -> {heat, thirst, fatigue, tiers}`
- `UI.pushHint(player, key, priority, ttl)`
- `Combat.applyKnock(p, dir, power, stance)`
- `Scavenge.roll(container, context) -> List<ItemStack>`

## Tech & Versions
- Forge 1.20.1, Java 21+ build.
- Optional: GeckoLib (visual rigs).

## Performance Budget
- MBA M2 target ≥ 90 FPS at default view distance.
- Tick budgets: encumbrance 10t (moving 5t), states 20t, combat event‑driven.

## Definition of Done (MVP)
Encumbrance penalties observable; subjective hints without numbers; scavenge box with timed refresh; basic heat/thirst/fatigue gradients; combat hitstun and sprint collisions; minimal UI with handheld instruments for numeric reads; 2‑player sync stable.

## Roadmap (Milestones A0–A6)
A0 scaffolding + encumbrance MVP → A1 hints → A2 scavenge → A3 states → A4 combat → A5 perf/net → A6 closed test & i18n.
