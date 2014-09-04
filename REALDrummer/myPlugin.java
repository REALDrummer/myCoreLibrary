package REALDrummer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import REALDrummer.flags.myFlag;
import REALDrummer.interfaces.Commander;
import REALDrummer.interfaces.Inquirer;
import REALDrummer.interfaces.Matchable;
import REALDrummer.settings.*;
import static REALDrummer.utils.ListUtilities.*;
import static REALDrummer.utils.ColorUtilities.decolor;
import static REALDrummer.utils.StringUtilities.*;

/** This abstract class is meant to be the parent of the main class of every other plugin. It extends {@link JavaPlugin Bukkit's JavaPlugin class} and implements
 * {@link Inquirer}, {@link Listener}, and {@link Comparable}, making it a master of handling {@link myQuestion myQuestions}, {@link org.bukkit.event.Event Bukkit's Events},
 * and self-sorting in myList-based structures. It also comes with a number of utilities, especially message utilities like {@link #debug(String) debug},
 * {@link #err(String, Throwable, Object...) err}, and {@link #tellOps(String, String...) tellOps}.
 * 
 * @author connor */
public abstract class myPlugin extends JavaPlugin implements Inquirer, Listener, Comparable<myPlugin>, Matchable {
    /** This {@link HashMap}<{@link UUID}, <tt>ArrayList&lt;String></tt>> keeps track of all of the notices for any given player. Notices are messages that a plugin was unable
     * to send to a certain player because that player was offline, but are so important that the player must be given the message the next time they log in. */
    public static final HashMap<UUID, ArrayList<String>> notices = new HashMap<UUID, ArrayList<String>>();

    /** This <tt>String</tt> represents the {@link myPlugin}'s abbreviated name. Its value is initialized to be a lowercase "m" (to represent the "my" at the beginning of the
     * name) followed by all of the capitalized letters in the plugin's name. For example, <u>m</u>y<u>U</u>ltra<u>W</u>arps's abbreviation is "mUW". This abbreviation can be
     * changed only by the {@link myPlugin}'s main class, but should generally be left as is. */
    protected String ABBREVIATION = 'm' + getName().replaceAll("[a-z0-9]", "");

    /** This {@link myList}<<tt>String</tt>> keeps track of the players (or {@link ConsoleCommandSender console}, indicated by "\console") who are currently in debugging mode. */
    public myList<UUID> debuggers = new myList<UUID>();

    private myList<mySetting> settings = new myList<mySetting>();

    // TODO: account for UUID changes

    // standard plugin methods
    @Override
    public void onEnable() {
        try {
            // TODO TEMP
            debuggers.add((UUID) null);

            debug("\n" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + getName() + " ENABLE CALLED");

            // load all the command usages from the @Commander annotations
            for (Method method : getClass().getDeclaredMethods()) {
                Commander command_settings = method.getAnnotation(Commander.class);
                if (command_settings != null) {
                    debug("found command method \"" + method.getName() + "()\"; processing usage...");
                    myFlag.readUsage(this, method.getName(), command_settings.usage());
                }
            }
            debug("processed command usages");

            // register this class as a Listener
            getServer().getPluginManager().registerEvents(this, this);
            debug("registered main class as Listener");

            // load all the data
            for (Class<? extends myData> data_type : myData.data_types)
                loadData(null, data_type);
            debug("loaded data");
            loadTheConfig(null);
            debug("loaded config");

            // call the individual enable method specified by the plugin's code
            String[] enable_messages = myEnable();

            // auto-update if this plugin is configured to do so
            if (getSetting("auto-update").booleanValue())
                checkForUpdates(null);

            // TODO TEMP
            debug("usage-parsed flags:");
            for (myFlag.myFlagOrder order : myFlag.flags)
                debug(order.toString());

            // display the enable message
            tellOps(getColor() + enable_messages[(int) (Math.random() * enable_messages.length)], true);
        } catch (Exception exception) {
            err("There was a problem enabling " + getName() + "!", exception);
        }
    }

