package REALDrummer.settings;

import REALDrummer.myCoreLibrary;
import REALDrummer.myData;
import REALDrummer.myList;
import REALDrummer.myPlugin;
import static REALDrummer.utils.ArrayUtilities.compare;
import static REALDrummer.utils.MessageUtilities.*;

public abstract class mySetting extends myData {
    public myList<mySetting> mySettings = new myList<mySetting>();

    protected String target = null, key;

    // getters
    public String getKey() {
        return key;
    }

    @Override
    public myPlugin getPlugin() {
        return myCoreLibrary.mCL;
    }

    public String getTarget() {
        return target;
    }

    public abstract Object getValue();

    // cast-return methods
    public String[] arrayValue() {
        try {
            return (String[]) getValue();
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a boolean getValue() from a non-myBSetting mySetting!", exception, this);
            return null;
        }
    }

    public boolean booleanValue() {
        try {
            return (boolean) getValue();
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a boolean getValue() from a non-myBSetting mySetting!", exception, this);
            return false;
        }
    }

    public String optionValue() {
        return (String) getValue();
    }

    public int intValue() {
        try {
            return (int) getValue();
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get an int getValue() from a non-myISetting mySetting!", exception, this);
            return -1;
        }
    }

    public String stringValue() {
        try {
            return (String) getValue();
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a String getValue() from a non-mySSetting mySetting!", exception, this);
            return "";
        }
    }

    public long timeValue() {
        try {
            return (long) getValue();
        } catch (ClassCastException exception) {
            err(myCoreLibrary.mCL, "Someone tried to get a long time getValue() from a non-myTSetting mySetting!", exception, this);
            return -1;
        }
    }

    // storage
    @Override
    public void store(myList<myData> data) {

    }

    // overrides
    @Override
    public int compareTo(myData data) {
        if (!(data instanceof mySetting))
            return 0;

        mySetting setting = (mySetting) data;
        int target_compare = target.compareTo(setting.target);
        if (target_compare == 0)
            return key.compareTo(setting.key);
        else
            return target_compare;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof mySetting))
            return false;

        mySetting setting = (mySetting) object;
        return (setting.key == null && key == null || setting.key != null && key != null && setting.key.equals(key))
                && (setting.target == null && target == null || setting.target != null && target != null && setting.target.equals(target));
    }

    @Override
    public int matchTo(String... match_parameters) {
        return compare(new String[] { target, key }, match_parameters);
    }

    @Override
    public String toString() {
        return "\"" + key + "\" for " + target + ": " + (getValue() instanceof String ? "\"" + getValue() + "\"" : getValue());
    }
}
