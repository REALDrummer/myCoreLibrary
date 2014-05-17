package REALDrummer.settings;

import REALDrummer.myPlugin;

public class myASetting extends mySetting {
    private String[] value;

    public myASetting(String key, String... initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myASetting(myPlugin plugin, String target, String key, String... initial_value) {
        this.plugin = plugin;
        this.target = target;
        new myASetting(key, initial_value);
    }

    @Override
    public String[] getValue() {
        return (String[]) value;
    }

    @Override
    public void setValue(Object new_value) {
        if (new_value instanceof String[])
            value = (String[]) new_value;
        else
            plugin.err("Someone tried to set the value of a myASetting to something other than a String[]!", "illegal value given in setValue()", "setting: " + toString(),
                    "intended value: " + new_value);
    }
}
