package REALDrummer;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.bukkit.command.CommandSender;

public abstract class myData implements Comparable<myData>, Matchable {
    // standard setting methods
    public abstract myPlugin getPlugin();

    public abstract String getSaveFormat();

    public void debug(String message) {
        getPlugin().debug(message);
    }

    // loaders and savers
    public boolean load(CommandSender sender) {
        String data_type = getClass().getSimpleName().toLowerCase() + "s";
        debug("loading " + data_type + " (" + getPlugin().getName() + ")...");

        try {
            // TODO: load the file
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
    public abstract myData read(@Nonnull String line);

    public abstract void store(myList<myData> data);

    public abstract String write();

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
