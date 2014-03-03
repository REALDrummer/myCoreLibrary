package REALDrummer;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import static REALDrummer.ArrayUtilities.*;
import static REALDrummer.MessageUtilities.*;

public class StringUtilities {
    public static final String[] BORDERS = { "[]", "\\/", "\"*", "_^", "-=", ":;", "&%", "#@", ",.", "<>", "~$", ")(", "+-", "|o" };

    public static String border() {
        String border_unit = BORDERS[(int) (Math.random() * BORDERS.length)], border = "";
        for (int i = 0; i < 20; i++)
            border += border_unit;
        return border;
    }

    public static boolean isBorder(String test) {
        if (test.length() == 40) {
            for (String border : BORDERS)
                if (test.contains(border)) {
                    test = replaceAll(test, border, "");
                    break;
                }
            if (test.equals(""))
                return true;
        }
        return false;
    }

    public static HashMap<String, String> readData(String save_line, String format) {
        myCoreLibrary.debug("reading data; save line=\"" + save_line + "\"; format=\"" + format + "\"");

        // HashMap<input name from format, value for that input from save_line>
        HashMap<String, String> data = new HashMap<String, String>();
        while (format.contains("[") || format.contains("]")) {
            myCoreLibrary.debug("reading next input...");

            /* start by cutting off everything before the next input in the format and removing the same number of characters from the beginning of the save line. This puts
             * the next input point right at the beginning of both the format and save_line Strings */
            save_line = save_line.substring(format.indexOf('['));
            format = format.substring(format.indexOf('['));
            myCoreLibrary.debug("removed precedent from Strings: format=\"" + format + "\"; save line=\"" + save_line + "\"");

            // next, isolate the String between the current input that we're analyzing (the one at the beginning of both Strings) and the next input
            String successor;
            if (format.substring(1).contains("["))
                successor = format.substring(format.indexOf(']') + 1, format.substring(1).indexOf('['));
            else
                successor = format.substring(format.indexOf(']') + 1);
            myCoreLibrary.debug("successor = \"" + successor + "\"");

            // now use the successor obtained to find the end of the input in the save line and put it in the data HashMap
            data.put(format.substring(1, format.indexOf(']')), save_line.substring(0, save_line.indexOf(successor)));
            myCoreLibrary.debug("data read: variable name=\"" + format.substring(1, format.indexOf(']')) + "\"; data=\""
                    + save_line.substring(0, save_line.indexOf(successor)) + "\"");

            // finally, remove the input data from format and save_line now that we're done reading it
            format = format.substring(format.indexOf(successor));
            save_line = save_line.substring(format.indexOf(successor));
            myCoreLibrary.debug("data input removed: format=\"" + format + "\"; save line=\"" + save_line + "\"");
        }

        // return the data obtained
        return data;
    }

    public static Location readLocation(String string, boolean display_errors) {
        // location format: ([x], [y], [z]) (facing ([pitch], [yaw])) in "[world]"
        String[] temp = string.split(", ");
        float pitch = 0, yaw = 0;
        try {
            if (string.indexOf(") facing (") > 0) {
                String tempS = string.substring(string.indexOf(") facing (") + 10, string.indexOf(") in \""));
                pitch = Float.parseFloat(tempS.split(", ")[0]);
                yaw = Float.parseFloat(tempS.split(", ")[1]);
            }
            World world = myCoreLibrary.server.getWorld(string.substring(string.indexOf("\""), string.length() - 1));
            if (world == null) {
                if (display_errors) {
                    tellOps(ChatColor.DARK_RED + "I couldn't read the world name on this location String!", true);
                    tellOps(ChatColor.DARK_RED + "location: \"" + ChatColor.WHITE + string + ChatColor.DARK_RED + "\"", true);
                    tellOps(ChatColor.DARK_RED + "I read \"" + ChatColor.WHITE + string.substring(string.indexOf("\"")) + ChatColor.DARK_RED + "\" as the world name!", true);
                }
                return null;
            }
            return new Location(world, Double.parseDouble(temp[0].substring(1)), Double.parseDouble(temp[1]), Double.parseDouble(temp[2].substring(0, temp[2].indexOf(")"))),
                    yaw, pitch);
        } catch (NumberFormatException exception) {
            if (display_errors) {
                tellOps(ChatColor.DARK_RED + "I couldn't read this location message properly!", true);
                tellOps(ChatColor.DARK_RED + "location: \"" + ChatColor.WHITE + string + ChatColor.DARK_RED + "\"", true);
            }
            return null;
        }
    }

