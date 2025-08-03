package com.showcase.gametest;

import com.showcase.utils.BlockCategories;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;


/**
 * GameTest for BlockCategories utility class
 * Tests block categorization logic in a real Minecraft environment
 */
public class BlockCategoriesGameTest {

    @GameTest
    public void testColoredBlockCategorization(TestContext context) {
        // Test colored wool categorization
        var woolCategory = BlockCategories.getBlockCategory(Blocks.RED_WOOL);
        context.assertTrue(
            woolCategory.equals(ItemGroups.COLORED_BLOCKS),
            Text.of("Red wool should be categorized as COLORED_BLOCKS, but got: " + woolCategory)
        );

        var blueConcreteCategory = BlockCategories.getBlockCategory(Blocks.BLUE_CONCRETE);
        context.assertTrue(
            blueConcreteCategory.equals(ItemGroups.COLORED_BLOCKS),
            Text.of("Blue concrete should be categorized as COLORED_BLOCKS, but got: " + blueConcreteCategory)
        );

        var whiteTerracottaCategory = BlockCategories.getBlockCategory(Blocks.WHITE_TERRACOTTA);
        context.assertTrue(
            whiteTerracottaCategory.equals(ItemGroups.COLORED_BLOCKS),
            Text.of("White terracotta should be categorized as COLORED_BLOCKS, but got: " + whiteTerracottaCategory)
        );

        context.complete();
    }

    @GameTest
    public void testNaturalBlockCategorization(TestContext context) {
        // Test natural blocks
        var stoneCategory = BlockCategories.getBlockCategory(Blocks.STONE);
        context.assertTrue(
            stoneCategory.equals(ItemGroups.NATURAL),
            Text.of("Stone should be categorized as NATURAL, but got: " + stoneCategory)
        );

        var grassCategory = BlockCategories.getBlockCategory(Blocks.GRASS_BLOCK);
        context.assertTrue(
            grassCategory.equals(ItemGroups.NATURAL),
            Text.of("Grass block should be categorized as NATURAL, but got: " + grassCategory)
        );

        var oakLogCategory = BlockCategories.getBlockCategory(Blocks.OAK_LOG);
        context.assertTrue(
            oakLogCategory.equals(ItemGroups.NATURAL),
            Text.of("Oak log should be categorized as NATURAL, but got: " + oakLogCategory)
        );

        var ironOreCategory = BlockCategories.getBlockCategory(Blocks.IRON_ORE);
        context.assertTrue(
            ironOreCategory.equals(ItemGroups.NATURAL),
            Text.of("Iron ore should be categorized as NATURAL, but got: " + ironOreCategory)
        );

        context.complete();
    }

    @GameTest
    public void testBuildingBlockCategorization(TestContext context) {
        // Test building blocks
        var oakPlanksCategory = BlockCategories.getBlockCategory(Blocks.OAK_PLANKS);
        context.assertTrue(
            oakPlanksCategory.equals(ItemGroups.BUILDING_BLOCKS),
            Text.of("Oak planks should be categorized as BUILDING_BLOCKS, but got: " + oakPlanksCategory)
        );

        var stoneBricksCategory = BlockCategories.getBlockCategory(Blocks.STONE_BRICKS);
        context.assertTrue(
            stoneBricksCategory.equals(ItemGroups.BUILDING_BLOCKS),
            Text.of("Stone bricks should be categorized as BUILDING_BLOCKS, but got: " + stoneBricksCategory)
        );

        var polishedGraniteCategory = BlockCategories.getBlockCategory(Blocks.POLISHED_GRANITE);
        context.assertTrue(
            polishedGraniteCategory.equals(ItemGroups.BUILDING_BLOCKS),
            Text.of("Polished granite should be categorized as BUILDING_BLOCKS, but got: " + polishedGraniteCategory)
        );

        context.complete();
    }

    @GameTest
    public void testRedstoneBlockCategorization(TestContext context) {
        // Test redstone blocks
        var redstoneBlockCategory = BlockCategories.getBlockCategory(Blocks.REDSTONE_BLOCK);
        context.assertTrue(
            redstoneBlockCategory.equals(ItemGroups.REDSTONE),
            Text.of("Redstone block should be categorized as REDSTONE, but got: " + redstoneBlockCategory)
        );

        var pistonCategory = BlockCategories.getBlockCategory(Blocks.PISTON);
        context.assertTrue(
            pistonCategory.equals(ItemGroups.REDSTONE),
            Text.of("Piston should be categorized as REDSTONE, but got: " + pistonCategory)
        );

        var hopperCategory = BlockCategories.getBlockCategory(Blocks.HOPPER);
        context.assertTrue(
            hopperCategory.equals(ItemGroups.REDSTONE),
            Text.of("Hopper should be categorized as REDSTONE, but got: " + hopperCategory)
        );

        var leverCategory = BlockCategories.getBlockCategory(Blocks.LEVER);
        context.assertTrue(
            leverCategory.equals(ItemGroups.REDSTONE),
            Text.of("Lever should be categorized as REDSTONE, but got: " + leverCategory)
        );

        context.complete();
    }

