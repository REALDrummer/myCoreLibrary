package REALDrummer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import REALDrummer.interfaces.Matchable;
import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.ListUtilities.*;
import static REALDrummer.utils.ColorUtilities.decolor;
import static REALDrummer.utils.StringUtilities.*;

/** This class is meant to be the parent of all <tt>Object</tt> classes that should be automatically loaded and saved as part of a given getPlugin()'s data bank. Anything that
 * should be saved to a file when the getPlugin() is disabled and loaded later when the getPlugin() is enabled should extend this class.
 * 
 * @author REALDrummer */
public abstract class myData implements Comparable<myData>, Matchable, Serializable {
    private static final long serialVersionUID = 6762013288193888326L;
    @SuppressWarnings("unchecked")
    public static myList<Class<? extends myData>> data_types = new myList<Class<? extends myData>>();

    public myList<myData> data = new myList<myData>();

    // default constructor
    public myData() {
        if (!data_types.contains(getClass()))
            data_types.add(getClass());
    }

    // message utilities
    public void announce(String message, long expiration_time) {
        getPlugin().announce(message, expiration_time);
    }

    public void broadcast(String message) {
        getPlugin().broadcast(message);
    }

    public void debug(String message) {
        getPlugin().debug(message);
    }

    public void err(String message, Throwable exception, Object... additional_information) {
        getPlugin().err(message, exception, additional_information);
    }

    public void err(String message, String issue, Object... additional_information) {
        getPlugin().err(message, issue, additional_information);
    }

    public void inform(String recipient, String message) {
        getPlugin().inform(recipient, message);
    }

    public void inform(String recipient, String message, long expiration_time) {
        getPlugin().inform(recipient, message, expiration_time);
    }

    public void inform(UUID player, String message) {
        getPlugin().inform(player, message);
    }

    public void inform(UUID player, String message, long expiration_time) {
        getPlugin().inform(player, message, expiration_time);
    }

    public void inform(OfflinePlayer player, String message) {
        getPlugin().inform(player, message);
    }

    public void inform(OfflinePlayer player, String message, long expiration_time) {
        getPlugin().inform(player, message, expiration_time);
    }

    public String paginate(String message, String command_format, String not_enough_pages, int page_number, boolean not_console) {
        return getPlugin().paginate(message, command_format, not_enough_pages, page_number, not_console);
    }

    public void tell(CommandSender sender, String message) {
        getPlugin().tell(sender, message);
    }

    public void tellOps(String message, String... exempt_ops) {
        getPlugin().tellOps(message, exempt_ops);
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
        getPlugin().tellOps(message, also_tell_console, exempt_ops);
    }

