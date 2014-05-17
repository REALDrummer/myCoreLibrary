package REALDrummer;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import org.bukkit.command.CommandSender;

import static REALDrummer.utils.ArrayUtilities.readArray;

public abstract class myData implements Comparable<myData>, Matchable {
    // default constructor
    public myData() {

    }

    // standard setting methods
    public abstract myData new_default();

    @Nonnull
    public abstract myPlugin getPlugin();

    @Nonnull
    public abstract String getSaveFormat(double version);

    @Nonnull
    public String getSaveFormat() {
        return getSaveFormat(Double.parseDouble(getPlugin().getDescription().getVersion()));
    }

    // message utilities
    public void debug(String message) {
        getPlugin().debug(message);
    }

    public void err(String message, String issue, Object... additional_information) {
        getPlugin().err(message, issue, additional_information);
    }

    public void err(String message, Throwable exception, Object... additional_information) {
        getPlugin().err(message, exception, additional_information);
    }

    // loaders and savers
    public void load(CommandSender sender) {
        String data_type = getClass().getSimpleName().toLowerCase() + "s";
        debug("loading " + data_type + " (" + getPlugin().getName() + ")...");

        myList<myData> data = new myList<myData>();
        try {
            File verbose_file = new File(getPlugin().getDataFolder(), data_type + ".txt"), concise_file = new File(getPlugin().getDataFolder(), data_type + " (concise).txt"), raf_file =
                    new File(getPlugin().getDataFolder(), data_type + ".raf");
            String current_format = getPlugin().getSetting(data_type + " file format").optionValue();

            // load from the RAF
            if (raf_file.exists()
                    && (current_format.equals("RAF") || !verbose_file.exists() && current_format.equals("verbose") || !concise_file.exists()
                            && current_format.equals("concise"))) {
                debug("reading \"" + data_type + "\" data from RAF file...");
                RandomAccessFile in = new RandomAccessFile(raf_file, "r");
                in.seek(0);

                debug("reading file version number...");
                double file_version = in.readDouble();

                debug("file version=" + file_version + "; reading data...");

                // RandomAccessFiles end with an EOFException
                while (true)
                    try {
                        String[] fields = getSaveFormat().split("[\\[\\]]");
                        myData object = new_default();
                        for (byte i = 1; i < fields.length; i += 2) {
                            debug("reading \"" + fields[i] + "\"...");
                            Field field;
                            try {
                                field = getClass().getField(fields[i]);
                            } catch (NoSuchFieldException exception) {
                                err("A field specified in the " + getClass().getSimpleName() + " class's save format doesn't exist!", exception, "field=\"" + fields[i] + "\"");
                                continue;
                            } catch (SecurityException exception) {
                                err("A field specified in the " + getClass().getSimpleName() + " class's save format isn't accessible!", exception, "field=\"" + fields[i]
                                        + "\"");
                                continue;
                            }
                            String field_type = field.getType().getSimpleName();
                            debug("field found; type=\"" + field_type + "\"");

                            try {
                                if (field_type.equalsIgnoreCase("Boolean")) {
                                    debug("reading boolean...");
                                    field.setBoolean(object, in.readBoolean());
                                } else if (field_type.equalsIgnoreCase("Byte")) {
                                    debug("reading byte...");
                                    field.setByte(object, in.readByte());
                                } else if (field_type.equals("Character") || field_type.equals("char")) {
                                    debug("reading char...");
                                    field.setChar(object, in.readChar());
                                } else if (field_type.equalsIgnoreCase("Double")) {
                                    debug("reading double...");
                                    field.setDouble(object, in.readDouble());
                                } else if (field_type.equalsIgnoreCase("Float")) {
                                    debug("reading float...");
                                    field.setFloat(object, in.readFloat());
                                } else if (field_type.equals("Integer") || field_type.equals("int")) {
                                    debug("reading int...");
                                    field.setInt(object, in.readInt());
                                } else if (field_type.equalsIgnoreCase("Long")) {
                                    debug("reading long...");
                                    field.setLong(object, in.readLong());
                                } else if (field_type.equalsIgnoreCase("short")) {
                                    debug("reading short...");
                                    field.setShort(object, in.readShort());
                                } else if (field_type.equals("String")) {
                                    debug("reading String...");
                                    field.set(object, in.readUTF());
                                } else if (field_type.equalsIgnoreCase("String[]")) {
                                    debug("reading String[]...");
                                    field.set(object, readArray(in.readUTF(), ", ", ""));
                                } else
                                    err("I don't know how to read this \"" + data_type + "\" argument!", "unrecognized data type", "field=\"" + field.getName(),
                                            "field data type=\"" + field_type + "\"");
                            } catch (IllegalArgumentException exception) {
                                err("There was a problem setting " + field.getName() + "'s value!", exception, field);
                            } catch (IllegalAccessException exception) {
                                err("I tried to set " + field + "'s value, but there was an access issue!", exception, field);
                            }
                        }
                    } catch (EOFException exception) {
                        debug("end of file reached; \"" + data_type + "\" loading complete");
                        break;
                    }
                in.close();
            } // load from the concise text file
            else if (concise_file.exists()
                    && (current_format.equals("concise") || !raf_file.exists() && current_format.equals("RAF") || !verbose_file.exists() && current_format.equals("verbose"))) {
                debug("reading \"" + data_type + "\" data from concise text file...");
                BufferedReader in = new BufferedReader(new FileReader(concise_file));

                debug("reading file version number...");
                double file_version = Double.parseDouble(in.readLine());

                debug("file version=" + file_version + "; reading data...");

                String save_line = in.readLine();
                while (save_line != null)
                    try {
                        String[] fields = getSaveFormat().split("[\\[\\]]");
                        for (byte i = 1; i < fields.length; i += 2) {
                            debug("reading \"" + fields[i] + "\"...");
                            Field field;
                            try {
                                field = getClass().getField(fields[i]);
                            } catch (NoSuchFieldException exception) {
                                err("A field specified in the " + getClass().getSimpleName() + " class's save format doesn't exist!", exception, "field=\"" + fields[i] + "\"");
                                continue;
                            } catch (SecurityException exception) {
                                err("A field specified in the " + getClass().getSimpleName() + " class's save format isn't accessible!", exception, "field=\"" + fields[i]
                                        + "\"");
                                continue;
                            }
                            String field_type = field.getType().getSimpleName();
                            debug("field found; type=\"" + field_type + "\"");

                            try {
                                if (field_type.equalsIgnoreCase("Boolean")) {
                                    debug("reading boolean...");
                                    field.setBoolean(field, in.readBoolean());
                                } else if (field_type.equalsIgnoreCase("Byte")) {
                                    debug("reading byte...");
                                    field.setByte(field, in.readByte());
                                } else if (field_type.equals("Character") || field_type.equals("char")) {
                                    debug("reading char...");
                                    field.setChar(field, in.readChar());
                                } else if (field_type.equalsIgnoreCase("Double")) {
                                    debug("reading double...");
                                    field.setDouble(field, in.readDouble());
                                } else if (field_type.equalsIgnoreCase("Float")) {
                                    debug("reading float...");
                                    field.setFloat(field, in.readFloat());
                                } else if (field_type.equals("Integer") || field_type.equals("int")) {
                                    debug("reading int...");
                                    field.setInt(field, in.readInt());
                                } else if (field_type.equalsIgnoreCase("Long")) {
                                    debug("reading long...");
                                    field.setLong(field, in.readLong());
                                } else if (field_type.equalsIgnoreCase("short")) {
                                    debug("reading short...");
                                    field.setShort(field, in.readShort());
                                } else if (field_type.equals("String")) {
                                    debug("reading String...");
                                    field.set(field, in.readUTF());
                                } else if (field_type.equalsIgnoreCase("String[]")) {
                                    debug("reading String[]...");
                                    field.set(field, readArray(in.readUTF(), ", ", ""));
                                } else
                                    err("I don't know how to read this \"" + data_type + "\" argument!", "unrecognized data type", "field=\"" + field.getName(),
                                            "field data type=\"" + field_type + "\"");
                            } catch (IllegalArgumentException exception) {
                                err("There was a problem setting " + field.getName() + "'s value!", exception, field);
                            } catch (IllegalAccessException exception) {
                                err("I tried to set " + field + "'s value, but there was an access issue!", exception, field);
                            }
                        }
                        save_line = in.readLine();
                    } catch (EOFException exception) {
                        debug("end of file reached; \"" + data_type + "\" loading complete");
                        break;
                    }
                in.close();
            } // load from the verbose text file
            else if (verbose_file.exists()
                    && (current_format.equals("verbose") || !raf_file.exists() && current_format.equals("RAF") || !concise_file.exists() && current_format.equals("concise"))) {
                // TODO: load from the verbose text file
            } else
                getPlugin().tellOps("I couldn't find any data files for the " + data_type + ".");
        } catch (IOException exception) {
            err("There was an IOException while trying to read the \"" + data_type + "\" data file!", exception);
            return;
        }

        // use the abstract method to store the read data
        store(data);
    }

    public boolean save(CommandSender sender) {
        return save(sender, true);
    }

    private boolean save(CommandSender sender, boolean display_message) {

    }

    // data-handling methods
    public myData read(String line) {

    }

    public abstract void store(myList<myData> data);

    public String write() {

    }

    public static String write(myData data) {
        return data.write();
    }

    // important overridden methods
    @Override
    public abstract int compareTo(myData data);

    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int matchTo(String... match_parameters);

    @Override
    public abstract String toString();
}
