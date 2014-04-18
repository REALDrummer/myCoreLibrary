package REALDrummer.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class myISetting extends mySetting {
    public myISetting(String target, String key, int initial_value) {
        this.target = target;
        this.key = key;
        value = initial_value;
    }

    // getters
    public int getValue() {
        return (int) value;
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
            int read_value;
            try {
                read_value = Integer.parseInt(save_line.substring(save_line.indexOf(':') + 1));
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
            out.write("    " + (!target.equals("\\server") ? "    " : "") + key + ": " + value);
            out.newLine();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}
