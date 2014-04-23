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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import REALDrummer.settings.mySetting;

import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.MessageUtilities.*;
import static REALDrummer.utils.StringUtilities.*;

public abstract class myPlugin extends JavaPlugin implements Inquirer, Listener, Comparable<myPlugin> {
    public static myList<myPlugin> enabled_myPlugins = new myList<myPlugin>();

    public ChatColor COLOR = ChatColor.GOLD;
    
    // TODO: take into account op_command in the commands
    
    private myList<mySetting> settings = new myList<mySetting>();
    private String abbreviation = null;
    private boolean auto_update = true;
    private myList<String> debuggers = new myList<String>();

    // standard plugin methods
    @Override
    public void onEnable() {
        // register this class as a Listener, an Inquirer, and an enabled myPlugin
        getServer().getPluginManager().registerEvents(this, this);
        myQuestion.inquirers.add(this);
        enabled_myPlugins.add(this);

        // construct the abbreviation of the plugin from the capital letters of the name
        abbreviation = "";
        for (char letter_in_name : getName().toCharArray())
            if (Character.isUpperCase(letter_in_name))
                abbreviation += letter_in_name;
        abbreviation = 'm' + abbreviation;

        // load all the data
        callPluginLoad(null, null);

        // call the individual enable method specified by the plugin's code
        String[] enable_messages = myEnable();

        // auto-update if this plugin is configured to do so
        if (auto_update)
            checkForUpdates(null);

        // display the enable message
        tellOps(COLOR + enable_messages[(int) (Math.random() * enable_messages.length)], true);
    }

