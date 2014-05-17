package REALDrummer.settings;

import REALDrummer.myPlugin;

public class myISetting extends mySetting {
    private int value;

    public myISetting(String key, int initial_value) {
        this.key = key;
        value = initial_value;
    }

    public myISetting(myPlugin plugin, String target, String key, int initial_value) {
        this.plugin = plugin;
        this.target = target;
        new myISetting(key, initial_value);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Object new_value) {
        if (new_value instanceof Integer)
            value = (Integer) new_value;
        else
            plugin.err("Someone tried to set the value of a myISetting to something other than an Integer!", "illegal value given in setValue()", "setting: " + toString(),
                    "intended value: " + new_value);
    }
}
