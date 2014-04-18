package REALDrummer.utils;

import java.util.Map;

import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import REALDrummer.myCoreLibrary;
import REALDrummer.Wiki;

import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.StringUtilities.*;
import static REALDrummer.utils.WikiUtilities.*;

public class PlayerUtilities {
    /** This enum Object is to be used in specifying different parameters for the {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()} method.
     * 
     * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#NON_SOLID}, {@link BlockSearch#LIQUID} ,
     *      {@link BlockSearch#SOLID}, {@link BlockSearch#SWITCH}, and {@link BlockSearch#NON_SWITCH_NON_SOLID} */
    public static enum BlockSearch {
        /** This <tt>BlockSearch</tt> enum value specifies that the block found by <tt>{@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}</tt>
         * must be a non-solid block such as a sign or button; note that all switches (buttons and pressure plates and such as specified by <tt>BlockSearch.<i>SWITCH</i></tt>)
         * are also non-solid blocks.
         * 
         * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#LIQUID} , {@link BlockSearch#SOLID},
         *      {@link BlockSearch#SWITCH}, and {@link BlockSearch#NON_SWITCH_NON_SOLID} */
        NON_SOLID,
        /** This <tt>BlockSearch</tt> enum value specifies that the block found by <tt>{@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}</tt>
         * must be a liquid block such as water or lava.
         * 
         * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#NON_SOLID} , {@link BlockSearch#SOLID},
         *      {@link BlockSearch#SWITCH}, and {@link BlockSearch#NON_SWITCH_NON_SOLID} */
        LIQUID,
        /** This <tt>BlockSearch</tt> enum value specifies that the block found by <tt>{@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}</tt>
         * must be a solid block such as dirt or a chest.
         * 
         * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#NON_SOLID} , {@link BlockSearch#LIQUID},
         *      {@link BlockSearch#SWITCH}, and {@link BlockSearch#NON_SWITCH_NON_SOLID} */
        SOLID,
        /** This <tt>BlockSearch</tt> enum value specifies that the block found by <tt>{@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}</tt>
         * must be a switch such as a button, lever, or pressure plate.
         * 
         * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#NON_SOLID} , {@link BlockSearch#LIQUID},
         *      {@link BlockSearch#SOLID}, and {@link BlockSearch#NON_SWITCH_NON_SOLID} */
        SWITCH,
        /** This <tt>BlockSearch</tt> enum value specifies that the block found by <tt>{@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}</tt>
         * must be a non-solid block that si <i>not</i> a switch, e.g. a sign or tall grass block.
         * 
         * @see {@link #getTargetBlock(Player player, BlockSearch... parameters) getTargetBlock()}; {@link BlockSearch#NON_SOLID} , {@link BlockSearch#LIQUID},
         *      {@link BlockSearch#SOLID}, and {@link BlockSearch#SWITCH} */
        NON_SWITCH_NON_SOLID;

        @Override
        public String toString() {
            return replaceAll(name().toLowerCase(), "_", " ");
        }
    }

