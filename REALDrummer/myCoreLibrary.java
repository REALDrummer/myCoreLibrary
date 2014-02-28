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
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static REALDrummer.ColorUtilities.colorCode;
import static REALDrummer.MessageUtilities.tellOps;
import static REALDrummer.MessageUtilities.err;
import static REALDrummer.StringUtilities.readResponse;
import static REALDrummer.WikiUtilities.getItemIdAndData;
import static REALDrummer.WikiUtilities.getItemName;
import static REALDrummer.WikiUtilities.getRecipe;

public class myCoreLibrary extends JavaPlugin {
    public static final ChatColor COLOR = ChatColor.DARK_PURPLE;

    public static Plugin mCL;
    public static Server server;
    public static ConsoleCommandSender console;

    public static boolean auto_update = true;
    public static ArrayList<String> debuggers = new ArrayList<String>();

    @Override
    public void onEnable() {
        mCL = this;
        server = getServer();
        console = server.getConsoleSender();
        loadTheConfig(console);
        if (auto_update)
            checkForUpdates(console);
        // done enabling
        String[] enable_messages =
                { "I'm like the Minecraft-opedia!", "I have info galore! I even have info about my info! Info is coming out of my .classes!",
                        "The Minecraft Library of Congress has got nothing on me." };
        tellOps(COLOR + enable_messages[(int) (Math.random() * enable_messages.length)], true);
    }

