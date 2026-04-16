# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew build            # Full build (debug + release)
./gradlew clean            # Clean build artifacts
./gradlew test             # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests (requires device/emulator)
```

- **Min SDK:** 30, **Target SDK:** 36, **Java:** 11
- Package: `edu.hitsz`

## Architecture Overview

This is an Android vertical-scrolling shooter game built on `SurfaceView`. The core game loop runs in a background thread at ~62.5 FPS (16ms sleep intervals).

### Entry Flow

`StartActivity` → `DifficultyActivity` → `MainActivity` (hosts `Game` view) → `EndActivity` / `RankingActivity`

`MainActivity` passes a `GameManager` to `Game` via setter injection; `GameManager` encapsulates difficulty parameters.

### Core Game Loop (`application/Game.java`)

Each frame cycle:
1. `action()` — spawn enemies/props, trigger shooting, run collision detection, apply damage
2. `draw(canvas)` — render all flying objects and HUD to canvas
3. `Thread.sleep(16)` — frame pacing

Thread safety is managed via `threadLock` for pause/resume/reset operations.

### Class Hierarchy

```
AbstractFlyingObject          — position, velocity, hitbox, valid/invalid state
├── AbstractAircraft          — HP, ShootStrategy, shootStrategy()
│   ├── HeroAircraft          — Singleton; touch-controlled; bomb ability
│   ├── AbstractEnemy
│   │   ├── MobEnemy          — straight-line movement, no shooting
│   │   ├── EliteEnemy        — direct shoot, drops props
│   │   ├── ElitePlusEnemy    — scatter shoot, drops props
│   │   └── BossEnemy         — circular shoot, high HP, score-triggered spawn
└── AbstractProp              — one-time pickup effect on hero
    ├── HpProp / BombProp / FireProp / FirePlusProp
└── AbstractBullet (BaseBullet → HeroBullet / EnemyBullet)
```

### Design Patterns

| Pattern | Where Used |
|---|---|
| Singleton | `HeroAircraft` (double-checked locking) |
| Factory | `EnemyFactory` → `MobEnemyFactory`, `EliteEnemyFactory`, `BossFactory`; `PropFactory` variants |
| Strategy | `ShootStrategy` interface → `DirectShoot`, `CircularShoot`, `ScatterShoot`, `NoShoot` |
| Observer | `BombListener` interface for bomb explosion callbacks |
| Template Method | `AbstractFlyingObject` defines movement/collision; subclasses override behavior |

### Difficulty System (`manager/`)

`GameManager` interface exposes spawn rates, max enemy counts, and speed multipliers. `SinglePlayerManager` has three difficulty configurations:
- **Easy:** Max 7 enemies, 700ms spawn
- **Normal:** Max 5 enemies, 450ms spawn, 1.3× speed
- **Hard:** Max 4 enemies, 400→200ms dynamic spawn, 1.6× speed, Boss HP scales with count

### Resource Management

- **`ImageManager`**: Lazy-loads `Bitmap` per class type; uses class→Bitmap map
- **`MusicManager`**: Handles BGM and SFX (shoot, hit, bomb, supply, game-over)
- **`GameRecordDaoImpl`**: Persists scores via `ObjectOutputStream` to `files/records.dat` in app sandbox

### Fire Buff

`FireProp` and `FirePlusProp` swap the hero's `ShootStrategy` at runtime. A timestamp records buff start; `Game.action()` checks `System.currentTimeMillis()` against the 6000ms duration and reverts to `DirectShoot` on expiry. The HUD displays a live "FIRE: Xs" countdown.

### Collision Detection

Uses rectangular hitboxes. Aircraft use a Y-axis factor of 0.5 (cockpit zone); bullets use 1.0. Collision invalidates both objects; damage is applied to the aircraft's HP.
