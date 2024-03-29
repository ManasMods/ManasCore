![#ManasCore](https://www.bisecthosting.com/images/CF/ManasCore/BH_MC_Header.png)
<p align="center"><a href="https://www.curseforge.com/minecraft/mc-mods/manascore"><img src="https://cf.way2muchnoise.eu/full_619025_downloads.svg"> <img src="https://cf.way2muchnoise.eu/versions/619025.svg"></a></p>
This Mod doesn't provide any new functionality for Minecraft itself. It is required by other Mods and provides useful utilities for your Mod Development.

# Features
- Inventory Tab API
- Extended BlockStateProvider (for DataGen)
- Extended BlockTagProvider (for DataGen)
- CustomDataProvider (for DataGen)
- Extended ItemModelProvider (for DataGen)
- Extended ItemTagProvider (for DataGen)
- Extended LanguageProvider (for DataGen)
- Extended RecipeProvider (for DataGen)
- Entity Attribute helpers for easy Attribute handling
- JUMP_POWER Attribute for Entities (allows to modify the jump height of entities)
- Permission helper for easy permission creation and checks
- DataManager for custom datapack file creation
- Builder for custom Biome creation

## Development Setup
You only need to add our maven repository and dependency to your build.gradle file
```groovy
repositories {
    //...
    maven { url "https://dl.cloudsmith.io/public/manasmods/manascore/maven/" }
}

dependencies {
    //...
    implementation fg.deobf("com.github.manasmods:ManasCore:${minecraftVersion}-${manasCoreVersion}")
}
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