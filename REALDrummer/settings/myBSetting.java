package REALDrummer.settings;

import static REALDrummer.utils.StringUtilities.readResponse;
import REALDrummer.myCoreLibrary;

public class myBSetting extends mySetting {
    public myBSetting(String key, boolean initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myBSetting(String target, String key, boolean initial_value) {
        this.target = target;
        new myBSetting(key, initial_value);
    }

    // getters
    public boolean getValue() {
        return (boolean) value;
    }

    // readers and writers
    @Override
    public myBSetting read(String save_line) {
        myCoreLibrary.mCL.debug("reading myBSetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        String[] split = save_line.split("?");
        if (split.length < 2) {
            myCoreLibrary.mCL.debug("myBSetting read failure; no \"?\" found");
            return null;
        }

        return new myBSetting(split[0], readResponse(split[1]));
    }

    @Override
    public String write() {
        return key + "? " + (getValue() ? "yeah" : "nah");
    }
}