    public static HashMap<String, String> readParameters(String format, String[] parameters, byte... indices) {
        /* [?] = required parameter
         * 
         * (?) = optional parameter
         * 
         * [?...] or (?...) = parsing parameters can include spaces and can continue indefinitely
         * 
         * ["?"] or ("?") = literal parameters are the actual word or character in quotes ("?" here)
         * 
         * ("$"?) or (?"$") or ("$"?"$$") or with "[]"s = specific parameters must start and/or end with a given String or Strings to be accepted
         * 
         * [?],[?] or (?),(?) = list parameters can appear in any order; some may appear multiple times and more than one may appear
         * 
         * [?]/[?] or (?)/(?) = multi-option parameters can be ONE of these options */
        // if this command does not have parameters, ignore this method call and return an empty HashMap
        if (format.length() == 0)
            return new HashMap<String, String>();

        if (format.endsWith("\n"))
            format = format.substring(0, format.length() - 1);

        myCoreLibrary.debug("reading parameters; parameters=\"" + combine(parameters, " ") + "\"; format=\"" + format + "\"");

        // interpret the index values if any were given
        byte start = 0, end = (byte) parameters.length;
        if (indices != null && indices.length > 0) {
            start = indices[0];
            if (indices.length > 1)
                end = indices[1];
        }

        // HashMap<input name from format, value for that input from parameters>
        HashMap<String, String> data = new HashMap<String, String>();

        String[] format_parts = format.split(" ");
        // parsing is used to keep track of the input being parsed over multiple parameters if there is one, e.g. for a warp message
        String parsing = null;
        // i is for the parameters given themselves; j is for parsing through format_parts
        for (byte i = start, j = 0; i < end && (j < format_parts.length || parsing != null);) {
            // keep j on the last parameter if it has been over-incremented because of parameters for a parsing variable that were left untouched
            if (parsing != null && j >= format_parts.length)
                j = (byte) (format_parts.length - 1);

            /* this part generalizes the current format part, allowing us to basically use the same algorithm for a format part whether it is one possiblity or a series of
             * possibilities that may appear in any order */
            String[] options = new String[] { format_parts[j] };
            if (format_parts[j].contains(","))
                options = format_parts[j].split(",");
            else if (format_parts[j].contains("/"))
                options = format_parts[j].split("/");
            myCoreLibrary.debug(options.length + " option(s) found...");

            boolean fits = false;
            // first, see if the current parameter fits the current format part; skip to the next format part if the current format part is optional and doesn't fit
            for (String option : options) {
                myCoreLibrary.debug("testing option " + option + "...");

                // determine the starting and terminating Strings needed if it's a specific parameter; default to "" because .startsWith("") >> true
                String required_starter = "", required_terminator = "";
                if (option.contains("\"")) {
                    if (option.toCharArray()[1] == '"')
                        required_starter = option.substring(2, option.substring(2).indexOf('"') + 2);
                    if (option.toCharArray()[option.length() - 2] == '"')
                        required_terminator = option.substring(option.substring(0, option.lastIndexOf('"')).lastIndexOf('"') + 1, option.length() - 2);
                }
                myCoreLibrary.debug("required starter=\"" + required_starter + "\"; required terminator=\"" + required_terminator + "\"");

                boolean specific = required_starter.length() != 0 && (!required_starter.equals(required_terminator) || option.split("\"").length > 3);

                String option_name =
                        option.substring(1 + (specific && required_starter.length() > 0 ? 2 + required_starter.length() : 0), option.length() - 1
                                - (specific && required_terminator.length() > 0 ? 2 + required_terminator.length() : 0));
                myCoreLibrary.debug("option name=\"" + option_name + "\"");

                // see if the current parameter fits this option's requirements
                if (parameters[i].startsWith(required_starter) && parameters[i].endsWith(required_terminator)) {
                    myCoreLibrary.debug("match identified: \"" + parameters[i] + "\" instance of " + option);
                    data.put(option_name, parameters[i].substring(specific ? required_starter.length() : 0, parameters[i].length()
                            - (specific ? required_terminator.length() : 0)));
                    fits = true;

                    // begin parsing this variable if it is a parsing parameter
                    if (option_name.endsWith("...")) {
                        parsing = option_name;
                        myCoreLibrary.debug("parsing parameter; parsing " + option_name);
                    } // if it is not a parsing parameter, make any current parsing stop
                    else if (parsing != null) {
                        myCoreLibrary.debug("stopping parsing " + parsing + "...");
                        parsing = null;
                    }

                    // increment the format parts (if it's not a list parameter) and break
                    i++;
                    if (!format_parts[j].contains(","))
                        j++;
                    break;
                }
            }

            if (!fits)
                // if the current parameter did not fit the current format part, add the current parameter to the end of the current parsing variable if there is one
                if (parsing != null) {
                    data.put(parsing, data.get(parsing) + " " + parameters[i]);
                    myCoreLibrary.debug("added parameter to current parsing: " + parsing + "=\"" + data.get(parsing) + "\"");
                    i++;
                } else if (format_parts[j].startsWith("(") && !(j == format_parts.length - 1 && format_parts[j].contains(","))) {
                    myCoreLibrary.debug("no match, but parameter is optional; skipping to next format part...");
                    j++;
                } else {
                    myCoreLibrary.debug("WARNING: unrecognized parameter \"" + parameters[i] + "\"; ignoring...");
                    i++;
                }
        }

        for (String key : data.keySet())
            myCoreLibrary.debug("\"" + key + "\" >> \"" + data.get(key) + "\"");
        return data;
    }

