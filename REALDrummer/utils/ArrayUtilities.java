package REALDrummer.utils;

import java.util.ArrayList;

import static REALDrummer.myCoreLibrary.mCL;
import static REALDrummer.utils.MessageUtilities.*;

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
                err(mCL, "Someone gave me bad indices!", e);
            }
            if (i < end_index - 1)
                combination += separator;
        }
        return combination;
    }

    public static String combine(String[] strings, int... indices) {
        return combine(strings, "", indices);
    }

    @SafeVarargs
    public static <T extends Comparable<? super T>> int compare(T[] initial_array, T... compare_array) {
        // first, try parsing through the lists and comparing the elements in each
        for (int i = 0; i < initial_array.length && i < compare_array.length; i++) {
            int compare = initial_array[i].compareTo(compare_array[i]);
            if (compare != 0)
                return compare;
        }

        // if the loop finished without finding any differences, compare the lengths
        return initial_array.length - compare_array.length;
    }

    /** This method determines whether or not a given array contains a given int.
     * 
     * @param ints
     *            is the array of ints that will be searched.
     * @param target
     *            is the int that <b><tt>ints</b></tt> may contain.
     * @return <b>true</b> if <b><tt>object</b></tt> contains <b><tt>target</b></tt>; <b>false</b> otherwise. */
    public static boolean contains(int[] ints, int target) {
        for (int object : ints)
            if (object == target)
                return true;
        return false;
    }

    /** This method determines whether or not a given array contains a given short.
     * 
     * @param shorts
     *            is the array of shorts that will be searched.
     * @param target
     *            is the short that <b><tt>shorts</b></tt> may contain.
     * @return <b>true</b> if <b><tt>object</b></tt> contains <b><tt>target</b></tt>; <b>false</b> otherwise. */
    public static boolean contains(short[] shorts, short target) {
        for (short object : shorts)
            if (object == target)
                return true;
        return false;
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

    @SuppressWarnings("unchecked")
    public static <T> T[] subArray(T[] objects, int start, int end) {
        try {
            Object[] new_objects = new Object[end - start];
            for (int i = 0; i < new_objects.length; i++)
                new_objects[i] = objects[i + start];
            return (T[]) new_objects;
        } catch (ArrayIndexOutOfBoundsException exception) {
            err(mCL, "Someone gave subArray() bad indices!", exception, objects, start, end);
            return null;
        }
    }

    public static <T> T[] subArray(T[] objects, int start) {
        return subArray(objects, start, objects.length);
    }

    /** This method returns a grammatically correct list that contains all of the items given in a String array.
     * 
     * @param objects
     *            is the String array which will be written into a list.
     * @param separator
     *            is the String that will be used to separate terms in the list; by default, the separator is ", "
     * @param final_conjunction
     *            is the String that will be added between the second to last and last terms in a list of two or more items; by default, the final conjunction is "and"
     * @param prefix
     *            is the String that will be added to the beginning of each item in the list; by default, the prefix is an empty String ("")
     * @param suffix
     *            is the String that will be added to the end of each item in the list; by default, the suffix is an empty String ("")
     * @return a grammatically correct list of the objects in <b><tt>objects</b></tt>. */
    public static String writeArray(Object[] objects, String separator, String final_conjunction, String prefix, String suffix) {
        // establish default for all the arguments
        if (separator == null)
            separator = ", ";
        if (prefix == null)
            prefix = "";
        if (suffix == null)
            suffix = "";
        if (final_conjunction == null)
            final_conjunction = "and";
        else
            final_conjunction = final_conjunction.trim();

        if (objects.length == 0)
            return "";
        else if (objects.length == 1)
            return prefix + objects[0].toString() + suffix;
        else if (objects.length == 2)
            return objects[0] + " " + final_conjunction + " " + objects[1];
        else {
            String list = "";
            for (int i = 0; i < objects.length; i++) {
                list += prefix + objects[i] + suffix;
                if (i < objects.length - 1) {
                    list += separator;
                    if (i == objects.length - 2)
                        list += final_conjunction + " ";
                }
            }
            return list;
        }
    }

    public static String writeArray(Object[] objects, String separator, String final_conjunction, String prefix) {
        return writeArray(objects, separator, final_conjunction, prefix, null);
    }

    public static String writeArray(Object[] objects, String separator, String final_conjunction) {
        return writeArray(objects, separator, final_conjunction, null, null);
    }

    public static String writeArray(Object[] objects, String separator) {
        return writeArray(objects, separator, null, null, null);
    }

    public static String writeArray(Object[] objects) {
        return writeArray(objects, null, null, null, null);
    }

    public static String writeArrayList(ArrayList<?> objects, String separator, String final_conjunction, String prefix, String suffix) {
        return writeArray(objects.toArray(), separator, final_conjunction, prefix, suffix);
    }

    public static String writeArrayList(ArrayList<?> objects, String separator, String final_conjunction, String prefix) {
        return writeArrayList(objects, separator, final_conjunction, prefix, null);
    }

    public static String writeArrayList(ArrayList<?> objects, String separator, String final_conjunction) {
        return writeArrayList(objects, separator, final_conjunction, null, null);
    }

    public static String writeArrayList(ArrayList<?> objects, String separator) {
        return writeArrayList(objects, separator, null, null, null);
    }

    public static String writeArrayList(ArrayList<?> objects) {
        return writeArrayList(objects, null, null, null, null);
    }
}
