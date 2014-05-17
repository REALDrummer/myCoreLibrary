package REALDrummer.settings;

import REALDrummer.Matchable;
import REALDrummer.myCoreLibrary;
import REALDrummer.myPlugin;

import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.MessageUtilities.*;

public abstract class mySetting implements Comparable<mySetting>, Matchable {
    protected myPlugin plugin = null;
    protected String target = null, key;

    // getters
    public String getKey() {
        return key;
    }

    public myPlugin getPlugin() {
        return plugin;
    }

    public String getSaveFormat() {
        return "[key]: [value]";
    }

    public String getTarget() {
        return target;
    }

    public abstract Object getValue();

    public abstract void setValue(Object new_value);

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

    // overrides
    @Override
    public int compareTo(mySetting setting) {
        return compare(new Object[] { target, key, plugin }, new Object[] { setting.target, setting.key, setting.plugin });
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
        return compare(new String[] { target, key, plugin.getName() }, match_parameters);
    }

    @Override
    public String toString() {
        return "\"" + key + "\" for " + target + " (" + (plugin != null ? plugin.getName() : "null") + "): "
                + (getValue() instanceof String ? "\"" + getValue() + "\"" : getValue() instanceof String[] ? writeArray((String[]) getValue()) : getValue());
    }
}