    // other utilities
    @Nonnull
    public final String getSaveFormat() {
        return getSaveFormat(getPlugin().getVersion());
    }

    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }

    public String getPluralName() {
        return getName() + "s";
    }

    // loaders and savers
    /** This method checks to see whether or not this {@link myData} type is configured to auto-save; then, if it is configured to auto-save, it will save the current data on
     * the server.
     * 
     * @return <b>false</b> only if the data type is configured to auto-save and the save was not successful; <b>true</b> otherwise. */
    public boolean autoSave() {
        if (getPlugin().getSetting("auto-save " + getClass().getSimpleName().toLowerCase() + "s").booleanValue())
            return save(null, false);
        return true;
    }

    @SuppressWarnings("unchecked")
    public boolean load(CommandSender sender) {
        // TODO: account for UUID changes during loading
        mCL.debug("loading " + getName() + " (" + getPlugin().getName() + ")...");

        myList<myData> data = new myList<myData>();
        try {
            File txt_file = new File(getPlugin().getDataFolder(), getName() + ".txt"), dat_file = new File(getPlugin().getDataFolder(), getName() + ".dat");
            String current_format = getPlugin().getSetting(getName() + " file format").optionValue();

            // load from the .dat file
            if (dat_file.exists() && (current_format.equals("dat") || !txt_file.exists() && current_format.equals("txt"))) {
                // TODO TEST: make sure this still works if you modify the object's class and try to load the data in the old version of the class
                mCL.debug("reading \"" + getName() + "\" data from dat file...");
                ObjectInputStream in = new ObjectInputStream(new FileInputStream(dat_file));

                while (true)
                    try {
                        myData object = (myData) in.readObject();
                        data.add(object);
                        mCL.debug("read " + object.getClass().getName() + ": " + object.toString());
                    } catch (EOFException exception) {
                        mCL.debug("end of file reached; \"" + getName() + "\" loading complete");
                        break;
                    } catch (ClassNotFoundException exception) {
                        mCL.err("The ObjectInputStream reading \"" + getName() + "\" from " + dat_file.getName() + " for " + getPlugin().getName()
                                + " could not find the class for this Object!", exception);
                        continue;
                    } catch (ClassCastException exception) {
                        mCL.err("The ObjectInputStream reading \"" + getName() + "\" from " + dat_file.getName() + " for " + getPlugin().getName()
                                + " didn't identify this Object as a myData!", exception);
                        continue;
                    }
                in.close();
            } // load from the txt text file
            else if (txt_file.exists() && current_format.equals("txt")) {
                mCL.debug("reading \"" + getName() + "\" data from txt file...");
                BufferedReader in = new BufferedReader(new FileReader(txt_file));

                mCL.debug("reading file version number...");
                double file_version = Double.parseDouble(in.readLine());

                mCL.debug("file version=" + file_version + "; reading data...");

                String save_line = in.readLine();
                while (save_line != null) {
                    // gather the fields, split the save line, and create a new instanceof the myData
                    myData object = defaultData();
                    HashMap<String, String> data_pieces = readData(getSaveFormat(file_version), save_line);

                    for (String key : data_pieces.keySet()) {
                        mCL.debug("reading \"" + key + "\"...");

                        // retrieve the field
                        Field field;
                        try {
                            field = getClass().getField(key);
                        } catch (NoSuchFieldException exception) {
                            mCL.err("A field specified in the " + getClass().getSimpleName() + " class's save format doesn't exist!", exception, "field=\"" + key + "\"");
                            continue;
                        } catch (SecurityException exception) {
                            mCL.err("A field specified in the " + getClass().getSimpleName() + " class's save format isn't accessible!", exception, "field=\"" + key + "\"");
                            continue;
                        }

                        // retrieve the field type
                        String field_type = field.getType().getSimpleName();
                        mCL.debug("field found; type=\"" + field_type + "\"");

                        // read the data from the concise file appropriately
                        try {
                            if (field_type.equalsIgnoreCase("Boolean")) {
                                mCL.debug("reading boolean...");
                                field.setBoolean(object, data_pieces.get(key).equals("true"));
                            } else if (field_type.equalsIgnoreCase("Byte")) {
                                mCL.debug("reading byte...");
                                field.setByte(object, Byte.parseByte(data_pieces.get(key)));
                            } else if (field_type.equals("Character") || field_type.equals("char")) {
                                mCL.debug("reading char...");
                                field.setChar(object, data_pieces.get(key).charAt(0));
                            } else if (field_type.equalsIgnoreCase("Double")) {
                                mCL.debug("reading double...");
                                field.setDouble(object, Double.parseDouble(data_pieces.get(key)));
                            } else if (field_type.equalsIgnoreCase("Float")) {
                                mCL.debug("reading float...");
                                field.setFloat(object, Float.parseFloat(data_pieces.get(key)));
                            } else if (field_type.equals("Integer") || field_type.equals("int")) {
                                mCL.debug("reading int...");
                                field.setInt(object, Integer.parseInt(data_pieces.get(key)));
                            } else if (field_type.equalsIgnoreCase("Long")) {
                                mCL.debug("reading long...");
                                field.setLong(object, Long.parseLong(data_pieces.get(key)));
                            } else if (field_type.equalsIgnoreCase("short")) {
                                mCL.debug("reading short...");
                                field.setShort(object, Short.parseShort(data_pieces.get(key)));
                            } else if (field_type.equals("String")) {
                                mCL.debug("reading String...");
                                field.set(object, data_pieces.get(key));
                            } else if (field_type.equals("String[]")) {
                                mCL.debug("reading String[]...");
                                field.set(object, readArray(data_pieces.get(key), ", ", "and"));
                            } else if (field_type.startsWith("myList")) {
                                mCL.debug("reading myList...");
                                field.set(object, new myList(readArray(data_pieces.get(key), ", ", "and")));
                            } else if (field_type.equals("Location")) {
                                mCL.debug("reading Location...");
                                field.set(object, readLocation(data_pieces.get(key), true));
                            } else if (field_type.equals("UUID")) {
                                mCL.debug("reading UUID...");
                                field.set(object, UUID.fromString(data_pieces.get(key)));
                            } else if (field_type.equals("Player"))
                                mCL.err("Someone used Players as myData variables!\nPlayers should not be used as myData variables because things go wrong if the Player is offline!"
                                        + "\nThey should have used UUIDs instead!", "Player myData Field", "field=\"" + field.getName() + "\"", "data type=\""
                                        + getClass().getSimpleName().toLowerCase() + "\" (" + getPlugin().getName() + ")");
                            else if (field_type.equals("OfflinePlayer"))
                                mCL.err("Someone used OfflinePlayers as myData variables!\nOfflinePlayers should not be used as myData variables because things go wrong if there are UUID changes!"
                                        + "\nThey should have used UUIDs instead!", "OfflinePlayer myData Field", "field=\"" + field.getName() + "\"", "data type=\""
                                        + getClass().getSimpleName().toLowerCase() + "\" (" + getPlugin().getName() + ")");
                            else
                                mCL.err("I don't know how to read this \"" + getName() + "\" argument!", "unrecognized data type", "field=\"" + field.getName(),
                                        "field data type=\"" + field_type + "\"");
                        } catch (IllegalArgumentException exception) {
                            mCL.err("There was a problem setting " + field.getName() + "'s value!", exception, field);
                        } catch (IllegalAccessException exception) {
                            mCL.err("I tried to set " + field + "'s value, but there was an access issue!", exception, field);
                        }
                    }

                    mCL.debug("read " + object.getClass().getName() + ": " + object.toString());

                    // store the new data
                    data.add(object);

                    // increment the loop
                    save_line = in.readLine();
                }

                mCL.debug("end of file reached; \"" + getName() + "\" loading complete");

                in.close();
            } else {
                mCL.tellOps("I couldn't find any data files for the " + getName() + ".");
                return false;
            }
        } catch (IOException exception) {
            mCL.err("There was an IOException while trying to read the \"" + getName() + "\" data file!", exception);
            return false;
        }

        // send confirmation messages
        mCL.debug("load successful; sending load confirmation message...");

        // singularize the data type to make messages more easy
        if (sender == null)
            if (data.size() > 1)
                mCL.tellOps("Your " + data.size() + " " + getPluralName() + " have been loaded.");
            else if (data.size() == 1)
                mCL.tellOps("Your 1 " + getName() + " has been loaded.");
            else
                mCL.tellOps("You have no " + getPluralName() + " to load!");
        else {
            String sender_name = "Someone on the console";
            if (sender instanceof Player)
                sender_name = ((Player) sender).getName();

            if (data.size() > 1) {
                sender.sendMessage("Your " + data.size() + " " + getPluralName() + " have been loaded.");
                mCL.tellOps(sender_name + " loaded " + data.size() + " " + getPluralName() + " to file.", sender instanceof Player, sender.getName());
            } else if (data.size() == 1) {
                sender.sendMessage("Your 1 " + getName() + " has been loaded.");
                mCL.tellOps(sender_name + " loaded the server's 1 " + getName() + " to file.", sender instanceof Player, sender.getName());
            } else {
                sender.sendMessage("You have no " + getPluralName() + " to load!");
                mCL.tellOps(sender_name + " tried to load the server's " + getPluralName() + " to file, but there were no " + getPluralName() + " on the server to load.",
                        sender instanceof Player, sender.getName());
            }
        }

        // if all went well, put the data in the instance variable data
        this.data = data;
        mCL.debug(getPluralName() + " stored; loading complete; resaving...");
        save(sender, false);

        return true;
    }

    public boolean save(CommandSender sender) {
        return save(sender, true);
    }

    protected boolean save(CommandSender sender, boolean display_messages) {
        mCL.debug("saving " + getName() + " (" + getPlugin().getName() + ")...");

        String current_format = "???";    // current_format is declared out here so that it can be used as additional information if an IOException occurs
        try {
            File txt_file = new File(getPlugin().getDataFolder(), getName() + ".txt"), dat_file = new File(getPlugin().getDataFolder(), getName() + ".dat");
            current_format = getPlugin().getSetting(getName() + " file format").optionValue();

            // load from the dat
            if (current_format.equals("dat")) {
                mCL.debug("writing \"" + getName() + "\" data to dat file...");
                if (!dat_file.exists()) {
                    mCL.debug("dat file does not exist; creating new dat file...");
                    getPlugin().getDataFolder().mkdirs();
                    dat_file.createNewFile();
                }
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dat_file));

                mCL.debug("writing version number (" + getPlugin().getVersion() + ")");
                out.writeDouble(getPlugin().getVersion());

                mCL.debug("beginning data writing...");
                for (myData data_piece : data) {
                    out.writeObject(data_piece);
                    mCL.debug("wrote object: " + data_piece);
                }

                mCL.debug("\"" + getName() + "\" saving complete");
                out.close();
            } // load from the text file
            else if (current_format.equals("txt")) {
                mCL.debug("writing \"" + getName() + "\" data to txt file...");
                if (!txt_file.exists()) {
                    mCL.debug("txt file does not exist; creating new txt file...");
                    getPlugin().getDataFolder().mkdirs();
                    txt_file.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(txt_file));

                mCL.debug("writing file version number (" + getPlugin().getVersion() + ")...");
                out.write(String.valueOf(getPlugin().getVersion()));
                out.newLine();

                mCL.debug("beginning data writing...");
                for (myData data_piece : data) {
                    // assemble the save line
                    String save_line = getSaveFormat();
                    while (save_line.contains("[") && save_line.contains("]") && save_line.indexOf("[") < save_line.indexOf("]")) {
                        String key = substring(save_line, "[", "]");
                        mCL.debug("replacing key \"" + key + "\"...");

                        // quotes can't be in non-boolean fields; only boolean fields contain quotes since variable names can't contain quotes
                        if (key.contains("\"")) {
                            mCL.debug("preliminary check shows boolean key; verifying...");

                            String[] split = key.split("[\"/~]");

                            // make sure the boolean field key is properly formatted: key "true value"/~"false value"
                            if (split.length <= 1 || split[0].length() != split[0].replaceAll("\"", "").length() + 2
                                    || split[1].length() != split[1].replaceAll("\"", "").length() + 2) {
                                mCL.err("This key in " + getClass().getSimpleName() + "'s save format wasn't properly formatted!", "improperly formatted key in the "
                                        + getClass().getSimpleName() + "save format", "key=\"" + key + "\"");
                                continue;
                            }

                            mCL.debug("boolean key verified");

                            // extract the relevant pieces of information
                            String true_value = split[0].substring(split[0].indexOf('"') + 1).trim(), false_value = split[1].substring(0, split[1].indexOf('"')).trim();

                            // get the value of the key for this object
                            boolean value;
                            try {
                                value = data_piece.getClass().getField(key.substring(0, key.indexOf("\"")).trim()).getBoolean(data_piece);
                            } catch (NoSuchFieldException exception) {
                                mCL.err("I couldn't find the field \"" + key.substring(0, key.indexOf("\"")).trim() + "\" in this " + getClass().getSimpleName() + ".",
                                        exception);
                                continue;
                            } catch (IllegalArgumentException | IllegalAccessException | SecurityException exception) {
                                mCL.err("There was a problem accessing the field \"" + key.substring(0, key.indexOf("\"")).trim() + "\" in this " + getClass().getSimpleName()
                                        + ".", exception);
                                continue;
                            }

                            // replace all the key markers with the true_value
                            mCL.debug("value=" + value + "; replacing key with \"" + (value ? true_value : false_value));
                            save_line = save_line.replaceAll("\\[" + key + "\\]", value ? true_value : false_value);
                        } // for non-boolean fields (both the else if and the else
                        else if (key.contains(" "))
                            mCL.err("This key in " + getClass().getSimpleName() + "'s save format wasn't properly formatted!", "improperly formatted key in the "
                                    + getClass().getSimpleName() + "save format", "key=\"" + key + "\"");
                        else {
                            String value;
                            try {
                                // TODO: check the type and use write methods as needed
                                value = data_piece.getClass().getField(key).get(data_piece).toString();
                            } catch (NoSuchFieldException exception) {
                                mCL.err("I couldn't find the field \"" + key.substring(0, key.indexOf("\"")).trim() + "\" in this " + getClass().getSimpleName() + ".",
                                        exception);
                                continue;
                            } catch (IllegalArgumentException | IllegalAccessException | SecurityException exception) {
                                mCL.err("There was a problem accessing the field \"" + key.substring(0, key.indexOf("\"")).trim() + "\" in this " + getClass().getSimpleName()
                                        + ".", exception);
                                continue;
                            }
                            mCL.debug("non-boolean key; replacing key with \"" + value + "\"");
                            save_line = save_line.replaceAll("\\[" + key + "\\]", value);
                        }
                    }

                    // write the assembled save line
                    out.write(save_line);
                    out.newLine();
                }

                mCL.debug("\"" + getName() + "\" saving complete");
                out.close();
            } else {
                mCL.err("What kind of save format is \"" + current_format + "\"?", "unknown value for the current save format");
                return false;
            }
        } catch (IOException exception) {
            mCL.err("There was an IOException while trying to write the \"" + getName() + "\" data file!", exception, "current_format=\"" + current_format + "\"");
            return false;
        }

        if (display_messages) {
            // send confirmation messages
            mCL.debug("save successful; sending save confirmation messages...");

            if (sender == null)
                if (data.size() > 1)
                    mCL.tellOps("Your " + data.size() + " " + getPluralName() + " have been saved.");
                else if (data.size() == 1)
                    mCL.tellOps("Your 1 " + getName() + " has been saved.");
                else
                    mCL.tellOps("You have no " + getPluralName() + " to save!");
            else {
                String sender_name = "Someone on the console";
                if (sender instanceof Player)
                    sender_name = capitalize(((Player) sender).getName());

                if (data.size() > 1) {
                    sender.sendMessage("Your " + data.size() + " " + getPluralName() + " have been saved.");
                    mCL.tellOps(sender_name + " saved " + data.size() + " " + getPluralName() + " to file.", sender instanceof Player, sender.getName());
                } else if (data.size() == 1) {
                    sender.sendMessage("Your 1 " + getName() + " has been saved.");
                    mCL.tellOps(sender_name + " saved the server's 1 " + getName() + " to file.", sender instanceof Player, sender.getName());
                } else {
                    sender.sendMessage("You have no " + getPluralName() + " to save!");
                    mCL.tellOps(sender_name + " tried to save the server's " + getPluralName() + " to file, but there were no " + getPluralName() + " on the server to load.",
                            sender instanceof Player, sender.getName());
                }
            }
        } else
            mCL.debug(getPluralName() + " saved to file; save successful");

        return true;
    }

    // comparison filtration
    @Override
    public int compareTo(myData data) {
        if (!data.getClass().isAssignableFrom(getClass()))
            return Integer.MAX_VALUE;
        else
            return myDataCompare(data);
    }

    public boolean equals(Object object) {
        if (!data.getClass().isAssignableFrom(getClass()))
            return false;
        else
            return myDataEquals((myData) object);
    }

    // abstract methods
    public abstract myData defaultData();

    @Nonnull
    public abstract myPlugin getPlugin();

    @Nonnull
    public abstract String getSaveFormat(double version);

    @Override
    public abstract int matchTo(String... match_parameters);

    public abstract int myDataCompare(myData data);

    public abstract boolean myDataEquals(myData data);

    @Override
    public abstract String toString();
}
