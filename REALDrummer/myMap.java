package REALDrummer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;

import REALDrummer.utils.ArrayUtilities;

import static REALDrummer.utils.ArrayUtilities.*;
import static REALDrummer.utils.MessageUtilities.*;

@SuppressWarnings("unchecked")
public class myMap<K extends Comparable<? super K>, V> implements Comparable<myMap<K, V>>, Cloneable, Map<K, V>, Iterable<K> {
    private K key = null;
    private V value = null;
    private myMap<K, V> left = null, right = null, root = null;

    // TODO: change all "add"s to "put"s
    public void oldStuff() {
        // public int find(T object) {
        // return find(object, hasLeft() ? left.length() : 0);
        // }
        //
        // public int[] find(T... objects) {
        // int[] indices = new int[objects.length];
        // for (int i = 0; i < objects.length; i++)
        // indices[i] = find(objects[i]);
        // return indices;
        // }
        //
        // public int[] find(Collection<T> objects) {
        // return find((T[]) objects.toArray());
        // }
        //
        // public int[] find(myList<T> objects) {
        // return find(objects.toArray());
        // }

        // public int add(T object) {
        // if (object == null)
        // throw new IllegalArgumentException();
        //
        // if (isEmpty()) {
        // data = object;
        // return 0;
        // } else
        // return add(object, hasLeft() ? left.length() : 0);
        // }
        //
        // public int[] add(T... objects) {
        // if (objects.length == 0)
        // return new int[0];
        //
        // int[] indices = new int[objects.length];
        // for (int i = 0; i < objects.length; i++) {
        // indices[i] = add(objects[i]);
        // /* add 1 to all indices greater than index already in the indices list to account for the fact that adding an element to the list shifts the indices of every
        // * element after it in the list */
        // for (int j = 0; j < i; j++)
        // if (indices[j] >= indices[i])
        // indices[j]++;
        // }
        //
        // return indices;
        // }
        //
        // public int[] add(Collection<T> objects) {
        // return add((T[]) objects.toArray());
        // }
        //
        // public int[] add(myList<T> objects) {
        // return add(objects.toArray());
        // }

    }

    // constructors
    public myMap(Object... objects) {
        for (int i = 0; i < objects.length; i += 2)
            try {
                put((K) objects[i], (V) objects[i + 1]);
            } catch (ClassCastException exception) {
                err(myCoreLibrary.mCL, "Someone gave me stuff to put into this myMap that didn't match the proper keys and/or values!", exception, "key: " + objects[i],
                        "value: " + objects[i + 1], "index of arguments=" + i);
            } catch (ArrayIndexOutOfBoundsException exception) {
                err(myCoreLibrary.mCL, "Someone gave me an odd number of things to put into a map like they expect me to put in a key without a value!", exception,
                        "objects: " + writeArray(objects));
            }
    }

    public myMap(Collection<Object> objects) {
        new myMap(objects.toArray());
    }

    public myMap(myMap<K, V> objects) {
        put(objects);
    }

