package REALDrummer.settings;

import REALDrummer.myCoreLibrary;
import static REALDrummer.utils.StringUtilities.readTime;
import static REALDrummer.utils.StringUtilities.writeTime;

public class myTSetting extends mySetting {
    public myTSetting(String key, long initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myTSetting(String target, String key, long initial_value) {
        this.target = target;
        new myTSetting(key, initial_value);
    }

    // getters are instantiated in myLSetting
    public long getValue() {
        return (long) value;
    }

    // readers and writers
    @Override
    public myTSetting read(String save_line) {
        myCoreLibrary.mCL.debug("reading myTSetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        String[] split = save_line.split(":");
        if (split.length < 2) {
            myCoreLibrary.mCL.debug("myTSetting read failure; no \":\" found");
            return null;
        }

        try {
            return new myTSetting(split[0], readTime(split[1]));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    @Override
    public String write() {
        return key + ": " + writeTime(getValue(), false);
    }
}
