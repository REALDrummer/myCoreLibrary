package REALDrummer.settings;

public class mySSetting extends mySetting {
    public mySSetting(String key, String initial_value) {
        this.key = key;
        value = initial_value;
    }

    // getters
    public String getValue() {
        return (String) value;
    }
}