    /** This method is used to interpret the answers to questions.
     * 
     * @param unformatted_response
     *            is the raw String message that will be formatted in this message to be all lower case with no punctuation and analyzed for a "yes" or "no" answer.
     * @param current_status_line
     *            is for use with the <tt>config.txt</tt> questions only; it allows this method to default to the current status of a configuration if no answer is given to a
     *            <tt>config.txt</tt> question.
     * @param current_status_is_true_message
     *            is for use with the <tt>config.txt</tt> questions only; it allows this method to compare <b>current_status_line</b> to this message to determine whether or
     *            not the current status of the configuration handled by this config question is <b>true</b> or <b>false</b>.
     * @return <b>for chat responses:</b> <b>true</b> if the response matches one of the words or phrases in <tt>yeses</tt>, <b>false</b> if the response matches one of the
     *         words or phrases in <tt>nos</tt>, or <b>null</b> if the message did not seem to answer the question. <b>for <tt>config.txt</tt> question responses:</b>
     *         <b>true</b> if the answer to the question matches one of the words or phrases in <tt>yeses</tt>, <b>false</b> if the answer to the question matches one of the
     *         words or phrases in <tt>nos</tt>. If there is no answer to the question or the answer does not match a "yes" or a "no" response, it will return <b>true</b> if
     *         <b><tt>current_status_line</tt></b> matches <b> <tt>current_status_is_true_message</tt></b> or <b>false</b> if it does not. */
    public static Boolean readResponse(String unformatted_response, String current_status_line, String current_status_is_true_message) {
        String[] yeses =
                { "yes", "yea", "yep", "ja", "sure", "why not", "ok", "do it", "fine", "whatever", "w/e", "very well", "accept", "tpa", "cool", "hell yeah", "hells yeah",
                        "hells yes", "come", "k ", "kk" }, nos =
                { "no ", "nah", "nope", "no thanks", "no don't", "shut up", "ignore", "it's not", "its not", "creeper", "unsafe", "wait", "one ", "1 " };
        boolean said_yes = false, said_no = false;
        String formatted_response = unformatted_response;
        // elimiate unnecessary spaces and punctuation
        while (formatted_response.startsWith(" "))
            formatted_response = formatted_response.substring(1);
        while (formatted_response.endsWith(" "))
            formatted_response = formatted_response.substring(0, formatted_response.length() - 1);
        formatted_response = formatted_response.toLowerCase();
        // check their response
        for (String yes : yeses)
            if (formatted_response.startsWith(yes))
                said_yes = true;
        if (said_yes)
            return true;
        else {
            for (String no : nos)
                if (formatted_response.startsWith(no))
                    said_no = true;
            if (said_no)
                return false;
            else if (current_status_line != null)
                if (current_status_line.trim().startsWith(current_status_is_true_message))
                    return true;
                else
                    return false;
            else
                return null;
        }
    }

