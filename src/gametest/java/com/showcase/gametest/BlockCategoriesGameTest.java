package com.showcase.gametest;

import com.showcase.utils.minecraft.BlockCategoryCache;
import com.showcase.utils.minecraft.BlockCategoryRegistry;
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
            var blockItem = block.asItem();
            if (blockItem != null) {
                RegistryKey<ItemGroup> actualCategory = BlockCategoryCache.getCachedCategory(blockItem);
                context.assertTrue(
                    actualCategory.equals(expectedCategory),
                    Text.of(String.format("%s should be categorized as %s, but got: %s", 
                        block.getTranslationKey(), categoryName, actualCategory))
                );
            }
        }
    }
    
    private void testEdgeCases(TestContext context) {
        // Test that bedrock is categorized as NATURAL
        var bedrockItem = Blocks.BEDROCK.asItem();
        if (bedrockItem != null) {
            RegistryKey<ItemGroup> bedrockCategory = BlockCategoryCache.getCachedCategory(bedrockItem);
            context.assertTrue(
                bedrockCategory.equals(ItemGroups.NATURAL),
                Text.of("Bedrock should be categorized as NATURAL, but got: " + bedrockCategory)
            );
        }

        // Test that air gets default category
        var airItem = Blocks.AIR.asItem();
        if (airItem != null) {
            RegistryKey<ItemGroup> airCategory = BlockCategoryCache.getCachedCategory(airItem);
            context.assertTrue(
                airCategory.equals(ItemGroups.BUILDING_BLOCKS),
                Text.of("Air should get default category BUILDING_BLOCKS, but got: " + airCategory)
            );
        }
    }
    
    private void testCategorizationConsistency(TestContext context) {
        // Test that categorization is consistent across multiple calls
        var diamondBlockItem = Blocks.DIAMOND_BLOCK.asItem();
        if (diamondBlockItem != null) {
            RegistryKey<ItemGroup> firstCall = BlockCategoryCache.getCachedCategory(diamondBlockItem);
            RegistryKey<ItemGroup> secondCall = BlockCategoryCache.getCachedCategory(diamondBlockItem);
            
            context.assertTrue(
                firstCall.equals(secondCall),
                Text.of("Block categorization should be consistent across calls")
            );
        }

        // Test multiple blocks of the same expected category
        var redWoolItem = Blocks.RED_WOOL.asItem();
        var blueWoolItem = Blocks.BLUE_WOOL.asItem();
        var greenWoolItem = Blocks.GREEN_WOOL.asItem();
        
        if (redWoolItem != null && blueWoolItem != null && greenWoolItem != null) {
            RegistryKey<ItemGroup> redWool = BlockCategoryCache.getCachedCategory(redWoolItem);
            RegistryKey<ItemGroup> blueWool = BlockCategoryCache.getCachedCategory(blueWoolItem);
            RegistryKey<ItemGroup> greenWool = BlockCategoryCache.getCachedCategory(greenWoolItem);

            context.assertTrue(
                redWool.equals(blueWool) && blueWool.equals(greenWool),
                Text.of("All colored wool should have the same category")
            );
        }
    }
    
    private void testTranslationKeys(TestContext context) {
        // Test critical translation keys
        String buildingKey = BlockCategoryRegistry.getTranslationKey(ItemGroups.BUILDING_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.building_blocks".equals(buildingKey),
            Text.of("Building blocks translation key should be 'showcase.stats.block.building_blocks', but got: " + buildingKey)
        );

        String coloredKey = BlockCategoryRegistry.getTranslationKey(ItemGroups.COLORED_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.colored_blocks".equals(coloredKey),
            Text.of("Colored blocks translation key should be 'showcase.stats.block.colored_blocks', but got: " + coloredKey)
        );

        String naturalKey = BlockCategoryRegistry.getTranslationKey(ItemGroups.NATURAL);
        context.assertTrue(
            "showcase.stats.block.natural_blocks".equals(naturalKey),
            Text.of("Natural blocks translation key should be 'showcase.stats.block.natural_blocks', but got: " + naturalKey)
        );
    }
}