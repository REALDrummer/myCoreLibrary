package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import static REALDrummer.ArrayUtilities.*;
import static REALDrummer.MessageUtilities.*;
import static REALDrummer.StringUtilities.*;

public class WikiUtilities {
    /** This method will tell whether or not a certain block will break if water or lava flows to it. No data value is required as input for this method because items with the
     * same I.D. consistently have this property in common.
     * 
     * @param id
     *            is the I.D. of the block that needs to be checked for the "can be broken by liquids" property.
     * @return <b>true</b> if the block given by the I.D. will break if water or lava flows to it, <b>false</b> if the block will hold back water or lava, or <b>null</b> if
     *         the I.D. given does not apply to a block at all.
     * @see {@link #canBeBrokenByLiquids(String) canBeBrokenByLiquids(String)} and {@link #canBeBrokenByLiquids(Block) canBeBrokenByLiquids(Block)} */
    public static Boolean canBeBrokenByLiquids(int id) {
        // return null if the I.D. doesn't belong to anything or is an item I.D. instead of a block I.D.
        if (getItemName(id, -1, false, false, true) == null || id >= 256)
            return null;
        for (int can_be_broken_by_liquids_ID : Wiki.CAN_BE_BROKEN_BY_LIQUIDS_IDS)
            if (id == can_be_broken_by_liquids_ID)
                return true;
        return false;
    }

    /** This method will tell whether or not a certain block will break if water or lava flows to it.
     * 
     * @param block
     *            is the block that needs to be checked for the "can be broken by liquids" property.
     * @return <b>true</b> if the block given will break if water or lava flows to it or <b>false</b> if the block will hold back water or lava.
     * @see {@link #canBeBrokenByLiquids(String) canBeBrokenByLiquids(String)} and {@link #canBeBrokenByLiquids(Block) canBeBrokenByLiquids(Block)} */
    public static Boolean canBeBrokenByLiquids(Block block) {
        return canBeBrokenByLiquids(block.getTypeId());
    }

    /** This method will tell whether or not a certain block will break if water or lava flows to it.
     * 
     * @param item_name
     *            is the name of the block that needs to be checked for the "can be broken by liquids" property.
     * @return <b>true</b> if the block given by the name will break if water or lava flows to it or <b>false</b> if the block will hold back water or lava.
     * @see {@link #canBeBrokenByLiquids(int) canBeBrokenByLiquids(int)} and {@link #canBeBrokenByLiquids(Block) canBeBrokenByLiquids(Block)} */
    public static Boolean canBeBrokenByLiquids(String item_name) {
        return canBeBrokenByLiquids(getItemIdAndData(item_name, false)[0]);
    }

    public static Enchantment getEnchantment(String[] name) {
        Integer id = getEnchantmentId(name);
        if (id == null)
            return null;
        return Enchantment.getById(id);
    }

    public static Enchantment getEnchantment(String name) {
        return getEnchantment(name.split(" "));
    }

    public static Enchantment getEnchantment(int id) {
        return Enchantment.getById(id);
    }

    public static String getEnchantmentFullName(Enchantment enchantment, int level) {
        String name = getEnchantmentName(enchantment);
        if (level > 1 || (level == 1 && enchantment.getMaxLevel() > 1))
            name += " " + writeRomanNumeral(level);
        return name;
    }

    public static Integer getEnchantmentId(String[] enchantment_name) {
        Integer result_id = null, result_i = null;

        for (int id = 0; id < Wiki.ENCHANTMENT_IDS.length; id++)
            if (Wiki.ENCHANTMENT_IDS[id] != null)
                for (int i = 0; i < Wiki.ENCHANTMENT_IDS[id].length; i++) {
                    boolean contains_query = true;
                    for (String word : enchantment_name)
                        // if word starts and ends with parentheses, it's a data suffix, so ignore it in the search; also ignore articles
                        if (!(word.startsWith("(") && word.endsWith(")")) && !word.equalsIgnoreCase("a") && !word.equalsIgnoreCase("an") && !word.equalsIgnoreCase("the")
                                && !word.equalsIgnoreCase("some") && !Wiki.ENCHANTMENT_IDS[id][i].toLowerCase().contains(word.toLowerCase())) {
                            contains_query = false;
                            break;
                        }
                    /* translation of this if statement: if the entity contains the query and either we haven't found another result yet, the old result has a longer name than
                     * this new one, or the length of the names is the same but this new result is an entity I.D. while the old one is a block I.D., then change the current
                     * result to this new entity */
                    if (contains_query
                            && (result_id == null || Wiki.ENCHANTMENT_IDS[result_id][result_i].length() > Wiki.ENCHANTMENT_IDS[id][i].length() || (Wiki.ENCHANTMENT_IDS[result_id][result_i]
                                    .length() == Wiki.ENCHANTMENT_IDS[id][i].length()
                                    && result_id < 256 && id >= 256))) {
                        result_id = id;
                        result_i = i;
                    }
                }

        // if we returned no results, it's possible that the object was "something with the I.D. [id](":"[data])"
        if (result_id == null)
            if (enchantment_name.length > 3 && enchantment_name[0].equalsIgnoreCase("something") && enchantment_name[1].equalsIgnoreCase("with")
                    && enchantment_name[2].equalsIgnoreCase("the") && enchantment_name[3].equalsIgnoreCase("I.D."))
                try {
                    // try reading it as "something with the I.D. [id]"
                    result_id = Integer.parseInt(enchantment_name[4]);
                    return result_id;
                } catch (NumberFormatException e) {
                    try {
                        // try reading it as "something with the I.D. [id]:[data]"
                        String[] id_and_data = enchantment_name[4].split(":");
                        if (id_and_data.length != 2) {
                            tellOps(ChatColor.DARK_RED + "Aww! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE
                                    + combine(enchantment_name, " ") + ChatColor.DARK_RED + "\"", true);
                            return null;
                        }
                        result_id = Integer.parseInt(id_and_data[0]);
                        return result_id;
                    } catch (NumberFormatException e2) {
                        err(myCoreLibrary.mCL, "Darn! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE
                                + combine(enchantment_name, " ") + ChatColor.DARK_RED + "\"", e2);
                        return null;
                    }
                }
            else
                return null;
        else {
            // now we need to adjust the final result based on the gaps in I.D.s for the I.D. gaps
            for (short[] gap : Wiki.ENCHANTMENT_GAPS)
                if (result_id > gap[0])
                    result_id += (gap[1] - gap[0] - 1);
                else
                    break;
        }
        return result_id;
    }

    public static Integer getEnchantmentId(Enchantment enchantment) {
        return enchantment.getId();
    }

    public static Integer getEnchantmentId(String name) {
        return getEnchantmentId(name.split(" "));
    }

    public static String getEnchantmentName(int id) {
        // return null if the I.D. is inside a gap
        for (short[] gap : Wiki.ENCHANTMENT_GAPS)
            if (id > gap[0] && id < gap[1])
                return null;
        // account for the gaps in the Enchantment I.D.s
        for (int i = Wiki.ENCHANTMENT_GAPS.length - 1; i >= 0; i--)
            if (id >= Wiki.ENCHANTMENT_GAPS[i][1])
                id -= (Wiki.ENCHANTMENT_GAPS[i][1] - Wiki.ENCHANTMENT_GAPS[i][0] - 1);
        String enchantment = null;
        // the Exceptions in this block of code can be ArrayIndexOutOfBoundsExceptions or NullPointerExceptions
        try {
            // try using the data and I.D. given
            enchantment = Wiki.ENCHANTMENT_IDS[id][0];
        } catch (ArrayIndexOutOfBoundsException exception) {
            return null;
        } catch (NullPointerException exception) {
            return null;
        }
        if (enchantment == null)
            return null;
        return enchantment;
    }