    @Override
    public void onDisable() {
        try {
            debug("\n" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + getName() + " DISABLE CALLED");

            // save all the data
            for (Class<? extends myData> data_type : myData.data_types)
                saveData(null, data_type);
            saveTheConfig(null);

            // call the individual disable method specified by the plugin's code
            String[] disable_messages = myDisable();

            // display the disable message
            tellOps(disable_messages[(int) (Math.random() * disable_messages.length)], true);
        } catch (Exception exception) {
            err("There was a problem disabling " + getName() + "!", exception);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] parameters) {
        try {
            debug("\n" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + getName(sender) + " ISSUED /" + command + " " + writeArray(parameters, " ", ""));

            // remove the plugin's name from the labels on commands that directly refer to this plugin with a colon (":")
            if (command.contains(":"))
                command = command.substring(command.indexOf(":") + 1);

            // TODO: use tellOps to inform other ops of what's happening when these commands are used
            // /[abbreviation/name] (...)
            if (command.equalsIgnoreCase(getName()) || command.equalsIgnoreCase(getAbbreviation()))
                // /[abbreviaition/name]
                if (parameters.length == 0)
                    sender.sendMessage(getColor() + "Hi! I'm " + getName() + "!\n\"" + getDescription().getDescription().replaceAll("\n", "") + "\"\nI'm currently version "
                            + getVersion() + ".\nUse " + getColor() + ChatColor.ITALIC + "/" + ABBREVIATION + " help" + getColor() + " for more information.");
                // /[abbreviaition/name] debug
                else if (parameters[0].equalsIgnoreCase("debug"))
                    if (isAdmin(sender)) {
                        if (debuggers.contains(getUUID(sender))) {
                            sender.sendMessage(getColor() + "Bugs exterminated!");
                            debuggers.remove(getUUID(sender));
                        } else {
                            sender.sendMessage(getColor() + "Let's squash some bugs!");
                            debuggers.add(getUUID(sender));
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to debug " + getName() + ".");
                // TODO TEST
                // /[abbreviaition/name] disable
                else if (parameters[0].equalsIgnoreCase("disable"))
                    if (isAdmin(sender))
                        if (isEnabled()) {
                            sender.sendMessage(getColor() + getName() + " has been disabled.");
                            tellOps(getName(sender, "Someone on the console") + " disabled " + getName() + ".");
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
                            sender.sendMessage(getColor() + getName() + " has been enabled.");
                            tellOps(getName(sender, "Someone on the console") + " enabled " + getName() + ".");
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
                        if (data_type == null || data_type.toLowerCase().startsWith("conf"))
                            loadTheConfig(sender);

                        // if no data_type was given, load all the myData types
                        if (data_type == null)
                            for (Class<? extends myData> data : myData.data_types)
                                loadData(sender, data);
                        // otherwise, call the appropriate myData load method
                        else {
                            Boolean result = loadData(sender, data_type);

                            if (result == null)
                                sender.sendMessage(ChatColor.RED + "I'm not sure what \"" + data_type + "\" means.");
                            else if (!result)
                                sender.sendMessage(ChatColor.DARK_RED + "There was a problem loading the " + data_type + ".");
                            else
                                debug("loaded \"" + data_type + "\" successfully");
                        }
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

                        // start saving with the standard config file saving method
                        if (data_type == null || data_type.toLowerCase().startsWith("conf"))
                            saveTheConfig(sender);

                        // if no data_type was given, save all the myData types
                        if (data_type == null)
                            for (Class<? extends myData> data : myData.data_types)
                                saveData(sender, data);
                        // otherwise, call the appropriate myData save method
                        else {
                            Boolean result = saveData(sender, data_type);

                            if (result == null)
                                sender.sendMessage(ChatColor.RED + "I'm not sure what \"" + data_type + "\" means.");
                            else if (!result)
                                sender.sendMessage(ChatColor.DARK_RED + "There was a problem saving the " + data_type + ".");
                            else
                                debug("saved \"" + data_type + "\" successfully");
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to force " + getName() + " to save its data.");
                // /[abbreviaition/name] update(r)
                else if (parameters[0].equalsIgnoreCase("update") || parameters[0].equalsIgnoreCase("updater"))
                    if (isAdmin(sender)) {
                        myBSetting auto_update = (myBSetting) getSetting("auto-update");
                        if (parameters.length == 1 || !parameters[2].equalsIgnoreCase("off") && !parameters[2].equalsIgnoreCase("on"))
                            if (parameters[0].equalsIgnoreCase("update"))
                                checkForUpdates(sender);
                            else if (auto_update.getValue())
                                sender.sendMessage(getColor() + "The " + getName() + " auto-updater is currently on, so I will check for " + getName()
                                        + " updates every time I'm enabled.\n" + "You can also use " + ChatColor.ITALIC + "/" + ABBREVIATION + " update" + getColor()
                                        + " to check for updates whenever you like.\n" + "Use " + ChatColor.ITALIC + "/" + ABBREVIATION + " updater off" + getColor()
                                        + " if you do not wish to check for updates automatically.");
                            else
                                sender.sendMessage(getColor() + "The " + getName() + " auto-updater is currently off, but you can use " + ChatColor.ITALIC + "/"
                                        + ABBREVIATION + " update" + getColor() + " whenever you like to check for updates.\n" + "Use " + ChatColor.ITALIC + "/"
                                        + ABBREVIATION + " updater on" + getColor()
                                        + " if you want to automatically check for updates whenever this plugin is enabled (highly recommended!).");
                        else if (parameters[2].equalsIgnoreCase("off"))
                            if (auto_update.getValue()) {
                                auto_update.setValue(false);
                                sender.sendMessage(getColor() + "The " + getName() + " auto-updater has been disabled.\n"
                                        + "I highly suggest that you reconsider disabling it, though! You could miss out on cool updates!");
                                tellOps(getName(sender, "Someone on the console") + " disabled the " + getName() + " auto-updater.", sender instanceof Player, sender
                                        .getName());
                            } else
                                sender.sendMessage(ChatColor.RED + "The " + getName() + " auto-updater is already disabled.\nI still recommend that you re-enable it....");
                        else if (!auto_update.getValue()) {
                            auto_update.setValue(true);
                            sender.sendMessage(getColor() + "The " + getName() + " auto-updater has been enabled!\n" + capitalize(getName())
                                    + " will now automatically check for and receive updates as soon as they come out.");
                            tellOps(getName(sender, "Someone on the console") + " enabled the " + getName() + " auto-updater.", sender instanceof Player, sender.getName());
                        } else
                            sender.sendMessage(ChatColor.RED + "The " + getName() + " auto-updater is already enabled.");
                    } else
                        sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to control the " + getName() + " updater.");
                // /[abbreviation/name] config
                else if (parameters[0].toLowerCase().startsWith("config")) {
                    if (isAdmin(sender)) {
                        if (parameters.length == 1) {
                            // if no additional parameters are given, just list all the available configurations
                            String last_target = null;
                            for (mySetting setting : settings) /* note that mySettings are set up specifically so that when they sort, they will sort themselves automatically
                                                                * by target */{
                                // if the target changed, write a line indicating the beginning of a new data section
                                if (last_target == null && setting.getTarget() != null || last_target != null && !last_target.equals(setting.getTarget()))
                                    sender.sendMessage(getColor() + "\n- " + setting.getTarget());

                                // write the value
                                sender.sendMessage(getColor() + setting.writeValue());
                            }
                        }
                    } else
                        sender.sendMessage(ChatColor.RED + "Sorry, but only admins can configure " + getName() + "'s preferences.");
                }
                // /[abbreviaition/name] help (note that this must be separate from regular /help because it's plugin-specific)
                else if (parameters[0].equalsIgnoreCase("help") || parameters[0].equalsIgnoreCase("?")) {
                    debug(getName() + "-specific help command received; sending for help...");
                    help(sender, subArray(parameters, 1));
                } // /[abbreviaition/name] [????]
                else {
                    debug("detected direct plugin specification of command; redirecting to " + getName() + " as average command...");
                    onCommand(sender, cmd, parameters[0], subArray(parameters, 1));
                }
            // /[????] --help
            else if (parameters.length > 0 && parameters[0].equalsIgnoreCase("--help")) {
                debug("command help specified through --help flag; displaying command help...");
                help(sender, "/" + command);
            } // /[????]
            else {
                debug("non-centralized command issued; calling plugin command...");
                callPluginCommand(cmd.getName(), sender, parameters, command);
            }
        } catch (Exception exception) {
            err("There was a problem executing " + getColor() + ChatColor.ITALIC + "/" + command + " " + writeArray(parameters, " ", "") + ChatColor.DARK_RED + " in "
                    + getName() + "!", exception, "sender=\"" + getName(sender) + "\"");
        }
        return true;
    }

    // message utilities
    public void announce(String message, long expiration_time) {
        for (OfflinePlayer player : getServer().getOfflinePlayers())
            inform(player, message, expiration_time);

        // also send the message to the console
        getServer().getConsoleSender().sendMessage(getColor() + message);
    }

    public void broadcast(String message) {
        for (Player player : getServer().getOnlinePlayers())
            player.sendMessage(getColor() + message);

        getServer().getConsoleSender().sendMessage(message);
    }

    public void debug(String message) {
        if (debuggers.size() == 0)
            return;
        if (debuggers.contains((UUID) null)) {
            getServer().getConsoleSender().sendMessage(getColor() + message);
            if (debuggers.size() == 1)
                return;
        }
        for (Player player : getServer().getOnlinePlayers())
            if (debuggers.contains(player.getUniqueId()))
                player.sendMessage(getColor() + message);
    }

    public void err(String message, Throwable exception, Object... additional_information) {
        err(message, exception, exception.getClass().getSimpleName(), false, additional_information);
    }

    public void err(String message, String issue, Object... additional_information) {
        err(message, new NullPointerException(), issue, true, additional_information);
    }

    private void err(String message, Throwable e, String issue_name, boolean skip_one_more, Object... additional_information) {
        // TODO TEST
        // generate a full description of the issue
        String description = "There was ";

        int lines_to_skip = 0;
        // skip the first line if we created our own exception here to avoid getting useless information
        if (skip_one_more)
            lines_to_skip++;
        // skip stack trace lines until we get to some lines with line numbers associated with them not from the native Java code; those are the lines that will be helpful
        while (lines_to_skip < e.getStackTrace().length
                && (e.getStackTrace()[lines_to_skip].getLineNumber() < 0 || !e.getStackTrace()[lines_to_skip].getClassName().contains("REALDrummer")))
            lines_to_skip++;

        // create and format a message that gives only pertinent information from the stack trace
        while (e != null) {
            description += aOrAn(issue_name) + "...\n";
            if (e.getMessage() != null)
                description += "...to which Java says \"" + e.getMessage() + "\"...\n";
            description += "...at line " + e.getStackTrace()[lines_to_skip].getLineNumber() + " of " + e.getStackTrace()[lines_to_skip].getClassName();
            if (lines_to_skip + 1 < e.getStackTrace().length)
                for (int i = lines_to_skip + 1; i < e.getStackTrace().length; i++)
                    if (e.getStackTrace()[i].getLineNumber() < 0 || !e.getStackTrace()[i].getClassName().contains("REALDrummer"))
                        break;
                    else
                        description += "\n...and at line " + e.getStackTrace()[i].getLineNumber() + " of " + e.getStackTrace()[i].getClassName();
            e = e.getCause();
            if (e != null)
                message += "\n...which was caused by:\n";
        }
        for (int i = 0; i < additional_information.length; i++)
            description += "\n..." + (i == 0 ? "that involved" : "and") + ":\n" + additional_information[i].toString();

        // log the error
        try {
            File error_log = new File(getDataFolder(), "error log.txt");
            if (!error_log.exists()) {
                getDataFolder().mkdirs();
                error_log.createNewFile();
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(error_log, true));
            GregorianCalendar calendar = new GregorianCalendar();
            out.write(/* date */(calendar.get(2) + 1) + "/" + calendar.get(5) + "/" + calendar.get(1) + " " + /* time */new SimpleDateFormat("HH:mm:ss").format(new Date())
                    + /* error info */"\n" + decolor(message) + "\n" + description + "\n\n");
            out.close();
        } catch (IOException exception) {
            tellOps(ChatColor.DARK_RED + "I got " + aOrAn(exception.getClass().getSimpleName()) + " trying to log this error!\n" + message + "\n" + description);
        }

        // display the error to ops (note: \u2639 is a Unicode frowny face)
        tellOps(getName() + ChatColor.DARK_RED + " had an accident! \u2639\n" + message + "\nPlease give REALDrummer your error log.txt (in the " + getName()
                + " data folder)!", true);
    }

    public void inform(String recipient, String message) {
        inform(recipient, message, -1);
    }

    public void inform(String recipient, String message, long expiration_time) {
        if (recipient.equals("\\console"))
            getServer().getConsoleSender().sendMessage(message);
        else
            inform(getServer().getOfflinePlayer(recipient), message, expiration_time);
    }

    public void inform(UUID player, String message) {
        inform(player, message, -1);
    }

    public void inform(UUID player, String message, long expiration_time) {
        inform(getServer().getOfflinePlayer(player), message, expiration_time);
    }

    public void inform(OfflinePlayer player, String message) {
        inform(player, message, -1);
    }

    public void inform(OfflinePlayer player, String message, long expiration_time) {
        // TODO: make expiration_time set up a time for the message to expire if expiration_time>100

        // if the player is online, simply send them the message
        if (player.isOnline()) {
            player.getPlayer().sendMessage(message);
            return;
        }

        // if the player is not online, save the message as a new notice
        ArrayList<String> current_notices = notices.get(player.getUniqueId());
        if (current_notices == null)
            current_notices = new ArrayList<String>();
        current_notices.add(getColorCode() + message);
        notices.put(player.getUniqueId(), current_notices);
    }

    public String paginate(String message, String command_format, String not_enough_pages, int page_number, boolean not_console) {
        ChatPage chat_page;
        if (not_console) {
            // try to make it all one page up to 10 lines tall
            chat_page = ChatPaginator.paginate(message, page_number, 64, 10);
            // if the message is too long to be one page, even with a 10-line height, reduce the page height to 8
            if (chat_page.getTotalPages() > 1)
                chat_page = ChatPaginator.paginate(message, page_number, 64, 8);
        } else
            chat_page = ChatPaginator.paginate(message, page_number, 64, 20);

        // if the page number given is too high, format and return the not_enough_pages message
        if (page_number > chat_page.getTotalPages()) {
            String total_pages = chat_page.getTotalPages() + " pages";
            if (chat_page.getTotalPages() == 1)
                total_pages = "1 page";
            return replaceAll(ChatColor.RED + not_enough_pages, "[total]", total_pages);
        }

        String page = combine(chat_page.getLines(), "\n");
        // if there's more than one page, add a prefix notifying the user
        if (chat_page.getTotalPages() > 1) {
            String prefix = getColor() + "Here's page " + page_number + " of " + chat_page.getTotalPages() + "!\n";
            if (page_number > 1)
                prefix += ChatColor.WHITE + "...";
            page = prefix + page;
        }
        // if there are more pages, add a suffix notifying the user
        if (chat_page.getTotalPages() > page_number)
            page +=
                    ChatColor.WHITE + "...\n" + getColor() + "Use " + replaceAll(ChatColor.ITALIC + command_format, "[#]", String.valueOf(page_number + 1)) + getColor()
                            + " to see more.";
        return page;
    }

    public void tell(CommandSender sender, String message) {
        message = getColor() + message;
        if (sender == null)
            tellOps(message);
        else
            sender.sendMessage(message);
    }

    public void tellOps(String message, String... exempt_ops) {
        tellOps(message, true, exempt_ops);
    }

    /** This method sends a given message to every operator currently on the server as well as to the console.
     * 
     * @param message
     *            is the message that will be sent to all operators and the console. <b><tt>Message</b></tt> will be color coded using myPluginUtils's
     *            {@link #colorCode(String) colorCode(String)} method.
     * @param also_tell_console
     *            indicates whether or not <b><tt>message</b></tt> will also be sent to the console.
     * @param exempt_ops
     *            is an optional parameter in which you may list any ops by exact username that should not receive <b><tt>message</b></tt>. */
    public void tellOps(String message, boolean also_tell_console, String... exempt_ops) {
        for (Player player : getServer().getOnlinePlayers())
            if (player.isOp() && !contains(exempt_ops, player.getName()))
                player.sendMessage(getColor() + message);

        if (also_tell_console)
            getServer().getConsoleSender().sendMessage(getColor() + message);
    }

    // other utility methods
    /** This method returns the abbreviation for this plugin, a three-character <tt>String</tt> that begins with an "m" followed by the uppercased first letterof the two other
     * words that make up the plugin's name (e.g. myUltraWarps's abbreviation is "mUW").
     * 
     * @return the plugin's three-character abbrevation. */
    public String getAbbreviation() {
        return ABBREVIATION;
    }

    /** This method returns the (lowercase) <b>char</b> representing the {@link ChatColor} of this plugin (0-9 or a-f).
     * 
     * @return a <b>char</b> representing the {@link ChatColor} of this plugin.
     * @see {@link #getColor()} and {@link #getColorCode()} */
    public char getColorChar() {
        return getColor().getChar();
    }

    /** This method returns the (lowercase) <tt>String</tt> representing the {@link ChatColor} of this plugin (& followed by 0-9 or a-f).
     * 
     * @return a char representing the {@link ChatColor} of this plugin.
     * @see {@link #getColorChar()} and {@link #getColor()} */
    public String getColorCode() {
        return "&" + getColor().getChar();
    }

    public String getName(CommandSender sender) {
        return getName(sender, "\\console");
    }

    public String getName(CommandSender sender, String console_name) {
        if (sender instanceof ConsoleCommandSender)
            return console_name;
        else
            return ((Player) sender).getName();
    }

    /** This method returns the global {@link mySetting} described by the given key.
     * 
     * @param key
     *            is the key of the setting to search for, e.g. "auto-update".
     * @return the global (<tt>target</tt>={@link mySetting#DEFAULT_TARGET}) {@link mySetting} named by <b><tt>key</b></tt>.
     * @see {@link #getSetting(String, String)}. */
    public mySetting getSetting(String key) {
        return getSetting(mySetting.DEFAULT_TARGET, key);
    }

    /** This method returns the {@link mySetting} described by the given target and key.
     * 
     * @param target
     *            is the target that the {@link mySetting} refers to. If the setting is a global (getServer()-wide) setting, <b><tt>target</b></tt> should be
     *            {@link mySetting#DEFAULT_TARGET} (or better yet, you should use {@link #getSetting(String)}); if the setting is a group setting, <b><tt>target</b></tt>
     *            should be enclosed in brackets ("[]"); if the setting is an individual setting, the <b><tt>target</b></tt> should simply be the player's username.
     * @param key
     *            is the key of the setting to search for, e.g. "auto-update".
     * @return the {@link mySetting} specified by <b><tt>target</b></tt> and <b><tt>key</b></tt>.
     * @see {@link #getSetting(String)}. */
    public mySetting getSetting(String target, String key) {
        return settings.findMatch(target, key);
    }

    public UUID getUUID(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender)
            return null;
        else
            return ((Player) sender).getUniqueId();
    }

    /** This method simply retrieves the version number from {@link JavaPlugin}'s {@link org.bukkit.plugin.PluginDescriptionFile PluginDescriptionFile} and reads it as a
     * double.
     * 
     * @return this plugin's version as a double. */
    public double getVersion() {
        try {
            return Double.parseDouble(getDescription().getVersion());
        } catch (NumberFormatException exception) {
            err(getName() + "'s version number isn't a proper decimal number! It needs to be a proper decimal number for version checking!", exception, "version=\""
                    + getDescription().getVersion() + "\"");
            return 0;
        }
    }

    /** This method determines whether or not the given {@link CommandSender} has admin permissions for this {@link myPlugin}.
     * 
     * @param sender
     *            is the {@link CommandSender} that may or may not have admin permissions for this {@link myPlugin}.
     * @return <b>true</b> if <b><tt>sender</b></tt> is the console ({@link ConsoleCommandSender}) or if <b><tt>sender</b></tt> is a {@link Player} who has this plugin's admin
     *         permission ("<tt>[plugin name].admin</tt>"). */
    public boolean isAdmin(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission(getName().toLowerCase() + ".admin");
    }

    /** This method determines whether or not the given {@link CommandSender} has user permissions for this {@link myPlugin}.
     * 
     * @param sender
     *            is the {@link CommandSender} that may or may not have user permissions for this {@link myPlugin}.
     * @return <b>true</b> if <b><tt>sender</b></tt> is the console ({@link ConsoleCommandSender}) or if <b><tt>sender</b></tt> is a {@link Player} who has this plugin's user
     *         permission ("<tt>[plugin name].user</tt>"). */
    public boolean isUser(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender)
            return true;
        else
            return sender.hasPermission(getName().toLowerCase() + ".user");
    }

    private mySetting[] trueConfigDefaults() {
        mySetting[] instance_defaults = configDefaults();

        // retrieve the names of all the data types
        String[] data_type_names = new String[myData.data_types.length()];
        int index = 0;
        for (Class<? extends myData> data_type : myData.data_types) {
            data_type_names[index] = data_type.getSimpleName().toLowerCase();
            index++;
        }

        // contents (in order):
        // + auto-update setting (true)
        // + save file format for the config ("txt")
        // + save file format for all myData types ("dat")
        // + auto-save setting for the config (true)
        // + auto-save setting for all myData types (false)
        // + instance_defaults (from configDefaults() from the plugin itself)
        mySetting[] true_defaults = new mySetting[instance_defaults.length + data_type_names.length * 2 + 3];
        true_defaults[0] = new myBSetting("auto-update", true);
        true_defaults[1] = new myOSetting("config file format", "txt", "txt", "dat");
        true_defaults[data_type_names.length + 2] = new myBSetting("auto-save config", true);
        for (byte i = 0; i < data_type_names.length; i++) {
            true_defaults[i + 2] = new myOSetting(data_type_names[i] + "s file format", "dat", "txt", "dat");
            true_defaults[i + data_type_names.length + 3] = new myBSetting("auto-save " + data_type_names[i] + "s", false);
        }
        for (byte i = 0; i < instance_defaults.length; i++)
            true_defaults[i + data_type_names.length * 2 + 3] = instance_defaults[i];

        return true_defaults;
    }

    /** This method sets the value of the global {@link mySetting} specified by <b><tt>key</b></tt> to <b><tt>value</b></tt>.
     * 
     * @param key
     *            is the key of the setting to search for, e.g. "auto-update".
     * @param value
     *            is the value to give the specified setting.
     * @return <b>true</b> if this method successfully set the value of the specified method to <b><tt>value</b></tt>; <b>false</b> otherwise. This method may fail to set the
     *         value of the specified setting if <b><i>1)</b></i> no global setting was found with the given <b><tt>key</b></tt> or <b><i>2)</b></i> if <b><tt>value</b></tt>
     *         was not of the correct type for the {@link mySetting} found, e.g. if <b><tt>value</b></tt> was a <tt>String</tt> and the setting found was a {@link myISetting},
     *         which holds an <tt>Integer</tt> value.
     * @see {@link #setSetting(String, String, Object)}.
     * @NOTE If no {@link mySetting} exists for this plugin with the given key, <b><i>this method will not create a new setting for it!</b></i> No new settings should be added
     *       to the plugin after initialization because then it is possible that a setting can be found some times and not other times, which makes things unnecessarily
     *       confusing. If a new setting should be added to this plugin, add it to the list in the plugin's {@link #configDefaults()} method to make the setting initialize
     *       with the rest of the settings. */
    public boolean setSetting(String key, Object value) {
        return setSetting(mySetting.DEFAULT_TARGET, key, value);
    }

    /** This method sets the value of the global {@link mySetting} specified by <b><tt>key</b></tt> to <b><tt>value</b></tt>.
     * 
     * @param target
     *            is the target that the {@link mySetting} refers to. If the setting is a global (getServer()-wide) setting, <b><tt>target</b></tt> should be
     *            {@link mySetting#DEFAULT_TARGET} (or better yet, you should use {@link #getSetting(String)}); if the setting is a group setting, <b><tt>target</b></tt>
     *            should be enclosed in brackets ("[]"); if the setting is an individual setting, the <b><tt>target</b></tt> should simply be the player's username.
     * @param key
     *            is the key of the setting to search for, e.g. "auto-update".
     * @param value
     *            is the value to give the specified setting.
     * @return <b>true</b> if this method successfully set the value of the specified method to <b><tt>value</b></tt>; <b>false</b> otherwise. This method may fail to set the
     *         value of the specified setting if <b><i>1)</b></i> no global setting was found with the given <b><tt>target</b></tt> and <b><tt>key</b></tt> or <b><i>2)</b></i>
     *         if <b><tt>value</b></tt> was not of the correct type for the {@link mySetting} found, e.g. if <b><tt>value</b></tt> was a <tt>String</tt> and the setting found
     *         was a {@link myISetting}, which holds an <tt>Integer</tt> value.
     * @see {@link #setSetting(String, Object)}.
     * @NOTE If no {@link mySetting} exists for this plugin with the given target and key, <b><i>this method will not create a new setting for it!</b></i> No new settings
     *       should be added to the plugin after initialization because then it is possible that a setting can be found some times and not other times, which makes things
     *       unnecessarily confusing. If a new setting should be added to this plugin, add it to the list in the plugin's {@link #configDefaults()} method to make the setting
     *       initialize with the rest of the settings. */
    public boolean setSetting(String target, String key, Object value) {
        mySetting setting = getSetting(target, key);
        if (setting == null)
            return false;

        return setting.setValue(value);
    }

    // command methods
    private void callPluginCommand(String primary_alias, CommandSender sender, String[] raw_parameters, String command) {
        debug("attempting to call /" + command + (!primary_alias.equals(command) ? " (/" + primary_alias + ")" : "") + " in " + getName() + "...");

        for (Method method : getClass().getDeclaredMethods()) {
            // check that the method's name is the same as the command's primary alias
            if (!method.getName().equalsIgnoreCase(primary_alias))
                continue;

            // retrieve the Commander annotation; if it does not have one, it is not the method we're looking for
            Commander command_settings = method.getAnnotation(Commander.class);
            if (command_settings == null)
                continue;

            debug("found command method " + method.getName() + "(); checking command settings...");

            // check the command's permission settings
            if (sender instanceof ConsoleCommandSender && !command_settings.console_command()) {
                debug("DENIED; non-console command");
                sender.sendMessage(ChatColor.RED + "You're a console! You can't use " + getColor() + ChatColor.ITALIC + "/" + command + ChatColor.RED + "!");
            } else if (!isAdmin(sender) && !sender.hasPermission(getName().toLowerCase() + "." + command.toLowerCase())
                    && !(isUser(sender) && command_settings.user_command()) && !(sender.isOp() && command_settings.op_command()) && !command_settings.open_command()) {
                debug("DENIED; insufficient permission");
                sender.sendMessage(ChatColor.RED + "Sorry, but you don't have permission to use " + getColor() + ChatColor.ITALIC + "/" + command.toLowerCase()
                        + ChatColor.RED + ".");
            }

            debug("permissions and CommandSender type approved; translating parameters into flags...");

            // use myFlag.readCommand to read and organize the parameters using the myFlags
            HashMap<String, Object> parameters = myFlag.readCommand(this, sender, primary_alias, raw_parameters);
            if (parameters == null) {
                debug("command cancelled");
                return;
            } else
                debug("parameters processed; calling command method...");

            // call the method
            try {
                if (method.getParameterTypes().length == 3)
                    method.invoke(this, sender, parameters, raw_parameters);
                else if (method.getParameterTypes().length == 2)
                    method.invoke(this, sender, parameters);
                else
                    err("This command method doesn't take the right arguments!", "improper command method", "/" + command + " (/" + primary_alias + ")");
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
                err("There was a problem trying to invoke the command method in " + getName() + ".", exception, method, "command=\"" + command + "\"");
                return;
            }
            return;
        }

        err("I couldn't find the plugin command method that corresponds to " + getColor() + ChatColor.ITALIC + "/" + command + ChatColor.DARK_RED + "!",
                "unexecutable command", "sender=\"" + sender + "\"", "parameters=" + writeArray(raw_parameters, ", ", "and", "\"", "\""));
    }

    @SuppressWarnings("resource")
    private void checkForUpdates(CommandSender sender) {
        URL url = null;
        try {
            url = new URL("http://dev.bukkit.org/getServer()-mods/realdrummers-" + getName().toLowerCase() + "/files.rss/");
        } catch (MalformedURLException exception) {
            err("I've never seen a U.R.L. like this before!", exception);
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
                } catch (IOException exception) {
                    tell(sender, ChatColor.RED + "Our Internet resources on BukkitDev aren't working right now. Please try again later.");
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
                err("We seem to have hit an XMLStreamException.", exception);
                return;
            }
            boolean new_version_is_out = false;
            String version = getDescription().getVersion(), newest_online_version = "";
            if (new_version_name == null) {
                err("Something seems to have gone awry while trying to retrieve the newest version of " + getName() + ".", "problem retrieving the new version name");
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
                err("Oh, no! REALDrummer didn't properly catalog the newest version of " + getName() + "! Please tell him to fix it immediately!",
                        "problem reading the current version of " + getName());
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
                    tell(sender, ChatColor.RED + "Our Internet resources on BukkitDev aren't working right now. Please try again later.");
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
                            tellOps(getColor() + "" + ChatColor.UNDERLINE + getName() + " v" + newest_online_version + " has been released! Please replace your old "
                                    + getName() + " with the new one in your data folder and we'll increase our stores of information!", true);
                        } catch (Exception exception) {
                            err("Oh, no! It seems " + getName() + " v" + newest_online_version
                                    + " has been released, but I can't retrieve the new version from BukkitDev! I'm afraid you'll have to go get it yourself.", exception);
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
                        tell(sender, ChatColor.RED + "You still have the newest version of " + getName()
                                + " lying useless in your data folder! You should put it on the getServer()!");
                }
            } else
                tell(sender, "There are no new versions of " + getName() + ".");
        }
    }