    @Override
    public void onDisable() {
        saveTheConfig(console, true);
        // done disabling
        String[] disable_messages =
                { "I hope you returned all your files, because we're closing the library for the day!", "This information kiosk is now closed.",
                        "All right. All done with work. Time to go home and read the Minecraft dictionary!" };
        tellOps(COLOR + disable_messages[(int) (Math.random() * disable_messages.length)], true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
        if (command.equalsIgnoreCase("ids") || command.equalsIgnoreCase("id")) {
            id(sender, parameters);
            return true;
        } else if (command.equalsIgnoreCase("recipe") || command.equalsIgnoreCase("craft")) {
            recipe(sender, parameters);
            return true;
        } else if ((command.equalsIgnoreCase("myPluginWiki") || command.equalsIgnoreCase("mPW")) && parameters.length >= 1 && parameters[0].toLowerCase().startsWith("update")) {
            if (sender instanceof Player && !sender.isOp())
                if (command.equalsIgnoreCase("myPluginWiki"))
                    sender.sendMessage(ChatColor.RED + "Sorry, but you can't use " + COLOR + "/myPluginWiki update" + ChatColor.RED + ".");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you can't use " + COLOR + "/mPW update" + ChatColor.RED + ".");
            else if (parameters.length == 1)
                checkForUpdates(sender);
            else if (parameters[1].equalsIgnoreCase("on"))
                if (auto_update)
                    sender.sendMessage(ChatColor.RED + "I'm already checking for myPluginWiki updates.");
                else {
                    auto_update = true;
                    sender.sendMessage(COLOR + "All right. I would be happy to check for updates for you.");
                }
            else if (parameters[1].equalsIgnoreCase("off"))
                if (!auto_update)
                    sender.sendMessage(ChatColor.RED
                            + "I'm already not checking for myPluginWiki updates...but I'm not sure why you did that to begin with. Don't you want to expand your server's wealth of knowledge?");
                else {
                    auto_update = false;
                    sender.sendMessage(COLOR + "Fine. I won't check for updates any more, but I would advise that you let me. You might miss out on new knowledge.");
                }
            else
                return false;
            return true;
        } else if ((command.equalsIgnoreCase("mPW") || command.equalsIgnoreCase("myPluginWiki")) && parameters.length == 1 && parameters[0].equalsIgnoreCase("save")) {
            if (!(sender instanceof Player) || sender.hasPermission("mypluginwiki.admin"))
                saveTheConfig(sender, true);
            else if (command.equalsIgnoreCase("myPluginWiki"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myPluginWiki save" + ChatColor.RED + ".");
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mPW save" + ChatColor.RED + ".");
            return true;
        } else if ((command.equalsIgnoreCase("mPW") || command.equalsIgnoreCase("myPluginWiki")) && parameters.length == 1 && parameters[0].equalsIgnoreCase("load")) {
            if (!(sender instanceof Player) || sender.hasPermission("mypluginwiki.admin"))
                loadTheConfig(sender);
            else if (command.equalsIgnoreCase("myPluginWiki"))
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/myPluginWiki load" + ChatColor.RED + ".");
            else
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + ChatColor.GREEN + "/mPW load" + ChatColor.RED + ".");
            return true;
        }
        return false;
    }

    // specific utils
    /** This method sends a given message to everyone who is currently debugging this plugin. Players and the console can enter debugging mode using <i>/mUW debug</i>.
     * 
     * @param message
     *            is the <tt>String</tt> that will be sent as a message to any users currently debugging this plugin. */
    public static void debug(String message) {
        if (debuggers.size() == 0)
            return;
        if (debuggers.contains("console")) {
            console.sendMessage(COLOR + message);
            if (debuggers.size() == 1)
                return;
        }
        for (Player player : server.getOnlinePlayers())
            if (debuggers.contains(player.getName()))
                player.sendMessage(COLOR + message);
    }

    // loading
    public void loadTheConfig(CommandSender sender) {
        // check the config file
        File config_file = new File(getDataFolder(), "config.txt");
        try {
            if (!config_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a config.txt file. I'll make a new one.");
                config_file.createNewFile();
                saveTheConfig(sender, false);
                return;
            }
            // read the config.txt file
            BufferedReader in = new BufferedReader(new FileReader(config_file));
            String save_line = in.readLine();
            while (save_line != null) {
                // skip empty lines
                while (save_line != null && save_line.trim().equals(""))
                    save_line = in.readLine();
                if (save_line == null)
                    break;
                save_line = save_line.trim();
                if (save_line.startsWith("Do you want myPluginWiki to check for updates every time it is enabled?"))
                    auto_update = readResponse(save_line.substring(71), in.readLine(), "Right now, myPluginWiki will auto-update.");
                save_line = in.readLine();
            }
            in.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "Oh, goodness me! An IOEcxeption in config.txt!");
            exception.printStackTrace();
        }
        saveTheConfig(sender, false);
        sender.sendMessage(COLOR + "Your configurations have been loaded.");
        if (sender instanceof Player)
            console.sendMessage(COLOR + sender.getName() + " loaded the myPluginWiki config from file.");
    }

    // saving
    public void saveTheConfig(CommandSender sender, boolean display_message) {
        File config_file = new File(getDataFolder(), "config.txt");
        // save the configurations
        try {
            if (!config_file.exists()) {
                getDataFolder().mkdir();
                sender.sendMessage(COLOR + "I couldn't find a config.txt file. I'll make a new one.");
                config_file.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(config_file));
            out.write("Do you want myPluginWiki to check for updates every time it is enabled? ");
            out.newLine();
            if (auto_update)
                out.write("   Right now, myPluginWiki will auto-update.");
            else
                out.write("   Right now, myPluginWiki will not auto-update! I REALLY think you should let it auto-update!");
            out.close();
        } catch (IOException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I got an IOException while trying to save your configurations.");
            exception.printStackTrace();
            return;
        }
        if (display_message) {
            sender.sendMessage(COLOR + "Your configurations have been saved.");
            if (sender instanceof Player)
                console.sendMessage(COLOR + ((Player) sender).getName() + " saved the server's configurations to file.");
        }
    }

    // plugin commands
    @SuppressWarnings("resource")
    private void checkForUpdates(CommandSender sender) {
        URL url = null;
        try {
            url = new URL("http://dev.bukkit.org/server-mods/realdrummers-mycorelibrary/files.rss/");
        } catch (MalformedURLException exception) {
            sender.sendMessage(ChatColor.DARK_RED + "I've never seen a U.R.L. like this in any of my readings!");
        }
        if (url != null) {
            String new_version_name = null, new_version_link = null;
            try {
                // Set header values intial to the empty string
                String title = "";
                String link = "";
                // First create a new XMLInputFactory
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                // Setup a new eventReader
                InputStream in = null;
                try {
                    in = url.openStream();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.DARK_RED + "Our Internet resources on BukkitDev are not working.");
                    return;
                }
                XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
                // Read the XML document
                while (eventReader.hasNext()) {
                    XMLEvent event = eventReader.nextEvent();
                    if (event.isStartElement()) {
                        if (event.asStartElement().getName().getLocalPart().equals("title")) {
                            event = eventReader.nextEvent();
                            title = event.asCharacters().getData();
                            continue;
                        }
                        if (event.asStartElement().getName().getLocalPart().equals("link")) {
                            event = eventReader.nextEvent();
                            link = event.asCharacters().getData();
                            continue;
                        }
                    } else if (event.isEndElement()) {
                        if (event.asEndElement().getName().getLocalPart().equals("item")) {
                            new_version_name = title;
                            new_version_link = link;
                            // All done, we don't need to know about older
                            // files.
                            break;
                        }
                    }
                }
            } catch (XMLStreamException exception) {
                err(mCL, "I'm afraid that we have encountered a knowledge-hating XMLStreamException.", exception);
                return;
            }
            boolean new_version_is_out = false;
            String version = getDescription().getVersion(), newest_online_version = "";
            if (new_version_name == null) {
                tellOps(ChatColor.DARK_RED + "Something seems to have gone awry while trying to retrieve the newest version of ", true);
                return;
            }
            if (new_version_name.split("v").length == 2) {
                newest_online_version = new_version_name.split("v")[new_version_name.split("v").length - 1].split(" ")[0];
                // get the newest file's version number
                if (!version.contains("-DEV") && !version.contains("-PRE") && !version.equalsIgnoreCase(newest_online_version))
                    try {
                        if (Double.parseDouble(version) < Double.parseDouble(newest_online_version))
                            new_version_is_out = true;
                    } catch (NumberFormatException exception) {
                        //
                    }
            } else
                sender.sendMessage(ChatColor.RED + "Oh, no! REALDrummer didn't properly catalog the newest version of myPluginWiki! Please tell him to fix it immediately!");
            if (new_version_is_out) {
                String fileLink = null;
                try {
                    // Open a connection to the page
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(new_version_link).openConnection().getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null)
                        // Search for the download link
                        if (line.contains("<li class=\"user-action user-action-download\">"))
                            // Get the raw link
                            fileLink = line.split("<a href=\"")[1].split("\">Download</a>")[0];
                    reader.close();
                    reader = null;
                } catch (Exception exception) {
                    sender.sendMessage(ChatColor.DARK_RED + "Our Internet resources on BukkitDev are not working.");
                    exception.printStackTrace();
                    return;
                }
                if (fileLink != null) {
                    if (!new File(this.getDataFolder(), "jar").exists()) {
                        BufferedInputStream in = null;
                        FileOutputStream fout = null;
                        try {
                            getDataFolder().mkdirs();
                            // download the file
                            url = new URL(fileLink);
                            in = new BufferedInputStream(url.openStream());
                            fout = new FileOutputStream(this.getDataFolder().getAbsolutePath() + "/jar");
                            byte[] data = new byte[1024];
                            int count;
                            while ((count = in.read(data, 0, 1024)) != -1)
                                fout.write(data, 0, count);
                            tellOps(COLOR + "" + ChatColor.UNDERLINE + "The myPluginWiki library has increased its stores! It's now at v" + newest_online_version
                                    + ". Please replace your old myPluginWiki with the new one in your data folder and we'll increase our stores of information!", true);
                        } catch (Exception ex) {
                            sender.sendMessage(ChatColor.DARK_RED + "Oh, no! It seems myPluginWiki v" + newest_online_version
                                    + " has been released, but I can't retrieve the new information stores from BukkitDev! I'm afraid you'll have to go get it yourself.");
                        } finally {
                            try {
                                if (in != null)
                                    in.close();
                                if (fout != null)
                                    fout.close();
                            } catch (Exception ex) {
                                //
                            }
                        }
                    } else
                        sender.sendMessage(ChatColor.RED
                                + "Hey, all those library books are still lying useless in your data folder! Please put your new myPluginWiki on the server so that we can expand our knowledge of Minecraft!");
                }
            } else
                sender.sendMessage(COLOR + "No new books have been added to the library that is ");
        }
    }

    private void recipe(CommandSender sender, String[] parameters) {
        // TODO: make no parameters return the recipe of what the sender is holding or pointing at

        // assemble the query
        String query = "";
        for (String parameter : parameters)
            query = query + parameter.toLowerCase() + " ";
        query = query.substring(0, query.length() - 1);
        // get the item's I.D. and data
        int id = -1, data = -1;
        try {
            id = Integer.parseInt(query);
        } catch (NumberFormatException exception) {
            if (query.split(":").length == 2) {
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
            } else {
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

    private void id(CommandSender sender, String[] parameters) {
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