    // private recursive methods
    private int put(K key, V value, int current_index) {
        // see whether the element should be added to the left or right side or replaced at the current index
        if (this.key.compareTo(key) == 0) {
            // if the key is exactly the same, just change the value and end the process
            this.value = value;
            return current_index;
        } else if (this.key.compareTo(key) < 0)
            // the right side is the correct side
            if (!hasRight() || !right.isFull() || hasLeft() && left.isFull() && right.length() <= left.length())
                return putRight(key, value, current_index);
            else {
                // knock-down data-shuffle from the right to the left side
                putLeft(this.key, this.value, current_index);
                current_index++; // add 1 to the current index since we added an element to the left side of the list
                myMap<K, V> lowest_right = right.lowestValuedNode();
                if (lowest_right.key.compareTo(key) < 0) {
                    // if the lowest_right's data is lower than the given key, use lowest_right for the knock-down
                    this.key = lowest_right.key;
                    this.value = lowest_right.value;
                    lowest_right.remove();
                    return putRight(key, value, current_index);
                } else {
                    // if the object is lower than lowest_right's data, use the given key and value for the knock-down
                    this.key = key;
                    this.value = value;
                    return current_index;
                }
            }
        else {
            // the left side is the correct side
            if (!hasLeft() || !left.isFull() || hasRight() && right.isFull() && left.length() <= right.length())
                return putLeft(key, value, current_index);
            else {
                // knock-down data-shuffle from the left to the right side
                putRight(this.key, this.value, current_index);
                myMap<K, V> highest_left = left.highestValuedNode();
                if (highest_left.key.compareTo(key) > 0) {
                    // if the highest_left's data is higher than the given key, use highest_left for the knock-down
                    this.key = highest_left.key;
                    this.value = highest_left.value;
                    highest_left.remove();
                } else {
                    // if the object is higher than highest_right's data, use the given key and value for the knock-down
                    this.key = key;
                    this.value = value;
                    return current_index;
                }

                return putLeft(key, value, current_index);
            }
        }
    }

    private int putLeft(K key, V value, int current_index) {
        if (hasLeft())
            return left.put(key, value, current_index - (left.hasRight() ? left.right.length() : 0) - 1);
        else {
            left = new myMap<K, V>(key, value);
            left.root = this;
            return current_index;
        }
    }

    private int putRight(K key, V value, int current_index) {
        if (hasRight())
            return right.put(key, value, current_index + (right.hasLeft() ? right.left.length() : 0) + 1);
        else {
            right = new myMap<K, V>(key, value);
            right.root = this;
            return current_index + 1;
        }
    }

    private int indexOf(K key, int current_index) {
        if (this.key.equals(key)) {
            // find the lowest index value of that item
            int lower_index = -1;
            if (hasLeft())
                lower_index = left.indexOf(key, current_index - 1 - (left.hasRight() ? left.right.length() : 0));

            // return the lowest index if there was a lower index or this index otherwise (adjusted for the left)
            if (lower_index == -1)
                return current_index;
            else
                return lower_index;
        } else if (this.key.compareTo(key) < 0)
            if (hasRight())
                return right.indexOf(key, current_index + 1 + (right.hasLeft() ? right.left.length() : 0));
            else
                return -1;
        else if (hasLeft())
            return left.indexOf(key, current_index - 1 - (left.hasRight() ? left.right.length() : 0));
        else
            return -1;
    }

    private K getKey(int index, int current_index) {
        int left_length = hasLeft() ? left.length() : 0;
        if (left_length + current_index > index)
            return left.getKey(index, current_index);
        else if (left_length + current_index == index)
            return this.key;
        else if (!hasRight())
            return null;
        else
            return right.getKey(index, current_index + left_length + 1);
    }

    private V getValue(int index, int current_index) {
        int left_length = hasLeft() ? left.length() : 0;
        if (left_length + current_index > index)
            return left.getValue(index, current_index);
        else if (left_length + current_index == index)
            return this.value;
        else if (!hasRight())
            return null;
        else
            return right.getValue(index, current_index + left_length + 1);
    }

    private myMap<K, V> getNode(int index, int current_index) {
        int left_length = hasLeft() ? left.length() : 0;
        if (left_length + current_index > index)
            return left.getNode(index, current_index);
        else if (left_length + current_index == index)
            return this;
        else if (!hasRight())
            return null;
        else
            return right.getNode(index, current_index + left_length + 1);
    }