    public static String getEnchantmentName(Enchantment enchantment) {
        return getEnchantmentName(enchantment.getId());
    }

    /** This method returns a two-item Integer array or <b>null</b>. <tt>[0]</tt> is the I.D. of the entity given by <tt>entity_name</tt>. <tt>[1]</tt> is the data value of the
     * item given, e.g. 1 for charged creepers (because all creepers have the I.D. 50, but a data value of 1 refers to charged creepers specifically). If
     * <tt><b>entity_name</b></tt> specifies a general item name such as "creepers", the data value returned will be -1.
     * 
     * @param entity_name
     *            is the name of the entity that was specified split into separate words.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>entity_name</tt></b> in a two-item Integer array or <tt><b>null</b></tt> if the item
     *         specified does not exist.
     * @see {@link #getEntityIdAndData(String) getEntityIdAndData(String)}, {@link #getEntityIdAndDataString(String[]) getEntityIdAndDataString(String[])}, and
     *      {@link #getEntityIdAndDataString(String) getEntityIdAndDataString(String)}
     * @NOTE This method returns both the I.D. and the data value of an entity based on the entity's name because it encourages the programmer to only use this method once as
     *       necessary, not once to get the I.D. and again to get the data. It is a long, somewhat complex method and it must search through hundreds of Strings in the
     *       <tt>Wiki.ENTITY_IDS</tt> array to find a match. This method should only be called when necessary and results returned by this method should be stored in a
     *       variable if needed more than once; do not simply call this method a second time. */
    public static Integer[] getEntityIdAndData(String[] entity_name) {
        Integer result_id = null, result_data = null, result_i = null;
        for (int id = 0; id < Wiki.ENTITY_IDS.length; id++)
            if (Wiki.ENTITY_IDS[id] != null)
                for (int data = 0; data < Wiki.ENTITY_IDS[id].length; data++)
                    if (Wiki.ENTITY_IDS[id][data] != null)
                        for (int i = 0; i < Wiki.ENTITY_IDS[id][data].length; i++) {
                            boolean contains_query = true;
                            for (String word : entity_name)
                                // if word starts and ends with parentheses, it's a data suffix, so ignore it in the search; also ignore articles
                                if (!(word.startsWith("(") && word.endsWith(")")) && !word.equalsIgnoreCase("a") && !word.equalsIgnoreCase("an")
                                        && !word.equalsIgnoreCase("the") && !word.equalsIgnoreCase("some")
                                        && !Wiki.ENTITY_IDS[id][data][i].toLowerCase().contains(word.toLowerCase())) {
                                    contains_query = false;
                                    break;
                                }
                            // translation of this if statement: if the entity contains the query and either we haven't found another result yet, the old result
                            // has a longer name than this new one, or the length of the names is the same but this new result is an entity I.D. while the old
                            // one is a block I.D., then change the current result to this new entity
                            if (contains_query
                                    && (result_id == null || Wiki.ENTITY_IDS[result_id][result_data][result_i].length() > Wiki.ENTITY_IDS[id][data][i].length() || (Wiki.ENTITY_IDS[result_id][result_data][result_i]
                                            .length() == Wiki.ENTITY_IDS[id][data][i].length()
                                            && result_id < 256 && id >= 256))) {
                                result_id = id;
                                result_data = data;
                                result_i = i;
                            }
                        }
        if (result_id == null || result_data == null)
            if (entity_name[0].equalsIgnoreCase("something") && entity_name[1].equalsIgnoreCase("with") && entity_name[2].equalsIgnoreCase("the")
                    && entity_name[3].equalsIgnoreCase("I.D."))
                try {
                    // try reading it as "something with the I.D. [id]"
                    result_id = Integer.parseInt(entity_name[4]);
                    result_data = 0;
                    return new Integer[] { result_id, result_data };
                } catch (NumberFormatException e) {
                    try {
                        // try reading it as "something with the I.D. [id]:[data]"
                        String[] id_and_data = entity_name[4].split(":");
                        if (id_and_data.length != 2) {
                            tellOps(ChatColor.DARK_RED + "Aww! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE + "\""
                                    + combine(entity_name, " ") + ChatColor.DARK_RED + "\".", true);
                            return null;
                        }
                        result_id = Integer.parseInt(id_and_data[0]);
                        result_data = Integer.parseInt(id_and_data[1]);
                        return new Integer[] { result_id, result_data };
                    } catch (NumberFormatException e2) {
                        err(myCoreLibrary.mCL, "Darn! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE + "\""
                                + combine(entity_name, " ") + ChatColor.DARK_RED + "\"", e2);
                        return null;
                    }
                }
            else
                return null;
        else {
            // subtract 1 from the data to get the real data (remember: [0] is the general name and [1] is data = 0)
            result_data -= 1;
            // if the entity name contained a data suffix, read the data suffix to get the real data value
            if (entity_name.length > 1 && entity_name[entity_name.length - 1].startsWith("(") && entity_name[entity_name.length - 1].endsWith(")"))
                try {
                    result_data = Integer.parseInt(entity_name[entity_name.length - 1].substring(1, entity_name[entity_name.length - 1].length() - 1));
                } catch (NumberFormatException e) {
                    err(myCoreLibrary.mCL, "Oh, nos! I got an error trying to read the data suffix on this item name: \"" + ChatColor.WHITE + combine(entity_name, " ")
                            + ChatColor.DARK_RED + "\".\nI read \"" + ChatColor.WHITE
                            + entity_name[entity_name.length - 1].substring(1, entity_name[entity_name.length - 1].length() - 1) + ChatColor.DARK_RED
                            + "\" as the data value in the data suffix.", e);
                }
            // now we need to adjust the final result based on the gaps in I.D.s
            for (short[] gap : Wiki.ENTITY_GAPS)
                if (result_id > gap[0])
                    result_id += (gap[1] - gap[0] - 1);
                else
                    break;
        }
        return new Integer[] { result_id, result_data };
    }

    /** This method returns a two-item Integer array or <b>null</b>. <tt>[0]</tt> is the I.D. of the entity given by <tt>entity_name</tt>. <tt>[1]</tt> is the data value of the
     * item given, e.g. 1 for charged creepers (because all creepers have the I.D. 50, but a data value of 1 refers to charged creepers specifically). If
     * <tt><b>entity_name</b></tt> specifies a general item name such as "creepers", the data value returned will be -1.
     * 
     * @param entity_name
     *            is the name of the entity that was specified.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>entity_name</tt></b> in a two-item Integer array or <tt><b>null</b></tt> if the item
     *         specified does not exist.
     * @see {@link #getEntityIdAndData(String[]) getEntityIdAndData(String[])}, {@link #getEntityIdAndDataString(String[]) getEntityIdAndDataString(String[])}, and
     *      {@link #getEntityIdAndDataString(String) getEntityIdAndDataString(String)}
     * @NOTE This method returns both the I.D. and the data value of an entity based on the entity's name because it encourages the programmer to only use this method once as
     *       necessary, not once to get the I.D. and again to get the data. It is a long, somewhat complex method and it must search through hundreds of Strings in the
     *       <tt>ENTITY_IDS</tt> array to find a match. This method should only be called when necessary and results returned by this method should be stored in a variable if
     *       needed more than once; do not simply call this method a second time. */
    public static Integer[] getEntityIdAndData(String entity_name) {
        return getEntityIdAndData(entity_name.replaceAll("_", " ").split(" "));
    }

