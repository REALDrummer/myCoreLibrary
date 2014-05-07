package REALDrummer.settings;

public class myOSetting extends mySetting {
    public String[] options;
    public String value = null, raw_value;
    public byte option;

    public myOSetting(String key, String initial_value, String... options) {
        this.key = key;
        this.raw_value = initial_value;
        this.options = options;

        // determine the value based on the raw value and the options
        for ()
    }

    public myOSetting(String target, String key, String initial_value, String... options) {
        this.target = target;
        new myOSetting(key, initial_value, options);
    }

    // getters
    @Override
    public String getValue() {
        return value;
    }

    public String getRawValue() {
        return raw_value;
    }

    // readers and writers
    @Override
    public String getSaveFormat() {
        return "[key] ([options]): [value]";
    }
}
