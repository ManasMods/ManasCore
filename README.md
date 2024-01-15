![#ManasCore](https://www.bisecthosting.com/images/CF/ManasCore/BH_MC_Header.png)
<p align="center"><a href="https://www.curseforge.com/minecraft/mc-mods/manascore"><img src="https://cf.way2muchnoise.eu/full_619025_downloads.svg"> <img src="https://cf.way2muchnoise.eu/versions/619025.svg"></a></p>
This Mod doesn't provide any new functionality for Minecraft itself. It is required by other Mods and provides useful utilities for your Mod Development.

# Features

- Storage API
    - Works on Fabric, Forge and NeoForge
    - Supports data storage for Entities, Chunks and Worlds (Dimensions)
    - Automatically saves and loads data
    - Automatically syncs data between server and client
- Events
    - ChunkEvents
        - CHUNK_PRE_TICK -> Called every tick for every loaded chunk before the tick is executed
        - CHUNK_POST_TICK -> Called every tick for every loaded chunk after the tick is executed
    - EntityEvents
        - LIVING_PRE_TICK -> Called every tick for every living entity before the tick is executed
        - LIVING_POST_TICK -> Called every tick for every living entity after the tick is executed
        - LIVING_CHANGE_TARGET -> Called when a living entity changes its target
- Extended Keybindings
    - Works on Fabric, Forge and NeoForge
    - Automatically runs an action when a key is pressed (no need to check for key presses every tick)
    - 2 Action Modes
        - VANILLA -> Action is run every tick while the key is pressed
        - ON_CHANCE -> A `onPress` Action is executed when the key is pressed and a `onRelease` Action is executed when
          the key is released
- Builder like content creation
    - Create custom blocks, items, entities, blockEntities and attributes. with a builder like syntax
- Skill API

## Development Setup

- [Architectury](#architectury)
- [Forge](#forge)
- [NeoForge](#neoforge)
- [Fabric](#fabric)

### Architectury

Global `gradle.properties`:

```properties
manascore_version=<version>
```

Common `build.gradle`:

```groovy
// tbd
```

### Forge

build.gradle:

```groovy
// tbd
```

### NeoForge

build.gradle:

```groovy
// tbd
```

### Fabric

build.gradle:

```groovy
// tbd
```

# Notes

See [LICENCE](https://github.com/ManasMods/ManasCore/blob/master/LICENSE) for ManasCore license.

For a list of all authors, see [CONTRIBUTORS](https://github.com/ManasMods/ManasCore/graphs/contributors) graph.

# Supporting Companies

[<img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" width="200">](https://jb.gg/OpenSourceSupport)
[<img src="https://user-images.githubusercontent.com/35544624/202033667-5064bf39-f8a0-46ec-9ddd-bcbb313e1d26.png" width="200">](https://bisecthosting.com/bloodmoon)

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that
enables your organization to create, store and share packages in any format, to any place, with total
confidence.