    public void help(CommandSender sender, String... parameters) {
        // TODO
    }

    // loading and saving
    /** This method searches for the {@link myData} type that corresponds to the given <b><tt>data_type</b></tt>, then calls {@link myData#load(CommandSender) the myData's
     * type's load() method} to load the data for that type from file to the getServer().
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to load this data. The <b><tt>sender</b></tt> may be <b>null</b> if the load command was called by a
     *            plugin, e.g. during the plugin's enable process.
     * @param data_type
     *            is the name of the {@link myData} type to load. This <tt>String</tt> will be matched with the {@link myData} type using case-insensitive auto-completion
     *            searching.
     * @return <b>false</b> if the attempt to load the given data type failed; <b>true</b> otherwise. The attempt to load may fail if <b><i>1)</b></i> no {@link myData} type
     *         was found for this plugin that matches the given <b><tt>data_type</b> String</tt> or <b><i>2)</b></i> if an issue occurred while trying to load the data, e.g.
     *         if no data files were found for the given data type.
     * @see {@link #loadData(CommandSender, Class)}, {@link #loadTheConfig(CommandSender)}, and {@link #saveData(CommandSender, String)}. */
    protected boolean loadData(CommandSender sender, String data_type) {
        // find the closest match to the given data_type
        Class<? extends myData> target = null;
        for (Class<? extends myData> potential_target : myData.data_types)
            if (potential_target.getSimpleName().toLowerCase().startsWith(data_type)
                    && (target == null || target.getSimpleName().length() > potential_target.getSimpleName().length()))
                target = potential_target;

        if (target == null)
            return false;

        return loadData(sender, target);
    }