    /** This method is used to interpret the answers to questions.
     * 
     * @param unformatted_response
     *            is the raw String message that will be formatted in this message to be all lower case with no punctuation and analyzed for a "yes" or "no" answer.
     * @return <b>true</b> if the response matches one of the words or phrases in <tt>yeses</tt>, <b>false</b> if the response matches one of the words or phrases in
     *         <tt>nos</tt>, or <b>null</b> if the message did not seem to answer the question. */
    public static Boolean readResponse(String unformatted_response) {
        return readResponse(unformatted_response, null, null);
    }

    public static int readRomanNumeral(String roman_numeral) {
        int value = 0;
        char[] chars = new char[] { 'M', 'D', 'C', 'L', 'X', 'V', 'I' };
        int[] values = new int[] { 1000, 500, 100, 50, 10, 5, 1 };
        while (roman_numeral.length() > 0) {
            char[] digits = roman_numeral.trim().toUpperCase().toCharArray();
            int digit_value = 0;
            for (int i = 0; i < chars.length; i++)
                if (digits[0] == chars[i])
                    digit_value = values[i];
            if (digit_value == 0)
                return 0;
            int zeroless = digit_value;
            while (zeroless >= 10)
                zeroless = zeroless / 10;
            if (digits[0] != chars[0] && zeroless == 1 && digits.length > 1) {
                // if the digit value starts with a 1 and it's not 'M', it could be being used to subtract from the subsequent digit (e.g. "IV"); however, this
                // can only be true if the subsequent digit has a greater value than the current one
                int next_digit_value = 0;
                for (int i = 0; i < chars.length; i++)
                    if (digits[1] == chars[i])
                        next_digit_value = values[i];
                if (next_digit_value == 0)
                    return 0;
                // so, if the current digit's value is less than the subsequent digit's value, the current digit's value must be subtracted, not added
                if (next_digit_value > digit_value)
                    value -= digit_value;
                else
                    value += digit_value;
            } else
                value += digit_value;
            roman_numeral = roman_numeral.substring(1).toLowerCase();
        }
        return value;
    }

    /** This method can translate a String of time terms and values to a single int time in milliseconds (ms). It can interpret a variety of formats from "2d 3s 4m" to
     * "2 days, 4 minutes, and 3 seconds" to "2.375 minutes + 5.369s & 3.29days". Punctuation is irrelevant. Spelling is irrelevant as long as the time terms begin with the
     * correct letter. Order of values is irrelevant. (Days can come before seconds, after seconds, or both.) Repetition of values is irrelevant; all terms are simply
     * converted to ms and summed. Integers and decimal numbers are equally readable. The highest time value it can read is days; it cannot read years or months (to avoid the
     * complications of months' different numbers of days and leap years).
     * 
     * @param written
     *            is the String to be translated into a time in milliseconds (ms).
     * @return the time given by the String <b><tt>written</b></tt> translated into milliseconds (ms). */
    public static int readTime(String written) {
        int time = 0;
        String[] temp = written.split(" ");
        ArrayList<String> words = new ArrayList<String>();
        for (String word : temp)
            if (!word.equalsIgnoreCase("and") && !word.equalsIgnoreCase("&"))
                words.add(word.toLowerCase().replaceAll(",", ""));
        while (words.size() > 0) {
            // for formats like "2 days 3 minutes 5.57 seconds" or "3 d 5 m 12 s"
            try {
                double amount = Double.parseDouble(words.get(0));
                if (words.get(0).contains("d") || words.get(0).contains("h") || words.get(0).contains("m") || words.get(0).contains("s"))
                    throw new NumberFormatException();
                int factor = 0;
                if (words.size() > 1) {
                    if (words.get(1).startsWith("d"))
                        factor = 86400000;
                    else if (words.get(1).startsWith("h"))
                        factor = 3600000;
                    else if (words.get(1).startsWith("m"))
                        factor = 60000;
                    else if (words.get(1).startsWith("s"))
                        factor = 1000;
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                    words.remove(0);
                    words.remove(0);
                } else
                    words.remove(0);
            } catch (NumberFormatException e) {
                // if there's no space between the time and units, e.g. "2h, 5m, 25s" or "4hours, 3min, 2.265secs"
                double amount = 0;
                int factor = 0;
                try {
                    if (words.get(0).contains("d") && (!words.get(0).contains("s") || words.get(0).indexOf("s") > words.get(0).indexOf("d"))) {
                        amount = Double.parseDouble(words.get(0).split("d")[0]);
                        factor = 86400000;
                    } else if (words.get(0).contains("h")) {
                        amount = Double.parseDouble(words.get(0).split("h")[0]);
                        factor = 3600000;
                    } else if (words.get(0).contains("m")) {
                        amount = Double.parseDouble(words.get(0).split("m")[0]);
                        factor = 60000;
                    } else if (words.get(0).contains("s")) {
                        amount = Double.parseDouble(words.get(0).split("s")[0]);
                        factor = 1000;
                    }
                    if (factor > 0)
                        // since a double of, say, 1.0 is actually 0.99999..., (int)ing it will reduce exact numbers by one, so I added 0.1 to it to avoid that.
                        time = time + (int) (amount * factor + 0.1);
                } catch (NumberFormatException e2) {
                    //
                }
                words.remove(0);
            }
        }
        return time;
    }