    /** This method returns a String describing the id and data of the entity specified or <b>null</b>. The String is equivalent to <tt>String.valueOf(</tt>the I.D. of the item
     * or block specified<tt>)</tt> if data < 1, but if data > 0, the String is formatted as "[id]:[data]". If <tt><b>entity_name</b></tt> specifies a general item name such
     * as "thrown potions", the data value returned will be -1; therefore, no data will be included in the String returned.
     * 
     * @param entity_name
     *            is the name of the item or block type that was specified.
     * @return the I.D. and numerical data value for the entity given by name in <tt><b>entity_name</tt></b> in a String formatted as "[id]" if data < 1 or "[id]:[data]"
     *         otherwise or <tt><b>null</b></tt> if the entity specified does not exist.
     * @see {@link #getEntityIdAndData(String[], Boolean) getEntityIdAndData(String[], Boolean)}, {@link #getEntityIdAndData(String, Boolean) getEntityIdAndData(String,
     *      Boolean)}, and {@link #getEntityIdAndDataString(String, Boolean) getEntityIdAndDataString(String, Boolean)} */
    public static String getEntityIdAndDataString(String[] entity_name) {
        Integer[] id_and_data = getEntityIdAndData(entity_name);
        if (id_and_data == null)
            return null;
        String result = String.valueOf(id_and_data[0]);
        if (id_and_data[1] > 0)
            result += ":" + id_and_data[1];
        return result;
    }

    /** This method returns a String describing the id and data of the entity specified or <b>null</b>. The String is equivalent to <tt>String.valueOf(</tt>the I.D. of the item
     * or block specified<tt>)</tt> if data < 1, but if data > 0, the String is formatted as "[id]:[data]". If <tt><b>entity_name</b></tt> specifies a general item name such
     * as "thrown potions", the data value returned will be -1; therefore, no data will be included in the String returned.
     * 
     * @param entity_name
     *            is the name of the item or block type that was specified.
     * @return the I.D. and numerical data value for the entity given by name in <tt><b>entity_name</tt></b> in a String formatted as "[id]" if data < 1 or "[id]:[data]"
     *         otherwise or <tt><b>null</b></tt> if the entity specified does not exist.
     * @see {@link #getEntityIdAndData(String[], Boolean) getEntityIdAndData(String[], Boolean)}, {@link #getEntityIdAndData(String, Boolean) getEntityIdAndData(String,
     *      Boolean)}, and {@link #getEntityIdAndDataString(String[], Boolean) getEntityIdAndDataString(String[], Boolean)} */
    public static String getEntityIdAndDataString(String entity_name) {
        return getEntityIdAndDataString(entity_name.replaceAll("_", " ").split(" "));
    }

    /** This method returns the name of the entity specified by the I.D. and data given.
     * 
     * @param id
     *            is the entity type I.D.
     * @param data
     *            is the numerical data value for the entity. Data is only used for creepers states (charged vs. non-charged), villagers (professions), and sheep (colors).
     *            Giving a data value of -1 will result in the general name for such an entity, e.g. "sheep" rather than "white sheep" or "orange sheep".
     * @param give_data_suffix
     *            specifies whether or not the name of the entity should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/eid</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the entity name returned should be returned in the singular form (e.g. "a creeper") or in the plural form (e.g. "creepers"). Singular forms
     *            include an article at the beginning.
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the entity specified by the I.D. and data given.
     * @see {@link #getEntityName(Entity, boolean, boolean) getEntityName(Entity, boolean, boolean)} */
    public static String getEntityName(int id, int data, boolean give_data_suffix, boolean singular, boolean without_article) {
        // return null if the I.D. is inside a gap
        for (short[] gap : Wiki.ENTITY_GAPS)
            if (id > gap[0] && id < gap[1])
                return null;
        // for the entity gaps
        for (int i = Wiki.ENTITY_GAPS.length - 1; i >= 0; i--)
            if (id >= Wiki.ENTITY_GAPS[i][1])
                id -= (Wiki.ENTITY_GAPS[i][1] - Wiki.ENTITY_GAPS[i][0] - 1);
        int sing_plur = 0;
        if (singular)
            sing_plur = 1;
        String entity = null;
        // the Exceptions in this block of code can be ArrayIndexOutOfBoundsExceptions or NullPointerExceptions
        try {
            // try using the data and I.D. given
            entity = Wiki.ENTITY_IDS[id][data + 1][sing_plur];
        } catch (ArrayIndexOutOfBoundsException exception) {
            try {
                // if that doesn't work, try substracting 4 from the data until we can't any more and try again
                entity = Wiki.ENTITY_IDS[id][data % 4 + 1][sing_plur];
                if (entity != null && give_data_suffix && data > 0)
                    entity += " (" + data + ")";
            } catch (Exception exception2) {
                try {
                    // if that doesn't work, try giving the general name for the entity with the I.D.
                    entity = Wiki.ENTITY_IDS[id][0][sing_plur];
                    if (entity != null && give_data_suffix && data > 0)
                        entity += " (" + data + ")";
                } catch (Exception exception3) {
                    //
                }
            }
        } catch (NullPointerException exception) {
            //
        }
        if (entity == null)
            return null;
        // if the item is singular and we want no article, remove the preexisting article
        if (singular && without_article)
            entity = entity.substring(entity.split(" ")[0].length() + 1);
        // if the item is plural and we want an article, just add a "some" to the beginning
        else if (!singular && !without_article)
            entity = "some " + entity;
        return entity;
    }

    /** This method returns the name of the entity specified.
     * 
     * @param entity
     *            is the Entity that will be named.
     * @param give_data_suffix
     *            specifies whether or not the name of the entity should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/eid</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the entity name returned should be returned in the singular form (e.g. "a creeper") or in the plural form (e.g. "creepers"). Singular forms
     *            include an article at the beginning.
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the entity specified.
     * @see {@link #getEntityName(int, int, boolean, boolean) getEntityName(int, int, boolean, boolean)} */
    public static String getEntityName(Entity entity, boolean give_data_suffix, boolean singular, boolean without_article) {
        int data = -1;
        if (entity.getType() == EntityType.VILLAGER)
            data = ((Villager) entity).getProfession().getId();
        else if (entity.getType() == EntityType.CREEPER)
            if (!((Creeper) entity).isPowered())
                data = 0;
            else
                data = 1;
        else if (entity.getType() == EntityType.SHEEP)
            // the data for the sheep is organized in the same way as the wool data; dye data goes in the opposite direction
            data = ((Sheep) entity).getColor().getWoolData();
        else if (entity.getType() == EntityType.PAINTING)
            data = ((Painting) entity).getAttachedFace().ordinal();
        else if (entity.getType() == EntityType.ITEM_FRAME)
            data = ((ItemFrame) entity).getAttachedFace().ordinal();
        return getEntityName(entity.getType().getTypeId(), data, give_data_suffix, singular, without_article);
    }

    /** This method returns the name of the entity type specified.
     * 
     * @param type
     *            is the EntityType that will be named. EntityTypes cannot contain special data such as sheep color or villager profession, so if possible, consider using
     *            {@link #getEntityName(Entity, boolean, boolean, boolean) getEntityName(Entity, boolean, boolean, boolean)} instead.
     * @param give_data_suffix
     *            specifies whether or not the name of the entity should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/eid</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the entity name returned should be returned in the singular form (e.g. "a creeper") or in the plural form (e.g. "creepers"). Singular forms
     *            include an article at the beginning.
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the entity type specified.
     * @see {@link #getEntityName(int, int, boolean, boolean) getEntityName(int, int, boolean, boolean)} */
    public static String getEntityName(EntityType type, boolean give_data_suffix, boolean singular, boolean without_article) {
        return getEntityName(type.getTypeId(), -1, give_data_suffix, singular, without_article);
    }

