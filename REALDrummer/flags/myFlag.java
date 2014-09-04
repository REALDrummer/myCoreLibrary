package REALDrummer.flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import REALDrummer.myList;
import REALDrummer.myListIterator;
import REALDrummer.myPlugin;
import REALDrummer.interfaces.Matchable;
import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.ListUtilities.compare;
import static REALDrummer.utils.ListUtilities.match;
import static REALDrummer.utils.ListUtilities.writeArray;
import static REALDrummer.utils.StringUtilities.*;

/** The <tt>myFlag</tt> class represents a parameter in a command. Its purpose is to not only contain the value of the parameter and ease command parsing, but to translate it
 * into the necessary form -- be it a plain <tt>String</tt>, an <b>int</b>, a list of <tt>String</tt>s, or an autocompleted {@link org.bukkit.entity.Player Player}'s name --
 * pair it with a name for the parameter and pass it easily and conveniently to a method to handle the command.
 * 
 * @author connor */
public class myFlag implements Comparable<myFlag>, Matchable {
    /** This <tt>String</tt> represents the default final conjunction for "L"-type ({@link myFlagType#LIST LIST}-type) {@link myFlag myFlags}. The final conjunction will be
     * used to separate the items in a two-item list or the second to last and last items of a 3+-item list. The recommended values for an "L"-type {@link myFlag}'s final
     * conjunction are "and" and "or". */
    public static final String DEFAULT_FINAL_CONJUNCTION = "and";

    /** This {@link myList} of {@link myFlagOrder}s keep track of the {@link myFlag}s for all the commands for all of the {@link myPlugin}s currently present on the server. It
     * is automatically sorted by the command's name and secondarily by the {@link myFlagOrder}'s order in the command. */
    public static final myList<myFlagOrder> flags = new myList<myFlagOrder>();

    private String name;
    private boolean flagged, parsing;
    private myFlagType type = null;
    private Object additional_info;

    /** This constructor is the only constructor for the {@link myFlag} <tt>Object</tt>. {@link myFlag MyFlag}s are used in command parsing for storing data on the parameters
     * of any given command, inclusing not only the value of the parameter, but also the name of the parameter and whether or not the parameter's value may stretch across
     * multiple space-separated words.
     * 
     * @param name
     *            is the name of the flag. This name is a one-word description of the value of the parameter as well as the full name of the flag if any is required in
     *            designating the parameter.
     * @param flagged
     *            determines whether or not the {@link myFlag} must be explicitly stated with a flag (beginning with "<i>--</i>") in command, e.g. "<i>--type private</i>".
     * @param parsing
     *            determines whether or not this flag's parameter may be multiple words in length (separated by spaces).
     * @param type_char
     *            is a <b>char</b> found in the flag's usage describing the type of {@link myFlag}. See {@link myFlagType} for a full listing of the possible flag types and
     *            their associated type <b>char</b>s.
     * @param additional_info
     *            is a <tt>String</tt> representing any additional information given in the flag's desclaration in the command's usage. Most {@link myFlag}s do not have any
     *            additional */
    private myFlag(String name, boolean flagged, boolean parsing, char type_char, String additional_info) {
        this.name = name;
        this.flagged = flagged;
        this.parsing = parsing;

        // determine this myFlag's myFlagType
        for (myFlagType type : myFlagType.values())
            if (type.getTypeChar() == type_char) {
                this.type = type;
                break;
            }
        if (type == null) {
            mCL.err("I couldn't identify this myFlag's type!", "un-classifiable myFlag", type_char);
            return;
        }

        switch (type) {
            case OPTION:
                // for OPTION-type myFlags, the additional info is the list of possible options split by "/"s
                this.additional_info = additional_info.split("/");
                break;
            case LIST:
                // for LIST-type flags, the additional info is an optional final conjunction
                if (additional_info == null || additional_info.trim().equals(""))
                    this.additional_info = DEFAULT_FINAL_CONJUNCTION;
                else
                    this.additional_info = additional_info.replaceAll("\"", "").trim();
                break;
            default:
                // other myFlags do not need additional_info
                this.additional_info = null;
        }
    }

