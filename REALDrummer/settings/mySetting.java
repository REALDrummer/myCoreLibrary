package REALDrummer.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import REALDrummer.Matchable;
import REALDrummer.myCoreLibrary;
import REALDrummer.myPlugin;

import static REALDrummer.utils.ArrayUtilities.compare;
import static REALDrummer.utils.MessageUtilities.*;

public abstract class mySetting implements Comparable<mySetting>, Matchable {
    protected String target = null, key;
    protected Object value;

    // getters
    public String getKey() {
        return key;
    }

    public String getTarget() {
        return target;
    }

    // cast-return methods
    public boolean booleanValue() {
        try {
            return (boolean) value;
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a boolean value from a non-myBSetting mySetting!", exception, this);
            return false;
        }
    }

    public int intValue() {
        try {
            return (int) value;
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get an int value from a non-myISetting mySetting!", exception, this);
            return -1;
        }
    }

    public String stringValue() {
        try {
            return (String) value;
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a String value from a non-mySSetting mySetting!", exception, this);
            return "";
        }
    }

    public long timeValue() {
        try {
            return (long) value;
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a long time value from a non-myTSetting mySetting!", exception, this);
            return -1;
        }
    }

    // readers and writers
    public BufferedReader read(myPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.txt");
        if (!file.exists())
            return null;

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException exception) {
            return null;
        }

        if (read(in))
            return in;
        else
            return null;
    }

    public abstract boolean read(BufferedReader in);

    public BufferedWriter write(myPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.txt");
        BufferedWriter out;
        try {
            if (!file.exists())
                file.createNewFile();

            out = new BufferedWriter(new FileWriter(file));
        } catch (IOException exception) {
            return null;
        }

        if (write(out))
            return out;
        else
            return null;
    }

    public abstract boolean write(BufferedWriter out);

    // overrides
    @Override
    public int compareTo(mySetting setting) {
        int target_compare = target.compareTo(setting.target);
        if (target_compare == 0)
            return key.compareTo(setting.key);
        else
            return target_compare;
    }

    @Override
    public int matchTo(String... match_parameters) {
        return compare(new String[] { target, key }, match_parameters);
    }

    @Override
    public String toString() {
        return "\"" + key + "\" for " + target + ": " + (value instanceof String ? "\"" + value + "\"" : value);
    }
}
