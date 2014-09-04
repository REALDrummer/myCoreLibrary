package REALDrummer;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import REALDrummer.interfaces.Commander;
import REALDrummer.settings.mySetting;
import REALDrummer.utils.PlayerUtilities;
import static REALDrummer.utils.ColorUtilities.*;
import static REALDrummer.utils.WikiUtilities.*;

/** This is the main class of the {@link myPlugin} myCoreLibrary, a plugin which acts as a library, A.P.I., and "parent" to all other {@link myPlugin}s. It contains the
 * {@link myPlugin}, {@link myData}, and {@link myWiki} classes among others. */
public class myCoreLibrary extends myPlugin {
    /** This is a static singleton-like representation of myCoreLibrary. It is essential to allow other classes to access myCoreLibrary non-statically. */
    public static myPlugin mCL;

    // enable/disable
    @Override
    public void onLoad() {
        mCL = this;
    }

    @Override
    public String[] myEnable() {
        // initialize the list of player names in PlayerUtilities with all the OfflinePlayers' names
        for (OfflinePlayer player : getServer().getOfflinePlayers())
            PlayerUtilities.players.put(player.getUniqueId(), player.getName());

        // register the myQuestion class as a Listener
        getServer().getPluginManager().registerEvents(new myQuestion(), this);

        return new String[] { "I'm like the Minecraft-opedia!", "I have info galore! I even have info about my info! Info is coming out of my .classes!",
                "The Minecraft Library of Congress has got nothing on me." };
    }

    @Override
    public String[] myDisable() {
        return new String[] { "I hope you returned all your files, because we're closing the library for the day!", "This information kiosk is now closed.",
                "All right. All done with work. Time to go home and read the Minecraft dictionary!" };
    }

    // listeners
    /** This listener method adds {@link Player}s who have never played here before to the player name list in {@link PlayerUtilities} for later use in things like name
     * auto-completion.
     * 
     * @param event
     *            is the {@link PlayerJoinEvent} that triggers this listener method. */
    @EventHandler
    public static void addNewPlayersToThePlayerNameList(PlayerJoinEvent event) {
        // if the player is new, add them to the player names list
        if (!event.getPlayer().hasPlayedBefore())
            PlayerUtilities.players.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    /** This listener method sends players who have notices waiting their notices when they log onto the server.
     * 
     * @param event
     *            is the {@link PlayerJoinEvent} that triggers this listener method. */
    @EventHandler
    public void sendPlayersTheirNotices(PlayerJoinEvent event) {
        // send players their notices
        ArrayList<String> players_notices = notices.get(event.getPlayer().getUniqueId());
        if (players_notices != null && players_notices.size() > 0)
            if (players_notices.size() == 1)
                event.getPlayer().sendMessage(getColor() + "You have a new notice!\nUse " + ChatColor.ITALIC + "/notice" + getColor() + " to read it!");
            else
                event.getPlayer().sendMessage(
                        getColor() + "You have " + players_notices.size() + " new notices!\nUse " + ChatColor.ITALIC + "/notice" + getColor() + " to read them!");
    }

    // commanders
    // TODO EXT TEMP
    @Commander(usage = "(ufl-op-parse S...)", open_command = true)
    public void unflaggedParsingOptionalTest(CommandSender sender, HashMap<String, Object> parameters) {
        if (parameters.size() > 0)
            for (String key : parameters.keySet())
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + key + ChatColor.AQUA + "" + ChatColor.BOLD + ":" + ChatColor.WHITE + "" + ChatColor.ITALIC + "\""
                        + ChatColor.WHITE + parameters.get(key).toString() + ChatColor.ITALIC + "\"");
        else
            sender.sendMessage(ChatColor.RED + "No flags read!");
    }