    /** This method replaces every instance of each String given in the text given with another String. This method has a few advantages over Java's standard
     * <tt>String.replaceAll(String, String)</tt> method: <b>1)</b> this method can replace multiple Strings with other Strings using a single method while
     * <tt>String.replaceAll(String, String)</tt> only has the ability to replace one String with one other String and <b>2)</b> this method treats brackets ("[]"), hyphens
     * ("-"), braces ("{}"), and other symbols normally whereas many of these symbols have special meanings in <tt>String.replaceAll(String, String)</tt>.
     * 
     * @param text
     *            is the text that must be modified.
     * @param changes
     *            are the changes that must be made to <b><tt>text</b></tt>. Every even-numbered item in this list will be replaced by the next (odd-numbered) String given;
     *            for example, if the four parameters given for <b><tt>changes</b></tt> are <tt>replaceAll(...,"wierd", "weird", "[player]", player.getName())</tt>, this
     *            method will replace all instances of "wierd" with "weird" and all instances of "[player]" with <tt>player.getName()</tt> in <b><tt>text</b></tt>.
     * @return <b><tt>text</b></tt> will all modifications given in <b><tt>changes</b></tt> made. */
    public static String replaceAll(String text, String... changes) {
        if (changes.length == 0)
            return text;
        for (int j = 0; j < changes.length; j += 2) {
            if (!text.toLowerCase().contains(changes[j].toLowerCase()))
                return text;
            for (int i = 0; text.length() >= i + changes[j].length(); i++) {
                if (text.substring(i, i + changes[j].length()).equalsIgnoreCase(changes[j])) {
                    text = text.substring(0, i) + changes[j + 1] + text.substring(i + changes[j].length());
                    i += changes[j + 1].length() - 1;
                }
                if (!text.toLowerCase().contains(changes[j].toLowerCase()))
                    break;
            }
        }
        return text;
    }

    public static String writeData(String format, String... inputs) {
        myCoreLibrary.debug("writing data; format=\"" + format + "\"; inputs=" + writeArray(inputs));

        // progress will be used to ensure that no text contained in "[]"s from intersted data will be replaced like the input points
        int i = 0, progress = 0;
        for (i = 0; i < inputs.length && format.substring(progress).contains("[") && format.substring(progress).contains("]"); i++) {
            /* save the location of progress into a temporary int to increment progress after it is used below; progress cannot be incremented now because its old value is to
             * be used below and it cannot be incremented directly afterward because the "[]"s used to determine the position of progress will be replaced at that stage */
            int temp = format.substring(progress).indexOf('[') + progress + inputs[i].length();

            // replace all the instances of the next input point with the next input
            format =
                    replaceAll(format, format.substring(format.substring(progress).indexOf('[') + progress, format.substring(progress).indexOf(']') + progress + 1), inputs[i]);

            // increment progress to the value saved earlier
            progress = temp;
        }

        // generate debug warnings if any indicators of issues are present
        if (i < inputs.length - 1)
            myCoreLibrary.debug(ChatColor.RED + "WARNING: " + (inputs.length - 1 - i) + " inputs were not used");
        if (format.contains("[") && format.contains("]"))
            myCoreLibrary.debug(ChatColor.RED + "WARNING: data may contain unused input points");
        myCoreLibrary.debug("data=\"" + format + "\"");

        return format;
    }