    private myMap<K, V> submap(int begin_index, int end_index, int current_index, myMap<K, V> submap) {
        // first, search through the tree until we find the first element whose index is within the bounds; this element will be the root of the sublist
        myMap<K, V> clone = this;
        while (begin_index > current_index || end_index < current_index)
            if (begin_index > current_index && right != null) {
                clone = clone.right;
                current_index += 1 + (clone.hasLeft() ? left.length() : 0);
            } else if (end_index < current_index && left != null) {
                clone = clone.left;
                current_index -= 1 + (clone.hasRight() ? right.length() : 0);
            } else
                return null;

        // once we have found the root of the sublist, add that element to the new list and check the left and right for additional elements
        if (hasLeft()) {
            myMap<K, V> left_submap = submap(begin_index, end_index, current_index - (left.hasRight() ? left.right.length() : 0) - 1, left);
            if (left_submap != null)
                clone.put(left_submap);
        }
        if (hasRight()) {
            myMap<K, V> right_submap = submap(begin_index, end_index, current_index + (right.hasLeft() ? right.left.length() : 0) + 1, right);
            if (right_submap != null)
                clone.put(right_submap);
        }

        return clone;
    }

    // public methods
    public void clear() {
        free();
    }

    public boolean contains(T object) {
        return find(object) != -1;
    }

    public boolean[] contains(T... objects) {
        boolean[] results = new boolean[objects.length];

        for (int i = 0; i < objects.length; i++)
            results[i] = contains(objects[i]);

        return results;
    }

    public boolean[] contains(Collection<T> objects) {
        boolean[] results = new boolean[objects.size()];

        int i = 0;
        for (T object : objects) {
            results[i] = contains(object);
            i++;
        }

        return results;
    }

    public boolean[] contains(myList<T> objects) {
        boolean[] results = new boolean[objects.size()];

        int i = 0;
        for (T object : objects) {
            results[i] = contains(object);
            i++;
        }

        return results;
    }

    public boolean containsAND(T... objects) {
        for (T object : objects)
            if (!contains(object))
                return false;
        return true;
    }

    public boolean containsAND(Collection<T> objects) {
        for (T object : objects)
            if (!contains(object))
                return false;
        return true;
    }

    public boolean containsAND(myList<T> objects) {
        for (T object : objects)
            if (!contains(object))
                return false;
        return true;
    }

    public boolean containsOR(T... objects) {
        for (T object : objects)
            if (contains(object))
                return true;
        return false;
    }

    public boolean containsOR(Collection<T> objects) {
        for (T object : objects)
            if (contains(object))
                return true;
        return false;
    }

    public boolean containsOR(myList<T> objects) {
        for (T object : objects)
            if (contains(object))
                return true;
        return false;
    }

    public T data() {
        return data;
    }

    public void delete() {
        free();
    }

    public void debug() {
        myCoreLibrary.mCL.debug(String.valueOf(length()) + (hasLeft() ? "; " + left.length() + "l" : "") + (hasRight() ? "; " + right.length() + "r" : "")
                + (hasRoot() ? "; has root!" : "") + "\n" + toString());
    }

    public myList<T> findNode(T object) {
        if (data.equals(object)) {
            // find the lowest index value of that item
            myList<T> lower_indexed_node = null;
            if (hasLeft())
                lower_indexed_node = left.findNode(object);

            // return the lowest indexed node if there was one with a lower index or this node otherwise
            if (lower_indexed_node == null)
                return this;
            else
                return lower_indexed_node;
        } else if (compareTo(object) <= 0)
            if (hasRight())
                return right.findNode(object);
            else
                return null;
        else if (hasLeft())
            return left.findNode(object);
        else
            return null;
    }

    public myList<T>[] findNodes(T... objects) {
        myList<T>[] nodes = new myList[objects.length];
        for (int i = 0; i < objects.length; i++)
            nodes[i] = findNode(objects[i]);
        return nodes;
    }

    public myList<T>[] findNodes(Collection<T> objects) {
        return findNodes((T[]) objects.toArray());
    }

    public myList<T>[] findNodes(myList<T> objects) {
        return findNodes(objects.toArray());
    }

    public void free() {
        // free the left and the right of this list first
        if (hasLeft())
            left.free();
        if (hasRight())
            right.free();

        // also remove the root's pointer that points to this node
        if (hasRoot())
            if (isLeft())
                root.left = null;
            else
                root.right = null;

        // finally, set everything to null and leave the garbage collector to finish up
        data = null;
        left = null;
        right = null;
        root = null;
    }

