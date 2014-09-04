package REALDrummer.settings;

import static REALDrummer.utils.ListUtilities.*;

public class myASetting extends mySetting {
    private String[] value;
    private String separator = ", ", final_conjunction = "and";

    public myASetting(String key, String[] initial_value, String... options) {
        this.key = key;
        this.value = initial_value;
        if (options.length > 0) {
            separator = options[0];
            if (options.length > 1)
                final_conjunction = options[1];
        }
    }

    public myASetting(String target, String key, String[] initial_value, String... options) {
        this.target = target;
        new myASetting(key, initial_value);
    }

    @Override
    public String[] getValue() {
        return value;
    }

    @Override
    public boolean readValue(String read_value) {
        try {
            value = readArray(read_value, separator, final_conjunction);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof String[]) {
            value = (String[]) new_value;
            return true;
        }
        return false;
    }

    @Override
    public String writeValue() {
        return "key: " + writeArray(value, separator, final_conjunction);
    }
}