    public static String writeLocation(Location location, boolean use_block_coordinates, boolean include_pitch_and_yaw) {
        // location format: ([x], [y], [z]) (facing ([pitch], [yaw])) in "[world]"
        String string = "(";
        if (use_block_coordinates)
            string += location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ") ";
        else
            string += location.getX() + ", " + location.getY() + ", " + location.getZ() + ") ";
        if (include_pitch_and_yaw && (location.getPitch() != 0 || location.getYaw() != 0))
            string += "facing (" + location.getPitch() + ", " + location.getYaw() + ") ";
        return string + "in \"" + location.getWorld().getWorldFolder().getName() + "\"";
    }

    public static String writeLocation(Location location, boolean use_block_coordinates) {
        return writeLocation(location, use_block_coordinates, true);
    }

    public static String writeLocation(Entity entity, boolean use_block_coordinates, boolean include_pitch_and_yaw) {
        return writeLocation(entity.getLocation(), use_block_coordinates, include_pitch_and_yaw);
    }

    public static String writeLocation(Entity entity, boolean use_block_coordinates) {
        return writeLocation(entity, use_block_coordinates, true);
    }

    public static String writeLocation(Block block) {
        return writeLocation(block.getLocation(), true, false);
    }

    public static String writeRomanNumeral(int value) {
        String roman_numeral = "";
        String[] chars = new String[] { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
        int[] values = new int[] { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        for (int i = 0; i < chars.length; i++)
            while (value >= values[i]) {
                roman_numeral += chars[i];
                value -= values[i];
            }
        return roman_numeral;
    }

    /** This method is the inverse counterpart to the {@link #readTime(long, boolean) translateStringToTimeInms()} method. It can construct a String to describe an amount of
     * time in ms in an elegant format that is readable by the aforementioned counterpart method as well as human readers.
     * 
     * @param time
     *            is the time in milliseconds (ms) that is to be translated into a readable phrase.
     * @param round_seconds
     *            determines whether or not the number of seconds should be rounded to make the phrase more elegant and readable to humans. This parameter is normally false if
     *            this method is used to save data for the plugin because we want to be as specific as possible; however, for messages sent to players in game, dropping excess
     *            decimal places makes the phrase more friendly and readable.
     * @return a String describing <b><tt>time</b></tt> */
    public static String writeTime(int time, boolean round_seconds) {
        // get the values (e.g. "2 days" or "55.7 seconds")
        ArrayList<String> values = new ArrayList<String>();
        if (time > 86400000) {
            if ((int) (time / 86400000) > 1)
                values.add((int) (time / 86400000) + " days");
            else
                values.add("1 day");
            time = time % 86400000;
        }
        if (time > 3600000) {
            if ((int) (time / 3600000) > 1)
                values.add((int) (time / 3600000) + " hours");
            else
                values.add("1 hour");
            time = time % 3600000;
        }
        if (time > 60000) {
            if ((int) (time / 60000) > 1)
                values.add((int) (time / 60000) + " minutes");
            else
                values.add("1 minute");
            time = time % 60000;
        }
        // add a seconds value if there is still time remaining or if there are no other values
        if (time > 0 || values.size() == 0)
            // if you have partial seconds and !round_seconds, it's written as a double so it doesn't truncate the decimals
            if ((time / 1000.0) != (time / 1000) && !round_seconds)
                values.add((time / 1000.0) + " seconds");
            // if seconds are a whole number, just write it as a whole number (integer)
            else if (Math.round(time / 1000) > 1)
                values.add(Math.round(time / 1000) + " seconds");
            else
                values.add("1 second");
        // if there are two or more values, add an "and"
        if (values.size() >= 2)
            values.add(values.size() - 1, "and");
        // assemble the final String
        String written = "";
        for (int i = 0; i < values.size(); i++) {
            // add spaces as needed
            if (i > 0)
                written = written + " ";
            written = written + values.get(i);
            // add commas as needed
            if (values.size() >= 4 && i < values.size() - 1 && !values.get(i).equals("and"))
                written = written + ",";
        }
        if (!written.equals(""))
            return written;
        else
            return null;
    }

}