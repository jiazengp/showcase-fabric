package com.showcase.gametest;

import com.showcase.utils.BlockCategories;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Optimized GameTest for BlockCategories utility class.
 * Tests block categorization logic with improved structure and reduced redundancy.
 */
public class BlockCategoriesGameTest {

    @GameTest
    public void testBlockCategorizationByType(TestContext context) {
        try {
            // Test-colored blocks
            testCategoryGroup(context, ItemGroups.COLORED_BLOCKS, "COLORED_BLOCKS",
                Arrays.asList(Blocks.RED_WOOL, Blocks.BLUE_CONCRETE, Blocks.WHITE_TERRACOTTA));
            
            // Test natural blocks
            testCategoryGroup(context, ItemGroups.NATURAL, "NATURAL",
                Arrays.asList(Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.OAK_LOG, Blocks.IRON_ORE));
            
            // Test building blocks
            testCategoryGroup(context, ItemGroups.BUILDING_BLOCKS, "BUILDING_BLOCKS",
                Arrays.asList(Blocks.OAK_PLANKS, Blocks.STONE_BRICKS, Blocks.POLISHED_GRANITE));
            
            // Test redstone blocks
            testCategoryGroup(context, ItemGroups.REDSTONE, "REDSTONE",
                Arrays.asList(Blocks.REDSTONE_BLOCK, Blocks.PISTON, Blocks.HOPPER, Blocks.LEVER));
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Block categorization test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    @GameTest
    public void testSpecialBlockCategories(TestContext context) {
        try {
            // Test functional blocks
            testCategoryGroup(context, ItemGroups.FUNCTIONAL, "FUNCTIONAL",
                Arrays.asList(Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.CHEST, Blocks.ANVIL));
            
            // Test combat blocks
            testCategoryGroup(context, ItemGroups.COMBAT, "COMBAT",
                Arrays.asList(Blocks.TNT, Blocks.MAGMA_BLOCK, Blocks.CACTUS));
            
            // Test edge cases and defaults
            testEdgeCases(context);
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Special categories test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    @GameTest
    public void testSystemConsistencyAndTranslations(TestContext context) {
        try {
            // Test categorization consistency
            testCategorizationConsistency(context);
            
            // Test translation keys
            testTranslationKeys(context);
            
        } catch (Exception e) {
            context.throwGameTestException(Text.of("Consistency and translations test failed: " + e.getMessage()));
        }
        
        context.complete();
    }

    // Helper Methods
    
    private void testCategoryGroup(TestContext context, RegistryKey<ItemGroup> expectedCategory, String categoryName, List<Block> blocks) {
        for (Block block : blocks) {
            RegistryKey<ItemGroup> actualCategory = BlockCategories.getBlockCategory(block);
            context.assertTrue(
                actualCategory.equals(expectedCategory),
                Text.of(String.format("%s should be categorized as %s, but got: %s", 
                    block.getTranslationKey(), categoryName, actualCategory))
            );
        }
    }
    
    private void testEdgeCases(TestContext context) {
        // Test that bedrock is categorized as NATURAL
        RegistryKey<ItemGroup> bedrockCategory = BlockCategories.getBlockCategory(Blocks.BEDROCK);
        context.assertTrue(
            bedrockCategory.equals(ItemGroups.NATURAL),
            Text.of("Bedrock should be categorized as NATURAL, but got: " + bedrockCategory)
        );

        // Test that air gets default category
        RegistryKey<ItemGroup> airCategory = BlockCategories.getBlockCategory(Blocks.AIR);
        context.assertTrue(
            airCategory.equals(ItemGroups.BUILDING_BLOCKS),
            Text.of("Air should get default category BUILDING_BLOCKS, but got: " + airCategory)
        );
    }
    
    private void testCategorizationConsistency(TestContext context) {
        // Test that categorization is consistent across multiple calls
        RegistryKey<ItemGroup> firstCall = BlockCategories.getBlockCategory(Blocks.DIAMOND_BLOCK);
        RegistryKey<ItemGroup> secondCall = BlockCategories.getBlockCategory(Blocks.DIAMOND_BLOCK);
        
        context.assertTrue(
            firstCall.equals(secondCall),
            Text.of("Block categorization should be consistent across calls")
        );

        // Test multiple blocks of the same expected category
        RegistryKey<ItemGroup> redWool = BlockCategories.getBlockCategory(Blocks.RED_WOOL);
        RegistryKey<ItemGroup> blueWool = BlockCategories.getBlockCategory(Blocks.BLUE_WOOL);
        RegistryKey<ItemGroup> greenWool = BlockCategories.getBlockCategory(Blocks.GREEN_WOOL);

        context.assertTrue(
            redWool.equals(blueWool) && blueWool.equals(greenWool),
            Text.of("All colored wool should have the same category")
        );
    }
    
    private void testTranslationKeys(TestContext context) {
        // Test critical translation keys
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
    }
}