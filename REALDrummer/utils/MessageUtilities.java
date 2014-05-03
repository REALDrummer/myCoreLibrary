package REALDrummer.utils;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import REALDrummer.myCoreLibrary;

import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.ColorUtilities.*;
import static REALDrummer.utils.StringUtilities.*;

public class MessageUtilities {
    public static void err(Plugin plugin, String message, Throwable exception, Object... additional_information) {
        err(plugin, message, exception, exception.getClass().getSimpleName(), false, additional_information);
    }

    public static void err(Plugin plugin, String message, String issue, Object... additional_information) {
        err(plugin, message, new NullPointerException(), issue, true, additional_information);
    }

    private static void err(Plugin plugin, String message, Throwable e, String issue_name, boolean skip_one_more, Object... additional_information) {
        // TODO: test and implement in place of err()
        // generate a full description of the issue
        String description = "There was ";

        int lines_to_skip = 0;
        // skip the first line if we created our own exception here to avoid getting useless information
        if (skip_one_more)
            lines_to_skip++;
        // skip stack trace lines until we get to some lines with line numbers associated with them not from the native Java code; those are the lines that will be helpful
        while (lines_to_skip < e.getStackTrace().length
                && (e.getStackTrace()[lines_to_skip].getLineNumber() < 0 || e.getStackTrace()[lines_to_skip].getClassName().startsWith("java")))
            lines_to_skip++;

        // create and format a message that gives only pertinent information from the stack trace
        while (e != null) {
            description += aOrAn(issue_name) + " at line " + e.getStackTrace()[lines_to_skip].getLineNumber() + " of " + e.getStackTrace()[lines_to_skip].getClassName();
            if (lines_to_skip + 1 < e.getStackTrace().length)
                for (int i = lines_to_skip + 1; i < e.getStackTrace().length; i++)
                    if (e.getStackTrace()[i].getLineNumber() < 0 || e.getStackTrace()[i].getClassName().startsWith("java"))
                        break;
                    else
                        description += "\n...and at line " + e.getStackTrace()[i].getLineNumber() + " of " + e.getStackTrace()[i].getClassName();
            e = (Throwable) e.getCause();
            if (e != null)
                message += "\n...which was caused by:\n";
        }
        for (int i = 0; i < additional_information.length; i++)
            description += "\n..." + (i == 0 ? "that involved" : "and") + ":\n" + additional_information[i].toString();

        // display the error (note: \u2639 is a Unicode frowny face)
        tellOps(ChatColor.DARK_RED + plugin.getName() + " had an accident! \u2639\n" + description, true);
    }

    public static void inform(String player, String message) {
        inform(player, message, -1);
    }

    public static void inform(String player, String message, long time_in_millis) {
        // if the player is online, simply send them the message
        Player actual_player = myCoreLibrary.mCL.getServer().getPlayerExact(player);
        if (actual_player != null) {
            actual_player.sendMessage(colorCode(message));
            return;
        }

        // if the player is not online, save the message as a new notice
        ArrayList<String> current_notices = myCoreLibrary.notices.get(player);
        if (current_notices == null)
            current_notices = new ArrayList<String>();
        current_notices.add(message);
        myCoreLibrary.notices.put(player, current_notices);
    }

    public static String paginate(String message, ChatColor COLOR, String command_format, String not_enough_pages, int page_number, boolean not_console) {
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
            String prefix = COLOR + "Here's page " + page_number + " of " + chat_page.getTotalPages() + "!\n";
            if (page_number > 1)
                prefix += ChatColor.WHITE + "...";
            page = prefix + page;
        }
        // if there are more pages, add a suffix notifying the user
        if (chat_page.getTotalPages() > page_number)
            page +=
                    ChatColor.WHITE + "...\n" + COLOR + "Use " + replaceAll(ChatColor.ITALIC + command_format, "[#]", String.valueOf(page_number + 1)) + COLOR
                            + " to see more.";
        return page;
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
        for (Player player : myCoreLibrary.mCL.getServer().getOnlinePlayers())
            if (player.isOp() && !contains(exempt_ops, player.getName()))
                player.sendMessage(colorCode(message));
        if (also_tell_console)
            myCoreLibrary.mCL.getServer().getConsoleSender().sendMessage(colorCode(message));
    }

    public static void tellOps(String message, String... exempt_ops) {
        tellOps(message, true, exempt_ops);
    }
}