    // inner classes
    private enum myFlagOrderRequirement {
        SINGLE_OPTIONAL, MULTIPLE_OPTIONAL, SINGLE_REQUIRED, SINGLE_OR_MORE_REQUIRED, ALL_REQUIRED;
    }

    public static class myFlagOrder implements Comparable<myFlagOrder>, Matchable {
        private myList<myFlag> flags;

        private myPlugin plugin;
        private String command;
        private byte order;
        private myFlagOrderRequirement requirement;

        private myFlagOrder(myPlugin plugin, String command, byte order, myFlagOrderRequirement requirement, myList<myFlag> flags) {
            this.plugin = plugin;
            this.command = command;
            this.order = order;
            this.requirement = requirement;
            this.flags = flags;
        }

        // getters
        public String getCommand() {
            return command;
        }

        public myList<myFlag> getFlags() {
            return flags;
        }

        public byte getOrder() {
            return order;
        }

        // utilities
        public boolean allowsMultiple() {
            return requirement == myFlagOrderRequirement.MULTIPLE_OPTIONAL || requirement.ordinal() >= myFlagOrderRequirement.SINGLE_OR_MORE_REQUIRED.ordinal();
        }

        public boolean isOptional() {
            return requirement.ordinal() <= myFlagOrderRequirement.MULTIPLE_OPTIONAL.ordinal();
        }

        public boolean isRequired() {
            return requirement.ordinal() >= myFlagOrderRequirement.SINGLE_REQUIRED.ordinal();
        }

        // overrides
        @Override
        public int compareTo(myFlagOrder order) {
            return compare(new Object[] { plugin, command, order }, new Object[] { order.command, order.order });
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof myFlagOrder && ((myFlagOrder) object).command.equals(command) && ((myFlagOrder) object).order == order;
        }

        @Override
        public int matchTo(String... match_parameters) {
            return match(new Object[] { plugin.getName(), command, order }, match_parameters);
        }

        @Override
        public String toString() {
            // assemble a list of all the flags' toString() outputs
            String[] flag_descriptions = new String[flags.size()];
            byte i = 0;
            for (myFlag flag : flags) {        // I used a foreach here because myLists are faster with iterators than with fetching from indices
                flag_descriptions[i] = flag.toString();
                i++;
            }

            // return the result
            return '/' + command + "[" + order + "]: " + writeArray(flag_descriptions, allowsMultiple() ? "," : "/", "", isOptional() ? "(" : "[", isOptional() ? ")" : "]");
        }
    }

    private static enum myFlagType {
        LITERAL('\0'), BOOLEAN, DECIMAL, INTEGER, LIST, OPTION, PLAYER, STRING, TIME;

        private char type_char;

        private myFlagType() {
            type_char = name().charAt(0);
        }

        private myFlagType(char type_char) {
            this.type_char = type_char;
        }

        public char getTypeChar() {
            return type_char;
        }
    }

    // getters
    /** This method retrieves the name of this {@link myFlag}. Names are one-word <tt>String</tt>s that describe the value of the {@link myFlag} as well as give the name of the
     * flag used for designating this parameter if one is used.
     * 
     * @return the one-word name of this {@link myFlag}. */
    public String getName() {
        return name;
    }

    /** This method determines whether or not this {@link myFlag} requires a flagged designation to be recognized, e.g. "--flag value" vs. a simple "value".
     * 
     * @return <b>true</b> if this {@link myFlag} requires a flag; <b>false</b> otherwise. */
    public boolean isFlagged() {
        return flagged;
    }

    /** This method determines whether or not this {@link myFlag}'s value may be given across multiple space-delimited words in the command.
     * 
     * @return <b>true</b> if this {@link myFlag} is contains a value that may be more than one space-delimited word; <b>false</b> otherwise. */
    public boolean isParsing() {
        return parsing;
    }

