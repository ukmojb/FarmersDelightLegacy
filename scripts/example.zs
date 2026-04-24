import crafttweaker.block.IBlockState;
import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;
import crafttweaker.world.IBlockPos;
import crafttweaker.world.IWorld;

// Farmers Delight Legacy 的 CraftTweaker 示例脚本。
// CraftTweaker example script for Farmers Delight Legacy.
//
// 说明 / Notes:
// 1. 本文件演示当前已实现的 CRT 接口。
//    This file demonstrates the currently implemented CRT APIs.
// 2. 配方 key 建议始终使用你自己的命名空间，避免与别的脚本冲突。
//    Always use your own namespace for recipe keys to avoid conflicts with other scripts.
// 3. 厨锅支持“默认容器 / 指定容器 / 显式无容器”。
//    Cooking Pot supports "default container / explicit container / explicitly no container".
// 4. 砧板支持“默认刀 / 指定工具 / 显式无工具”。
//    Cutting Board supports "default knife / explicit tool / explicitly no tool".
// 5. 现在也支持“按输出物品批量删除配方”。
//    Removing recipes by output item is also supported now.


// =========================
// 热源接口示例 / Heat Source Examples
// =========================

// 将点燃的熔炉注册为直接热源。
// Register a lit furnace as a direct heat source.
mods.farmersdelight.HeatSource.addDirectHeatSourceBlock(
    "example:lit_furnace_direct_heat",
    "minecraft:lit_furnace"
);

// 将漏斗注册为“热源检测下移一格”的方块。
// Register a hopper as a block that offsets heat detection downward by one block.
mods.farmersdelight.HeatSource.addOffsetBlock(
    "example:hopper_heat_offset",
    "minecraft:hopper"
);

// 通过回调动态判定直接热源。
// Dynamically determine direct heat sources with a callback.
mods.farmersdelight.HeatSource.addDirectHeatSourcePredicate(
    "example:ice_direct_heat",
    function(world as IWorld, pos as IBlockPos, state as IBlockState) as bool {
        return state.block.definition.id == "minecraft:ice";
    }
);

// 通过回调动态判定是否需要把检测位置下移一格。
// Dynamically determine whether the heat check should be offset downward.
mods.farmersdelight.HeatSource.addOffsetPredicate(
    "example:bedrock_offset_heat",
    function(world as IWorld, pos as IBlockPos, state as IBlockState) as bool {
        return state.block.definition.id == "minecraft:bedrock";
    }
);


// =========================
// 营火配方示例 / Campfire Recipe Examples
// =========================

// 基础写法：使用 IIngredient / IItemStack。
// Basic form: uses IIngredient / IItemStack.
mods.farmersdelight.Campfire.addRecipe(
    "example:campfire_beef",
    [<ore:listAllbeefraw> as IIngredient],
    <minecraft:cooked_beef> as IItemStack
);

// 高级写法：字符串版，可自定义产物数量和烹饪时间。
// Advanced form: string version with custom output count and cooking time.
mods.farmersdelight.Campfire.addRecipeAdvanced(
    "example:campfire_baked_potato_fast",
    ["minecraft:potato"],
    "minecraft:baked_potato",
    1,
    200
);


// =========================
// 厨锅配方示例 / Cooking Pot Recipe Examples
// =========================

// 默认容器逻辑：
// 如果产物自己有容器返回物，则优先使用该容器；
// 如果没有，则按模组默认逻辑处理，多数餐食会默认使用碗。
// Default container logic:
// If the result item already defines a crafting remainder/container, that container is used first;
// otherwise the mod falls back to its default logic, and most meals will use bowls by default.
mods.farmersdelight.CookingPot.addRecipe(
    "example:pot_default_container",
    [<minecraft:carrot>, <minecraft:potato>] as IIngredient[],
    <minecraft:rabbit_stew>
);

// 显式指定容器：
// 这个配方最终只能用碗来盛装。
// Explicit container:
// This recipe can only be served with bowls.
mods.farmersdelight.CookingPot.addRecipeWithContainer(
    "example:pot_force_bowl",
    [<minecraft:apple>, <minecraft:sugar>] as IIngredient[],
    <minecraft:cookie>,
    <minecraft:bowl>
);

// 显式无容器：
// 即使产物没有默认容器，这里也明确声明“不需要额外容器”。
// Explicitly no container:
// Even if the result has no default container, this explicitly marks the recipe as requiring none.
mods.farmersdelight.CookingPot.addRecipeWithoutContainer(
    "example:pot_without_container",
    [<minecraft:wheat>, <minecraft:sugar>] as IIngredient[],
    <minecraft:bread>
);