    public static String getEntityName(String incomplete_name, boolean give_data_suffix, boolean singular, boolean without_article) {
        Integer[] id_and_data = getEntityIdAndData(incomplete_name);
        if (id_and_data == null)
            return null;
        return getEntityName(id_and_data[0], id_and_data[1], give_data_suffix, singular, without_article);
    }

    /** This method returns a two-item Integer array or <b>null</b>. <tt>[0]</tt> is the I.D. of the item given by <tt>item_name</tt>. <tt>[1]</tt> is the data value of the
     * item given, e.g. 2 for birch wood (because all logs have the I.D. 17, but a data value of 2 refers to birch wood specifically). If <tt><b>item_name</tt></b> specifies a
     * general item name such as "logs", the data value returned will be -1.
     * 
     * @param item_name
     *            is the name of the item or block type that was specified split into separate words.
     * @param item_ID
     *            is <b>true</b> if this method should only return item I.D.s and not block type I.D.s, <b>false</b> if this method should only return block type I.D.s and not
     *            item I.D.s, or <b>null</b> if it should return either item I.D.s or block type I.D.s, in which case it will proritize item I.D.s over block type I.D.s.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>item_name</tt></b> in a two-item Integer array or <tt><b>null</b></tt> if <b>1)</b> the
     *         item specified does not exist or <b>2)</b> the object specified is an item, not a block type, and it was specified in <tt><b>item_ID</b></tt> that this method
     *         should only return block types or vice versa.
     * @NOTE This method returns both the I.D. and the data value of an item based on the item's name because it encourages the programmer to only use this method once as
     *       necessary, not once to get the I.D. and again to get the data. It is a long, somewhat complex method and it must search through hundreds and hundreds of Strings
     *       in the <tt>Wiki.ITEM_IDS</tt> array to find a match. This method should only be called when necessary and results returned by this method should be stored in a
     *       variable if needed more than once; do not simply call this method a second time.
     * @see {@link #getItemIdAndData(String, Boolean) getItemIdAndData(String, Boolean)}, {@link #getItemIdAndDataString(String[], Boolean) getItemIdAndDataString(String[],
     *      Boolean)}, and {@link #getItemIdAndDataString(String, Boolean) getItemIdAndDataString(String, Boolean)} */
    public static Integer[] getItemIdAndData(String[] item_name, Boolean item_ID) {
        Integer result_id = null, result_data = null, result_i = null;
        int start_id = 0;
        // if item_ID is true, we only want item I.D.s, so start searching at item I.D.s
        if (item_ID != null && item_ID)
            start_id = Wiki.ITEM_GAPS[0][0] + 1;
        for (int check_id = start_id; check_id < Wiki.ITEM_IDS.length; check_id++) {
            if (Wiki.ITEM_IDS[check_id] != null)
                for (int check_data = 0; check_data < Wiki.ITEM_IDS[check_id].length; check_data++)
                    if (Wiki.ITEM_IDS[check_id][check_data] != null)
                        for (int i = 0; i < Wiki.ITEM_IDS[check_id][check_data].length; i++) {
                            boolean contains_query = true;
                            String[] nothing_words = { "a", "an", "the", "some", "of", "o'", "with", "for", "in", "on" };
                            for (String word : item_name)
                                /* if word starts and ends with parentheses, it's a data suffix, so ignore it in the search; also ignore nothing words like prepositions and
                                 * articles */
                                if (!(word.startsWith("(") && word.endsWith(")")) && !contains(nothing_words, word.toLowerCase())
                                        && !Wiki.ITEM_IDS[check_id][check_data][i].toLowerCase().contains(word.toLowerCase())) {
                                    contains_query = false;
                                    break;
                                }
                            // translation of this if statement: if the item contains the query and either we haven't found another result yet, the old result
                            // has a longer name than this new one, or the length of the names is the same but this new result is an item I.D. while the old one
                            // is a block I.D. and item_ID is null, then change the current result to this new item
                            if (contains_query
                                    && (result_id == null || Wiki.ITEM_IDS[result_id][result_data][result_i].length() > Wiki.ITEM_IDS[check_id][check_data][i].length() || (Wiki.ITEM_IDS[result_id][result_data][result_i]
                                            .equals(Wiki.ITEM_IDS[check_id][check_data][i])
                                            && item_ID == null && result_id < check_id))) {
                                result_id = check_id;
                                result_data = check_data;
                                result_i = i;
                            }
                        }
            // if Wiki.ITEM_IDS is false, we don't want item I.D.s, so stop checking after we have checked the first part, the block I.D.s
            if (check_id > Wiki.ITEM_GAPS[0][0] && item_ID != null && !item_ID)
                break;
        }
        // if we returned no results, it's possible that the object was "something with the I.D. [id](":"[data])"
        if (result_id == null || result_data == null)
            if (item_name[0].equalsIgnoreCase("something") && item_name[1].equalsIgnoreCase("with") && item_name[2].equalsIgnoreCase("the")
                    && item_name[3].equalsIgnoreCase("I.D."))
                try {
                    // try reading it as "something with the I.D. [id]"
                    result_id = Integer.parseInt(item_name[4]);
                    result_data = -1;
                    return new Integer[] { result_id, result_data };
                } catch (NumberFormatException exception) {
                    try {
                        // try reading it as "something with the I.D. [id]:[data]"
                        String[] id_and_data = item_name[4].split(":");
                        if (id_and_data.length != 2) {
                            tellOps(ChatColor.DARK_RED + "Aww! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE
                                    + combine(item_name) + ChatColor.DARK_RED + "\".", true);
                            return null;
                        }
                        result_id = Integer.parseInt(id_and_data[0]);
                        result_data = Integer.parseInt(id_and_data[1]);
                        return new Integer[] { result_id, result_data };
                    } catch (NumberFormatException exception2) {
                        tellOps(ChatColor.DARK_RED + "Darn! Something went wrong! I couldn't get the I.D. and data from this object name: \"" + ChatColor.WHITE
                                + combine(item_name) + ChatColor.DARK_RED + "\".", true);
                        return null;
                    }
                }
            else
                return null;
        else {
            // subtract 1 from the data to get the real data (remember: [0] is the general name and [1] is data = 0)
            result_data -= 1;
            // now we need to adjust the final result based on the gaps in I.D.s
            // for the I.D. gaps
            for (short[] gap : Wiki.ITEM_GAPS)
                if (result_id > gap[0])
                    result_id += (gap[1] - gap[0] - 1);
                else
                    break;
            // if the item name contained a data suffix, read the data suffix to get the real data value
            if (item_name.length > 1 && item_name[item_name.length - 1].startsWith("(") && item_name[item_name.length - 1].endsWith(")"))
                try {
                    result_data = Integer.parseInt(item_name[item_name.length - 1].substring(1, item_name[item_name.length - 1].length() - 1));
                } catch (NumberFormatException e) {
                    err(myCoreLibrary.mCL, "Oh, nos! I got an error trying to read the data suffix on this item name!: \"" + ChatColor.WHITE + combine(item_name, " ")
                            + ChatColor.DARK_RED + "\".\nI read " + ChatColor.WHITE + "\""
                            + item_name[item_name.length - 1].substring(1, item_name[item_name.length - 1].length() - 1) + "\"" + ChatColor.DARK_RED
                            + " as the data value in the data suffix.", e);
                }
            // only adjust the result data if there was no data suffix to get the true data from
            else {
                // for the potion data values gaps
                if (result_id == 373)
                    for (short[] gap : Wiki.POTION_DATA_GAPS)
                        if (result_data > gap[0])
                            result_data += (gap[1] - gap[0] - 1);
                        else
                            break;
                // for the spawn egg data values gaps
                else if (result_id == 383)
                    for (short[] gap : Wiki.SPAWN_EGG_DATA_GAPS)
                        if (result_data > gap[0])
                            result_data += (gap[1] - gap[0] - 1);
                        else
                            break;
            }
        }
        return new Integer[] { result_id, result_data };
    }

