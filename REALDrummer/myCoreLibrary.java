package REALDrummer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import REALDrummer.utils.PlayerUtilities;
import static REALDrummer.utils.ColorUtilities.*;
import static REALDrummer.utils.MessageUtilities.*;
import static REALDrummer.utils.StringUtilities.*;
import static REALDrummer.utils.WikiUtilities.*;

public class myCoreLibrary extends myPlugin implements Listener {
    public static myPlugin mCL;

    public static ArrayList<UUID> frozen_players = new ArrayList<UUID>();
    public static HashMap<String, ArrayList<String>> notices = new HashMap<String, ArrayList<String>>();

    // enable/disable
    @Override
    public String[] myEnable() {
        mCL = this;
        COLOR = ChatColor.DARK_PURPLE;

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
    @EventHandler
    public void freezeFrozenPlayers(PlayerMoveEvent event) {
        if (frozen_players.contains(event.getPlayer().getName())
                && !(event.getTo().getX() == event.getFrom().getX() && event.getTo().getY() <= event.getFrom().getY() && event.getTo().getZ() == event.getFrom().getZ())) {
            Location cancel_to = event.getFrom();
            // allow looking
            cancel_to.setPitch(event.getTo().getPitch());
            cancel_to.setYaw(event.getTo().getYaw());
            // allow falling
            if (event.getTo().getY() < event.getFrom().getY())
                cancel_to.setY(event.getTo().getY());
            event.getPlayer().teleport(event.getFrom());
        }
    }

    @EventHandler
    public void sendPlayersTheirNotices(PlayerJoinEvent event) {

    }

    // commanders
    @Commander
    public static void help(CommandSender sender, String subject) {

    }

    @Commander
    private void recipe(CommandSender sender, HashMap<String, String> parameters) {
        // TODO: make "this" or "that" return the recipe of what the sender is holding or pointing at, respectively
        // TODO: change this method to format the chat message directly from the Recipe object in the server's stores

        // retrieve the query String from the parameters
        String query = parameters.get("query");
        if (query == null) {
            sender.sendMessage(ChatColor.RED + "You forgot to tell me what item you want to get the recipe for!");
            return;
        }

        // get the item's I.D. and data
        int id = -1, data = -1;

        // if the query is "this" or "that", retrieve the I.D. and data from the object in the player's hand or the block that the player is pointing at (respectively)
        if (query.equalsIgnoreCase("this")||query.equalsIgnoreCase("that"))
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED+"I'm afraid you must specify an object name or I.D.; you have no hands to hold anything or point at anything!");
                return;
            } else if (query.equalsIgnoreCase("this")) {
                id = ((Player)sender).getItemInHand().getTypeId();
                data = ((Player)sender).getItemInHand().getData().getData();
            } else {
                Block target_block = PlayerUtilities.getTargetBlock((Player) sender, false);
                if 
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
        String recipe = getRecipe(id, data), item_name = getItemName(id, data, false, false, true);
        if (recipe != null)
            sender.sendMessage(colorCode(recipe));
        else if (item_name != null)
            sender.sendMessage(COLOR + "You can't craft " + item_name + "!");
        else if (query.toLowerCase().startsWith("a") || query.toLowerCase().startsWith("e") || query.toLowerCase().startsWith("i") || query.toLowerCase().startsWith("o")
                || query.toLowerCase().startsWith("u"))
            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what an \"" + query + "\" is.");
        else
            sender.sendMessage(ChatColor.RED + "Sorry, but I don't know what a \"" + query + "\" is.");
    }

    @Commander
    private void id(CommandSender sender, HashMap<String, String> parameters) {
        if (parameters.length == 0 || parameters[0].equalsIgnoreCase("this") || parameters[0].equalsIgnoreCase("that"))
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Block block = player.getTargetBlock(null, 1024);
                String block_name = getItemName(block, false, true, true), id_and_data = String.valueOf(block.getTypeId());
                if (block.getData() > 0)
                    id_and_data += ":" + block.getData();
                // send the message
                if (block_name != null)
                    player.sendMessage(COLOR + "That " + block_name + " you're pointing at has the I.D. " + id_and_data + ".");
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
                        player.sendMessage(COLOR + "Those " + item_name + " you're holding have the I.D. " + id_and_data + ".");
                    else
                        player.sendMessage(COLOR + "That " + item_name + " you're holding has the I.D. " + id_and_data + ".");
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
            String query = "";
            for (String parameter : parameters)
                if (query.equals(""))
                    query = parameter;
                else
                    query += " " + parameter;
            // for simple I.D. queries
            try {
                int id = Integer.parseInt(query);
                String item_name = getItemName(id, -1, false, false, true);
                if (item_name != null)
                    // if the singular form uses the "some" artcile or the item name ends in "s" but not "ss" (like "wooden planks", but not like
                    // "grass"), the item name is a true plural
                    if (!getItemName(id, -1, false, true, false).startsWith("some ") || (item_name.endsWith("s") && !item_name.endsWith("ss")))
                        sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id + ".");
                    else
                        sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id + ".");
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
                                sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + query + ".");
                            else
                                sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + query + ".");
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
                            sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id_and_data_term + ".");
                        else
                            sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id_and_data_term + ".");
                    }
                } catch (NumberFormatException e) {
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
                        sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " have the I.D. " + id_and_data_term + ".");
                    else
                        sender.sendMessage(COLOR + item_name.substring(0, 1).toUpperCase() + item_name.substring(1) + " has the I.D. " + id_and_data_term + ".");
                }
            }
        }
    }

}