// 字符串版模板。
// String overload templates.
// 字符串物品 token 使用 modid:item@meta 表示精确 metadata。
// String item tokens use modid:item@meta for exact metadata matching.
// 例如 / Example:
// "minecraft:fish@1" 代表 metadata 为 1 的 minecraft:fish。
// "minecraft:fish@1" represents minecraft:fish with metadata 1.
// mods.farmersdelight.CookingPot.addRecipeWithContainer(
//     "yourpack:pot_string_container",
//     ["minecraft:fish@1", "ore:listAllbeefraw"],
//     "minecraft:rabbit_stew",
//     1,
//     "minecraft:bowl",
//     1
// );
//
// mods.farmersdelight.CookingPot.addRecipeWithoutContainer(
//     "yourpack:pot_string_no_container",
//     ["minecraft:wheat", "minecraft:sugar"],
//     "minecraft:bread",
//     1
// );
//
// mods.farmersdelight.CookingPot.addRecipeAdvanced(
//     "yourpack:pot_full_control",
//     ["minecraft:beetroot", "minecraft:beetroot", "minecraft:beetroot"],
//     "minecraft:beetroot_soup",
//     1,
//     "minecraft:bowl",
//     1,
//     120,
//     1.0,
//     true
// );


// =========================
// 砧板配方示例 / Cutting Board Recipe Examples
// =========================

// 默认写法：默认要求工具为刀。
// Default form: requires a knife by default.
mods.farmersdelight.CuttingBoard.addRecipe(
    "example:board_default_knife",
    [<minecraft:pumpkin>] as IIngredient[],
    [<minecraft:pumpkin_seeds> * 4] as IItemStack[]
);

// 指定工具：这个配方只能用斧头处理。
// Explicit tool: this recipe can only be processed with an axe.
mods.farmersdelight.CuttingBoard.addRecipeWithTool(
    "example:board_axe_only",
    [<minecraft:log>] as IIngredient[],
    [<ore:toolAxe>] as IIngredient[],
    [<minecraft:planks> * 4, <farmersdelight:tree_bark>] as IItemStack[]
);

// 显式无工具：把物品放到砧板后，空手主手右击即可处理。
// Explicitly no tool: place the item on the board and right-click with an empty main hand to process it.
mods.farmersdelight.CuttingBoard.addRecipeWithoutTool(
    "example:board_hand_only",
    [<minecraft:melon_block>] as IIngredient[],
    [<minecraft:melon> * 4] as IItemStack[]
);

// 带掉率的完整写法模板。
// Full template with result chances.
// mods.farmersdelight.CuttingBoard.addRecipeAdvanced(
//     "yourpack:board_full_control",
//     [<minecraft:fish:1>] as IIngredient[],
//     [<ore:toolKnife>] as IIngredient[],
//     [<minecraft:fish:1> * 2, <minecraft:dye:15>] as IItemStack[],
//     [1.0, 0.25] as float[]
// );
//
// 字符串版模板。
// String overload templates.
// 字符串物品 token 使用 modid:item@meta 表示精确 metadata。
// String item tokens use modid:item@meta for exact metadata matching.
// mods.farmersdelight.CuttingBoard.addRecipeWithTool(
//     "yourpack:board_string_tool",
//     ["minecraft:fish@1"],
//     ["ore:toolAxe"],
//     ["minecraft:fish@1", "farmersdelight:tree_bark"]
// );
//
// mods.farmersdelight.CuttingBoard.addRecipeWithoutTool(
//     "yourpack:board_string_no_tool",
//     ["minecraft:melon_block"],
//     ["minecraft:melon"]
// );


// =========================
// 删除配方模板 / Recipe Removal Templates
// =========================

// 按 key 删除单个配方。
// Remove a single recipe by key.
// mods.farmersdelight.Campfire.removeRecipe("example:campfire_beef");
// mods.farmersdelight.CookingPot.removeRecipe("example:pot_force_bowl");
// mods.farmersdelight.CuttingBoard.removeRecipe("example:board_hand_only");

// 按输出物品批量删除所有同产物配方。
// Remove all recipes that share the same output item.
// mods.farmersdelight.Campfire.removeRecipesByOutput(<minecraft:cooked_beef>);
// mods.farmersdelight.CookingPot.removeRecipesByOutput(<minecraft:beetroot_soup>);
// mods.farmersdelight.CuttingBoard.removeRecipesByOutput(<minecraft:dye:15>);
//
// 字符串版删除模板。
// String overload templates for removal.
// mods.farmersdelight.Campfire.removeRecipesByOutput("minecraft:cooked_beef");
// mods.farmersdelight.CookingPot.removeRecipesByOutput("minecraft:beetroot_soup");
// mods.farmersdelight.CuttingBoard.removeRecipesByOutput("minecraft:fish@1");
