package REALDrummer.settings;

import static REALDrummer.utils.ArrayUtilities.readArray;
import static REALDrummer.utils.ArrayUtilities.writeArray;

public class myOSetting extends mySetting {
    public String[] options;

    public myOSetting(String key, byte initial_value, String... options) {
        this.key = key;
        this.value = initial_value;
        this.options = options;
    }

    public myOSetting(String target, String key, byte initial_value, String... options) {
        this.target = target;
        new myOSetting(key, initial_value, options);
    }

    // getters
    public byte getValue() {
        return (byte) value;
    }

    // readers and writers
    @Override
    public myOSetting read(String save_line) {
        debug("reading myOSetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        // split the save line into its key and value
        String[] split = save_line.split(":");
        if (split.length < 2) {
            debug("myOSetting read failure; no \":\" found");
            return null;
        }

        // extract the options
        if (!split[0].contains("(") || !split[0].contains("(")) {
            debug("myOSetting read failure; no \"(\" and/or \")\" found");
            return null;
        }

        String key = split[0].substring(0, split[0].indexOf("(")).trim(), value = split[1].toLowerCase().trim();
        String[] options = readArray(split[0].substring(split[0].indexOf("(") + 1, split[0].indexOf(")")), ", ", "or");

        byte selected_option = -1;
        for (byte i = 0; i < options.length; i++)
            if (options[i].toLowerCase().startsWith(value) && (selected_option == -1 || options[selected_option].length() > options[i].length()))
                selected_option = i;
        if (selected_option == -1) {
            debug("myOSetting read failure; value given for myOSetting (\"" + value + "\") does not match any options (\"" + writeArray(options) + "\")");
            return null;
        }

        return new myOSetting(key, selected_option, options);
    }

    @Override
    public String write() {
        return key + "(" + writeArray(options, ", ", "or") + "): " + String.valueOf(getValue());
    }

}
