package REALDrummer.settings;

public class myASetting extends mySetting {
    public myASetting(String key, String[] initial_value) {
        this.key = key;
        this.value = initial_value;
    }

    public myASetting(boolean marker, String target, String key, String... initial_value) {
        this.target = target;
        new myASetting(key, initial_value);
    }

    // getters
    @Override
    public String[] getValue() {
        return (String[]) value;
    }

    @Override
    public String getSaveFormat() {
        return "[key]: [value]";
    }

}
