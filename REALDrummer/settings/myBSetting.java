package REALDrummer.settings;

import static REALDrummer.utils.StringUtilities.readResponse;

public class myBSetting extends mySetting {
    private boolean value;

    public myBSetting(String key, boolean initial_value) {
        this.key = key;
        value = initial_value;
    }

    public myBSetting(String target, String key, boolean initial_value) {
        this.target = target;
        new myBSetting(key, initial_value);
    }

    @Override
    public Boolean getValue() {
        return (boolean) value;
    }

    @Override
    public boolean readValue(String read_value) {
        Boolean read_answer = readResponse(read_value);
        if (read_answer == null)
            return false;

        value = read_answer;
        return true;
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof Boolean) {
            value = (Boolean) new_value;
            return true;
        }
        return false;
    }

    public String writeValue() {
        return key + "? " + (value ? "yes" : "no");
    }
}
