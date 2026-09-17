package com.showcase.gametest;

import com.showcase.utils.minecraft.BlockCategoryCache;
import com.showcase.utils.minecraft.BlockCategoryRegistry;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Optimized GameTest for BlockCategories utility class.
 * Tests block categorization logic with improved structure and reduced redundancy.
 */
public class BlockCategoriesGameTest {

    /**
     * Resolves a dyed block through the block registry.
     *
     * <p>26.2 folded the per-colour {@code Blocks.RED_WOOL}-style constants into {@code ColorCollection}
     * fields while 26.1.2 still exposes them individually, so the registry id is the only spelling that
     * holds across every supported row.
     */
    private static Block dyedBlock(String path) {
        return Objects.requireNonNull(
            BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", path)),
            "Missing block: minecraft:" + path);
    }

    @GameTest
    public void testBlockCategorizationByType(GameTestHelper context) {
        try {
            // Test-colored blocks
            testCategoryGroup(context, CreativeModeTabs.COLORED_BLOCKS, "COLORED_BLOCKS",
                Arrays.asList(dyedBlock("red_wool"), dyedBlock("blue_concrete"), dyedBlock("white_terracotta")));

            // Test natural blocks
            testCategoryGroup(context, CreativeModeTabs.NATURAL_BLOCKS, "NATURAL_BLOCKS",
                Arrays.asList(Blocks.STONE, Blocks.GRASS_BLOCK, Blocks.OAK_LOG, Blocks.IRON_ORE));

            // Test building blocks
            testCategoryGroup(context, CreativeModeTabs.BUILDING_BLOCKS, "BUILDING_BLOCKS",
                Arrays.asList(Blocks.OAK_PLANKS, Blocks.STONE_BRICKS, Blocks.POLISHED_GRANITE));

            // Test redstone blocks
            testCategoryGroup(context, CreativeModeTabs.REDSTONE_BLOCKS, "REDSTONE_BLOCKS",
                Arrays.asList(Blocks.REDSTONE_BLOCK, Blocks.PISTON, Blocks.HOPPER, Blocks.LEVER));

        } catch (Exception e) {
            context.fail(Component.literal("Block categorization test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testSpecialBlockCategories(GameTestHelper context) {
        try {
            // Test functional blocks
            testCategoryGroup(context, CreativeModeTabs.FUNCTIONAL_BLOCKS, "FUNCTIONAL_BLOCKS",
                Arrays.asList(Blocks.CRAFTING_TABLE, Blocks.FURNACE, Blocks.CHEST, Blocks.ANVIL));

            // Test combat blocks
            testCategoryGroup(context, CreativeModeTabs.COMBAT, "COMBAT",
                Arrays.asList(Blocks.TNT, Blocks.MAGMA_BLOCK, Blocks.CACTUS));

            // Test edge cases and defaults
            testEdgeCases(context);

        } catch (Exception e) {
            context.fail(Component.literal("Special categories test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    @GameTest
    public void testSystemConsistencyAndTranslations(GameTestHelper context) {
        try {
            // Test categorization consistency
            testCategorizationConsistency(context);

            // Test translation keys
            testTranslationKeys(context);

        } catch (Exception e) {
            context.fail(Component.literal("Consistency and translations test failed: " + e.getMessage()));
            return;
        }

        context.succeed();
    }

    // Helper Methods

    private void testCategoryGroup(GameTestHelper context, ResourceKey<CreativeModeTab> expectedCategory, String categoryName, List<Block> blocks) {
        for (Block block : blocks) {
            var blockItem = block.asItem();
            if (blockItem != null) {
                ResourceKey<CreativeModeTab> actualCategory = BlockCategoryCache.getCachedCategory(blockItem);
                context.assertTrue(
                    actualCategory.equals(expectedCategory),
                    Component.literal(String.format("%s should be categorized as %s, but got: %s",
                        block.getDescriptionId(), categoryName, actualCategory))
                );
            }
        }
    }

    private void testEdgeCases(GameTestHelper context) {
        // Test that bedrock is categorized as NATURAL
        var bedrockItem = Blocks.BEDROCK.asItem();
        if (bedrockItem != null) {
            ResourceKey<CreativeModeTab> bedrockCategory = BlockCategoryCache.getCachedCategory(bedrockItem);
            context.assertTrue(
                bedrockCategory.equals(CreativeModeTabs.NATURAL_BLOCKS),
                Component.literal("Bedrock should be categorized as NATURAL_BLOCKS, but got: " + bedrockCategory)
            );
        }

        // Test that air gets default category
        var airItem = Blocks.AIR.asItem();
        if (airItem != null) {
            ResourceKey<CreativeModeTab> airCategory = BlockCategoryCache.getCachedCategory(airItem);
            context.assertTrue(
                airCategory.equals(CreativeModeTabs.BUILDING_BLOCKS),
                Component.literal("Air should get default category BUILDING_BLOCKS, but got: " + airCategory)
            );
        }
    }

    private void testCategorizationConsistency(GameTestHelper context) {
        // Test that categorization is consistent across multiple calls
        var diamondBlockItem = Blocks.DIAMOND_BLOCK.asItem();
        if (diamondBlockItem != null) {
            ResourceKey<CreativeModeTab> firstCall = BlockCategoryCache.getCachedCategory(diamondBlockItem);
            ResourceKey<CreativeModeTab> secondCall = BlockCategoryCache.getCachedCategory(diamondBlockItem);

            context.assertTrue(
                firstCall.equals(secondCall),
                Component.literal("Block categorization should be consistent across calls")
            );
        }

        // Test multiple blocks of the same expected category
        var redWoolItem = dyedBlock("red_wool").asItem();
        var blueWoolItem = dyedBlock("blue_wool").asItem();
        var greenWoolItem = dyedBlock("green_wool").asItem();

        if (redWoolItem != null && blueWoolItem != null && greenWoolItem != null) {
            ResourceKey<CreativeModeTab> redWool = BlockCategoryCache.getCachedCategory(redWoolItem);
            ResourceKey<CreativeModeTab> blueWool = BlockCategoryCache.getCachedCategory(blueWoolItem);
            ResourceKey<CreativeModeTab> greenWool = BlockCategoryCache.getCachedCategory(greenWoolItem);

            context.assertTrue(
                redWool.equals(blueWool) && blueWool.equals(greenWool),
                Component.literal("All colored wool should have the same category")
            );
        }
    }

    private void testTranslationKeys(GameTestHelper context) {
        // Test critical translation keys
        String buildingKey = BlockCategoryRegistry.getTranslationKey(CreativeModeTabs.BUILDING_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.building_blocks".equals(buildingKey),
            Component.literal("Building blocks translation key should be 'showcase.stats.block.building_blocks', but got: " + buildingKey)
        );

        String coloredKey = BlockCategoryRegistry.getTranslationKey(CreativeModeTabs.COLORED_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.colored_blocks".equals(coloredKey),
            Component.literal("Colored blocks translation key should be 'showcase.stats.block.colored_blocks', but got: " + coloredKey)
        );

        String naturalKey = BlockCategoryRegistry.getTranslationKey(CreativeModeTabs.NATURAL_BLOCKS);
        context.assertTrue(
            "showcase.stats.block.natural_blocks".equals(naturalKey),
            Component.literal("Natural blocks translation key should be 'showcase.stats.block.natural_blocks', but got: " + naturalKey)
        );
    }
}