    public T getKey(int index) {
        return get(index, 0);
    }

    public T[] getKeys(int... indices) {
        Object[] results = new Object[indices.length];
        for (int i = 0; i < indices.length; i++)
            results[i] = get(indices[i]);
        return (T[]) results;
    }

    public int getKey(T object) {
        return find(object);
    }

    public int[] getKeys(T... objects) {
        return find(objects);
    }

    public int[] getKeys(Collection<T> objects) {
        return find(objects);
    }

    public int[] getKeys(myList<T> objects) {
        return find(objects);
    }

    public T getValue(int index) {
        return get(index, 0);
    }

    public T[] getValues(int... indices) {
        Object[] results = new Object[indices.length];
        for (int i = 0; i < indices.length; i++)
            results[i] = get(indices[i]);
        return (T[]) results;
    }

    public int getValue(T object) {
        return find(object);
    }

    public int[] getValues(T... objects) {
        return find(objects);
    }

    public int[] getValues(Collection<T> objects) {
        return find(objects);
    }

    public int[] getValues(myList<T> objects) {
        return find(objects);
    }

    public myList<T> getNode(int index) {
        return getNode(index, 0);
    }

    public myList<T>[] getNodes(int... indices) {
        myList<T>[] results = new myList[indices.length];
        for (int i = 0; i < indices.length; i++)
            results[i] = getNode(indices[i]);
        return (myList<T>[]) results;
    }

    public myList<T> getNode(T object) {
        return findNode(object);
    }

    public myList<T>[] getNodes(T... objects) {
        return findNodes(objects);
    }

    public myList<T>[] getNodes(Collection<T> objects) {
        return findNodes(objects);
    }

