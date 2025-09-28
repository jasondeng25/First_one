# Data Formats (Datapack‑Overridable)

## 1) Item Metrics
Path: `data/first_one/item_metrics/<namespace>/<path>.json`
```json
{ "vol": 0.25, "mass": 0.5, "form": "weapon" }
```

## 2) Status Tunings
Path: `data/first_one/tunings/status.json`
```json
{
  "thirst": { "decay_idle": 0.0002, "decay_run": 0.002, "tiers": [0.8,0.6,0.4,0.2] },
  "fatigue": { "gain_walk": 0.001, "gain_sprint": 0.006, "recover_sleep": 0.02, "tiers": [0.7,0.5,0.35,0.2] },
  "heat":    { "env_scale": 1.0, "wet_penalty": 0.15, "tiers": [0.8,0.6,0.4,0.2] }
}
```

## 3) Subjective Lines
Path: `assets/first_one/lang/zh_cn.json`
```json
{
  "first_one.feel.hungry.1": "我有点饿……",
  "first_one.feel.thirst.1": "嘴有点干。",
  "first_one.feel.fatigue.1": "腿有点沉。"
}
```

## 4) Scavenge Loot
Path: `data/first_one/loot_tables/scavenge/<tier>/<name>.json`
- Extends vanilla loot table with optional fields:
```json
{ "type": "minecraft:chest", "cooldown": 12000, "noise": 0.35, "heatWeight": 1.2, "pools": [] }
```