    @GameTest()
    public void testFunctionalBlockCategorization(TestContext context) {
        // Test functional blocks
        var craftingTableCategory = BlockCategories.getBlockCategory(Blocks.CRAFTING_TABLE);
        context.assertTrue(
            craftingTableCategory.equals(ItemGroups.FUNCTIONAL),
            Text.of("Crafting table should be categorized as FUNCTIONAL, but got: " + craftingTableCategory)
        );

        var furnaceCategory = BlockCategories.getBlockCategory(Blocks.FURNACE);
        context.assertTrue(
            furnaceCategory.equals(ItemGroups.FUNCTIONAL),
            Text.of("Furnace should be categorized as FUNCTIONAL, but got: " + furnaceCategory)
        );

        var chestCategory = BlockCategories.getBlockCategory(Blocks.CHEST);
        context.assertTrue(
            chestCategory.equals(ItemGroups.FUNCTIONAL),
            Text.of("Chest should be categorized as FUNCTIONAL, but got: " + chestCategory)
        );

        var anvilCategory = BlockCategories.getBlockCategory(Blocks.ANVIL);
        context.assertTrue(
            anvilCategory.equals(ItemGroups.FUNCTIONAL),
            Text.of("Anvil should be categorized as FUNCTIONAL, but got: " + anvilCategory)
        );

        context.complete();
    }

    @GameTest
    public void testCombatBlockCategorization(TestContext context) {
        // Test combat blocks
        var tntCategory = BlockCategories.getBlockCategory(Blocks.TNT);
        context.assertTrue(
            tntCategory.equals(ItemGroups.COMBAT),
            Text.of("TNT should be categorized as COMBAT, but got: " + tntCategory)
        );

        var magmaBlockCategory = BlockCategories.getBlockCategory(Blocks.MAGMA_BLOCK);
        context.assertTrue(
            magmaBlockCategory.equals(ItemGroups.COMBAT),
            Text.of("Magma block should be categorized as COMBAT, but got: " + magmaBlockCategory)
        );

        var cactusCategory = BlockCategories.getBlockCategory(Blocks.CACTUS);
        context.assertTrue(
            cactusCategory.equals(ItemGroups.COMBAT),
            Text.of("Cactus should be categorized as COMBAT, but got: " + cactusCategory)
        );

        context.complete();
    }

    @GameTest
    public void testDefaultCategorization(TestContext context) {
        // Test that unknown blocks default to BUILDING_BLOCKS
        var bedrockCategory = BlockCategories.getBlockCategory(Blocks.BEDROCK);
        // Bedrock should be categorized as NATURAL based on the current logic
        context.assertTrue(
            bedrockCategory.equals(ItemGroups.NATURAL),
            Text.of("Bedrock should be categorized as NATURAL, but got: " + bedrockCategory)
        );

        // Test that air gets default category
        var airCategory = BlockCategories.getBlockCategory(Blocks.AIR);
        context.assertTrue(
            airCategory.equals(ItemGroups.BUILDING_BLOCKS),
            Text.of("Air should get default category BUILDING_BLOCKS, but got: " + airCategory)
        );

        context.complete();
    }

    @GameTest
    public void testCategoryTranslationKeys(TestContext context) {
        // Test translation key retrieval
        String buildingKey = BlockCategories.getCategoryTranslationKey(ItemGroups.BUILDING_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.building_blocks".equals(buildingKey),
            Text.of("Building blocks translation key should be 'showcase.stats.block.building_blocks', but got: " + buildingKey)
        );

        String coloredKey = BlockCategories.getCategoryTranslationKey(ItemGroups.COLORED_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.colored_blocks".equals(coloredKey),
            Text.of("Colored blocks translation key should be 'showcase.stats.block.colored_blocks', but got: " + coloredKey)
        );

        String naturalKey = BlockCategories.getCategoryTranslationKey(ItemGroups.NATURAL);
        context.assertTrue(
            "showcase.stats.block.natural_blocks".equals(naturalKey),
            Text.of("Natural blocks translation key should be 'showcase.stats.block.natural_blocks', but got: " + naturalKey)
        );

        context.complete();
    }

    @GameTest
    public void testCategorizationConsistency(TestContext context) {
        // Test that categorization is consistent across multiple calls
        var firstCall = BlockCategories.getBlockCategory(Blocks.DIAMOND_BLOCK);
        var secondCall = BlockCategories.getBlockCategory(Blocks.DIAMOND_BLOCK);
        
        context.assertTrue(
            firstCall.equals(secondCall),
            Text.of("Block categorization should be consistent across calls")
        );

        // Test multiple blocks of the same expected category
        var redWool = BlockCategories.getBlockCategory(Blocks.RED_WOOL);
        var blueWool = BlockCategories.getBlockCategory(Blocks.BLUE_WOOL);
        var greenWool = BlockCategories.getBlockCategory(Blocks.GREEN_WOOL);

        context.assertTrue(
            redWool.equals(blueWool) && blueWool.equals(greenWool),
            Text.of("All colored wool should have the same category")
        );

        context.complete();
    }
}