    /** This method returns a two-item Integer array or <b>null</b>. <tt>[0]</tt> is the I.D. of the item given by <tt>item_name</tt>. <tt>[1]</tt> is the data value of the
     * item given, e.g. 2 for birch wood (because all logs have the I.D. 17, but a data value of 2 refers to birch wood specifically). If <tt><b>item_name</b></tt> specifies a
     * general item name such as "logs", the data value returned will be -1.
     * 
     * @param item_name
     *            is the name of the item or block type that was specified split into separate words.
     * @param item_ID
     *            is <b>true</b> if this method should only return item I.D.s and not block type I.D.s, <b>false</b> if this method should only return block type I.D.s and not
     *            item I.D.s, or <b>null</b> if it should return either item I.D.s or block type I.D.s, in which case it will proritize item I.D.s over block type I.D.s.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>item_name</tt></b> in a two-item Integer array or <tt><b>null</b></tt> if <b>1)</b> the
     *         item specified does not exist or <b>2)</b> the object specified is an item, not a block type, and it was specified in <tt><b>item_ID</b></tt> that this method
     *         should only return block types or vice versa.
     * @NOTE This method returns both the I.D. and the data value of an item based on the item's name because it encourages the programmer to only use this method once as
     *       necessary, not once to get the I.D. and again to get the data. It is a long, somewhat complex method and it must search through hundreds and hundreds of Strings
     *       in the <tt>ITEM_IDS</tt> array to find a match. This method should only be called when necessary and results returned by this method should be stored in a
     *       variable if needed more than once; do not simply call this method a second time.
     * @see {@link #getItemIdAndData(String[], Boolean) getItemIdAndData(String[], Boolean)}, {@link #getItemIdAndDataString(String[], Boolean)
     *      getItemIdAndDataString(String[], Boolean)}, and {@link #getItemIdAndDataString(String, Boolean) getItemIdAndDataString(String, Boolean)} */
    public static Integer[] getItemIdAndData(String item_name, Boolean item_ID) {
        return getItemIdAndData(item_name.replaceAll("_", " ").split(" "), item_ID);
    }

    /** This method returns a String describing the id and data of the item specified or <b>null</b>.
     * 
     * @param item_name
     *            is the name of the item or block type that was specified split into separate words.
     * @param item_ID
     *            is <b>true</b> if this method should only return item I.D.s and not block type I.D.s, <b>false</b> if this method should only return block type I.D.s and not
     *            item I.D.s, or <b>null</b> if it should return either item I.D.s or block type I.D.s, in which case it will proritize item I.D.s over block type I.D.s.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>item_name</tt></b> in a String formatted as "[id]" if data < 1 or "[id]:[data]" otherwise
     *         or <tt><b>null</b></tt> if <b>1)</b> the item specified does not exist or <b>2)</b> the object specified is an item, not a block type, and it was specified in
     *         <tt><b>item_ID</b></tt> that this method should only return block types or vice versa.
     * @see {@link #getItemIdAndData(String[], Boolean) getItemIdAndData(String[], Boolean)}, {@link #getItemIdAndData(String, Boolean) getItemIdAndData(String, Boolean)}, and
     *      {@link #getItemIdAndDataString(String, Boolean) getItemIdAndDataString(String, Boolean)} */
    public static String getItemIdAndDataString(String[] item_name, Boolean item_ID) {
        Integer[] id_and_data = getItemIdAndData(item_name, item_ID);
        if (id_and_data == null)
            return null;
        String result = String.valueOf(id_and_data[0]);
        if (id_and_data[1] > 0)
            result += ":" + id_and_data[1];
        return result;
    }

    /** This method returns a String describing the id and data of the item specified or <b>null</b>. The String is equivalent to <tt>String.valueOf(</tt>the I.D. of the item
     * or block specified<tt>)</tt> if data < 1, but if data > 0, the String is formatted as "[id]:[data]". If <tt><b>item_name</b></tt> specifies a general item name such as
     * "logs", the data value returned will be -1; therefore, no data will be included in the String returned.
     * 
     * @param item_name
     *            is the name of the item or block type that was specified split into separate words.
     * @param item_ID
     *            is <b>true</b> if this method should only return item I.D.s and not block type I.D.s, <b>false</b> if this method should only return block type I.D.s and not
     *            item I.D.s, or <b>null</b> if it should return either item I.D.s or block type I.D.s, in which case it will proritize item I.D.s over block type I.D.s.
     * @return the I.D. and numerical data value for the item given by name in <tt><b>item_name</tt></b> in a String formatted as "[id]" if data < 1 or "[id]:[data]" otherwise
     *         or <tt><b>null</b></tt> if <b>1)</b> the item specified does not exist or <b>2)</b> the object specified is an item, not a block type, and it was specified in
     *         <tt><b>item_ID</b></tt> that this method should only return block types or vice versa.
     * @see {@link #getItemIdAndData(String[], Boolean) getItemIdAndData(String[], Boolean)}, {@link #getItemIdAndData(String, Boolean) getItemIdAndData(String, Boolean)}, and
     *      {@link #getItemIdAndDataString(String[], Boolean) getItemIdAndDataString(String[], Boolean)} */
    public static String getItemIdAndDataString(String item_name, Boolean item_ID) {
        return getItemIdAndDataString(item_name.replaceAll("_", " ").split(" "), item_ID);
    }

