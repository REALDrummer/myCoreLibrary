package REALDrummer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

// Message Utilities
public class MU {
    public static void confirmDisable(ChatColor color, String... disable_messages) {
        tellOps(color + disable_messages[(int) (Math.random() * disable_messages.length)], true);
    }

    public static void confirmEnable(ChatColor color, String... enable_messages) {
        tellOps(color + enable_messages[(int) (Math.random() * enable_messages.length)], true);
    }

    public static void confirmLoad(CommandSender sender, ChatColor color, int number_of_objects, String object, String... settings_type) {
        if (sender == null)
            if (number_of_objects > 1)
                if (settings_type.length == 0)
                    tellOps(color + "Your " + number_of_objects + " " + object + "s have been loaded.", true);
                else
                    tellOps(color + "Your " + number_of_objects + " " + object + "s and your " + settings_type[0] + " settings have been loaded.", true);
            else if (number_of_objects == 1)
                if (settings_type.length == 0)
                    tellOps(color + "Your 1 " + object + " has been loaded.", true);
                else
                    tellOps(color + "Your 1 " + object + " and your " + settings_type[0] + " settings have been loaded.", true);
            else if (settings_type.length == 0)
                tellOps(color + "You have no " + object + "s to load!", true);
            else
                tellOps(color + "Your " + settings_type[0] + " settings have been loaded.", true);
        else {
            String sender_name = "Someone on the console";
            if (sender instanceof Player)
                sender_name = ((Player) sender).getName();
            if (number_of_objects > 1)
                if (settings_type.length == 0) {
                    sender.sendMessage(color + "Your " + number_of_objects + " " + object + "s have been loaded.");
                    tellOps(color + sender_name + " loaded " + number_of_objects + " " + object + "s to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage(color + "Your " + number_of_objects + " " + object + "s and your " + settings_type[0] + " have been loaded.");
                    tellOps(color + sender_name + " loaded " + number_of_objects + " " + object + "s to file.", sender instanceof Player, sender.getName());
                }
            else if (number_of_objects == 1)
                if (settings_type.length == 0) {
                    sender.sendMessage(color + "Your 1 " + object + " has been loaded.");
                    tellOps(color + sender_name + " loaded the server's 1 " + object + " to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage(color + "Your 1 " + object + " and your " + settings_type[0] + " settings have been loaded.");
                    tellOps(color + sender_name + " loaded the server's 1 " + object + " to file.", sender instanceof Player, sender.getName());
                }
            else if (settings_type.length == 0) {
                sender.sendMessage(color + "You have no " + object + "s to load!");
                tellOps(color + sender_name + " tried to load the server's " + object + "s to file, but there were no " + object + "s on the server to load.",
                        sender instanceof Player, sender.getName());
            } else {
                sender.sendMessage(color + "Your " + settings_type[0] + " settings have been loaded.");
                tellOps(color + sender_name + " loaded your " + settings_type[0] + " settings.", sender instanceof Player, sender.getName());
            }
        }
    }

    public static void confirmSave(CommandSender sender, ChatColor color, int number_of_objects, String object, String... settings_type) {
        if (sender == null)
            if (number_of_objects > 1)
                if (settings_type.length == 0)
                    tellOps(color + "Your " + number_of_objects + " " + object + "s have been saved.", true);
                else
                    tellOps(color + "Your " + number_of_objects + " " + object + "s and your " + settings_type[0] + " settings have been saved.", true);
            else if (number_of_objects == 1)
                if (settings_type.length == 0)
                    tellOps(color + "Your 1 " + object + " has been saved.", true);
                else
                    tellOps(color + "Your 1 " + object + " and your " + settings_type[0] + " settings have been saved.", true);
            else if (settings_type.length == 0)
                tellOps(color + "You have no " + object + "s to save!", true);
            else
                tellOps(color + "Your " + settings_type[0] + " settings have been saved.", true);
        else {
            String sender_name = "Someone on the console";
            if (sender instanceof Player)
                sender_name = ((Player) sender).getName();
            if (number_of_objects > 1)
                if (settings_type.length == 0) {
                    sender.sendMessage(color + "Your " + number_of_objects + " " + object + "s have been saved.");
                    tellOps(color + sender_name + " saved " + number_of_objects + " " + object + "s to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage(color + "Your " + number_of_objects + " " + object + "s and your " + settings_type[0] + " have been saved.");
                    tellOps(color + sender_name + " saved " + number_of_objects + " " + object + "s to file.", sender instanceof Player, sender.getName());
                }
            else if (number_of_objects == 1)
                if (settings_type.length == 0) {
                    sender.sendMessage(color + "Your 1 " + object + " has been saved.");
                    tellOps(color + sender_name + " saved the server's 1 " + object + " to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage(color + "Your 1 " + object + " and your " + settings_type[0] + " settings have been saved.");
                    tellOps(color + sender_name + " saved the server's 1 " + object + " to file.", sender instanceof Player, sender.getName());
                }
            else if (settings_type.length == 0) {
                sender.sendMessage(color + "You have no " + object + "s to save!");
                tellOps(color + sender_name + " tried to save the server's " + object + "s to file, but there were no " + object + "s on the server to save.",
                        sender instanceof Player, sender.getName());
            } else {
                sender.sendMessage(color + "Your " + settings_type[0] + " settings have been saved.");
                tellOps(color + sender_name + " saved your " + settings_type[0] + " settings.", sender instanceof Player, sender.getName());
            }
        }
    }

    /** This method can filter the pertinent information out of the stack trace of a given thrown Exception and send a detailed error message to all ops on the server
     * describing the error.
     * 
     * @param plugin
     *            is the plugin from which the error originated.
     * @param message
     *            is a brief message describing the type of error given and the context in which the error occurred.
     * @param e
     *            is the exception that was thrown.
     * @see {@link #err(String, Throwable) err(String, Throwable)} */
    public static void err(Plugin plugin, String message, Throwable e) {
        // TODO: test processing "Caused by" scenarios
        tellOps(ChatColor.DARK_RED + message, true);
        /* skip stack trace lines until we get to the part with explicit line numbers and class names that don't come from Java's standard libraries; the stuff we're skipping
         * is anything that comes from the native Java code with no line numbers or class names that will help us pinpoint the issue */
        int lines_to_skip = 0;
        while (lines_to_skip < e.getStackTrace().length
                && (e.getStackTrace()[lines_to_skip].getLineNumber() < 0 || e.getStackTrace()[lines_to_skip].getClassName().startsWith("java")))
            lines_to_skip++;
        while (e != null) {
            // output a maximum of three lines of the stack trace
            tellOps(ChatColor.DARK_RED + e.getClass().getName().substring(e.getClass().getName().lastIndexOf('.') + 1) + " at line "
                    + e.getStackTrace()[lines_to_skip].getLineNumber() + " of " + e.getStackTrace()[lines_to_skip].getClassName() + ".java (" + plugin.getName() + ")", true);
            if (lines_to_skip + 1 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 1].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 1].getClassName() + ".java (" + plugin.getName() + ")", true);
            if (lines_to_skip + 2 < e.getStackTrace().length)
                tellOps(ChatColor.DARK_RED + "  ...and at line " + e.getStackTrace()[lines_to_skip + 2].getLineNumber() + " of "
                        + e.getStackTrace()[lines_to_skip + 2].getClassName() + ".java (" + plugin.getName() + ")", true);
            e = e.getCause();
            if (e != null)
                tellOps(ChatColor.DARK_RED + "...which was caused by:", true);
        }
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
    public static void tellOps(String message, boolean also_tell_console, String... exempt_ops) {
        for (Player player : mCL.server.getOnlinePlayers())
            if (player.isOp() && !AU.contains(exempt_ops, player.getName()))
                player.sendMessage(CU.colorCode(message));
        if (also_tell_console)
            mCL.console.sendMessage(CU.colorCode(message));
    }
}
