package REALDrummer;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import REALDrummer.interfaces.Matchable;
import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.ListUtilities.compare;
import static REALDrummer.utils.StringUtilities.*;

/** @author connor */
public class myZone implements Matchable, Cloneable, Comparable<myZone> {
    myList<myZone> zones = new myList<myZone>();

    private Location corner1, corner2;

    /** This constructor creates a new {@link myZone} with the two given corners as its opposite diagonals.
     * 
     * @param corner1
     *            designates the first corner of the {@link myZone}.
     * @param corner2
     *            designates the corner of the {@link myZone} diagonally opposite <b><tt>corner1</b></tt>. */
    public myZone(Location corner1, Location corner2) {
        if (!corner1.getWorld().equals(corner2.getWorld()))
            throw new IllegalArgumentException();

        double x1 = corner1.getX(), y1 = corner1.getY(), z1 = corner1.getZ(), x2 = corner2.getX(), y2 = corner2.getY(), z2 = corner2.getZ();

        // put all the smallest values in corner1
        corner1 = new Location(corner1.getWorld(), x1 < x2 ? x1 : x2, y1 < y2 ? y1 : y2, z1 < z2 ? z1 : z2);
        // put all the largest values in corner2
        corner1 = new Location(corner1.getWorld(), x1 < x2 ? x2 : x1, y1 < y2 ? y2 : y1, z1 < z2 ? z2 : z1);
    }

    // utilities
    /** This method determines whether or not the given {@link Entity} is within this {@link myZone}.
     * 
     * @param entity
     *            is the {@link Entity} which this method will search for inside this {@link myZone}.
     * @return <b>true</b> if <b><tt>entity</b></tt> is inside this {@link myZone}; <b>false</b> otherwise.
     * @see {@link #contains(Location)} */
    public boolean contains(Entity entity) {
        return contains(entity.getLocation());
    }

    /** This method determines whether or not the given {@link Entity} is within this {@link myZone}.
     * 
     * @param location
     *            is the {@link Location} which this method will search for inside this {@link myZone}.
     * @return <b>true</b> if <b><tt>location</b></tt> is inside this {@link myZone}; <b>false</b> otherwise.
     * @see {@link #contains(Entity)} */
    public boolean contains(Location location) {
        return corner1.getX() <= location.getX() && corner2.getX() >= location.getX() && corner1.getY() <= location.getY() && corner2.getY() >= location.getY()
                && corner1.getZ() <= location.getZ() && corner2.getZ() >= location.getZ();
    }

    /** This method determines whether or not a given x-coordinate is contained inside this {@link myZone}.
     * 
     * @param x
     *            is the x-coordinate that this method will check for in the {@link myZone}.
     * @return <b>true</b> if this {@link myZone} contains the given <b><tt>x</b></tt> (as an x-coordinate); <b>false</b> otherwise. */
    public boolean containsX(double x) {
        return corner1.getX() < x && corner2.getX() > x;
    }

    /** This method determines whether or not a given y-coordinate is contained inside this {@link myZone}.
     * 
     * @param y
     *            is the y-coordinate that this method will check for in the {@link myZone}.
     * @return <b>true</b> if this {@link myZone} contains the given <b><tt>y</b></tt> (as an y-coordinate); <b>false</b> otherwise. */
    public boolean containsY(double y) {
        return corner1.getY() < y && corner2.getY() > y;
    }

    /** This method determines whether or not a given z-coordinate is contained inside this {@link myZone}.
     * 
     * @param z
     *            is the z-coordinate that this method will check for in the {@link myZone}.
     * @return <b>true</b> if this {@link myZone} contains the given <b><tt>z</b></tt> (as an z-coordinate); <b>false</b> otherwise. */
    public boolean containsZ(double z) {
        return corner1.getZ() < z && corner2.getZ() > z;
    }

    /** This method retrieves <i>all</i> of the {@link Entity Entities} that are currently inside this {@link myZone}.
     * 
     * @return a <tt>{@link myList}<{@link Entity}></tt> containing all of the {@link Entity Entities} currently inside this {@link myZone}.
     * @see {@link #getEntitiesInsideThatAre(Class)}, {@link #getEntitiesInsideThatAre(EntityType)}, and {@link #getPlayersInside()} */
    public myList<Entity> getAllEntitiesInside() {
        myList<Entity> entities_inside = new myList<Entity>();
        for (Entity entity : corner1.getWorld().getEntities())
            if (contains(entity))
                entities_inside.add(entity);
        return entities_inside;
    }