    /** This method calls {@link myData#load(CommandSender) the myData's type's load() method} to load the data for that type from file to the getServer() (using Java
     * Reflection).
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to load this data. The <b><tt>sender</b></tt> may be <b>null</b> if the load command was called by a
     *            plugin, e.g. during the plugin's enable process.
     * @param data_type
     *            is the <tt>Class</tt> of the {@link myData} type to load.
     * @return <b>false</b> if the attempt to load the given data type failed, e.g. if no data files were found for the given data type; <b>true</b> otherwise.
     * @see {@link #loadData(CommandSender, String)}, {@link #loadTheConfig(CommandSender)}, and {@link #saveData(CommandSender, String)}. */
    protected boolean loadData(CommandSender sender, Class<? extends myData> data_type) {
        // call the myData class's load method
        try {
            return (boolean) data_type.getMethod("load", CommandSender.class).invoke(sender);
        } catch (Exception exception) {
            err("There was an issue calling this myData's load method!", exception, data_type.getName());
            return false;
        }
    }

    /** This method loads the plugin's configurations from the config file.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to load this data. The <b><tt>sender</b></tt> may be <b>null</b> if the load command was called by a
     *            plugin, e.g. during the plugin's enable process.
     * @return <b>false</b> if the attempt to load the config failed, e.g. if no data files were found for the given data type; <b>true</b> otherwise.
     * @see {@link #loadData(CommandSender, String)} and {@link #saveTheConfig(CommandSender)}. */
    protected boolean loadTheConfig(CommandSender sender) {
        myList<mySetting> new_settings = new myList<mySetting>(trueConfigDefaults());
        debug("loading " + getName() + "'s configurations...");

        try {
            File txt_file = new File(getDataFolder(), "config.txt"), dat_file = new File(getDataFolder(), "config.dat");

            // attempt to find the current format for the configuration file
            String current_format = null;
            myOSetting current_format_setting = (myOSetting) getSetting("config save format");
            if (current_format_setting != null)
                current_format = current_format_setting.getValue();

            // load from the .dat file
            if (dat_file.exists() && (current_format == null || current_format.equals("dat") || !txt_file.exists() && current_format.equals("txt"))) {
                debug("reading config data from dat file...");
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(dat_file));

                while (true)
                    try {
                        mySetting setting = (mySetting) in.readObject();
                        new_settings.add(setting);
                        debug("read setting: " + setting);
                    } catch (EOFException exception) {
                        debug("end of file reached; " + getName() + " config loading complete");
                        break;
                    } catch (ClassNotFoundException exception) {
                        err("The ObjectInputStream reading the config file for " + getName() + " could not find the class for this Object!", exception);
                        continue;
                    } catch (ClassCastException exception) {
                        err("The ObjectInputStream reading the config file for " + getName() + " didn't identify this Object as a mySetting!", exception);
                        continue;
                    }
                in.close();
            } else if (txt_file.exists()) {
                debug("reading config data from txt file...");
                BufferedReader in = new BufferedReader(new FileReader(txt_file));

                String save_line = in.readLine() /* the assignment of save_line here is no accident; we want to skip the first line, which is just a brief explanation on how
                                                  * to use the config */, parsing_target = null;
                while ((save_line = in.readLine()) != null) {
                    save_line = save_line.trim();
                    debug("save line read: \"" + save_line + "\"");
                    if (save_line.equals("")) {
                        debug("skipping blank line...");
                        continue;
                    }

                    // if the line starts with a "-", it's the beginning of a settings grouping, so put it into parsing_target
                    if (save_line.startsWith("-")) {
                        parsing_target = save_line.substring(1).trim();
                        debug("settings group start; reading \"" + parsing_target + "\"'s settings...");
                    } // otherwise, read it as a setting
                    else {
                        // first, separate out the key and value
                        String key, value;
                        char separator;
                        if (!save_line.contains("?") && !save_line.contains(":") && !save_line.contains("(")) {
                            err("I couldn't read this save line's key in the " + getName() + " config!", "mis-formatted save line", "save line=\"" + save_line + "\"");
                            continue;
                        } else if (save_line.contains(":") && (!save_line.contains("?") || save_line.indexOf(':') < save_line.indexOf('?'))
                                && (!save_line.contains("(") || save_line.indexOf(':') < save_line.indexOf('('))) {
                            // for most mySettings (format = [key]: [value])
                            debug("reading setting with common mySetting format...");
                            separator = ':';
                        } else if (save_line.contains("?") && (!save_line.contains("(") || save_line.indexOf('?') < save_line.indexOf('('))) {
                            // for myBSettings (format = [key]? [value])
                            debug("reading setting with myBSetting format...");
                            separator = '?';
                        } else {
                            // for myOSettings (format = [key] ([options]): [value])
                            debug("reading setting with myOSetting format...");
                            separator = '(';
                        }
                        key = save_line.substring(0, save_line.indexOf(separator)).trim();
                        if (separator == '(')
                            separator = ':';
                        value = save_line.substring(save_line.indexOf(separator) + 1).trim();
                        debug("found key and value: key=\"" + key + "\"; value=\"" + value + "\"");

                        // now find the setting that corresponds to the key found
                        mySetting setting = new_settings.findMatch(parsing_target != null ? parsing_target : mySetting.DEFAULT_TARGET, key);
                        if (setting == null) {
                            err("I couldn't identify this save line's key in the " + getName() + " config!", "unidentified save line", "save line=\"" + save_line + "\"",
                                    "key=\"" + key + "\"", "value=\"" + value + "\"");
                            continue;
                        }

                        // finally, read the new value into the preexisting setting in the new_settings list
                        if (!setting.readValue(value))
                            err("The value of a configuration in the " + getName() + " was not properly formatted!", "problem reading this setting's value", "save line=\""
                                    + save_line + "\"", "key=\"" + key + "\"", "value=\"" + value + "\"", "setting type=" + setting.getClass().getSimpleName());
                    }
                }

                in.close();
            } else
                debug("no config files for " + getName() + "; using defaults...");
        } catch (IOException exception) {
            err("I ran into an IOException while trying to read the " + getName() + " config file!", exception, "sender=\"" + sender + "\"");
            return false;
        }

