package REALDrummer.settings;

public class myISetting extends mySetting {
    private int value;

    public myISetting(String key, int initial_value) {
        this.key = key;
        value = initial_value;
    }

    public myISetting(String target, String key, int initial_value) {
        this.target = target;
        new myISetting(key, initial_value);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public boolean readValue(String read_value) {
        if (read_value.toLowerCase().startsWith("infinit")) {
            value = -1;
            return true;
        }

        try {
            value = Integer.parseInt(read_value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof Integer) {
            value = (Integer) new_value;
            return true;
        }
        return false;
    }
}
