package REALDrummer.settings;

public class mySSetting extends mySetting {
    private String value;

    public mySSetting(String key, String initial_value) {
        this.key = key;
        value = initial_value;
    }

    public mySSetting(String target, String key, String initial_value) {
        this.target = target;
        new mySSetting(key, initial_value);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean readValue(String read_value) {
        value = read_value;
        return true;
    }

    @Override
    public boolean setValue(Object new_value) {
        if (new_value instanceof String) {
            value = (String) new_value;
            return true;
        }
        return false;
    }

}