    /** This method returns the name of the item specified by the item or block type I.D. and data given.
     * 
     * @param id
     *            is the item or block type I.D.
     * @param data
     *            is the numerical data value for the item or block.
     * @param give_data_suffix
     *            specifies whether or not the name of the item should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/id</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the item name returned should be returned in the singular form (e.g. "a lever") or in the plural form (e.g. "levers"). Non-countable items
     *            like grass are the same as their plural forms, but with "some" at the beginning ("grass" --> "some grass").
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the item specified by the item or block type I.D. and data given.
     * @see {@link #getItemName(Block, boolean, boolean) getItemName(Block, boolean, boolean)} and {@link #getItemName(ItemStack, boolean, boolean) getItemName(ItemStack,
     *      boolean, boolean)} */
    public static String getItemName(int id, int data, boolean give_data_suffix, boolean singular, boolean without_article) {
        // return null if the potion data is inside a gap
        if (id == 373)
            for (short[] gap : Wiki.POTION_DATA_GAPS)
                if (data > gap[0] && data < gap[1])
                    return null;
        // return null if the spawn egg data is inside a gap
        if (id == 383)
            for (short[] gap : Wiki.SPAWN_EGG_DATA_GAPS)
                if (data > gap[0] && data < gap[1])
                    return null;
        // return null if the I.D. is inside a gap
        for (short[] gap : Wiki.ITEM_GAPS)
            if (id > gap[0] && id < gap[1])
                return null;
        // we need to adjust the query I.D.s based on the gaps in I.D.s for the potion data values gaps
        if (id == 373)
            for (int i = Wiki.POTION_DATA_GAPS.length - 1; i >= 0; i--)
                if (data >= Wiki.POTION_DATA_GAPS[i][1])
                    data -= (Wiki.POTION_DATA_GAPS[i][1] - Wiki.POTION_DATA_GAPS[i][0] - 1);
        // for the spawn egg data values gaps
        if (id == 383)
            for (int i = Wiki.SPAWN_EGG_DATA_GAPS.length - 1; i >= 0; i--)
                if (data >= Wiki.SPAWN_EGG_DATA_GAPS[i][1])
                    data -= (Wiki.SPAWN_EGG_DATA_GAPS[i][1] - Wiki.SPAWN_EGG_DATA_GAPS[i][0] - 1);
        // for the item gaps
        for (int i = Wiki.ITEM_GAPS.length - 1; i >= 0; i--)
            if (id >= Wiki.ITEM_GAPS[i][1])
                id -= (Wiki.ITEM_GAPS[i][1] - Wiki.ITEM_GAPS[i][0] - 1);
        int sing_plur = 0;
        if (singular)
            sing_plur = 1;
        String item = null;
        // the Exceptions in this block of code can be ArrayIndexOutOfBoundsExceptions or NullPointerExceptions
        try {
            // try using the data and I.D. given
            item = Wiki.ITEM_IDS[id][data + 1][sing_plur];
        } catch (ArrayIndexOutOfBoundsException exception) {
            // try first subtracting the data by 8 until we get a result
            for (int temp_data = data; temp_data >= 8; temp_data -= 8) {
                try {
                    item = Wiki.ITEM_IDS[id][temp_data + 1][sing_plur];
                    break;
                } catch (Exception exception2) {
                    //
                }
            }
            // if that fails, try subtracting the data by 4 until we get a result
            if (item == null)
                for (int temp_data = data; temp_data >= 4; temp_data -= 8) {
                    try {
                        item = Wiki.ITEM_IDS[id][temp_data + 1][sing_plur];
                        break;
                    } catch (Exception exception2) {
                        //
                    }
                }
            // if that fails, use the general term
            if (item == null)
                try {
                    item = Wiki.ITEM_IDS[id][0][sing_plur];
                } catch (Exception exception2) {
                    return null;
                }
            if (item != null && give_data_suffix && data > 0)
                item += " (" + data + ")";
        } catch (NullPointerException exception) {
            return null;
        }
        if (item == null)
            return null;
        // if the item is singular and we want no article, remove the preexisting article
        if (singular && without_article)
            item = item.substring(item.split(" ")[0].length() + 1);
        // if the item is plural and we want an article, just add a "some" to the beginning
        else if (!singular && !without_article)
            item = "some " + item;
        return item;
    }

    /** This method returns the name of the item given.
     * 
     * @param item
     *            is the ItemStack which is being named.
     * @param give_data_suffix
     *            specifies whether or not the name of the item should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/id</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the item name returned should be returned in the singular form (e.g. "a lever") or in the plural form (e.g. "levers"). Singular forms
     *            include an article at the beginning. Non-countable items like grass are the same as their plural forms, but with "some" at the beginning ("grass" -->
     *            "some grass").
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the item specified by the item or block type I.D. and data given.
     * @see {@link #getItemName(int, int, boolean, boolean, boolean) getItemName(int, int, boolean, boolean, boolean)} and {@link #getItemName(Block, boolean, boolean)
     *      getItemName(Block, boolean, boolean)} */
    public static String getItemName(ItemStack item, boolean give_data_suffix, boolean singular, boolean without_article) {
        return getItemName(item.getTypeId(), item.getData().getData(), give_data_suffix, singular, without_article);
    }

    /** This method returns the name of the block given.
     * 
     * @param block
     *            is the Block which is being named.
     * @param give_data_suffix
     *            specifies whether or not the name of the item should include the numerical data value at the end of the item name in parentheses (e.g.
     *            "a trapdoor <b>(16)</b>"). For logging purposes in myGuardDog, for example, we should be as specific as possible on information about the item, so this
     *            argument should be <b>true</b>. However, for messages to users for commands like <i>/id</i>, the data suffix is not helpful and looks awkward, so this
     *            argument should be <b>false</b>.
     * @param singular
     *            specifies whether the item name returned should be returned in the singular form (e.g. "a lever") or in the plural form (e.g. "levers"). Singular forms
     *            include an article at the beginning. Non-countable items like grass are the same as their plural forms, but with "some" at the beginning ("grass" -->
     *            "some grass").
     * @param without_article
     *            specifies whether or not the article should be excluded from the item name returned. Plural item names are preceded by "some"; singular item names can be
     *            preceded by "some", "a", "an", or "the".
     * @return the name of the item specified by the item or block type I.D. and data given.
     * @see {@link #getItemName(int, int, boolean, boolean, boolean) getItemName(int, int, boolean, boolean, boolean)} and {@link #getItemName(ItemStack, boolean, boolean)
     *      getItemName(ItemStack, boolean, boolean)} */
    public static String getItemName(Block block, boolean give_data_suffix, boolean singular, boolean without_article) {
        return getItemName(block.getTypeId(), block.getData(), give_data_suffix, singular, without_article);
    }

    /** <b>This method has not been written yet!</b> This method returns a four-line String describing the recipe for crafting the specified item indicated by the provided I.D.
     * This String can be color coded and displayed in the Minecraft chat or console to describe how to craft the item indicated.
     * 
     * @param id
     *            is the I.D. of the item or block for which the recipe was requested.
     * @param data
     *            is the numerical data value for the item or block. A value of -1 for <b><tt>data</b></tt> will cause this method to return the general recipe for the general
     *            item.
     * @return a four-line String describing the recipe for crafting the specified item indicated by the provided I.D.
     * @see {@link #getRecipe(String) getRecipe(String)} and {@link #getRecipe(ItemStack) getRecipe(ItemStack)} */
    public static String getRecipe(int id, int data) {
        // TODO
        return ChatColor.GOLD + "Coming soon to a server near you!";
    }

    /** This method returns a four-line String describing the recipe for crafting the specified item indicated by the provided name. This String can be color coded and
     * displayed in the Minecraft chat or console to describe how to craft the item indicated.
     * 
     * @param item_name
     *            is the name of the item specified
     * @return a four-line String describing the recipe for crafting the specified item indicated by the provided I.D.
     * @see {@link #getRecipe(int, int) getRecipe(int, int)} and {@link #getRecipe(ItemStack) getRecipe(ItemStack)} */
    public static String getRecipe(String item_name) {
        Integer[] id_and_data = getItemIdAndData(item_name, null);
        if (id_and_data == null)
            return null;
        return getRecipe(id_and_data[0], id_and_data[1]);
    }

    /** This method returns a four-line String describing the recipe for crafting the specified item. This String can be color coded and displayed in the Minecraft chat or
     * console to describe how to craft the item indicated.
     * 
     * @param item
     *            is the item specified.
     * @return a four-line String describing the recipe for crafting the specified item indicated by the provided I.D.
     * @see {@link #getRecipe(int, int) getRecipe(int, int)} and {@link #getRecipe(String) getRecipe(String)} */
    public static String getRecipe(ItemStack item) {
        return getRecipe(item.getTypeId(), item.getData().getData());
    }

