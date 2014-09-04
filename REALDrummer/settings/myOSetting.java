package REALDrummer.settings;

import static REALDrummer.utils.ListUtilities.writeArray;

/** This {@link mySetting} allows the user to set
 * 
 * @author connor */
public class myOSetting extends mySetting {
    private String[] options;
    private String value = null, raw_value;

    public myOSetting(String key, String initial_value, String... options) {
        this.key = key;
        this.options = options;
        setValue(initial_value);
    }

    /** This constructor is for internal use in myCoreLibrary. Unlike {@link #myOSetting(String, String, String...) myOSetting's other constructor}, it allows the
     * initialization of a <b><tt>target</tt></b> for each setting to be used for individual or group-based settings as opposed to server-wide settings.
     * 
     * @param marker
     *            is an unused parameter that simply counters the ambiguity between this constructor and {@link #myOSetting(String, String, String...) myOSetting's other
     *            constructor}.
     * @param target
     *            describes the player or group of players to which this {@link mySetting} applies. A <b>null</b> target indicates a server-wide ("global") setting; a target
     *            enclosed in brackets ("[]") indicates a permissions group setting; and a target not enclosed in brackets indicates an individual player's setting.
     * @param key
     *            is a <tt>String</tt> describing what the setting determines, e.g. "auto-update".
     * @param initial_value
     *            is the default value of the setting.
     * @param options
     *            is a list of the different options that are available for this setting. MyOSettings have <tt>String</tt> values, but unlike {@link mySSetting}s, they can
     *            only be set to one of a variety of values given by this <b><tt>options</t></tt> argument. */
    public myOSetting(@SuppressWarnings("unused") boolean marker, String target, String key, String initial_value, String... options) {
        this.target = target;
        new myOSetting(key, initial_value, options);
    }

    public String getRawValue() {
        return raw_value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean readValue(String read_value) {
        return setValue(read_value);
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof String) {
            raw_value = (String) new_value;

            // determine the value based on the raw value and the options
            value = null;
            for (String option : options)
                if (raw_value.toLowerCase().startsWith(option.toLowerCase())) {
                    value = option;
                    break;
                }
            if (value == null)
                return false;

            return true;
        }
        return false;
    }

    @Override
    public String writeValue() {
        return key + " (" + writeArray(options, ", ", "or") + "): " + value;
    }
}