    /** This method calculates the minimum total number of experience points that a <tt>Player</tt> must have in order to acquire the specified level.
     * 
     * @param level
     *            is the experience level that will be converted to experience points.
     * @return an <b>int</b> representing the number of experience points required at minimum for a <tt>Player</tt> to acquire the given level <b><tt>level</b></tt>.
     * @see {@link #calcLevel(int)} and {@link #calcExactLevel(int)} */
    public static int calcTotalXp(int level) {
        // from the Minecraft Wiki (where x is the level and the result is the amount of total experience required to reach level x):
        // "for x≤16: 17x
        // for 15≤x≤31: 1.5x²-29.5x+360
        // for x≥30: 3.5x²-151.5x+2220"
        if (level < 0)
            return -1;
        else if (level >= 30)
            return (int) (3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
        else if (level <= 16)
            return 17 * level;
        else
            return (int) (1.5 * Math.pow(level, 2) - 29.5 * level + 360);
    }

    /** This method calculates the level that any given <tt>Player</tt> will have if the total number of experience points that they have is the amount specified.
     * 
     * @param xp
     *            is the total number of experience that the hypothetical <tt>Player</tt> has.
     * @return the level that any given <tt>Player</tt> will have if the total number of experience points that they have is the amount specified.
     * @see {@link #calcTotalXp(int)} and {@link #calcExactLevel(int)} */
    public static int calcLevel(int xp) {
        double level = calcExactLevel(xp);
        if (level == -1)
            return -1;
        else
            // round down the exact level value to get the true level
            return (int) calcExactLevel(xp);
    }

    /** This method calculates the level that any given <tt>Player</tt> will have if the total number of experience points that they have is the amount specified. Unlike
     * {@link #calcLevel(int)}, this method will return a double specifying the exact level value that corresponds to the given experience points instead of returning an int
     * representing the shown level value that would be displayed on the <tt>Player</tt>'s U.I.
     * 
     * @param xp
     *            is the total number of experience that the hypothetical <tt>Player</tt> has.
     * @return the precise level value that any given <tt>Player</tt> will have if the total number of experience points that they have is the amount specified. */
    public static double calcExactLevel(int xp) {
        // from the Minecraft Wiki (where x is the level and the result is the amount of total experience required to reach level x):
        // "for x≤16: 17x
        // for 15≤x≤31: 1.5x²-29.5x+360
        // for x≥30: 3.5x²-151.5x+2220"
        // for this method, I have rearranged the equations using the quadratic solution equation to find that (√ = square root) (±s changed to +s to ensure
        // positive results):
        // for level≤16 (xp≤272): xp/17
        // for 15≤level≤31 (255≤xp≤887): (29.5 + √(6*xp - 1,289.75))/3
        // for level≥30 (xp≥825): (151.5 + √(14*xp - 8,127.75))/7
        if (xp < 0)
            return -1;
        else if (xp >= 825)
            return (151.5 + Math.sqrt(14 * xp - 8127.75)) / 7.0;
        else if (xp <= 272)
            return xp / 17.0;
        else
            return (29.5 + Math.sqrt(6 * xp - 1289.75)) / 3.0;

    }

    /** This method prevents the specified <tt>Player</tt> from moving until the {@link #unfreezePlayer(Player) unFreezePlayer()} method is called for the same <tt>Player</tt>.
     * 
     * @param player
     *            is the <tt>Player</tt> that will be "frozen".
     * @see {@link #freezePlayer(String)} */
    public static void freezePlayer(Player player) {
        freezePlayer(player.getName());
    }

    /** This method prevents the specified <tt>Player</tt> from moving until the {@link #unfreezePlayer(String) unFreezePlayer()} method is called for the same <tt>Player</tt>.
     * 
     * @param player
     *            is the name of the <tt>Player</tt> that will be "frozen".
     * @see {@link #freezePlayer(Player)} */
    public static void freezePlayer(String player) {
        myCoreLibrary.frozen_players.add(player);
    }

    /** This method returns the data value that should correspond to the top of a proper door given the bottom <tt>Block</tt> of the door.
     * 
     * @param bottom_door_block
     *            is the <tt>Block</tt> that acts as the bottom of the door structure.
     * @return a <b>byte</b> representing the necessary data value for the top of the door (9 if the door is a double door, 8 otherwise). */
    public static byte getDoorTopData(Block bottom_door_block) {
        if (bottom_door_block.getType() != Material.WOODEN_DOOR && bottom_door_block.getType() != Material.IRON_DOOR_BLOCK)
            return -1;
        byte direction_data = bottom_door_block.getData();
        // if data>=4, the door was open, so just subtract 4 to check for the door's direction more easily
        if (direction_data >= 4)
            direction_data -= 4;
        // find the block face that needs to be checked for a door based on the direction of the current door; this will be used to determine whether or not the
        // current door will be the second door in a double door formation or simply a single door
        BlockFace face_to_check = BlockFace.NORTH;
        if (direction_data == 1)
            face_to_check = BlockFace.EAST;
        else if (direction_data == 2)
            face_to_check = BlockFace.SOUTH;
        else if (direction_data == 3)
            face_to_check = BlockFace.WEST;
        Block relevant_adjacent_door_block = bottom_door_block.getRelative(face_to_check);
        // if the block next to the bottom of the door is not a door, check the one next to the top of the door; the door may become a double door even if the
        // door adjacent to it is one block above it
        if (relevant_adjacent_door_block.getType() != Material.WOODEN_DOOR && relevant_adjacent_door_block.getType() != Material.IRON_DOOR_BLOCK)
            relevant_adjacent_door_block = relevant_adjacent_door_block.getRelative(BlockFace.UP);
        // if the relevant adjacent door block is not a door block at all or not the same type of door, the new door will not be a double door
        if (relevant_adjacent_door_block.getType() == Material.WOODEN_DOOR && bottom_door_block.getType() == Material.WOODEN_DOOR
                || relevant_adjacent_door_block.getType() == Material.IRON_DOOR_BLOCK && bottom_door_block.getType() == Material.IRON_DOOR_BLOCK) {
            // if the adjacent door block is already a double door, the current door will not be a double door.
            // if the adjacent door block is the top of a non-double door, there is still a chance the current door could be a double door; the door may become
            // a double door even if the door adjacent to it is one block below it
            if (relevant_adjacent_door_block.getData() == 8)
                relevant_adjacent_door_block = relevant_adjacent_door_block.getRelative(BlockFace.DOWN);
            // double-check to make sure that the relevant adjacent door block is still a door block; with WorldEdit or other world modifying tools, the top
            // half of a door might not have a bottom half
            if (relevant_adjacent_door_block.getType() == Material.WOODEN_DOOR && bottom_door_block.getType() == Material.WOODEN_DOOR
                    || relevant_adjacent_door_block.getType() == Material.IRON_DOOR_BLOCK && bottom_door_block.getType() == Material.IRON_DOOR_BLOCK)
                // finally, if the direction of both doors match, then it looks like we'll have a double door
                if (relevant_adjacent_door_block.getData() % 4 == bottom_door_block.getData() % 4)
                    // data=9 signifies a double door (as opposed to data=8, which is the top of a regular door)
                    return 9;
        }
        return 8;
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack item) {
        if (item.getType() != Material.ENCHANTED_BOOK)
            return item.getEnchantments();
        else
            return ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
    }

    /** This is a simple auto-complete method that can take the first few letters of a player's name and return the full name of the player. It prioritizes in two ways:
     * <b>1)</b> it gives online players priority over offline players and <b>2)</b> it gives shorter names priority over longer usernames because if a player tries to
     * designate a player and this plugin returns a different name than the user meant that starts with the same letters, the user can add more letters to get the longer
     * username instead. If these priorities were reversed, then there would be no way to specify a user whose username is the first part of another username, e.g. "Jeb" and
     * "Jebs_bro". This matching is <i>not</i> case-sensitive.
     * 
     * @param name
     *            is the String that represents the first few letters of a username that needs to be auto-completed.
     * @return the completed username that begins with <b><tt>name</b></tt> (<i>not</i> case-sensitive) */
    public static String getFullName(String name) {
        String full_name = null;
        for (Player possible_owner : myCoreLibrary.server.getOnlinePlayers())
            // if this player's name also matches and it shorter, return it instead becuase if someone is using an autocompleted command, we need to make sure
            // to get the shortest name because if they meant to use the longer username, they can remedy this by adding more letters to the parameter; however,
            // if they meant to do a shorter username and the auto-complete finds the longer one first, they're screwed
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        for (OfflinePlayer possible_owner : myCoreLibrary.server.getOfflinePlayers())
            if (possible_owner.getName().toLowerCase().startsWith(name.toLowerCase()) && (full_name == null || full_name.length() > possible_owner.getName().length()))
                full_name = possible_owner.getName();
        return full_name;
    }

    public static Player getPlayer(String name) {
        Player target = null;
        for (Player player : myCoreLibrary.server.getOnlinePlayers())
            if (player.getName().toLowerCase().startsWith(name.toLowerCase()) && (target == null || target.getName().length() > player.getName().length()))
                target = player;
        return target;
    }

    public static Block getOtherHalfOfLargeChest(Block first_half) {
        if (first_half.getType() != Material.CHEST && first_half.getType() != Material.TRAPPED_CHEST)
            return null;
        BlockFace[] relevant_block_faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST };
        for (BlockFace face : relevant_block_faces)
            if (first_half.getRelative(face).getType() == first_half.getType())
                return first_half.getRelative(face);
        return null;
    }