    @Override
    public void onDisable() {
        // save all the data
        callPluginSave(null, null, true);

        // call the individual disable method specified by the plugin's code
        String[] disable_messages = myDisable();

        // unregister this plugin as an Inquirer and an enabled myPlugin
        myQuestion.inquirers.remove(this);
        enabled_myPlugins.remove(this);

        // display the disable message
        tellOps(COLOR + disable_messages[(int) (Math.random() * disable_messages.length)], true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
        // /[abbreviation/name] (...)
        if (command.equalsIgnoreCase(getName()) || command.equalsIgnoreCase(abbreviation))
            // /[abbreviaition/name]
            if (parameters.length == 0)
                sender.sendMessage(COLOR + "Hi! I'm " + getName() + "!\n\"" + getDescription().getDescription() + "\"\nI'm currently version " + getDescription().getVersion()
                        + ".");
            // /[abbreviaition/name] debug
            else if (parameters[0].equalsIgnoreCase("debug"))
                if (isAdmin(sender)) {
                    String sender_name = sender instanceof ConsoleCommandSender ? "\\console" : ((Player) sender).getName();
                    if (debuggers.contains(sender_name)) {
                        sender.sendMessage(COLOR + "Bugs exterminated!");
                        debuggers.remove(sender_name);
                    } else {
                        sender.sendMessage(COLOR + "Let's squash some bugs!");
                        debuggers.add(sender_name);
                    }
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to debug " + getName() + ".");
            // TODO TEST
            // /[abbreviaition/name] disable
            else if (parameters[0].equalsIgnoreCase("disable"))
                if (isAdmin(sender))
                    if (isEnabled()) {
                        sender.sendMessage(COLOR + getName() + " has been disabled.");
                        tellOps(COLOR + (sender instanceof Player ? sender.getName() : "Someone on the console") + " disabled " + getName() + ".");
                        getServer().getPluginManager().disablePlugin(this);
                    } else
                        sender.sendMessage(ChatColor.RED + getName() + " is already disabled.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to disable " + getName() + ".");
            // TODO TEST
            // /[abbreviaition/name] enable
            else if (parameters[0].equalsIgnoreCase("enable"))
                if (isAdmin(sender))
                    if (!isEnabled()) {
                        getServer().getPluginManager().enablePlugin(this);
                        sender.sendMessage(COLOR + getName() + " has been enabled.");
                        tellOps(COLOR + (sender instanceof Player ? sender.getName() : "Someone on the console") + " enabled " + getName() + ".");
                    } else
                        sender.sendMessage(ChatColor.RED + getName() + " is already enabled.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you're not allowed to enable " + getName() + ".");
            // /[abbreviaition/name] load
            else if (parameters[0].equalsIgnoreCase("load"))
                if (isAdmin(sender)) {
                    // find the data type specified (if there was one)
                    String data_type = null;
                    if (parameters.length > 1)
                        if (!parameters[1].equalsIgnoreCase("the"))
                            data_type = parameters[1];
                        else if (parameters[1].equalsIgnoreCase("the") && parameters.length > 2)
                            data_type = parameters[2];

                    // start loading with the standard config file loading method
                    if (data_type == null || data_type.toLowerCase().startsWith("config"))
                        loadTheConfig(sender);

                    // call the plugin's load method(s) and if the load failed, send the command sender a message saying so
                    if (!callPluginLoad(sender, data_type))
                        sender.sendMessage(ChatColor.RED + "I'm not sure what \"" + data_type + "\" means.");
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to force " + getName() + " to load its data.");
            // /[abbreviaition/name] save
            else if (parameters[0].equalsIgnoreCase("save"))
                if (isAdmin(sender)) {
                    // find the data type specified (if there was one)
                    String data_type = null;
                    if (parameters.length > 1)
                        if (!parameters[1].equalsIgnoreCase("the"))
                            data_type = parameters[1];
                        else if (parameters[1].equalsIgnoreCase("the") && parameters.length > 2)
                            data_type = parameters[2];

                    // start saving with the standard config file loading method
                    if (data_type == null || data_type.toLowerCase().startsWith("config"))
                        saveTheConfig(sender);

                    // call the plugin's load method(s) and if the save failed, send the command sender a message saying so
                    if (!callPluginSave(sender, data_type, true))
                        sender.sendMessage(ChatColor.RED + "I'm not sure what \"" + data_type + "\" means.");
                } else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to force " + getName() + " to save its data.");
            // /[abbreviaition/name] update(r)
            else if (parameters[0].equalsIgnoreCase("update") || parameters[0].equalsIgnoreCase("updater"))
                if (isAdmin(sender))
                    if (parameters.length == 1 || !parameters[2].equalsIgnoreCase("off") && !parameters[2].equalsIgnoreCase("on"))
                        if (parameters[0].equalsIgnoreCase("update"))
                            checkForUpdates(sender);
                        else if (auto_update)
                            sender.sendMessage(COLOR + "The " + getName() + " auto-updater is currently on, so I will check for " + getName()
                                    + " updates every time I'm enabled.\n" + "You can also use " + ChatColor.ITALIC + "/" + abbreviation + " update" + COLOR
                                    + " to check for updates whenever you like.\n" + "Use " + ChatColor.ITALIC + "/" + abbreviation + " updater off" + COLOR
                                    + " if you do not wish to check for updates automatically.");
                        else
                            sender.sendMessage(COLOR + "The " + getName() + " auto-updater is currently off, but you can use " + ChatColor.ITALIC + "/" + abbreviation
                                    + " update" + COLOR + " whenever you like to check for updates.\n" + "Use " + ChatColor.ITALIC + "/" + abbreviation + " updater on"
                                    + COLOR + " if you want to automatically check for updates whenever this plugin is enabled (highly recommended!).");
                    else if (parameters[2].equalsIgnoreCase("off"))
                        if (auto_update) {
                            auto_update = false;
                            sender.sendMessage(COLOR + "The " + getName() + " auto-updater has been disabled.\n"
                                    + "I highly suggest that you reconsider disabling it, though! You could miss out on cool updates!");
                            tellOps(COLOR + (sender instanceof Player ? sender.getName() : "Someone on the console") + " disabled the " + getName() + " auto-updater.",
                                    sender instanceof Player, sender.getName());
                        } else
                            sender.sendMessage(ChatColor.RED + "The " + getName() + " auto-updater is already disabled.");
                    else if (!auto_update) {
                        auto_update = true;
                        sender.sendMessage(COLOR + "The " + getName() + " auto-updater has been enabled!\n" + "You will now automatically check for and receive updates to "
                                + getName() + " as soon as they come out.");
                        tellOps(COLOR + (sender instanceof Player ? sender.getName() : "Someone on the console") + " enabled the " + getName() + " auto-updater.",
                                sender instanceof Player, sender.getName());
                    } else
                        sender.sendMessage(ChatColor.RED + "The " + getName() + " auto-updater is already enabled.");
                else
                    sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to control the " + getName() + " updater.");
            // /[abbreviaition/name] help
            else if (parameters[0].equalsIgnoreCase("help") || parameters[0].equalsIgnoreCase("?"))
                myCoreLibrary.help(sender, getName());
            // /[abbreviaition/name] [????]
            else
                onCommand(sender, cmd, parameters[0], subArray(parameters, 1));
        // /[????]
        // TODO TEST: confirm that commands without aliases still have the primary alias of the command at aliases.get(0)
        else {
            /* check for permission first; assume that user permission is sufficient for now and whether or not user permission truly is sufficient will be determined later
             * inside the callPluginCommand method */
            if (!isAdmin(sender) && !sender.hasPermission(getName().toLowerCase() + ".user") && !sender.hasPermission(getName().toLowerCase() + "." + command.toLowerCase())) {
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + ChatColor.ITALIC + "/" + command.toLowerCase() + ChatColor.RED
                        + ".");
                return true;
            }

            // call the command method
            if (!callPluginCommand(command, sender, readParameters(cmd.getUsage(), parameters), cmd.getAliases().get(0)))
                err(this, "I couldn't find the plugin command method that corresponds to /" + command + "!", "unexecutable command", "sender=\"" + sender + "\"",
                        "parameters=" + writeArray(parameters, ", ", "and", "\"", "\""));
        }
        return true;
    }

    // utility methods
    /** This method sends a given message to everyone who is currently debugging this plugin. Players and the console can enter debugging mode using <i>/mUW debug</i>.
     * 
     * @param message
     *            is the <tt>String</tt> that will be sent as a message to any users currently debugging this plugin. */
    public void debug(String message) {
        if (debuggers.size() == 0)
            return;
        if (debuggers.contains("\\console")) {
            getServer().getConsoleSender().sendMessage(COLOR + message);
            if (debuggers.size() == 1)
                return;
        }
        for (Player player : getServer().getOnlinePlayers())
            if (debuggers.contains(player.getName()))
                player.sendMessage(COLOR + message);
    }

    public boolean isAdmin(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(getName().toLowerCase() + ".admin");
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    // command methods

    public boolean callPluginCommand(String command, CommandSender sender, HashMap<String, String> parameters, String primary_alias) {
        debug("attempting to call /" + command + "(/" + primary_alias + ") in " + getName() + "...");

        for (Method method : getClass().getMethods()) {
            // if the method doesn't have the same name as this command, ignore it
            if (!method.getName().equalsIgnoreCase(primary_alias))
                continue;

            debug("possible match: " + method.getName() + "; checking for Commander annotation...");
            // find the command method's Commander annotation
            Commander command_settings = null;
            for (Annotation annotation : method.getAnnotations())
                if (annotation.getClass().getName().equals("Commander")) {
                    command_settings = (Commander) annotation;
                    break;
                }
            if (command_settings == null) {
                debug("no Commander annotation found; continuing...");
                continue;
            }

            debug("found command method " + method.getName() + "(); checking command settings...");
            // check the command's settings
            if (sender instanceof ConsoleCommandSender && !command_settings.console_command()) {
                sender.sendMessage(ChatColor.RED + "You're a console! You can't use " + COLOR + ChatColor.ITALIC + "/" + command + ChatColor.RED + "!");
                return true;
            } else if (!isAdmin(sender) && !sender.hasPermission(getName().toLowerCase() + "." + command.toLowerCase()) && !command_settings.user_command()) {
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + COLOR + ChatColor.ITALIC + "/" + command.toLowerCase() + ChatColor.RED
                        + ".");
                return true;
            }

            // call the method
            try {
                method.invoke(sender, parameters);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                err(this, "There was a problem trying to invoke the command method in " + getName() + ".", exception, method, "command=\"" + command + "\"");
            }
            return true;
        }

        return false;
    }

    // TODO: remind users to make loadTheConfigurations() methods instead of loadTheConfig()
    public boolean callPluginLoad(CommandSender sender, String data_type) {
        debug("attempting to load \"" + data_type + "\"...");

        for (Method method : getClass().getMethods()) {
            // if the method doesn't start with the word "load", ignore it
            if (!method.getName().toLowerCase().startsWith("load"))
                continue;

            debug("possible match: " + method.getName() + "; checking for Loader annotation...");
            // find the load method's Loader annotation
            Loader load_settings = null;
            for (Annotation annotation : method.getAnnotations())
                if (annotation.getClass().getName().equals("Commander")) {
                    load_settings = (Loader) annotation;
                    break;
                }
            if (load_settings == null) {
                debug("no Loader annotation found; continuing...");
                continue;
            }

            debug("found load method " + method.getName() + "(); calling...");

            if (data_type == null || load_settings.data_type().startsWith(data_type.toLowerCase()))
                try {
                    // find the file
                    File file = new File(getDataFolder(), data_type + "s.txt");
                    if (!file.exists()) {
                        if (sender == null)
                            debug("no " + load_settings.data_type() + " file found; cancelling load...");
                        else
                            sender.sendMessage(ChatColor.RED + "I'm sorry, but there is no " + data_type + ".txt to load in your " + getName() + " data folder.");
                        return true;
                    }

                    // read the first entry as the auto-update setting if the config is being loaded
                    BufferedReader in = new BufferedReader(new FileReader(file));

                    // call the method
                    Object raw_result = method.invoke(in);
                    in.close();

                    // cast the result as a LoadSaveResult (as it should be)
                    LoadSaveResult result;
                    try {
                        result = (LoadSaveResult) raw_result;
                    } catch (ClassCastException exception) {
                        err(this, "This load method " + method.getName() + "() didn't return a LoadSaveResult!", exception, raw_result);
                        return true;
                    }

                    /* if the LoadSaveResult returned was null or the results indicate that nothign was loaded (settings==false and objects<0), something went wrong, so do not
                     * send a confirmation message; assume the load method itself sent an error message already */
                    if (result == null || result.objects < 0 && !result.settings) {
                        debug("returned LoadSaveResult inconclusive; cancelling load confirmation message...");
                        return true;
                    }

                    // save to the loaded file to update/reformat the file
                    debug("loading complete; using save to update data file...");
                    if (!callPluginSave(sender, load_settings.data_type(), false)) {
                        err(this, "I couldn't save the recently loaded " + data_type + " data.", "save call issue");
                        return false;
                    }

                    debug("load successful; sending load confirmation message...");
                    // send confirmation messages
                    if (sender == null)
                        if (result.objects > 1)
                            if (!result.settings)
                                tellOps(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s have been loaded.", true);
                            else
                                tellOps(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s and your " + load_settings.data_type()
                                        + " settings have been loaded.", true);
                        else if (result.objects == 1)
                            if (!result.settings)
                                tellOps(COLOR + "Your 1 " + load_settings.data_type() + " has been loaded.", true);
                            else
                                tellOps(COLOR + "Your 1 " + load_settings.data_type() + " and your " + load_settings.data_type() + " settings have been loaded.", true);
                        else if (result.objects == 0)
                            if (!result.settings)
                                tellOps(COLOR + "You have no " + load_settings.data_type() + "s to load!", true);
                            else
                                tellOps(COLOR + "Your " + load_settings.data_type() + " settings have been loaded, but I could find no " + load_settings.data_type()
                                        + "s to load.", true);
                        // the possibility of result.objects < 0 && !result.settings was eliminated just before the "send confirmation messages" block
                        else
                            tellOps(COLOR + "Your " + load_settings.data_type() + " settings have been loaded.", true);
                    else {
                        String sender_name = "Someone on the console";
                        if (sender instanceof Player)
                            sender_name = ((Player) sender).getName();
                        if (result.objects > 1)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s have been loaded.");
                                tellOps(COLOR + sender_name + " loaded " + result.objects + " " + load_settings.data_type() + "s to file.", sender instanceof Player, sender
                                        .getName());
                            } else {
                                sender.sendMessage(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s and your " + load_settings.data_type()
                                        + " have been loaded.");
                                tellOps(COLOR + sender_name + " loaded " + result.objects + " " + load_settings.data_type() + "s to file.", sender instanceof Player, sender
                                        .getName());
                            }
                        else if (result.objects == 1)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "Your 1 " + load_settings.data_type() + " has been loaded.");
                                tellOps(COLOR + sender_name + " loaded the server's 1 " + load_settings.data_type() + " to file.", sender instanceof Player, sender.getName());
                            } else {
                                sender.sendMessage(COLOR + "Your 1 " + load_settings.data_type() + " and your " + load_settings.data_type() + " settings have been loaded.");
                                tellOps(COLOR + sender_name + " loaded the server's 1 " + load_settings.data_type() + " to file.", sender instanceof Player, sender.getName());
                            }
                        else if (result.objects == 0)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "You have no " + load_settings.data_type() + "s to load!");
                                tellOps(COLOR + sender_name + " tried to load the server's " + load_settings.data_type() + "s to file, but there were no "
                                        + load_settings.data_type() + "s on the server to load.", sender instanceof Player, sender.getName());
                            } else {
                                sender.sendMessage(COLOR + "Your " + load_settings.data_type() + " settings have been loaded, but I could find no "
                                        + load_settings.data_type() + "s to load.");
                                tellOps(COLOR + sender_name + " loaded your " + load_settings.data_type() + " settings, but I could find no " + load_settings.data_type()
                                        + "s to load.", sender instanceof Player, sender.getName());
                            }
                        // the possibility of result.objects < 0 && !result.settings was eliminated just before the "send confirmation messages" block
                        else {
                            sender.sendMessage(COLOR + "Your " + load_settings.data_type() + " settings have been loaded.");
                            tellOps(COLOR + sender_name + " loaded your " + load_settings.data_type() + " settings.", sender instanceof Player, sender.getName());
                        }
                    }

                    // if data_type is not null, we've found the specified method and we're all done; otherwise, keep searching for more loading methods
                    if (data_type != null) {
                        debug("specific data type found; ending load call...");
                        return true;
                    } else
                        debug("continuing search for additional load methods...");
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                    err(this, "There was a problem trying to invoke the loading methods in " + getName() + ".", exception, method, "data_type=\"" + data_type + "\"");
                    return true;
                } catch (IOException exception) {
                    err(this, "There was a problem reading the " + (data_type != null ? data_type : "data") + " for " + getName() + ".", exception, method);
                    return true;
                }
        }

        // if the loop finishes with no return and a data_type was specified, then the load method for that data type most not have been found, so return false
        return data_type == null;
    }

    public boolean callPluginSave(CommandSender sender, String data_type, boolean display_message) {
        debug("attempting to save \"" + data_type + "\"...");

        for (Method method : getClass().getMethods()) {
            if (!method.getName().toLowerCase().startsWith("save"))
                continue;

            debug("found save method " + method.getName() + "(); calling...");

            /* this acquires the name of the object being saveed; the first argument of the substring removes "save" and/or "the"; the second eliminates the "s" to make the
             * object type singular, e.g. "saveEpithets" --> "epithet" or "saveTheEpithets" --> "epithet" */
            String load_settings.data_type() = method.getName().toLowerCase().substring(method.getName().toLowerCase().startsWith("savethe") ? 7 : 4, method.getName().length() - 1);

            if (data_type == null || load_settings.data_type().startsWith(data_type.toLowerCase()))
                try {
                    // find or create the file
                    File file = new File(getDataFolder(), data_type + "s.txt");
                    if (!file.exists()) {
                        debug("no " + data_type + ".txt file found; creating new file...");
                        getDataFolder().mkdir();
                        file.createNewFile();
                    }

                    // write the first entry as the auto-update setting if the data type is the config
                    BufferedWriter out = new BufferedWriter(new FileWriter(file));
                    // start the file with a reminder to use /[plugin name] load [data_type]
                    out.write("Remember to use \"/" + abbreviation + " load " + load_settings.data_type() + "\" to load your changes to this file to the server when you're done!");
                    out.newLine();
                    // TODO: remove after making a standard config-loading function
                    // if the file is the configuration file, follow the command reminder with the auto-update configuration question
                    if (load_settings.data_type().equalsIgnoreCase("configuration")) {
                        out.write("Do you want " + getName() + " to check for updates every time is enabled? ");
                        if (auto_update)
                            out.write("   Right now, " + getName() + " will auto-update.");
                        else
                            out.write("   Right now, " + getName() + " will not auto-update! I REALLY think you should let it auto-update!");
                    }

                    // call the method
                    Object raw_result = method.invoke(out, display_message);
                    out.close();

                    // cast the result as a LoadSaveResult (as it should be)
                    LoadSaveResult result;
                    try {
                        result = (LoadSaveResult) raw_result;
                    } catch (ClassCastException exception) {
                        err(this, "This load method " + method.getName() + "() didn't return a LoadSaveResult!", exception, raw_result);
                        return true;
                    }

                    /* if the LoadSaveResult returned was null or the results indicate that nothign was saved (settings==false and objects<0), something went wrong, so do not
                     * send a confirmation message; assume the save method itself sent an error message already */
                    if (result == null || result.objects < 0 && !result.settings) {
                        debug("returned LoadSaveResult inconclusive; cancelling save confirmation message...");
                        return true;
                    }

                    debug("save successful; sending save confirmation message...");
                    // send confirmation messages
                    if (sender == null)
                        if (result.objects > 1)
                            if (!result.settings)
                                tellOps(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s have been saved.", true);
                            else
                                tellOps(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s and your " + load_settings.data_type() + " settings have been saved.", true);
                        else if (result.objects == 1)
                            if (!result.settings)
                                tellOps(COLOR + "Your 1 " + load_settings.data_type() + " has been saved.", true);
                            else
                                tellOps(COLOR + "Your 1 " + load_settings.data_type() + " and your " + load_settings.data_type() + " settings have been saved.", true);
                        else if (result.objects == 0)
                            if (!result.settings)
                                tellOps(COLOR + "You have no " + load_settings.data_type() + "s to save!", true);
                            else
                                tellOps(COLOR + "Your " + load_settings.data_type() + " settings have been saved, but I could find no " + load_settings.data_type() + "s to save.", true);
                        // the possibility of result.objects < 0 && !result.settings was eliminated just before the "send confirmation messages" block
                        else
                            tellOps(COLOR + "Your " + load_settings.data_type() + " settings have been saved.", true);
                    else {
                        String sender_name = "Someone on the console";
                        if (sender instanceof Player)
                            sender_name = ((Player) sender).getName();
                        if (result.objects > 1)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s have been saved.");
                                tellOps(COLOR + sender_name + " saved " + result.objects + " " + load_settings.data_type() + "s to file.", sender instanceof Player, sender.getName());
                            } else {
                                sender.sendMessage(COLOR + "Your " + result.objects + " " + load_settings.data_type() + "s and your " + load_settings.data_type() + " have been saved.");
                                tellOps(COLOR + sender_name + " saved " + result.objects + " " + load_settings.data_type() + "s to file.", sender instanceof Player, sender.getName());
                            }
                        else if (result.objects == 1)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "Your 1 " + load_settings.data_type() + " has been saved.");
                                tellOps(COLOR + sender_name + " saved the server's 1 " + load_settings.data_type() + " to file.", sender instanceof Player, sender.getName());
                            } else {
                                sender.sendMessage(COLOR + "Your 1 " + load_settings.data_type() + " and your " + load_settings.data_type() + " settings have been saved.");
                                tellOps(COLOR + sender_name + " saved the server's 1 " + load_settings.data_type() + " to file.", sender instanceof Player, sender.getName());
                            }
                        else if (result.objects == 0)
                            if (!result.settings) {
                                sender.sendMessage(COLOR + "You have no " + load_settings.data_type() + "s to save!");
                                tellOps(COLOR + sender_name + " tried to save the server's " + load_settings.data_type() + "s to file, but there were no " + load_settings.data_type()
                                        + "s on the server to save.", sender instanceof Player, sender.getName());
                            } else {
                                sender.sendMessage(COLOR + "Your " + load_settings.data_type() + " settings have been saved, but I could find no " + load_settings.data_type() + "s to save.");
                                tellOps(COLOR + sender_name + " saved your " + load_settings.data_type() + " settings, but I could find no " + load_settings.data_type() + "s to save.",
                                        sender instanceof Player, sender.getName());
                            }
                        // the possibility of result.objects < 0 && !result.settings was eliminated just before the "send confirmation messages" block
                        else {
                            sender.sendMessage(COLOR + "Your " + load_settings.data_type() + " settings have been saved.");
                            tellOps(COLOR + sender_name + " saved your " + load_settings.data_type() + " settings.", sender instanceof Player, sender.getName());
                        }
                    }

                    // if data_type is not null, we've found the specified method and we're all done; otherwise, keep searching for more saving methods
                    if (data_type != null) {
                        debug("specific data type found; ending save call...");
                        return true;
                    } else
                        debug("continuing search for additional save methods...");
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                    err(this, "There was a problem trying to invoke the saving methods in " + getName() + ".", exception, method, "data_type=\"" + data_type + "\"");
                    return true;
                } catch (IOException exception) {
                    err(this, "There was a problem saving the " + (data_type != null ? data_type : "data") + " for " + getName() + ".", exception, method);
                    return true;
                }
        }

        // if the loop finishes with no return and a data_type was specified, then the save method for that data type most not have been found, so return false
        return data_type == null;
    }

    @SuppressWarnings("resource")
    // TODO: compensate for the possibility that sender == null
    private void checkForUpdates(CommandSender sender) {
        URL url = null;
        try {
            url = new URL("http://dev.bukkit.org/server-mods/realdrummers-" + getName().toLowerCase() + "/files.rss/");
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
                            // All done, we don't need to know about older files.
                            break;
                        }
                    }
                }
            } catch (XMLStreamException exception) {
                err(myCoreLibrary.mCL, "I'm afraid that we have encountered a knowledge-hating XMLStreamException.", exception);
                return;
            }
            boolean new_version_is_out = false;
            String version = getDescription().getVersion(), newest_online_version = "";
            if (new_version_name == null) {
                tellOps(ChatColor.DARK_RED + "Something seems to have gone awry while trying to retrieve the newest version of " + getName() + ".", true);
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
                sender.sendMessage(ChatColor.RED + "Oh, no! REALDrummer didn't properly catalog the newest version of " + getName()
                        + "! Please tell him to fix it immediately!");
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
                    if (!new File(getDataFolder(), "jar").exists()) {
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
                            tellOps(COLOR + "" + ChatColor.UNDERLINE + getName() + " v" + newest_online_version + " has been released! Please replace your old " + getName()
                                    + " with the new one in your data folder and we'll increase our stores of information!", true);
                        } catch (Exception ex) {
                            sender.sendMessage(ChatColor.DARK_RED + "Oh, no! It seems " + getName() + " v" + newest_online_version
                                    + " has been released, but I can't retrieve the new version from BukkitDev! I'm afraid you'll have to go get it yourself.");
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
                        sender.sendMessage(ChatColor.RED + "Hey, that brand new " + getName() + " is still lying useless in your data folder! Please put your new "
                                + getName() + " on the server so you can experience the amazing new updates!");
                }
            } else
                sender.sendMessage(COLOR + "There are no new versions of " + getName() + ".");
        }
    }

    // plugin-specific utilities
    public mySetting getSetting(String target, String key) {
        
    }

    // object standard methods
    @Override
    public int compareTo(myPlugin plugin) {
        return getName().compareTo(plugin.getName());
    }

    @Override
    public String toString() {
        return getName();
    }

    // abstract/placeholder methods
    @Override
    public void questionAnswered(myQuestion question, String answer_message, boolean answer) {
        // by default, this method should do absolutely nothing
    }

    @Override
    public void questionCancelled(myQuestion question) {
        // by default, this method should do absolutely nothing
    }

    public abstract String[] myEnable();

    public abstract String[] myDisable();
}