    // readers
    /** This method can read a command when it is executed, match the parameters of the command to flags generated from the command's usage, and parse those matches parameters
     * into a {@link HashMap}<<tt>String</tt>, <tt>Object</tt>> to be given as the second parameter to the command method that corresponds to the command given.
     * 
     * @param plugin
     *            is the {@link myPlugin} that received the command.
     * @param sender
     *            is the {@link CommandSender} that executed the command.
     * @param command
     *            is the primary alias of the command that was sent.
     * @param parameters
     *            is the <tt>String[]</tt> of parameters given with the command.
     * @return a {@link HashMap}<<tt>String</tt>, <tt>Object</tt>> in which the keys are the names of the flags that were successfully matched to the given parameters and the
     *         values are the <tT>Object</tt>s parsed into the flags and translated into a usable form as designated by the {@link myFlagType}, e.g. an <tt>Integer</tt> for
     *         {@link myFlagType#INTEGER}s; <b>null</b> if any errors occurred such as missing required parameters or non-matching parameter types. */
    public static HashMap<String, Object> readCommand(myPlugin plugin, CommandSender sender, String command, String[] parameters) {
        HashMap<String, Object> results = new HashMap<String, Object>();

        // find the first myFlagOrder for this plugin's command
        myList<myFlagOrder> order_node = flags.findMatchingNode(plugin.getName(), command);
        if (order_node == null) {
            plugin.debug("no usage parameters found for /" + command + "; returning empty results...");
            return results;
        }
        myListIterator<myFlagOrder> order_iterator = order_node.iterator();

        // define the current myFlagOrder and myFlag using the order_iterator
        myFlagOrder order = order_iterator.next();
        myFlag flag = null;  // keep flag null until we match a parameter to a flag

        // have a variable outside of the loop for keeping track of parsing flag values and used flags
        String parsing = "";
        myList<myFlag> used_flags = new myList<myFlag>();

        // go through the parameters and isolate the data
        plugin.debug("beginning parameter reading...");
        for (String parameter : parameters) {
            plugin.debug("reading parameter: \"" + parameter + "\"");

            // first, if the last flag found required a one-word value, read this parameter in as that value immediately
            if (flag != null && !flag.parsing) {
                plugin.debug("last flag requires value; reading in value...");
                Object read_value = flag.readValue(parameter);
                if (read_value instanceof String && ((String) read_value).startsWith("\\E:")) {
                    plugin.debug("received error \"" + ((String) read_value).substring(0, 3) + "\"; cancelling command...");
                    sender.sendMessage(ChatColor.RED + ((String) read_value).substring(0, 3));
                    return null;
                } else {
                    plugin.debug("received value \"" + read_value.toString() + "\"; adding to results...");
                    results.put(flag.name, read_value);
                }

                // add this flag to the used flags list
                used_flags.add(flag);

                // set flag to null to ensure that it is not parsed any longer
                flag = null;

                // since this parameter was used as the value of a flag, skip the rest of this loop
                continue;
            }

            // if the previous flag was not in need of a value, see if the parameter identifies as a flag
            plugin.debug("analyzing possibility of flag parameter...");

            // make localized copies of order and order_iterator so that if this parameter ends up not matching a flag, order_iterator's place will not be lost
            myListIterator<myFlagOrder> Lorder_iterator = order_iterator.clone();
            myFlagOrder Lorder = order;
            myList<myFlag> Lused_flags = used_flags;

            /* make a localized flag variable to keep track of the last found flag, which is important if it's a parsing flag because if we match this current parameter to a
             * new flag, that old parsing flag needs to be added to the results */
            myFlag Lflag = flag;

            // if the order used to capture the last flag doesn't allow multiple flags, then we must skip past it to the next order
            plugin.debug("preparing to parse order \"" + Lorder.toString() + "\"");
            if (!Lorder.allowsMultiple()) {
                Lorder = Lorder_iterator.next();
                Lused_flags = new myList<myFlag>();
                plugin.debug("previous order was singluar; skipping to next order \"" + Lorder.toString() + "\"...");
            }

            /* attempt to find a flag matching the current parameter in this order or a later one; if we find a match or we reach the end of the order list or we find no match
             * in a required order, end the loop */
            plugin.debug("searching for matching flag in order \"" + Lorder.toString() + "\"...");
            Lflag = Lorder.getFlags().findMatch(parameter);
            while (Lflag == null && Lorder != null) {
                // findMatch will find the first element matching the given parameters; now, we must skip past all the flag that have already been used
                int Lflag_index;
                while (Lflag != null && (Lflag_index = Lused_flags.find(Lflag)) != -1)
                    Lflag = Lorder.getFlags().get(Lflag_index + 1);

                // if insufficient flags were used from a required order, assume it's not a flag and break off the search for a matching flag
                if (Lorder.isRequired()
                        && (Lused_flags.length() == 0 || Lorder.requirement == myFlagOrderRequirement.ALL_REQUIRED && Lused_flags.length() < Lorder.getFlags().length())) {
                    plugin.debug("no matching flag in required order; concluded that this is not a flag");
                    break;
                } else
                    plugin.debug("no matching flag in optional order; continuing to next order...");

                // iterate to the next myFlagOrder in the localized order_iterator
                Lorder = Lorder_iterator.next();
                Lused_flags = new myList<myFlag>();

                // try to find a flag matching this parameter in this order
                plugin.debug("searching for matching flag in order \"" + Lorder.toString() + "\"...");
                Lflag = Lorder.getFlags().findMatch(parameter);
            }

            // if a matching flag has been found, skip the rest of this loop to iterate to the next parameter and read it
            if (Lflag != null) {
                plugin.debug("found matching flag: \"" + Lflag.toString() + "\"; attempting to read in value...");

                // if the last flag (kept in flag) was a parsing flag, add its parsed value to results and reset parsing
                if (flag != null && flag.parsing) {
                    plugin.debug("first, last flag was parsing; reading parsed value before reading current value into new flag...");
                    Object read_value = flag.readValue(parsing);
                    if (read_value instanceof String && ((String) read_value).startsWith("\\E:")) {
                        plugin.debug("received error \"" + ((String) read_value).substring(0, 3) + "\"; cancelling command...");
                        sender.sendMessage(ChatColor.RED + ((String) read_value).substring(0, 3));
                        return null;
                    } else {
                        plugin.debug("received value \"" + read_value.toString() + "\"; adding to results...");
                        results.put(flag.name, read_value);
                    }

                    parsing = "";
                }

                /* if the flag is a literal flag, there is no need to continue parsing this flag after this iteration, so add this flag to the results, set flag to null to
                 * prevent further value parsing, and move on */
                if (Lflag.type == myFlagType.LITERAL) {
                    plugin.debug("new flag is literal type; adding placeholder result to results...");
                    results.put(Lflag.name, '\0');
                    flag = null;
                } // otherwise, later value parsing is needed, so set the regular flag to the matching flag, Lflag
                else {
                    plugin.debug("new flag is valued type; preparing new flag for value parse next iteration...");
                    flag = Lflag;
                }

                // update the higher-up order and order_iterator with the localized versions
                order = Lorder;
                order_iterator = Lorder_iterator;

                continue;
            }

            // if the parameter is not a flag and we have found a flag previously, read in the value or continue parsing as necessary
            if (flag != null)
                if (flag.parsing) {
                    plugin.debug("parsing flag found previously; continuing parse...");
                    parsing += parameter;
                } else {
                    plugin.debug("non-parsing flag was found previously; attempting to read in value...");
                    Object read_value = flag.readValue(parameter);
                    if (read_value instanceof String && ((String) read_value).startsWith("\\E:")) {
                        plugin.debug("received error \"" + ((String) read_value).substring(0, 3) + "\"; cancelling command...");
                        sender.sendMessage(ChatColor.RED + ((String) read_value).substring(0, 3));
                        return null;
                    } else {
                        plugin.debug("received value \"" + read_value.toString() + "\"; adding to results...");
                        results.put(flag.name, read_value);
                    }
                }
            // if the parameter is not a flag and we have not found a flag previously, try to find an unflagged flag to match it to (as long as we still have orders to parse)
            else if (order != null) {
                // if the order used to capture the last flag doesn't allow multiple flags, then we must skip past it to the next order
                plugin.debug("preparing to parse order \"" + order.toString() + "\"");
                if (!order.allowsMultiple()) {
                    order = order_iterator.next();
                    used_flags = new myList<myFlag>();
                    plugin.debug("previous order was singluar; skipping to next order \"" + order.toString() + "\"...");
                }

                /* note that here, it's okay if we use flag instead of a localized copy because if the search turns up a match, we'll keep this place in the order iterator,
                 * and if not, we'll cancel the command */
                plugin.debug("no flag found previously; searching for unflagged flag to match to...");
                plugin.debug("searching for unflagged flag in order \"" + order + "\"...");
                flag = order.getFlags().findMatch(null, null, "false");
                while (flag == null && order != null) {
                    // findMatch will find the first element matching the given parameters; now, we must skip past all the flag that have already been used
                    int flag_index;
                    while (flag != null && (flag_index = used_flags.find(flag)) != -1)
                        flag = order.getFlags().get(flag_index + 1);

                    // if insufficient flags were used from a required order, throw an error
                    if (order.isRequired()
                            && (used_flags.length() == 0 || order.requirement == myFlagOrderRequirement.ALL_REQUIRED && used_flags.length() < order.getFlags().length())) {
                        plugin.debug("no unflagged flag in required order; cancelling command");
                        sender.sendMessage(ChatColor.RED + "You forgot to give me a value for the " + ChatColor.ITALIC
                                + order.toString().substring(order.toString().indexOf(':') + 2) + ChatColor.RED + " parameter!");
                        return null;
                    } else
                        plugin.debug("no unflagged flag in optional order; continuing to next order...");

                    // iterate to the next myFlagOrder in the localized order_iterator
                    order = order_iterator.next();
                    used_flags.clear();

                    // try to find a flag matching this parameter in this order
                    plugin.debug("searching for matching flag in order \"" + order.toString() + "\"...");
                    flag = order.getFlags().findMatch(null, null, "false");
                }

                // if we found a flag to match this parameter, read in the value if it's non-parsing or let it continue parsing otherwise
                if (flag != null) {
                    if (flag.isParsing()) {
                        plugin.debug("found unflagged parsing flag \"" + flag + "\"; setting up for continued parsing...");
                        parsing = parameter;
                    } else {
                        plugin.debug("found unflagged non-parsing flag \"" + flag + "\"; attempting to read in value...");
                        Object read_value = flag.readValue(parameter);
                        if (read_value instanceof String && ((String) read_value).startsWith("\\E:")) {
                            plugin.debug("received error \"" + ((String) read_value).substring(0, 3) + "\"; cancelling command...");
                            sender.sendMessage(ChatColor.RED + ((String) read_value).substring(0, 3));
                            return null;
                        } else {
                            plugin.debug("received value \"" + read_value.toString() + "\"; adding to results...");
                            results.put(flag.name, read_value);
                        }

                        // set flag to null again to ensure that it is not parsed any further
                        flag = null;
                    }
                } // otherwise, throw an error
                else {
                    sender.sendMessage(ChatColor.RED + "You forgot to give me a value for the " + ChatColor.ITALIC
                            + order.toString().substring(order.toString().indexOf(':') + 2) + ChatColor.RED + " parameter!");
                    return null;
                }
            } // if we have yet to match this parameter to a flag, but there are no more orders to parse, throw an error
            else {
                sender.sendMessage(ChatColor.RED + "I don't know what \"" + parameter + "\" means.");
                return null;
            }

            String big_debug_message =
                    "completed analysis of parameter \"" + parameter + "\"; resulting status:\norder=" + (order != null ? "\"" + order.toString() + "\"" : "null")
                            + ";\nflag=" + (flag != null ? "\"" + flag.toString() + "\"" : "null") + ";\nresults=";
            for (String key : results.keySet())
                big_debug_message = "\"" + key + "\"=" + results.get(key).toString() + ";";
            plugin.debug(big_debug_message);
        }

        // if the last flag found was parsing, read in the parsed value
        if (flag != null)
            if (flag.parsing) {
                plugin.debug("reading in parsed value for final flag...");
                Object read_value = flag.readValue(parsing);
                if (read_value instanceof String && ((String) read_value).startsWith("\\E:")) {
                    plugin.debug("received error \"" + ((String) read_value).substring(0, 3) + "\"; cancelling command...");
                    sender.sendMessage(ChatColor.RED + ((String) read_value).substring(0, 3));
                    return null;
                } else {
                    plugin.debug("received value \"" + read_value.toString() + "\"; adding to results...");
                    results.put(flag.name, read_value);
                }
            } // if the last flag found was not parsing, that means we received a flag with no given value, so throw an error
            else {
                plugin.debug("so close, but final flag received no value; cancelling command...");
                sender.sendMessage(ChatColor.RED + "You forgot to give me a value for the " + ChatColor.ITALIC + flag.toString() + ChatColor.RED + " parameter!");
                return null;
            }

        return results;
    }