    public static Block getTargetBlock(Player player) {
        return getTargetBlock(player, BlockSearch.SOLID, BlockSearch.LIQUID);
    }

    /** This method uses the given <tt>Player</tt>'s location to calculate the block that they're pointing at. It works like the <tt>Player.getTargetBlock()</tt> method from
     * CraftBukkit, but it's better because <b>1)</b> it can search for specifed block types (e.g. solid blocks, liquid blocks, or switches like buttons and pressure plates),
     * <b>2)</b> it can see much further (max 500 blocks), and <b>3)</b> it fixes CraftBukkit bugs like <tt>Location.getBlock()</tt> "y-coordinate rollover" and
     * <tt>Player.getTargetBlock()</tt> returning air blocks when out of range.
     * 
     * @param player
     *            is the <tt>Player</tt> that will be analyzed by this method to find the target block.
     * @param parameters
     *            is an optional parameter that includes a list of <tt>BlockSearch</tt> enums (specified in the {@link #PlayerUtilities PlayerUtilities} class). If <b>
     *            <tt>parameters</b></tt> is specified as <b>null</b>, the first non-air block found will be returned; if no parameters are specified, <b>
     *            <tt>parameters</b></tt> will default to <tt>BlockSearch.<i>LIQUID</i>, BlockSearch.<i>SOLID</i></tt>; otherwise, it will return the first block that
     *            corresponds to at least <i>one</i> of the <tt>BlockSearch</tt> parameters, e.g. the first solid block if <tt>BlockSearch.<i>SOLID</tt></i> is one of the
     *            parameters specified.
     * @return the block that <tt><b>player</b></tt> is pointing at that is of at least one of the types specified by the given <b><tt>parameters</b></tt>. */
    public static Block getTargetBlock(Player player, BlockSearch... parameters) {
        // d is for distance from the player's eye location
        for (int d = 0; d < 5000; d++) {
            double yaw = player.getLocation().getYaw(), pitch = player.getLocation().getPitch();
            Location location =
                    new Location(player.getWorld(), player.getLocation().getX() + d / 10.0 * Math.cos(Math.toRadians(yaw + 90)) * Math.cos(Math.toRadians(-pitch)), player
                            .getEyeLocation().getY()
                            + d / 10.0 * Math.sin(Math.toRadians(-pitch)), player.getLocation().getZ() + d / 10.0 * Math.sin(Math.toRadians(yaw + 90))
                            * Math.cos(Math.toRadians(-pitch)));
            Block block = location.getBlock();
            // make sure the location isn't outside the bounds of the world
            if (block == null || Math.abs(location.getBlockX()) >= 2000000 || Math.abs(location.getBlockZ()) >= 2000000 || location.getY() < 0
                    || location.getY() > location.getWorld().getMaxHeight()) {
                mCL.debug("No good target found; search ended at " + writeLocation(location, true, true));
                return null;
            }
            // make sure the location fits with the block-searching parameters
            if (block.getType() != Material.AIR
                    && (parameters.length == 0 || parameters.length == 1 && parameters[0] == null || contains(parameters, BlockSearch.SOLID) && isSolid(block)
                            || contains(parameters, BlockSearch.NON_SOLID) && isNonSolid(block) || contains(parameters, BlockSearch.SWITCH) && isSwitch(block)
                            || contains(parameters, BlockSearch.LIQUID) && isLiquid(block) || (contains(parameters, BlockSearch.NON_SWITCH_NON_SOLID) && isNonSolid(block) && !isSwitch(block)))) {
                mCL.debug("found target block at " + writeLocation(block.getLocation(), true, true));
                return block;
            }
        }
        return null;
    }

