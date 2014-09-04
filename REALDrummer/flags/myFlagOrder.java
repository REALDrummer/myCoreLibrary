package REALDrummer.flags;

import REALDrummer.myList;
import REALDrummer.myPlugin;
import REALDrummer.interfaces.Matchable;
import static REALDrummer.utils.ListUtilities.*;

class myFlagOrder implements Comparable<myFlagOrder>, Matchable {
    private myList<myFlag> flags;

    private myPlugin plugin;
    private String command;
    private byte order;
    // = -1 for 1 optional flag requested, = 0 for at least 1 optional flag requested, 1 for 1 flag required, 2 for 1+ flags required, 3 for all flags requried
    private byte amount_required;

    public myFlagOrder(myPlugin plugin, String command, byte order, byte amount_required, myFlag... flags) {
        new myFlagOrder(plugin, command, order, amount_required, new myList<myFlag>(flags));
    }

    public myFlagOrder(myPlugin plugin, String command, byte order, byte amount_required, myList<myFlag> flags) {
        this.plugin = plugin;
        this.command = command;
        this.order = order;
        this.amount_required = amount_required;
        this.flags = flags;
    }

    public String getCommand() {
        return command;
    }

    public myList<myFlag> getFlags() {
        return flags;
    }

    public byte getAmountRequired() {
        return amount_required;
    }

    public byte getOrder() {
        return order;
    }

    // utilities
    public boolean isOptional() {
        return amount_required < 1;
    }

    public boolean isRequired() {
        return amount_required > 0;
    }

    // overrides
    @Override
    public int compareTo(myFlagOrder order) {
        return compare(new Object[] { plugin, command, order }, new Object[] { order.command, order.order });
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof myFlagOrder && ((myFlagOrder) object).command.equals(command) && ((myFlagOrder) object).order == order;
    }

    @Override
    public int matchTo(String... match_parameters) {
        return match(new Object[] { plugin, command, order }, match_parameters);
    }

    @Override
    public String toString() {
        // add in the command and the order of the myFlagOrder
        String base = '/' + command + "[" + order + "]: ", opener = "[", closer = "]", separator = ",";
        if (amount_required <= 0) {
            opener = "(";
            closer = ")";
        }
        if (amount_required == 1 || amount_required == -1)
            separator = "/";

        // assemble a list of all the flags' toString() outputs
        String[] flag_descriptions = new String[flags.size()];
        byte i = 0;
        for (myFlag flag : flags) {        // I used a foreach here because myLists are faster with iterators than with fetching from indices
            flag_descriptions[i] = flag.toString();
            i++;
        }

        // return the result
        return base + writeArray(flag_descriptions, separator, "", opener, closer);
    }
}