    /** This method retrieves the {@link Entity Entities} that are currently inside this {@link myZone} whose types are the same as or subclasses of <b><tt>type</b></tt>.
     * 
     * @param type
     *            is a <tt>Class<{@link Entity}></tt> which all results will either be an instance of or a subclass of.
     * 
     * @return a <tt>{@link myList}<{@link Entity}></tt> containing all of the {@link Entity Entities} currently inside this {@link myZone} whose types are the same as or
     *         subclasses of <b><tt>type</b></tt>.
     * @see {@link #getAllEntitiesInside()}, {@link #getEntitiesInsideThatAre(EntityType)}, and {@link #getPlayersInside()} */
    public myList<Entity> getEntitiesInsideThatAre(Class<Entity> type) {
        myList<Entity> entities_inside = new myList<Entity>();
        for (Entity entity : corner1.getWorld().getEntities())
            if (type.isAssignableFrom(entity.getClass()) && contains(entity))
                entities_inside.add(entity);
        return entities_inside;
    }

    /** This method retrieves the {@link Entity Entities} that are currently inside this {@link myZone} whose {@link EntityType}s are the same as <b><tt>type</b></tt>.
     * 
     * @param type
     *            is a <tt>Class<{@link Entity}></tt> which all results will either be an instance of or a subclass of.
     * 
     * @return a <tt>{@link myList}<{@link Entity}></tt> containing all of the {@link Entity Entities} currently inside this {@link myZone} whose {@link EntityType}s are the
     *         same as <b><tt>type</b></tt>.
     * @see {@link #getAllEntitiesInside()}, {@link #getEntitiesInsideThatAre(Class)}, and {@link #getPlayersInside()} */
    public myList<Entity> getEntitiesInsideThatAre(EntityType type) {
        myList<Entity> entities_inside = new myList<Entity>();
        for (Entity entity : corner1.getWorld().getEntities())
            if (entity.getType() == type && contains(entity))
                entities_inside.add(entity);
        return entities_inside;
    }

    /** This method retrieves the {@link Player}s that are currently inside this {@link myZone}.
     * 
     * @return a <tt>{@link myList}<{@link Entity}></tt> containing all of the {@link Player}s currently inside this {@link myZone}.
     * @see {@link #getAllEntitiesInside()}, {@link #getEntitiesInsideThatAre(Class)}, and {@link #getEntitiesInsideThatAre(EntityType)} */
    public myList<Player> getPlayersInside() {
        myList<Player> players_inside = new myList<Player>();
        for (Player player : mCL.getServer().getOnlinePlayers())
            if (contains(player))
                players_inside.add(player);
        return players_inside;
    }

    /** This method determines whether or not this {@link myZone} overlaps with another given {@link myZone}.
     * 
     * @param zone
     *            is the {@link myZone} that will be checked for overlap with this {@link myZone}.
     * @return <b>true</b> if this {@link myZone} overlaps <b><tt>zone</b></tt>; <b>false</b> otherwise. */
    public boolean overlaps(myZone zone) {
        return zone.corner1.getX() <= corner2.getX() && zone.corner2.getX() >= corner2.getX() && zone.corner1.getY() <= corner2.getY()
                && zone.corner2.getY() >= corner2.getY() && zone.corner1.getZ() <= corner2.getZ() && zone.corner2.getZ() >= corner2.getZ();
    }

    // overrides
    @Override
    public int compareTo(myZone zone) {
        return compare(new Object[] { corner1, corner2 }, zone.corner1, zone.corner2);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof myZone && ((myZone) object).corner1.equals(corner1) && ((myZone) object).corner2.equals(corner2);
    }

    @Override
    public int matchTo(String... match_parameters) {
        return match(new Object[] { corner1, corner2 }, match_parameters);
    }

    @Override
    public String toString() {
        return "from " + writeLocation(corner1, false) + " to " + writeLocation(corner2, false);
    }
}