    /** This method checks to see if the given block is not the height of a full block. This can be true if <b>1)</b> the block is a half slab in the lower position, <b>2)</b>
     * the block is less than one full block tall, or <b>3)</b> the block directly below the given block is more than one full block tall such as a fence or fence gate.
     * 
     * @param block
     *            is the Block that is being checked for partial height.
     * @return <b>true</b> if <b><tt>block</b></tt> is */
    public static boolean topsAtPartialBlockHeight(Block block) {
        // if the block is a half slab, but it's in the higher position (data > 8), then it's not at half height, so return false
        if (block.getTypeId() == 44 && block.getData() >= 8)
            return false;
        for (int solid_partial_height_block_ID : Wiki.SOLID_PARTIAL_HEIGHT_BLOCK_IDS)
            if (solid_partial_height_block_ID == block.getTypeId())
                return true;
        Block lower_block = block.getRelative(BlockFace.DOWN);
        if (lower_block == null)
            return false;
        for (int fence_height_block_ID : Wiki.FENCE_HEIGHT_BLOCK_IDS)
            if (fence_height_block_ID == lower_block.getTypeId())
                return true;
        return false;
    }

    public static void unfreezePlayer(Player player) {
        unfreezePlayer(player.getName());
    }

    public static boolean unfreezePlayer(String player) {
        return myCoreLibrary.frozen_players.remove(player);
    }

}