        // if no severe errors were thrown, make the new_settings list the new settings list
        settings = new_settings;

        // make sure that all of the standard settings are there
        boolean some_were_missing = false;
        for (mySetting standard_setting : trueConfigDefaults())
            if (getSetting(standard_setting.getKey()) == null) {
                some_were_missing = true;
                debug(getName() + " config missing \"" + standard_setting.getKey() + "\" standard setting after config load; adding default...");
                settings.add(standard_setting);
            }
        if (!some_were_missing)
            debug("no config standard settings missing; " + getName() + " config load successful");
        else
            debug(getName() + " config load successful");

        debug("saving loaded config...");
        saveTheConfig(sender, false);

        // send confirmation messages
        debug("load successful; sending load confirmation messages...");

        if (sender == null)
            if (settings.size() > 1)
                tellOps("Your " + settings.size() + " settings have been loaded.");
            else
                tellOps("Your 1 setting has been loaded.");
        else if (settings.size() > 1) {
            sender.sendMessage("Your " + settings.size() + " settings have been loaded.");
            tellOps(getName(sender, "Someone on the console") + " loaded " + settings.size() + " settings to file.", sender instanceof Player, sender.getName());
        } else {
            sender.sendMessage("Your 1 setting has been loaded.");
            tellOps(getName(sender, "Someone on the console") + " loaded the getServer()'s 1 setting to file.", sender instanceof Player, sender.getName());
        }

