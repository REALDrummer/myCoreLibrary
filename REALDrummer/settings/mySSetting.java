package REALDrummer.settings;

import REALDrummer.myCoreLibrary;

public class mySSetting extends mySetting {
    public mySSetting(String key, String initial_value) {
        this.key = key;
        value = initial_value;
    }

    public mySSetting(String target, String key, String initial_value) {
        this.target = target;
        new mySSetting(key, initial_value);
    }

    // getters
    public String getValue() {
        return (String) value;
    }

    // readers and writers
    @Override
    public mySSetting read(String save_line) {
        myCoreLibrary.mCL.debug("reading mySSetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        String[] split = save_line.split(":");
        if (split.length < 2) {
            myCoreLibrary.mCL.debug("mySSetting read failure; no \":\" found");
            return null;
        }

        try {
            return new mySSetting(split[0], split[1]);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public String write() {
        return key + ": " + getValue();
    }
}
