package REALDrummer.settings;

import static REALDrummer.utils.ArrayUtilities.readArray;
import static REALDrummer.utils.ArrayUtilities.writeArray;
import REALDrummer.myCoreLibrary;

public class myASetting extends mySetting {
    public myASetting(String key, String[] initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myASetting(boolean marker, String target, String key, String... initial_value) {
        this.target = target;
        new myASetting(key, initial_value);
    }

    // getters
    public String[] getValue() {
        return (String[]) value;
    }

    // readers and writers
    @Override
    public myASetting read(String save_line) {
        myCoreLibrary.mCL.debug("reading myASetting: \"" + save_line + "\"");
        save_line = save_line.trim();

        String[] split = save_line.split(":");
        if (split.length < 2) {
            myCoreLibrary.mCL.debug("myASetting read failure; no \":\" found");
            return null;
        }

        return new myASetting(split[0], readArray(split[1]));
    }

    @Override
    public String write() {
        return key + ": " + writeArray(getValue());
    }
}