    public myList<T>[] getNodes(myList<T> objects) {
        return findNodes(objects);
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasNext() {
        return next() != null;
    }

    public boolean hasPrevious() {
        return previous() != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    public boolean hasRoot() {
        return root != null;
    }

    public T highestValue() {
        if (isEmpty())
            return null;
        else
            return highestValuedNode().data;
    }

    public myList<T> highestValuedNode() {
        if (isEmpty())
            return null;

        myList<T> highest_node = this;
        while (highest_node.hasRight())
            highest_node = highest_node.right;

        return highest_node;
    }

    public int index() {
        int index = hasLeft() ? left.length() : 0;

        // seach up roots to the left until we can't any more and add the lengths of all the roots and their lefts
        myList<T> parsing = this;
        while (parsing.isRight()) {
            parsing = parsing.root;
            index += 1 + (parsing.hasLeft() ? parsing.left.length() : 0);
        }

        return index;
    }

    public int indexOf(T object) {
        return find(object);
    }

    public int[] indicesOf(T... objects) {
        return find(objects);
    }

    public int[] indicesOf(Collection<T> objects) {
        return find(objects);
    }

    public int[] indicesOf(myList<T> objects) {
        return find(objects);
    }

    public myList<T> intersect(T... list) {
        myList<T> intersect = new myList<T>();
        for (T object : list)
            if (contains(object))
                intersect.add(object);
        return intersect;
    }

    public myList<T> intersect(Collection<T> list) {
        return intersect((T[]) list.toArray());
    }

    public myList<T> intersect(myList<T> list) {
        return intersect(list.toArray());
    }

    public boolean isEmpty() {
        return data == null && left == null && right == null;
    }

    public boolean isFull() {
        return isLeaf() || hasLeft() && hasRight() && left.isFull() && right.isFull();
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public boolean isLeft() {
        return hasRoot() && root.hasLeft() && this == root.left;
    }

    public boolean isRight() {
        return hasRoot() && root.hasRight() && this == root.right;
    }

    public int length() {
        return size();
    }

    public int levels() {
        if (isEmpty())
            return 0;
        else if (isLeaf())
            return 1;

        // find the number of levels in the left and right lists
        int left_levels = hasLeft() ? left.levels() : 0, right_levels = hasRight() ? right.levels() : 0;

        // return the maximum levels between the left and right lists (+1 for this level)
        if (left_levels >= right_levels)
            return left_levels + 1;
        else
            return right_levels + 1;
    }

    public T lowestValue() {
        if (isEmpty())
            return null;
        else
            return lowestValuedNode().data;
    }

    public myList<T> lowestValuedNode() {
        if (isEmpty())
            return null;

        myList<T> lowest_node = this;
        while (lowest_node.hasLeft())
            lowest_node = lowest_node.left;

        return lowest_node;
    }

    public myList<T> next() {
        if (hasRight())
            return right.lowestValuedNode();
        // if it's the left and has no right, the next is simply the root
        else if (isLeft())
            return root;
        // if a leaf is the right, search up roots until a root is another root's left, then return the root of that root
        else if (isRight()) {
            myList<T> parsing = root;
            // first, go up the roots as long as the root is the right of another root
            while (parsing.isRight())
                parsing = parsing.root;

            // if the node was the last node in the whole list,
            return parsing.root;
        } else
            return null;
    }

    public myList<T> next(int amount) {
        myList<T> clone = this;
        for (int i = 0; i < amount; i++) {
            clone = clone.next();
            if (clone == null)
                return null;
        }
        return clone;
    }

    public myList<T> previous() {
        // if a leaf has no root, it's a one-item myList, so there is no previous
        if (isEmpty() || isLeaf() && !hasRoot())
            return null;
        else if (hasLeft())
            return left.highestValuedNode();
        // if it's the left and has no right, the next is simply the root
        else if (isRight())
            return root;
        // if a leaf is the right, search up roots until a root is another root's left, then return the root of that root
        else if (isLeft()) {
            myList<T> clone = this;
            // first, go up the roots as long as the root is the right of another root
            while (isLeft())
                clone = clone.root;
            // then, if the root is the root of the whole list (!hasRoot()), we know that there's nothing more to the right of this, so we've reached the end of the list
            if (!clone.hasRoot())
                return null;
            else
                return clone.root;
        }

        // if it gets this far, I'm not sure what happened, but send an error report
        tellOps(ChatColor.DARK_RED + "I'm not sure that I got this list's previous node correctly.\nThe node's data is " + (data == null ? "null" : data.toString()) + ".",
                true);
        if (hasRoot())
            if (isLeft())
                tellOps(ChatColor.DARK_RED + "It's " + (data == null ? "null" : data.toString()) + "'s left node.", true);
            else
                tellOps(ChatColor.DARK_RED + "It's " + (data == null ? "null" : data.toString()) + "'s right node.", true);
        else
            tellOps(ChatColor.DARK_RED + "It has no root.", true);
        if (hasLeft())
            tellOps(ChatColor.DARK_RED + "Its left is " + (data == null ? "null" : data.toString()) + ".", true);
        else
            tellOps(ChatColor.DARK_RED + "It has no left node.", true);
        if (hasRight())
            tellOps(ChatColor.DARK_RED + "Its right is " + (data == null ? "null" : data.toString()) + ".", true);
        else
            tellOps(ChatColor.DARK_RED + "It has no right node.", true);
        return null;
    }

    public myList<T> previous(int amount) {
        myList<T> clone = this;
        for (int i = 0; i < amount; i++) {
            clone = clone.previous();
            if (clone == null)
                return null;
        }
        return clone;
    }

    public void remove(int index) {
        // find the node specified by index
        myList<T> node = getNode(index);
        if (node == null)
            return;

        node.remove();
    }

    public void remove(int... indices) {
        for (int i = 0; i < indices.length; i++)
            remove(indices[i]);
    }

    public int remove(T object) {
        myList<T> node = findNode(object);
        if (node == null)
            return -1;

        int index = node.index();
        node.remove();
        return index;
    }

    public int[] remove(T... objects) {
        int[] results = new int[objects.length];
        for (int i = 0; i < objects.length; i++)
            results[i] = remove(objects[i]);
        return results;
    }

    public int[] remove(Collection<T> objects) {
        return remove((T[]) objects.toArray());
    }

    public int[] removeAll(T object) {
        int index = find(object);
        ArrayList<Integer> indices = new ArrayList<Integer>();

        while (index != -1) {
            remove(index);
            indices.add(index);
            index = find(object);
        }

        int[] results = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++)
            results[i] = indices.get(i);
        return results;
    }

    public int[][] removeAll(T... objects) {
        int[][] results = new int[objects.length][];
        for (int i = 0; i < objects.length; i++)
            results[i] = removeAll(objects[i]);
        return results;
    }

    public int[][] removeAll(Collection<T> objects) {
        return removeAll((T[]) objects.toArray());
    }

    public int[][] removeAll(myList<T> objects) {
        return removeAll(objects.toArray());
    }

    public int[] removeRepeats() {
        if (isEmpty())
            return new int[0];

        myList<T> parsing = lowestValuedNode(), next = parsing.next();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        while (next != null) {
            while (next.data.equals(parsing.data)) {
                int index = next.index();
                indices.add(index);
                remove(index);
                next = parsing.next();
            }
            parsing = next;
            next = parsing.next();
        }

        // convert the ArrayList<Integer> to an int[]
        int[] results = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++)
            results[i] = indices.get(i).intValue();

        return results;
    }

    public void retain(T... objects) {
        if (isEmpty())
            return;

        myList<T> parsing = lowestValuedNode();
        int index = 0;
        while (parsing != null) {
            while (!ArrayUtilities.contains(objects, parsing.data)) {
                if (index == 0) {
                    remove(0);
                    parsing = lowestValuedNode();
                } else {
                    parsing = parsing.next();
                    remove(index);
                }
                remove(index);
            }
            index++;
            parsing = parsing.next();
        }
    }

    public void retain(Collection<T> objects) {
        retain((T[]) objects.toArray());
    }

    public void retain(myList<T> objects) {
        // this method is implemented separately from retain(T...) because the myList contains() is more efficient than the array's contains()

        if (isEmpty())
            return;

        myList<T> parsing = lowestValuedNode();
        int index = 0;
        while (parsing != null) {
            while (!objects.contains(parsing.data)) {
                if (index == 0) {
                    remove(0);
                    parsing = lowestValuedNode();
                } else {
                    parsing = parsing.next();
                    remove(index);
                }
                remove(index);
            }
            index++;
            parsing = parsing.next();
        }
    }

    public int size() {
        if (isEmpty())
            return 0;

        return 1 + (left != null ? left.size() : 0) + (right != null ? right.size() : 0);
    }

    public int search(T object) {
        return find(object);
    }

    public myList<T>[] split(int... indices) {
        ArrayList<myList<T>> pieces = new ArrayList<myList<T>>();
        myList<T> parsing = lowestValuedNode(), piece = new myList<T>();
        int index = 0;
        while (parsing != null) {
            if (ArrayUtilities.contains(indices, index)) {
                pieces.add(piece);
                piece = new myList<T>();
            }
            piece.add(parsing.data);

            parsing = parsing.next();
            index++;
        }

        if (piece != null && piece.isEmpty())
            pieces.add(piece);

        return (myList<T>[]) pieces.toArray();
    }

    public myList<T>[] split(T... objects) {
        ArrayList<myList<T>> pieces = new ArrayList<myList<T>>();
        myList<T> parsing = lowestValuedNode(), piece = new myList<T>();
        while (parsing != null) {
            if (ArrayUtilities.contains(objects, parsing.data)) {
                pieces.add(piece);
                piece = new myList<T>();
            }
            piece.add(parsing.data);

            parsing = parsing.next();
        }

        if (piece != null && piece.isEmpty())
            pieces.add(piece);

        return (myList<T>[]) pieces.toArray();
    }

    public myList<T>[] split(Collection<T> objects) {
        return split((T[]) objects.toArray());
    }

    public myList<T>[] split(myList<T> objects) {
        return split(objects.toArray());
    }

    public myList<T> sublist(int begin_index) {
        return sublist(begin_index, length());
    }

    public myList<T> sublist(int begin_index, int end_index) {
        if (begin_index < 0 || end_index > length())
            throw new IndexOutOfBoundsException();
        return sublist(begin_index, end_index, hasLeft() ? left.length() : 0, new myList<T>());
    }

    public T[] toArray() {
        return (T[]) toArrayList().toArray();
    }

    public ArrayList<T> toArrayList() {
        // if the list is empty, return an empty ArrayList
        if (isEmpty())
            return new ArrayList<T>();

        // start at the first sequential node
        myList<T> list = lowestValuedNode();

        // add the elements of the list to a new ArrayList sequentially
        ArrayList<T> to_return = new ArrayList<T>();
        while (list != null) {
            to_return.add(list.data);
            list = list.next();
        }

        // return the completed ArrayList
        return to_return;
    }

    public myList<T> union(T... list) {
        // make a new myList
        myList<T> union = clone();
        for (T object : list)
            if (!union.contains(object))
                union.add(object);
        return union;
    }

    public myList<T> union(Collection<T> list) {
        return union((T[]) list.toArray());
    }

    public myList<T> union(myList<T> list) {
        return union(list.toArray());
    }

    // overrides
    @Override
    public myList<T> clone() {
        return new myList<T>(this);
    }

    @Override
    public int compareTo(myMap<K, V> list) {
        if (isEmpty() && list.isEmpty())
            return 0;
        else if (isEmpty())
            return -1;
        else if (list.isEmpty())
            return 1;

        /* keep track of the original sizes of the two lists in order to ensure that if either or both of these lists are sublists, it only compares that sublist, not the rest
         * of the list that the sublist is connected to */
        int this_length = length(), list_length = list.length();

        myList<T> this_parser = lowestValuedNode(), list_parser = list.lowestValuedNode();

        // parse through each list and compare each element until a list ends or there is a difference in the elements
        int comparison = this_parser.data.compareTo(list_parser.data);
        for (int i = 0; comparison != 0 && (this_length <= list_length && i < this_length || i < list_length); i++) {
            this_parser = this_parser.next();
            list_parser = list_parser.next();
            comparison = this_parser.data.compareTo(list_parser.data);
        }

        // if comparison still = 0, the loop must have terminated because one of the lists ran out of elements, so compare the lengths
        if (comparison == 0)
            return this_length - list_length;
        else
            return comparison;
    }

    public int compareTo(T object) {
        return data.compareTo(object);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof myList<?> && ((myList<T>) object).compareTo(this) == 0;
    }

    public void remove() {
        // if the node has a left that's equal to or longer than the right, shift the left list's highest node to the root and delete the old left list's highest node
        if (hasLeft() && (!hasRight() || right.length() <= left.length())) {
            myList<T> highest_left = left.highestValuedNode();
            data = highest_left.data;
            highest_left.remove();
        } // if the node has a right that's longer than the left, shift the right list's lowest node to the root and delete the old right list's lowest node
        else if (hasRight()) {
            myList<T> lowest_right = right.lowestValuedNode();
            data = lowest_right.data;
            lowest_right.remove();
        } // if the node is a leaf, just delete its contents and leave it at that
        else
            delete();
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "--------";

        // start at the first sequential node
        myList<T> list = lowestValuedNode();

        // add the elements of the list to a new ArrayList sequentially
        ArrayList<String> strings = new ArrayList<String>();
        while (list != null) {
            strings.add(list.data.toString());
            list = list.next();
        }

        // return the completed ArrayList formatted into a list
        return writeArrayList(strings);
    }

    @Override
    public Iterator<T> iterator() {
        return new myListIterator<T>(this);
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V get(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<K> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public V put(K key, V value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        // TODO Auto-generated method stub

    }

    @Override
    public V remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }

}
