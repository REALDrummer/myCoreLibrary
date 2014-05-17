package REALDrummer.settings;

import REALDrummer.myPlugin;

public class myTSetting extends mySetting {
    private long value;

    public myTSetting(String key, long initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myTSetting(myPlugin plugin, String target, String key, long initial_value) {
        this.plugin = plugin;
        this.target = target;
        new myTSetting(key, initial_value);
    }

    @Override
    public Long getValue() {
        return (long) value;
    }

    @Override
    public void setValue(Object new_value) {
        if (new_value instanceof Long)
            value = (Long) new_value;
        else
            plugin.err("Someone tried to set the value of a myTSetting to something other than a Long!", "illegal value given in setValue()", "setting: " + toString(),
                    "intended value: " + new_value);
    }
}