        return true;
    }

    /** This method searches for the {@link myData} type that corresponds to the given <b><tt>data_type</b></tt>, then calls {@link myData#save(CommandSender) the myData's
     * type's save() method} to save the data for that type to file.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to save this data. The <b><tt>sender</b></tt> may be <b>null</b> if the save command was called by a
     *            plugin, e.g. during the plugin's disable process.
     * @param data_type
     *            is the name of the {@link myData} type to save. This <tt>String</tt> will be matched with the {@link myData} type using case-insensitive auto-completion
     *            searching.
     * @return <b>false</b> if the attempt to save the given data type failed; <b>true</b> otherwise. The attempt to save may fail if <b><i>1)</b></i> no {@link myData} type
     *         was found for this plugin that matches the given <b><tt>data_type</b> String</tt> or <b><i>2)</b></i> if an issue occurred while trying to save the data, e.g.
     *         an <tt>IOException</tt> was thrown.
     * @see {@link #saveData(CommandSender, Class)}, {@link #saveTheConfig(CommandSender)}, and {@link #loadData(CommandSender, String)}. */
    protected boolean saveData(CommandSender sender, String data_type) {
        // find the closest match to the given data_type
        Class<? extends myData> target = null;
        for (Class<? extends myData> potential_target : myData.data_types)
            if (potential_target.getSimpleName().toLowerCase().startsWith(data_type)
                    && (target == null || target.getSimpleName().length() > potential_target.getSimpleName().length()))
                target = potential_target;

        if (target == null)
            return false;

        return saveData(sender, target);
    }

    /** This method searches for the {@link myData} type that corresponds to the given <b><tt>data_type</b></tt>, then calls {@link myData#save(CommandSender) the myData's
     * type's save() method} to save the data for that type to file.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to save this data. The <b><tt>sender</b></tt> may be <b>null</b> if the save command was called by a
     *            plugin, e.g. during the plugin's disable process.
     * @param data_type
     *            is the <tt>Class</tt> of the {@link myData} type to save.
     * @return <b>false</b> if the attempt to save the given data type failed, e.g. if an <tt>IOException</tt> was thrown; <b>true</b> otherwise. The attempt to save may fail
     *         if an issue occurred while trying to save the data, e.g. an <tt>IOException</tt> was thrown.
     * @see {@link #saveData(CommandSender, String)}, {@link #saveTheConfig(CommandSender)}, and {@link #loadData(CommandSender, String)}. */
    protected boolean saveData(CommandSender sender, Class<? extends myData> data_type) {
        // call the myData class's save method
        try {
            return (boolean) data_type.getMethod("save", CommandSender.class).invoke(sender);
        } catch (Exception exception) {
            err("There was an issue calling this myData's load method!", exception, data_type.getName());
            return false;
        }
    }

    /** This method saves the plugin's configurations to file.
     * 
     * @param sender
     *            is the {@link CommandSender} that sent the command to save this data. The <b><tt>sender</b></tt> may be <b>null</b> if the save command was called by a
     *            plugin, e.g. during the plugin's disable process.
     * @return <b>false</b> if the attempt to save the config failed, e.g. if an <tt>IOException</tt> was thrown; <b>true</b> otherwise.
     * @see {@link #saveTheConfig(CommandSender, boolean)}, {@link #saveData(CommandSender, String)}, and {@link #loadTheConfig(CommandSender)}. */
    protected boolean saveTheConfig(CommandSender sender) {
        return saveTheConfig(sender, true);
    }

    private boolean saveTheConfig(CommandSender sender, boolean display_messages) {
        debug("saving " + getName() + " config...");

        String current_format = "???";    // current_format is declared out here so that it can be used as additional information if an IOException occurs
        try {
            File txt_file = new File(getDataFolder(), "config.txt"), dat_file = new File(getDataFolder(), "config.dat");
            current_format = getSetting("config file format").optionValue();

            // load from the dat
            if (current_format.equals("dat")) {
                debug("writing " + getName() + " config data to dat file...");
                if (!dat_file.exists()) {
                    debug("dat file does not exist; creating new dat file...");
                    getDataFolder().mkdirs();
                    dat_file.createNewFile();
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dat_file));

                debug("beginning data writing...");
                for (mySetting setting : settings) {
                    out.writeObject(setting);
                    debug("wrote object: " + setting);
                }

                debug(getName() + "config saving complete");
                out.close();
            } // load from the text file
            else if (current_format.equals("txt")) {
                debug("writing " + getName() + " config data to txt file...");
                if (!txt_file.exists()) {
                    debug("txt file does not exist; creating new txt file...");
                    getDataFolder().mkdirs();
                    txt_file.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(txt_file));

                debug("writing usage explanation line...");
                out.write("Remember to use \"/" + getAbbreviation() + " save config\" before modifying this file and \"/" + getAbbreviation()
                        + " load config\" once you're done modifying it to load your changes to the getServer().");
                out.newLine();
                out.newLine();

                debug("beginning data writing...");
                String last_target = null;
                for (mySetting setting : settings) /* note that mySettings are set up specifically so that when they sort, they will sort themselves automatically by target */{
                    debug("writing setting: " + setting + " (last target=" + (last_target == null ? "null" : "\"" + last_target + "\"") + ")");

                    // if the target changed, write a line indicating the beginning of a new data section
                    if (last_target == null && setting.getTarget() != null || last_target != null && !last_target.equals(setting.getTarget())) {
                        debug("new target detected; writing new section header...");
                        out.newLine();
                        out.write("- " + setting.getTarget());
                        out.newLine();
                    }

                    // write the value
                    debug("writing setting value...");
                    out.write(setting.writeValue());
                    out.newLine();
                    debug("wrote setting value");
                }

                debug(getName() + " config saving complete");
                out.close();
            } else {
                err("What kind of save format is \"" + current_format + "\"?", "unknown value for the current save format");
                return false;
            }
        } catch (IOException exception) {
            err("There was an IOException while trying to write the " + getName() + " config file!", exception, "current_format=\"" + current_format + "\"");
            return false;
        }

        if (display_messages) {
            // send confirmation messages
            debug("save successful; sending save confirmation messages...");

            if (sender == null)
                if (settings.size() > 1)
                    tellOps("Your " + settings.size() + " settings have been saved.");
                else if (settings.size() == 1)
                    tellOps("Your 1 setting has been saved.");
                else
                    tellOps("You have no settings to save!");
            else {
                String sender_name = capitalize(getName(sender, "Someone on the console"));
                if (settings.size() > 1) {
                    sender.sendMessage("Your " + settings.size() + " settings have been saved.");
                    tellOps(sender_name + " saved " + settings.size() + " settings to file.", sender instanceof Player, sender.getName());
                } else if (settings.size() == 1) {
                    sender.sendMessage("Your 1 setting has been saved.");
                    tellOps(sender_name + " saved the getServer()'s 1 setting to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage("You have no settings to saved!");
                    tellOps(sender_name + " tried to save the getServer()'s config, but there were no settings on the getServer() to load.", sender instanceof Player, sender
                            .getName());
                }
            }
        } else
            debug("configurations saved to file; save successful");

        return true;
    }

    // overrides
    @Override
    public int compareTo(myPlugin plugin) {
        return getName().compareTo(plugin.getName());
    }

    @Override
    public int matchTo(String... parameters) {
        return compare(new String[] { getName(), getDescription().getVersion() }, parameters);
    }

    @Override
    public String toString() {
        return getName() + getDescription().getVersion();
    }

    // abstract and placeholder methods
    @SuppressWarnings("unused")
    @Override
    public void questionAnswered(myQuestion question, String answer_message, boolean answer) {
        // by default, this method should do absolutely nothing
    }

    @SuppressWarnings("unused")
    @Override
    public void questionCancelled(myQuestion question) {
        // by default, this method should do absolutely nothing
    }

    /** This method is meant to be used <i>in lieu of</i> {@link JavaPlugin#onEnable() Bukkit's onEnable() method}. The {@link myPlugin} class calls this method when the plugin
     * is enabled after loading all of the plugin's configurations and data but before checking for updates (if configured to do so).
     * 
     * @return a list of enable messages, one of which will be randomly selected and displayed to all the ops when this plugin is enabled as kind of a greeting. These messages
     *         should be short, whimsical, and vaguely related to the plugin's function! */
    @Nonnull
    protected abstract String[] myEnable();

    /** This method is meant to be used <i>in lieu of</i> {@link JavaPlugin#onDisable() Bukkit's onDisable() method}. The {@link myPlugin} class calls this method when the
     * plugin is disabled after saving all of the plugin's configurations and data.
     * 
     * @return a list of disable messages, one of which will be randomly selected and displayed to all the ops when this plugin is enabled as kind of a farewell. These
     *         messages should be short, whimsical, and vaguely related to the plugin's function! */
    @Nonnull
    protected abstract String[] myDisable();

    /** This method is used to tell the {@link myPlugin} class the default settings for the plugin in order to allow the plugin to autoamtically handle config loading and
     * saving.
     * 
     * @return a list of {@link mySetting}s that represent the default (global) settings for this plugin. */
    @Nonnull
    protected abstract mySetting[] configDefaults();

    /** This abstract method should return the {@link ChatColor} associated with the plugin. Every plugin should have a different color associated with it if possible except
     * for minigames plugins, which should all be {@link ChatColor#GOLD}.
     * 
     * @return this plugin's {@link ChatColor}. */
    @Nonnull
    public abstract ChatColor getColor();
}
