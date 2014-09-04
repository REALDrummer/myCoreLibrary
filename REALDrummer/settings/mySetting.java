package REALDrummer.settings;

import REALDrummer.interfaces.Matchable;

import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.ListUtilities.*;

public abstract class mySetting implements Comparable<mySetting>, Matchable {
    public static final String DEFAULT_TARGET = null;

    protected String target = DEFAULT_TARGET, key;

    // getters
    public String getKey() {
        return key;
    }

    public String getTarget() {
        return target;
    }

    public abstract Object getValue();

    // value setters
    public abstract boolean readValue(String read_value);

    public abstract boolean setValue(Object new_value);

    public String writeValue() {
        return key + ": " + getValue().toString();
    }

    // cast-return methods
    public String[] arrayValue() {
        try {
            return (String[]) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get a boolean getValue() from a non-myASetting mySetting!", exception, this);
            return null;
        }
    }

    public boolean booleanValue() {
        try {
            return (boolean) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get a boolean getValue() from a non-myBSetting mySetting!", exception, this);
            return false;
        }
    }

    public String optionValue() {
        try {
            return (String) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get an option getValue() from a non-myOSetting mySetting!", exception, this);
            return "";
        }
    }

    public int intValue() {
        try {
            return (int) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get an int getValue() from a non-myISetting mySetting!", exception, this);
            return -1;
        }
    }

    public String stringValue() {
        try {
            return (String) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get a String getValue() from a non-mySSetting mySetting!", exception, this);
            return "";
        }
    }

    public long timeValue() {
        try {
            return (long) getValue();
        } catch (ClassCastException exception) {
            mCL.err("Someone tried to get a long time getValue() from a non-myTSetting mySetting!", exception, this);
            return -1;
        }
    }

    // overrides
    @Override
    public int compareTo(mySetting setting) {
        return compare(new String[] { target, key }, new String[] { setting.target, setting.key });
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
        return compare(new String[] { target, key }, new String[] { match_parameters[0], match_parameters[1] });
    }

    @Override
    public String toString() {
        return writeValue();
    }
}