    @Commander(usage = "(--fl-op-parse S...)", open_command = true)
    public void flaggedParsingOptionalTest(CommandSender sender, HashMap<String, Object> parameters) {
        if (parameters.size() > 0)
            for (String key : parameters.keySet())
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + key + ChatColor.AQUA + "" + ChatColor.BOLD + ":" + ChatColor.WHITE + "" + ChatColor.ITALIC + "\""
                        + ChatColor.WHITE + parameters.get(key).toString() + ChatColor.ITALIC + "\"");
        else
            sender.sendMessage(ChatColor.RED + "No flags read!");
    }

    @Commander(usage = "(ufl-op-nonparse S)", open_command = true)
    public void unflaggedNonParsingOptionalTest(CommandSender sender, HashMap<String, Object> parameters) {
        if (parameters.size() > 0)
            for (String key : parameters.keySet())
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + key + ChatColor.AQUA + "" + ChatColor.BOLD + ":" + ChatColor.WHITE + "" + ChatColor.ITALIC + "\""
                        + ChatColor.WHITE + parameters.get(key).toString() + ChatColor.ITALIC + "\"");
        else
            sender.sendMessage(ChatColor.RED + "No flags read!");
    }

    @Commander(usage = "(--fl-op-nonparse S)", open_command = true)
    public void flaggedNonParsingOptionalTest(CommandSender sender, HashMap<String, Object> parameters) {
        if (parameters.size() > 0)
            for (String key : parameters.keySet())
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + key + ChatColor.AQUA + "" + ChatColor.BOLD + ":" + ChatColor.WHITE + "" + ChatColor.ITALIC + "\""
                        + ChatColor.WHITE + parameters.get(key).toString() + ChatColor.ITALIC + "\"");
        else
            sender.sendMessage(ChatColor.RED + "No flags read!");
    }

    // TODO END TEMP

    /** This command method handles <i>/recipe</i>, which sends <b><tt>sender</b></tt> a chat message explaining how to craft the given item.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command.
     * @param parameters
     *            is a <tt>HashMap</tt><<tt>String</tt>, <tt>Object</tt>> representing the parameters given in the command parsed by
     *            {@link REALDrummer.flags.myFlag#readCommand(myPlugin, CommandSender, String, String[]) myFlag's readCommand() method}. */
    @Commander(usage = "(query S...)", open_command = true)
    public void recipe(CommandSender sender, HashMap<String, Object> parameters) {
        // TODO: make "this" or "that" return the recipe of what the sender is holding or pointing at, respectively
        // TODO: change this method to format the chat message directly from the Recipe object in the server's stores

        // retrieve the query String from the parameters
        String query = (String) parameters.get("query");
        if (query == null) {
            sender.sendMessage(ChatColor.RED + "You forgot to tell me what item you want to get the recipe for!");
            return;
        }

        // get the item's I.D. and data
        int id = -1, data = -1;

        // if the query is "this" or "that", retrieve the I.D. and data from the object in the player's hand or the block that the player is pointing at (respectively)
        if (query.equalsIgnoreCase("this") || query.equalsIgnoreCase("that"))
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "I'm afraid you must specify an object name or I.D.; you have no hands to hold anything or point at anything!");
                return;
            } else if (query.equalsIgnoreCase("this")) {
                id = ((Player) sender).getItemInHand().getTypeId();
                data = ((Player) sender).getItemInHand().getData().getData();
            } else {
                Block target_block = PlayerUtilities.getTargetBlock((Player) sender, 500);
                // if
                // TODO
            }

        try {
            id = Integer.parseInt(query);
        } catch (NumberFormatException exception) {
            if (query.split(":").length == 2)
                try {
                    id = Integer.parseInt(query.split(":")[0]);
                    data = Integer.parseInt(query.split(":")[1]);
                } catch (NumberFormatException exception2) {
                    id = -1;
                    data = -1;
                    Integer[] temp = getItemIdAndData(query, true);
                    if (temp[0] != null) {
                        id = temp[0];
                        data = temp[1];
                    }
                }
            else {
                Integer[] temp = getItemIdAndData(query, true);
                if (temp[0] != null) {
                    id = temp[0];
                    data = temp[1];
                }
            }
        }
        String recipe = null;
        // TODO TEMP
        if (id == 0)
            recipe = "";
        // TODO: find the item's recipe and construct the recipe String from the Recipe object

        String item_name = getItemName(id, data, false, false, true);
        if (recipe != null)
            sender.sendMessage(colorCode(recipe));
        else if (item_name != null)
            sender.sendMessage(getColor() + "You can't craft " + item_name + "!");
        else if (query.toLowerCase().startsWith("a") || query.toLowerCase().startsWith("e") || query.toLowerCase().startsWith("i") || query.toLowerCase().startsWith("o")
                || query.toLowerCase().startsWith("u"))
            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what an \"" + query + "\" is.");
        else
            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what a \"" + query + "\" is.");
    }

    /** This command method handles <i>/id</i>, which sends <b><tt>sender</b></tt> a chat message giving the item I.D. for the given item name or vice versa.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command.
     * @param parameters
     *            is a <tt>HashMap</tt><<tt>String</tt>, <tt>Object</tt>> representing the parameters given in the command parsed by
     *            {@link REALDrummer.flags.myFlag#readCommand(myPlugin, CommandSender, String, String[]) myFlag's readCommand() method}. */
    @Commander(usage = "(query S...)", open_command = true)
    public void id(CommandSender sender, HashMap<String, Object> parameters) {
        String query = (String) parameters.get("query");
        if (query == null || query.equalsIgnoreCase("this") || query.equalsIgnoreCase("that"))
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Block block = player.getTargetBlock(null, 1024);
                String block_name = getItemName(block, false, true, true), id_and_data = String.valueOf(block.getTypeId());
                if (block.getData() > 0)
                    id_and_data += ":" + block.getData();
                // send the message
                if (block_name != null)
                    player.sendMessage(getColor() + "That " + block_name + " you're pointing at has the I.D. " + id_and_data + ".");
                else {
                    player.sendMessage(ChatColor.RED + "Uh...what in the world " + ChatColor.ITALIC + "is" + ChatColor.RED + " that thing you're pointing at?");
                    player.sendMessage(ChatColor.RED + "Well, whatever it is, it has the I.D. " + id_and_data + ".");
                }
                String item_name = getItemName(player.getItemInHand(), false, player.getItemInHand().getAmount() <= 1, true);
                id_and_data = String.valueOf(player.getItemInHand().getTypeId());
                if (player.getItemInHand().getData().getData() > 0)
                    id_and_data += ":" + player.getItemInHand().getData().getData();
                // send the message
                if (item_name != null)
                    if (player.getItemInHand().getAmount() > 1)
                        player.sendMessage(getColor() + "Those " + item_name + " you're holding have the I.D. " + id_and_data + ".");
                    else
                        player.sendMessage(getColor() + "That " + item_name + " you're holding has the I.D. " + id_and_data + ".");
                else {
                    if (player.getItemInHand().getAmount() > 1)
                        player.sendMessage(ChatColor.RED + "Uh...what in the world " + ChatColor.ITALIC + "are" + ChatColor.RED + " those things you're holding?");
                    else
                        player.sendMessage(ChatColor.RED + "Uh...what in the world " + ChatColor.ITALIC + "is" + ChatColor.RED + " that thing you're holding?");
                    player.sendMessage(ChatColor.RED + "Well, whatever it is, it has the I.D. " + id_and_data + ".");
                }
            } else
                sender.sendMessage(ChatColor.RED + "You forgot to tell me what item or I.D. you want identified!");
        else {
            // for simple I.D. queries
            try {
                int id = Integer.parseInt(query);
                String item_name = getItemName(id, -1, false, false, true);
                if (item_name != null)
                    // if the singular form uses the "some" artcile or the item name ends in "s" but not "ss" (like "wooden planks", but not like
                    // "grass"), the item name is a true plural
                    if (!getItemName(id, -1, false, true, false).startsWith("some ") || (item_name.endsWith("s") && !item_name.endsWith("ss")))
                        sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id + ".");
                    else
                        sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id + ".");
                else
                    sender.sendMessage(ChatColor.RED + "No item has the I.D. " + id + ".");
            } catch (NumberFormatException exception) {
                try {
                    String[] temp = query.split(":");
                    if (temp.length == 2) {
                        // for "[id]:[data]" queries
                        int id = Integer.parseInt(temp[0]), data = Integer.parseInt(temp[1]);
                        String item_name = getItemName(id, data, false, false, true);
                        // send the message
                        if (item_name != null)
                            // if the singular form uses the "some" artcile or the item name ends in "s" but not "ss" (like "wooden planks", but not like
                            // "grass"), the item name is a true plural
                            if (!getItemName(id, data, false, true, false).startsWith("some ") || (item_name.endsWith("s") && !item_name.endsWith("ss")))
                                sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + query + ".");
                            else
                                sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + query + ".");
                        else
                            sender.sendMessage(ChatColor.RED + "No item has the I.D. " + query + ".");
                    } else {
                        // for word queries
                        Integer[] id_and_data = getItemIdAndData(query, null);
                        if (id_and_data == null) {
                            if (query.toLowerCase().startsWith("a") || query.toLowerCase().startsWith("e") || query.toLowerCase().startsWith("i")
                                    || query.toLowerCase().startsWith("o") || query.toLowerCase().startsWith("u"))
                                sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what an \"" + query + "\" is.");
                            else
                                sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what a \"" + query + "\" is.");
                            return;
                        }
                        // this part seems odd because it seems like it's a long roundabout way to get item_name. You might think: isn't item_name the same as
                        // query? Wrong. A query can (and probably is) just a few letters from the name of the item. By finding the id, then using that to get
                        // the name, it's an effective autocompletion of the item name.
                        String item_name = getItemName(id_and_data[0], id_and_data[1], false, false, true), id_and_data_term = String.valueOf(id_and_data[0]);
                        if (id_and_data[1] > 0)
                            id_and_data_term += ":" + id_and_data[1];
                        // if it found it, send the message
                        if (!getItemName(id_and_data[0], id_and_data[1], false, true, false).startsWith("some ") || (item_name.endsWith("s") && !item_name.endsWith("ss")))
                            sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id_and_data_term + ".");
                        else
                            sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id_and_data_term + ".");
                    }
                } catch (NumberFormatException exception2) {
                    // for word queries
                    Integer[] id_and_data = getItemIdAndData(query, null);
                    if (id_and_data == null) {
                        if (query.toLowerCase().startsWith("a") || query.toLowerCase().startsWith("e") || query.toLowerCase().startsWith("i")
                                || query.toLowerCase().startsWith("o") || query.toLowerCase().startsWith("u"))
                            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what an \"" + query + "\" is.");
                        else
                            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what a \"" + query + "\" is.");
                        return;
                    }
                    // this part seems odd because it seems like it's a long roundabout way to get item_name. You might think: isn't item_name the same as
                    // query? Wrong. A query can (and probably is) just a few letters from the name of the item. By finding the id, then using that to get
                    // the name, it's an effective autocompletion of the item name.
                    String item_name = getItemName(id_and_data[0], id_and_data[1], false, false, true), id_and_data_term = String.valueOf(id_and_data[0]);
                    if (id_and_data[1] > 0)
                        id_and_data_term += ":" + id_and_data[1];
                    // if it found it, send the message
                    if (!getItemName(id_and_data[0], id_and_data[1], false, true, false).startsWith("some ") || (item_name.endsWith("s") && !item_name.endsWith("ss")))
                        sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id_and_data_term + ".");
                    else
                        sender.sendMessage(getColor() + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id_and_data_term + ".");
                }
            }
        }
    }

    @Override
    public mySetting[] configDefaults() {
        return new mySetting[] {};
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_PURPLE;
    }

}