    /** This method can read a command's usage and translate it into {@link myFlag}s and {@link myFlagOrder}s which are added to {@link #flags}.
     * 
     * @param plugin
     *            is the {@link myPlugin} which the command corresponds to.
     * @param command
     *            is the primmary alias of the command that is being analyzed.
     * @param usage
     *            is a <tt>String</tt> that describes the format of the command's parameters.
     * @return <b>true</b> if the command's usage was successfully read into a series of {@link myFlag}s and {@link myFlagOrder}s; <b>false</b> otherwise. */
    public static boolean readUsage(myPlugin plugin, String command, String usage) {
        plugin.debug("reading /" + command + " usage:\n" + usage);

        // split the usage into the different parameters with different orders
        String[] ordered_parameters = usage.split("[\\]\\)] ");

        // split up the parameters by order
        for (byte order = 0; order < ordered_parameters.length; order++) {
            // split up the parameters within the order
            String[] order_parameters = ordered_parameters[order].split("(\\]|\\))[,/]");
            myList<myFlag> order_flags = new myList<myFlag>();

            // analyze parameters within the same order
            for (String parameter : order_parameters) {
                plugin.debug("reading parameter: " + parameter);

                // determine the properties of the flag
                boolean flagged =
                        parameter.length() > 1
                                && parameter.charAt(1) == '-'
                                && (parameter.charAt(0) == '-' || parameter.length() > 2 && parameter.charAt(2) == '-'
                                        && (parameter.charAt(0) == '(' || parameter.charAt(0) == '[')), parsing = parameter.contains("...");
                char type_char = parameter.contains(" ") ? parameter.charAt(parameter.indexOf(' ') + 1) : '\0';
                String name = parameter.substring(1 + (flagged ? 2 : 0), parameter.indexOf(' '));

                plugin.debug("read parameter properties: name=" + name + "; type=" + (type_char == '\0' ? "(literal)" : type_char) + "; " + (!flagged ? "un" : "")
                        + "flagged; " + (!parsing ? "not " : "") + "parsing");

                // add the new flag to the myFlagOrder currently being parsed
                order_flags.add(new myFlag(name, flagged, parsing, type_char, parameter.substring(parameter.indexOf(String.valueOf(type_char) + (parsing ? "..." : "")))
                        .trim()));
            }

            flags.add(new myFlagOrder(plugin, command, order, ordered_parameters[order].contains("),(") ? myFlagOrderRequirement.MULTIPLE_OPTIONAL : ordered_parameters[order]
                    .startsWith("(") ? myFlagOrderRequirement.SINGLE_OPTIONAL : ordered_parameters[order].contains("]/[") ? myFlagOrderRequirement.SINGLE_REQUIRED
                    : ordered_parameters[order].contains("],[") ? myFlagOrderRequirement.ALL_REQUIRED : myFlagOrderRequirement.SINGLE_OR_MORE_REQUIRED, order_flags));
        }

        plugin.debug("processed /" + command + " usage");
        return true;
    }

