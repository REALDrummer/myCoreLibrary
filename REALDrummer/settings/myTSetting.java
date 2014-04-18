package REALDrummer.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import static REALDrummer.utils.StringUtilities.*;

public class myTSetting extends mySetting {
    public myTSetting(String target, String key, long initial_value) {
        this.target = target;
        this.key = key;
        value = initial_value;
    }

    // getters
    public long getValue() {
        return (long) value;
    }

    // readers and writers
    @Override
    public boolean read(BufferedReader in) {
        try {
            String save_line = in.readLine();

            // if the save line wasn't found or isn't correctly formatted, return false to indicate an error
            if (save_line == null || !save_line.trim().toLowerCase().startsWith(key.toLowerCase()) || !save_line.contains(":"))
                return false;

            // attempt to read the value
            long read_value;
            try {
                read_value = readTime(save_line.substring(save_line.indexOf(':') + 1));
            } catch (NumberFormatException | StringIndexOutOfBoundsException exception) {
                return false;
            }

            value = read_value;
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public boolean write(BufferedWriter out) {
        try {
            out.write("    " + (!target.equals("\\server") ? "    " : "") + key + ": " + writeTime(getValue(), false));
            out.newLine();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}