    /** This method will tell whether or not a certain block will break if the block that it is attached to is broken. No data value is required as input for this method
     * because items with the same I.D. consistently have this property in common.
     * 
     * @param id
     *            is the I.D. of the block that needs to be checked for the "must be attached" property.
     * @param bottom_only
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the item is one that must be attached on the bottom only like redstone wire or a
     *            lily pad (indicated by a <b>true</b> value), <b>2)</b> if the item is one that can be attached sideways like a torch or a wall sign (indicated by a
     *            <b>false</b> value), or <b>3)</b> if the item needs to be attached on the bottom or sideways (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block given by the I.D. will break if the block it is attached to breaks and it attaches in the way indicated by <b> <tt>bottom_only</tt>
     *         </b>, <b>false</b> if the block does not need to be attached to another block or not in the way specified by <tt><b>bottom_only</tt></b>, and <b>null</b> if the
     *         I.D. given does not apply to a block at all.
     * @see {@link #mustBeAttached(String, Boolean) mustBeAttached(String, Boolean)} and {@link #mustBeAttached(Block, Boolean) mustBeAttached(Block, Boolean)} */
    public static Boolean mustBeAttached(int id, Boolean bottom_only) {
        // return null if the I.D. doesn't belong to anything or is an item I.D. instead of a block I.D.
        if (getItemName(id, -1, false, false, true) == null || id >= 256)
            return null;
        if (bottom_only == null || bottom_only)
            for (int must_be_attached_bottom_only_ID : Wiki.MUST_BE_ATTACHED_BOTTOM_ONLY_IDS)
                if (must_be_attached_bottom_only_ID == id)
                    return true;
        if (bottom_only == null || !bottom_only)
            for (int must_be_attached_can_be_sideways_ID : Wiki.MUST_BE_ATTACHED_CAN_BE_SIDEWAYS_IDS)
                if (must_be_attached_can_be_sideways_ID == id)
                    return true;
        return false;
    }

    /** This method will tell whether or not the block specified will break if the block that it is attached to is broken.
     * 
     * @param block
     *            is the block that needs to be checked for the "must be attached" property.
     * @param bottom_only
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the item is one that must be attached on the bottom only like redstone wire or a
     *            lily pad (indicated by a <b>true</b> value), <b>2)</b> if the item is one that can be attached sideways like a torch or a wall sign (indicated by a
     *            <b>false</b> value), or <b>3)</b> if the item needs to be attached on the bottom or sideways (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block given by the I.D. will break if the block it is attached to breaks and it attaches in the way indicated by <b> <tt>bottom_only</tt></b>
     *         or <b>false</b> if the block does not need to be attached to another block or not in the way specified by <tt><b>bottom_only</tt></b>.
     * @see {@link #mustBeAttached(int, Boolean) mustBeAttached(int, Boolean)} and {@link #mustBeAttached(String, Boolean) mustBeAttached(String, Boolean)} */
    public static Boolean mustBeAttached(Block block, Boolean bottom_only) {
        return mustBeAttached(block.getTypeId(), bottom_only);
    }

    /** This method will tell whether or not the block specified by the given name will break if the block that it is attached to is broken.
     * 
     * @param item_name
     *            is the name of the block that needs to be checked for the "must be attached" property.
     * @param bottom_only
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the item is one that must be attached on the bottom only like redstone wire or a
     *            lily pad (indicated by a <b>true</b> value), <b>2)</b> if the item is one that can be attached sideways like a torch or a wall sign (indicated by a
     *            <b>false</b> value), or <b>3)</b> if the item needs to be attached on the bottom or sideways (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block given by the I.D. will break if the block it is attached to breaks and it attaches in the way indicated by <b> <tt>bottom_only</tt></b>
     *         or <b>false</b> if the block does not need to be attached to another block or not in the way specified by <tt><b>bottom_only</tt></b>.
     * @see {@link #mustBeAttached(int, Boolean) mustBeAttached(int, Boolean)} and {@link #mustBeAttached(Block, Boolean) mustBeAttached(Block, Boolean)} */
    public static Boolean mustBeAttached(String item_name, Boolean bottom_only) {
        Integer[] id = getItemIdAndData(item_name, false);
        if (id == null)
            return null;
        else
            return mustBeAttached(id[0], bottom_only);
    }

    public static Boolean isContainer(int id, Boolean can_store) {
        if (getItemName(id, -1, false, true, true) == null)
            return null;
        else if (id >= 256)
            return false;
        for (int lockable : Wiki.LOCKABLE_CONTAINER_IDS)
            // in LOCKABLE_CONTAINER_IDS, all the values are positive or negative depending on whether or not they can store items when the player exits the
            // block's inventory; therefore, we have to take the absolute value of the stored lockable container I.D. here
            if (Math.abs(lockable) == id)
                if (can_store == null || can_store && lockable > 0 || !can_store && lockable < 0)
                    return true;
                else
                    return false;
        return false;
    }

    public static Boolean isContainer(Block block, Boolean can_store) {
        return isContainer(block.getTypeId(), can_store);
    }

    public static Boolean isContainer(String block_name, Boolean can_store) {
        Integer[] id_and_data = getItemIdAndData(block_name, false);
        if (id_and_data == null)
            return null;
        return isContainer(id_and_data[0], can_store);
    }

    public static Boolean isContainer(int id) {
        return isContainer(id, null);
    }

    public static Boolean isContainer(Block block) {
        return isContainer(block.getTypeId(), null);
    }

    public static Boolean isContainer(String block_name) {
        return isContainer(block_name, null);
    }

    public static Boolean isDamageable(int id) {
        if (getItemName(id, 0, true, true, true) == null)
            return null;
        for (short[] damageable : Wiki.DAMAGEABLE_ITEM_IDS)
            if (damageable[0] == id)
                return true;
        return false;
    }

    public static boolean isDamageable(ItemStack item) {
        Boolean result = isDamageable(item.getTypeId());
        if (result == null) {
            tellOps(ChatColor.DARK_RED + "Someone just tried to see if this item was damageable, but I don't know what this item is! It has the I.D. " + item.getTypeId()
                    + ". Is myPluginWiki up to date?", true);
            return false;
        }
        return result;
    }

    /** This method will tell whether or not a certain block can be locked (meaning that it's either a container--a block that contain other items, e.g. a chest--or a switch--a
     * block that can be pressed or toggled on and off, e.g. a button--or a portal block--a block that can be opened and closed, e.g. a door). No data value is required as
     * input for this method because items with the same I.D. consistently have this property in common.
     * 
     * @param id
     *            is the I.D. of the block that needs to be checked for the "lockable" property.
     * @param has_inventory
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the block is a container (indicated by a <b>true</b> value), <b>2)</b> if the block
     *            is not a container (indicated by a <b>false</b> value), or <b>3)</b> if the block is any kind of lockable block (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block given by the I.D. can be locked and it does or does not have an inventory in the way indicated by <b> <tt>has_inventory</tt></b>,
     *         <b>false</b> if the block cannot be locked or does or does not have an inventory opposite the requirement indicated by <tt><b>has_inventory</tt></b>, and
     *         <b>null</b> if the I.D. given does not apply to a block at all.
     * @see {@link #isLockable(String, Boolean) isLockable(String, Boolean)} and {@link #isLockable(Block, Boolean) isLockable(Block, Boolean)} */
    public static Boolean isLockable(int id) {
        if (getItemName(id, -1, false, true, true) == null)
            return null;
        else if (id >= 256)
            return false;
        for (int lockable : Wiki.LOCKABLE_CONTAINER_IDS)
            // in LOCKABLE_CONTAINER_IDS, all the values are positive or negative depending on whether or not they can store items when the player exits the
            // block's inventory; therefore, we have to take the absolute value of the stored lockable container I.D. here
            if (Math.abs(lockable) == id)
                return true;
        for (int lockable : Wiki.LOCKABLE_PORTAL_IDS)
            if (lockable == id)
                return true;
        for (int lockable : Wiki.LOCKABLE_SWITCH_IDS)
            if (lockable == id)
                return true;
        return false;
    }

