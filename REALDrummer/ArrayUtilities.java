package REALDrummer;

import java.util.ArrayList;

import static REALDrummer.MessageUtilities.err;

public class ArrayUtilities {
    public static <T> ArrayList<T> arrayToArrayList(T[] array) {
        ArrayList<T> array_list = new ArrayList<T>();
        for (T object : array)
            array_list.add(object);
        return array_list;
    }

    /** This method combines all of the given <tt>String</tt>s array into a single String.
     * 
     * @param strings
     *            is the list of <tt>String</tt>s that will be combined into a signel <tt>String</tt>.
     * @param separator
     *            is the String used to separate the different <tt>String</tt>s, e.g. ", " in the list "apple, orange, lemon, melon"
     * @param indices
     *            is an optional parameter that can be used to select a range of indices in the array <b> <tt>strings</b></tt>. If one index is given, it will be used as the
     *            minimum index (inclusive) for parsing <b><tt>strings</b></tt> for adding pieces to the resultant <tt>String</tt>. If two indices are given, the first index
     *            is used as the minimum index (inclusive) and the second is used as the maximum (non-inclusive).
     * @return the <tt>String</tt> constructed by putting all the <tt>String</tt>s in <b><tt>strings</tt></b> together into one <tt>String</tt>. */
    public static String combine(String[] strings, String separator, int... indices) {
        if (separator == null)
            separator = "";
        int start_index = 0, end_index = strings.length;
        if (indices.length > 0) {
            start_index = indices[0];
            if (indices.length > 1)
                end_index = indices[1];
        }
        String combination = "";
        for (int i = start_index; i < end_index; i++) {
            try {
                combination += strings[i];
            } catch (ArrayIndexOutOfBoundsException e) {
                err(myCoreLibrary.mCL, "Someone gave me bad indices!", e);
            }
            if (i < end_index - 1)
                combination += separator;
        }
        return combination;
    }

    public static String combine(String[] strings, int... indices) {
        return combine(strings, "", indices);
    }

    /** This method determines whether or not a given array contains a given Object.
     * 
     * @param objects
     *            is the array of Objects that will be searched.
     * @param target
     *            is the Object that <b><tt>objects</b></tt> may contain.
     * @return <b>true</b> if <b><tt>object</b></tt> contains <b><tt>target</b></tt>; <b>false</b> otherwise. */
    public static boolean contains(Object[] objects, Object target) {
        for (Object object : objects)
            if (object.equals(target))
                return true;
        return false;
    }

    /** This method determines whether or not a given array contains a given short.
     * 
     * @param shorts
     *            is the array of shorts that will be searched.
     * @param target
     *            is the short that <b><tt>objects</b></tt> may contain.
     * @return <b>true</b> if <b><tt>object</b></tt> contains <b><tt>target</b></tt>; <b>false</b> otherwise. */
    public static boolean contains(short[] shorts, short target) {
        for (short object : shorts)
            if (object == target)
                return true;
        return false;
    }

    /** This method separates items in a properly formatted list into individual Strings.
     * 
     * @param list
     *            is the String which will be divided into separate Strings by deconstructing the list framework.
     * @param options
     *            are optional parameters used to change the separator String and the final conjuction String. (By default, these are ", " and "and", respectively.) The first
     *            item is the separator String (the String used to separate the items in the list); the second item is a final conjunction String, a String which may be
     *            attached to the beginning of the last item in the list.
     * @return a String[] of all of the different items in the list given. */
    public static String[] readArray(String list, String... options) {
        String[] objects = null;
        String separator = ", ", final_conjunction = "and";
        if (options.length > 0 && options[0] != null)
            separator = options[0];
        if (options.length > 1 && options[1] != null)
            final_conjunction = options[1];
        // for 3+-item lists
        if (list.contains(separator)) {
            objects = list.split(separator);
            // remove the final conjunction (usually "and") at the beginning of the list object
            objects[objects.length - 1] = objects[objects.length - 1].substring(final_conjunction.length() + 1);
        }
        // for 2-item lists
        else if (list.contains(" " + final_conjunction + " "))
            return list.split(" " + final_conjunction + " ");
        // for 1-item lists
        else
            return new String[] { list };
        return objects;
    }

    public static ArrayList<String> readArrayList(String list, String... options) {
        String[] array = readArray(list, options);
        if (array == null)
            return null;
        ArrayList<String> product = new ArrayList<String>();
        for (String list_item : array)
            product.add(list_item);
        return product;
    }

    /** This method returns a grammatically correct list that contains all of the items given in a String array.
     * 
     * @param objects
     *            is the String array which will be written into a list.
     * @param options
     *            is an optional parameter that can allow the user to customize the String used to separate items in a 3+-item list (which is ", " by default) and/or the
     *            String used to separate the items in a 2-item list or the last item in a 3+-item list from the rest (which is "and" by default). The first option (
     *            <tt>[0]</tt>) is the separator String; the second option (<tt>[1]</tt>) is the final conjunction String.
     * @return a grammatically correct list of the objects in <b><tt>objects</b></tt>. */
    public static String writeArray(Object[] objects, String... options) {
        String separator = ", ", final_conjunction = "and";
        if (options.length > 0 && options[0] != null)
            separator = options[0];
        if (options.length > 1 && options[1] != null)
            final_conjunction = options[1];
        if (objects.length == 0)
            return "";
        else if (objects.length == 1)
            return String.valueOf(objects[0]);
        else if (objects.length == 2)
            return objects[0] + " " + final_conjunction + " " + objects[1];
        else {
            String list = "";
            for (int i = 0; i < objects.length; i++) {
                list += objects[i];
                if (i < objects.length - 1) {
                    list += separator;
                    if (i == objects.length - 2)
                        list += final_conjunction + " ";
                }
            }
            return list;
        }
    }

    public static String writeArrayList(ArrayList<?> objects, String... options) {
        String[] strings = new String[objects.size()];
        for (int i = 0; i < objects.size(); i++)
            if (objects.get(i) instanceof String)
                strings[i] = objects.get(i).toString();
            else
                return null;
        return writeArray(strings);
    }
}
