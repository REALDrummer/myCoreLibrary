package REALDrummer;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;

import static REALDrummer.utils.ListUtilities.*;

/** This subclass of {@link myData} is for data that is "directly modifiable" (hence the "DM") by the players. All {@link myDMData} are able to be modified directly by players
 * and can be configured to do such things as show up on a list command (or not) or allow or prohibit a specific set of players from using it. All {@link myDMData} objects
 * have a name, visibility settings, a list of players that acts as a whitelist or blacklist depending on the visibility, a list of owners. {@link myDMData} objects also come
 * with some standard command much like {@link myData} comes with standard loading and saving commands. <tt>MyCoreLibrary</tt>'s {@link REALDrummer.arenas.myArena myArenas} and
 * <tt>myUltraWarps</tt>'s <tt>UltraWarp</tt>s are examples.
 * 
 * @author connor */
public abstract class myDMData extends myData {
    private static final long serialVersionUID = 3647901911528041893L;

    private String name;
    private myDMDataVisibility visibility;
    private myList<UUID> list = new myList<UUID>(), owners;

    public enum myDMDataVisibility {
        OPEN(ChatColor.WHITE), ADVERTISED(ChatColor.AQUA), SECRET(ChatColor.GRAY), PRIVATE(ChatColor.DARK_GRAY);

        ChatColor list_color;

        private myDMDataVisibility(ChatColor list_color) {
            this.list_color = list_color;
        }

        public ChatColor getColor() {
            return list_color;
        }

        public static myDMDataVisibility getVisibility(boolean listed, boolean restricted) {
            if (listed)
                if (restricted)
                    return myDMDataVisibility.ADVERTISED;
                else
                    return myDMDataVisibility.OPEN;
            else if (restricted)
                return myDMDataVisibility.PRIVATE;
            else
                return myDMDataVisibility.SECRET;
        }

        public boolean isListed() {
            return this == OPEN || this == ADVERTISED;
        }

        public boolean isRestricted() {
            return this == ADVERTISED || this == PRIVATE;
        }
    }

    public myDMData(String name, boolean listed, boolean restricted, UUID... owners) {
        super();
        this.name = name;
        visibility = myDMDataVisibility.getVisibility(listed, restricted);
        this.owners = new myList<UUID>(owners);
    }

    public myDMData(String name, myDMDataVisibility visibility, UUID... owners) {
        super();
        this.name = name;
        this.visibility = visibility;
        this.owners = new myList<UUID>(owners);
    }

    public myDMData(String name, boolean listed, boolean restricted, myList<UUID> owners) {
        super();
        this.name = name;
        visibility = myDMDataVisibility.getVisibility(listed, restricted);
        this.owners = owners;
    }

    public myDMData(String name, myDMDataVisibility visibility, myList<UUID> owners) {
        super();
        this.name = name;
        this.visibility = visibility;
        this.owners = owners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public myDMDataVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(myDMDataVisibility visibility) {
        this.visibility = visibility;
    }

    public myList<UUID> getList() {
        return list;
    }

    public void setList(myList<UUID> list) {
        this.list = list;
    }

    public myList<UUID> getOwners() {
        return owners;
    }

    public void setOwners(myList<UUID> owners) {
        this.owners = owners;
    }

    // abstract methods
    public abstract void create(HashMap<String, Object> parameters);

    // overrides
    @Override
    public int myDataCompare(myData data) {
        return compare(new Object[] { name, owners }, new Object[] { ((myDMData) data).name, ((myDMData) data).owners }); /* data can be cast to myDMData because if it wasn't
                                                                                                                           * a type of myDMData, the myData compareTo() method
                                                                                                                           * would have already returned Integer.MAX_VALUE */
    }

    public boolean myDataEquals(myData data) {
        // if the two arenas' names are different, they are different
        if (!((myDMData) data).name.equals(name))
            return false;

        // if the two arenas share a name and at least one common owner, they are the same
        for (UUID owner : ((myDMData) data).owners)
            if (owners.contains(owner))
                return true;

        // if they have the same names but mutually exclusive sets of owners, they are different arenas
        return false;
    }

    public int matchTo(String[]... match_parameters) {
        return match(new Object[] { name, owners }, match_parameters);
    }
}