    /** This method will tell whether or not a certain block can be locked (meaning that it's either a container--a block that contain other items, e.g. a chest--or a switch--a
     * block that can be pressed or toggled on and off, e.g. a button--or a portal block--a block that can be opened and closed, e.g. a door).
     * 
     * @param block
     *            is the block that needs to be checked for the "lockable" property.
     * @param has_inventory
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the block is a container (indicated by a <b>true</b> value), <b>2)</b> if the block
     *            is not a container (indicated by a <b>false</b> value), or <b>3)</b> if the block is any kind of lockable block (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block can be locked and it does or does not have an inventory in the way indicated by <b> <tt>has_inventory</tt></b> or <b>false</b> if the
     *         block cannot be locked or does or does not have an inventory opposite the requirement indicated by <tt><b>has_inventory</tt> </b>.
     * @see {@link #isLockable(int, Boolean) isLockable(int, Boolean)} and {@link #isLockable(String, Boolean) isLockable(String, Boolean)} */
    public static Boolean isLockable(Block block) {
        return isLockable(block.getTypeId());
    }

    /** This method will tell whether or not a certain block can be locked (meaning that it's either a container--a block that contain other items, e.g. a chest--or a switch--a
     * block that can be pressed or toggled on and off, e.g. a button--or a portal block--a block that can be opened and closed, e.g. a door).
     * 
     * @param block_name
     *            is the name of the type of block that needs to be checked for the "lockable" property.
     * @param has_inventory
     *            indicates whether the method should return <b>true</b> only <b>1)</b> if the block is a container (indicated by a <b>true</b> value), <b>2)</b> if the block
     *            is not a container (indicated by a <b>false</b> value), or <b>3)</b> if the block is any kind of lockable block (indicated by a <b>null</b> value).
     * @return <b>true</b> if the block can be locked and it does or does not have an inventory in the way indicated by <b> <tt>has_inventory</tt></b>, <b>false</b> if the
     *         block cannot be locked or does or does not have an inventory opposite the requirement indicated by <tt><b>has_inventory</tt> </b>, and <b>null</b> if the I.D.
     *         given does not apply to a block at all.
     * @see {@link #isLockable(int, Boolean) isLockable(int, Boolean)} and {@link #isLockable(Block, Boolean) isLockable(Block, Boolean)} */
    public static Boolean isLockable(String block_name) {
        Integer[] id_and_data = getItemIdAndData(block_name, false);
        if (id_and_data == null)
            return null;
        return isLockable(id_and_data[0]);
    }

    public static Boolean isPortal(int id) {
        if (getItemName(id, -1, false, true, true) == null)
            return null;
        else if (id >= 256)
            return false;
        for (int lockable : Wiki.LOCKABLE_PORTAL_IDS)
            if (lockable == id)
                return true;
        return false;
    }

    public static Boolean isPortal(Block block) {
        return isPortal(block.getTypeId());
    }

    public static Boolean isPortal(String block_name) {
        Integer[] id_and_data = getItemIdAndData(block_name, false);
        if (id_and_data == null)
            return null;
        return isPortal(id_and_data[0]);
    }

    public static Boolean isRepairable(int id) {
        return isDamageable(id);
    }

    public static boolean isRepairable(ItemStack item) {
        return isDamageable(item);
    }

    public static Boolean isRepairableWithSomethingBesidesItself(int id) {
        if (getItemName(id, 0, true, true, true) == null)
            return null;
        for (short[] damageable : Wiki.DAMAGEABLE_ITEM_IDS)
            if (damageable[0] == id)
                if (damageable.length > 1)
                    return true;
                else
                    return false;
        return false;
    }

    public static boolean isRepairableWithSomethingBesidesItself(ItemStack item) {
        Boolean result = isRepairableWithSomethingBesidesItself(item.getTypeId());
        if (result == null) {
            tellOps(ChatColor.DARK_RED
                    + "Someone just tried to see if this item was repairable with something besides itself, but I don't know what this item is! It has the I.D. "
                    + item.getTypeId() + ". Is myPluginWiki up to date?", true);
            return false;
        }
        return result;
    }

    public static Boolean isRepairableWith(int id, int id2) {
        if (getItemName(id, 0, true, true, true) == null || getItemName(id2, 0, true, true, true) == null)
            return null;
        for (short[] damageable : Wiki.DAMAGEABLE_ITEM_IDS)
            if (damageable[0] == id)
                if (damageable.length > 1 && damageable[1] == id2)
                    return true;
                else
                    return false;
        return false;
    }

    public static boolean isRepairableWith(ItemStack item, ItemStack item2) {
        Boolean result = isRepairableWith(item.getTypeId(), item2.getTypeId());
        if (result == null) {
            tellOps(ChatColor.DARK_RED
                    + "Someone just tried to see if this item was repairable with this other item, but I don't know what this item is! One item has the I.D. "
                    + item.getTypeId() + " and the other has the I.D. " + item2.getTypeId() + ". Is myPluginWiki up to date?", true);
            return false;
        }
        return result;
    }

    public static boolean isRepairableWith(ItemStack item, Block block2) {
        Boolean result = isRepairableWith(item.getTypeId(), block2.getTypeId());
        if (result == null) {
            tellOps(ChatColor.DARK_RED + "Someone just tried to see if this item was repairable with this block, but I don't know what this item is! The item has the I.D. "
                    + item.getTypeId() + " and the block has the I.D. " + block2.getTypeId() + ". Is myPluginWiki up to date?", true);
            return false;
        }
        return result;
    }

    public static Boolean isRepairableWith(ItemStack item, int id2) {
        return isRepairableWith(item.getTypeId(), id2);
    }

    public static Boolean isRepairableWith(int id, ItemStack item2) {
        return isRepairableWith(id, item2.getTypeId());
    }

    public static Boolean isRepairableWith(int id, Block block2) {
        return isRepairableWith(id, block2.getTypeId());
    }

    /** This method will tell whether or not a certain block is solid. Mobs can walk on solid blocks without falling through them (e.g. grass blocks) while they simply fall
     * through non-solid blocks (e.g. signs). No data value is required as input for this method because items with the same I.D. consistently have this property in common.
     * 
     * @param id
     *            is the I.D. of the block that needs to be checked for the "solid" property.
     * @return <b>true</b> if the block given by the I.D. is solid, <b>false</b> if the block given is not solid, or <b>null</b> if the I.D. given does not apply to a block at
     *         all. */
    public static Boolean isSolid(int id) {
        if (getItemName(id, -1, false, true, true) == null)
            return null;
        for (int non_solid_ID : Wiki.NON_SOLID_BLOCK_IDS)
            if (non_solid_ID == id)
                return false;
        return true;
    }

    public static Boolean isSolid(Block block) {
        return isSolid(block.getTypeId());
    }

    public static Boolean isSwitch(int id) {
        if (getItemName(id, -1, false, true, true) == null)
            return null;
        else if (id >= 256)
            return false;
        for (int lockable : Wiki.LOCKABLE_SWITCH_IDS)
            if (lockable == id)
                return true;
        return false;
    }

    public static Boolean isSwitch(Block block) {
        return isSwitch(block.getTypeId());
    }

    public static Boolean isSwitch(String block_name) {
        Integer[] id_and_data = getItemIdAndData(block_name, false);
        if (id_and_data == null)
            return null;
        return isSwitch(id_and_data[0]);
    }

}