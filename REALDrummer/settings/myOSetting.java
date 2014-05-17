package REALDrummer.settings;

import REALDrummer.myPlugin;

public class myOSetting extends mySetting {
    public String[] options;
    public String value = null, raw_value;

    public myOSetting(String key, String initial_value, String... options) {
        this.key = key;
        this.raw_value = initial_value;
        this.options = options;
        setValue(initial_value);
    }

    public myOSetting(myPlugin plugin, String target, String key, String initial_value, String... options) {
        this.plugin = plugin;
        this.target = target;
        new myOSetting(key, initial_value, options);
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getRawValue() {
        return raw_value;
    }

    @Override
    public void setValue(Object new_value) {
        if (new_value instanceof String) {
            raw_value = (String) new_value;

            // determine the value based on the raw value and the options
            for (String option : options)
                if (raw_value.toLowerCase().startsWith(option.toLowerCase())) {
                    value = option;
                    break;
                }
        } else
            plugin.err("Someone tried to set the value of a myOSetting to something other than a String!", "illegal value given in setValue()", "setting: " + toString(),
                    "intended value: " + new_value);
    }

    // overridden save format
    @Override
    public String getSaveFormat() {
        return "[key] ([options]): [value]";
    }
}
