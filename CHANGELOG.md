# 2.0.11.1

- fix Sapling model location

# 2.0.11.0

- add sapling model gen method to BlockStateProvider

# 2.0.10.1

- fix issue with `@SlabModel` using the stairs gen method

# 2.0.10.0

- add render type support to `BlockStateProvider#defaultBlock` and `@CubeAllModel` annotation

# 2.0.9.0

- add `needsNetheriteTool`, `needsDiamondTool`, `needsIronTool`, `needsStoneTool`, `needsWoodenTools` and `needsGoldenTools` to BlockTagProvider

# 2.0.8.0

- add supplier supporting method of `subTag` to ItemTagProvider
- add supplier supporting method of `mineableWithAxe`, `mineableWithHoe`, `mineableWithPickaxe`, `mineableWithShovel`, `mineableWithAllTools` and `subTag` to BlockTagProvider

# 2.0.7.0

- add supplier supporting method of `slab`, `stairs`, `planksFromLogs` and `nineStorage`

# 2.0.6.0

- add `addBlock`, `addItem` and `addEntityType` to `LanguageProvider`

# 2.0.5.0

- automatically load known entities in EntityLoot using the `WithLootTables` annotation
- add `@GenerateEntityLoot`
- add extended `EntityLoot` class

# 2.0.4.0

- automatically load known block in BlockLoot using the `WithLootTables` annotation

# 2.0.3.0

- add `@GenerateBlockLoot`
- add extended `BlockLoot` class

# 2.0.2.1

- fix wrong annotation targets in `@GenerateBlockModels` sub annotations

# 2.0.2.0

- rename `@GenerateModels` Annotation to `@GenerateItemModels`
- add `@GenerateBlockModels`

# 2.0.1.0

- add `@GenerateModels` Annotation
- add `@SingleTextureModel` Annotation to generate a `singleTexture` model for the annotated registry object field
- add `@SingleHandheldTextureModel` Annotation to generate a `handheldSingleTexture` model for the annotated registry object field

# 2.0.0.0

- port to 1.19.2