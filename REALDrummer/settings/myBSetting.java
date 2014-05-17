package REALDrummer.settings;

import REALDrummer.myPlugin;

public class myBSetting extends mySetting {
    private boolean value;

    public myBSetting(String key, boolean initial_value) {
        this.key = key;
        value = initial_value;
    }

    public myBSetting(myPlugin plugin, String target, String key, boolean initial_value) {
        this.plugin = plugin;
        this.target = target;
        new myBSetting(key, initial_value);
    }

    @Override
    public Boolean getValue() {
        return (boolean) value;
    }

    @Override
    public void setValue(Object new_value) {
        if (new_value instanceof Boolean)
            value = (Boolean) new_value;
        else
            plugin.err("Someone tried to set the value of a myBSetting to something other than a Boolean!", "illegal value given in setValue()", "setting: " + toString(),
                    "intended value: " + new_value);
    }

    // overriden save format
    @Override
    public String getSaveFormat() {
        return "[key]? [value]";
    }
}
