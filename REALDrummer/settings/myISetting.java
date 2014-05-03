package REALDrummer.settings;

import REALDrummer.myCoreLibrary;

public class myISetting extends mySetting {
    public myISetting(String key, int initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myISetting(String target, String key, int initial_value) {
        this.target = target;
        new myISetting(key, initial_value);
    }

    // getters
    public int getValue() {
        return (int) value;
    }

    // readers and writers
    @Override
    public myISetting read(String save_line) {
        myCoreLibrary.mCL.debug("reading myISetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        String[] split = save_line.split(":");
        if (split.length < 2) {
            myCoreLibrary.mCL.debug("myISetting read failure; no \":\" found");
            return null;
        }

        try {
            return new myISetting(split[0], Integer.parseInt(split[1]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public String write() {
        return key + ": " + String.valueOf(getValue());
    }
}
