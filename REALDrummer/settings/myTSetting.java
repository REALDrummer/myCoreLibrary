package REALDrummer.settings;

import static REALDrummer.utils.StringUtilities.readTime;
import static REALDrummer.utils.StringUtilities.writeTime;

public class myTSetting extends mySetting {
    private long value;

    public myTSetting(String key, long initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myTSetting(String target, String key, long initial_value) {
        this.target = target;
        new myTSetting(key, initial_value);
    }

    @Override
    public Long getValue() {
        return (long) value;
    }

    @Override
    public boolean readValue(String read_value) {
        long read_time = readTime(read_value);
        if (read_time == -1)
            return false;
        else {
            value = read_time;
            return true;
        }
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof Long) {
            value = (Long) new_value;
            return true;
        }
        return false;
    }

    @Override
    public String writeValue() {
        return key + ": " + writeTime(value, true);
    }
}