    /** This method reads a <tt>String</tt> value and attempts to gather a value from it which can be stored in this {@link myFlag}; each different {@link myFlagType} runs this
     * method differently.
     * 
     * @param read_value
     *            is the <tt>String</tt> which will be read in an attempt to determine the value to store inside the {@link myFlag}.
     * @return an <tt>Object</tt> translated from the given <b><tt>read_value</b></tt> into the form appropriate to this {@link myFlag}'s {@link myFlag#type type} if all goes
     *         well; if there was an error, it will return a <tt>String</tt> marked with an <tT>"E:"</tt> at the beginning meant to be appended to the
     *         <tt>String "Your [flag name] parameter \"[read value]\" "</tt> (after the removal of the <tt>"E:"</tt>). */
    private Object readValue(String read_value) {
        switch (type) {
            case LITERAL:
                if (read_value.equals(name))
                    return true;
                else
                    return "\\E:doesn't match the required \"" + name + "\" part of the command.";
            case BOOLEAN:
                Boolean new_BOOLEAN_value = readResponse(read_value);
                if (new_BOOLEAN_value == null)
                    return "\\E:doesn't seem to be a proper \"yes\" or \"no\" answer.";
                else
                    return new_BOOLEAN_value;
            case DECIMAL:
                if (read_value.toLowerCase().startsWith("infinit"))
                    return -1;
                else
                    try {
                        return Double.parseDouble(read_value);
                    } catch (NumberFormatException exception) {
                        return "\\E:should be a number or \"infinite\".";
                    }
            case INTEGER:
                if (read_value.toLowerCase().startsWith("infinit")) {
                    return -1;
                } else
                    try {
                        return Integer.parseInt(read_value);
                    } catch (NumberFormatException exception) {
                        return "\\E:should be an integer (a number with no decimal point or fraction) or \"infinite\".";
                    }
            case OPTION:
                // determine the value based on the raw value and the options
                String new_OPTION_value = null;
                for (String option : (String[]) additional_info)
                    if (read_value.toLowerCase().startsWith(option.toLowerCase())) {
                        new_OPTION_value = option;
                        break;
                    }
                if (new_OPTION_value == null)
                    return "\\E:didn't match any of the available options.\nAvailable options: " + writeArray((String[]) additional_info, ", ", "and", "\"", "\"");
                else
                    return new_OPTION_value;
            case PLAYER:
                String new_PLAYER_value = REALDrummer.utils.PlayerUtilities.getFullName(read_value);
                if (new_PLAYER_value != null)
                    return new_PLAYER_value;
                else
                    return "\\E:didn't match the name of any of the players on this server!";
            case STRING:
                return read_value;
            case TIME:
                long read_time = readTime(read_value);
                if (read_time == -1)
                    return "\\E:doesn't look like a proper time.\nProper times should have numbers with time values after them like \"2 days, 3 hours, and 5 minutes\" (or even just \"2d 3h 5m\").";
                else
                    return read_time;
            default:
                mCL.err("I don't recognize this myFlag type!", "unrecognized myFlag type", type);
                return "\\E:caused an internal error. Sorry!";
        }
    }

    // overrides
    @Override
    public int compareTo(myFlag flag) {
        return name.compareTo(flag.name);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof myFlag && compareTo((myFlag) object) == 0;
    }

    @Override
    public int matchTo(String... match_parameters) {
        return match(new Object[] { name, isFlagged() }, match_parameters);
    }

    @Override
    public String toString() {
        return (isFlagged() ? "--" : "") + name;
    }
}
