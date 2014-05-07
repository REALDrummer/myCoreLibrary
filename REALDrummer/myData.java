package REALDrummer;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.bukkit.command.CommandSender;

public abstract class myData implements Comparable<myData>, Matchable {
    // standard setting methods
    public abstract myPlugin getPlugin();

    public abstract String getSaveFormat();

    // debug utility method
    public void debug(String message) {
        getPlugin().debug(message);
    }

    // loaders and savers
    public boolean load(CommandSender sender) {
        String data_type = getClass().getSimpleName().toLowerCase() + "s";
        debug("loading " + data_type + " (" + getPlugin().getName() + ")...");

        try {
            File text_file = new File(getPlugin().getDataFolder(), data_type+".txt"), raf_file = new File(getPlugin().getDataFolder(), data_type+".raf");
            if (raf_file.exists() && (!text_file.exists() || getPlugin().getSetting(data_type+" file format").optionValue().eq))
        } catch (IOException exception) {
            return false;
        }
    }

    public boolean save(CommandSender sender) {
        return save(sender, true);
    }

    private boolean save(CommandSender sender, boolean display_message) {

    }

    // data-handling methods
    public myData read(String line) {
        
    }

    public abstract void store(myList<myData> data);

    public String write() {
        
    }

    public static String write(myData data) {
        return data.write();
    }

    // important overridden methods
    @Override
    public abstract int compareTo(myData data);

    @Override
    public abstract boolean equals(Object object);

    @Override
    public abstract int matchTo(String... match_parameters);

    @Override
    public abstract String toString();
}
