package nl.tudelft.opencraft.yardstick.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.google.common.collect.Lists;
import nl.tudelft.opencraft.yardstick.bot.Bot;
import nl.tudelft.opencraft.yardstick.bot.ai.task.BreakBlocksTask;
import nl.tudelft.opencraft.yardstick.bot.ai.task.PlaceBlocksTask;
import nl.tudelft.opencraft.yardstick.playerbehavior.Task;
import nl.tudelft.opencraft.yardstick.bot.world.Block;
import nl.tudelft.opencraft.yardstick.bot.world.BlockFace;
import nl.tudelft.opencraft.yardstick.bot.world.ChunkNotLoadedException;
import nl.tudelft.opencraft.yardstick.bot.world.Material;
import nl.tudelft.opencraft.yardstick.bot.world.World;
import nl.tudelft.opencraft.yardstick.playerbehavior.Vector3i;
import nl.tudelft.opencraft.yardstick.util.WorldUtil;

/**
 * Represents a model which places and breaks blocks randomly.
 */
public class SimpleInteractionModel implements BotModel {

    private static final int INTERACT_BLOCK_AMOUNT = 3;
    private static final int BREAK_BLOCK_RADIUS = 1;
    private static final int PLACE_BLOCK_RADIUS = 3;
    private static final Material PLACE_BLOCK_MATERIAL = Material.STONE;

    @Override
    public Task newTask(Bot bot) {
        if (Math.random() < 0.5) {
            // Break blocks
            List<Block> selection = selectBreakBlocks(bot);
            if (selection.isEmpty()) {
                bot.getLogger().warning("Could not find breakable blocks!");
                return null;
            }

            return new BreakBlocksTask(bot, selection);
        }

        // Place blocks
        List<Vector3i> selection = selectPlaceBlocks(bot);
        if (selection.isEmpty()) {
            bot.getLogger().warning("Could not find placable blocks!");
            return null;
        }
        return new PlaceBlocksTask(bot, selection, PLACE_BLOCK_MATERIAL);
    }

    private static List<Block> selectBreakBlocks(Bot bot) {
        List<Block> possibilities = new ArrayList<>();

        Block playerBlock;
        try {
            playerBlock = bot.getWorld().getBlockAt(bot.getPlayer().getLocation().intVector());
        } catch (ChunkNotLoadedException ex) {
            return possibilities;
        }

        int radius = BREAK_BLOCK_RADIUS;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Can't break blocks under on in the player
                    if (x == 0 && z == 0 && y < 2) {
                        continue;
                    }

                    if (bot.getPlayer().getLocation().getY() + y <= 1) {
                        continue;
                    }

                    // Get the relative block
                    Block block;
                    try {
                        block = playerBlock.getRelative(x, y, z);
                    } catch (ChunkNotLoadedException ex) {
                        continue;
                    }

                    // Can't break AIR
                    if (block.getMaterial() == Material.AIR) {
                        continue;
                    }

                    int absX = Math.abs(x);
                    int absZ = Math.abs(z);

                    // Skip corners, we need to be able to see them to break them
                    // and implementing that logic is too complicated for now.
                    if (absX == 1 && absZ == 1) {
                        continue;
                    }

                    if (WorldUtil.getVisibleBlockFace(bot.getPlayer(), block) != null) {
                        possibilities.add(block);
                    }
                }
            }
        }

        Collections.shuffle(possibilities, ThreadLocalRandom.current());
        if (possibilities.size() < INTERACT_BLOCK_AMOUNT) {
            return possibilities;
        } else {
            return possibilities.subList(0, INTERACT_BLOCK_AMOUNT - 1);
        }
    }

    private static List<Vector3i> selectPlaceBlocks(Bot bot) {
        List<Vector3i> possibilities = Lists.newArrayList();

        World world = bot.getWorld();
        Vector3i playerLoc = bot.getPlayer().getLocation().intVector();

        int radius = PLACE_BLOCK_RADIUS;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) {
                    // Can't place a block in the player
                    continue;
                }

                // Find a block we can place on the floor
                for (int y = -2; y <= 1; y++) {
                    if (playerLoc.getY() + y <= 1) {
                        continue;
                    }

                    // Find a supporting block below
                    Block support;
                    try {
                        support = world.getBlockAt(playerLoc.add(x, y, z));
                    } catch (ChunkNotLoadedException ex) {
                        continue;
                    }

                    if (support.getMaterial().isTraversable()) {
                        continue;
                    }

                    // Find an empty (air) block above
                    Block at;
                    try {
                        at = support.getRelative(BlockFace.TOP);
                    } catch (ChunkNotLoadedException ex) {
                        continue;
                    }

                    if (at.getMaterial() != Material.AIR) {
                        continue;
                    }

                    // Found a block
                    possibilities.add(at.getLocation());
                    break;
                }

            }
        }

        Collections.shuffle(possibilities, ThreadLocalRandom.current());
        if (possibilities.size() < INTERACT_BLOCK_AMOUNT) {
            return possibilities;
        } else {
            return possibilities.subList(0, INTERACT_BLOCK_AMOUNT - 1);
        }
    }
}
