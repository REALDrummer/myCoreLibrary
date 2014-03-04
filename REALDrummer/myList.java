package REALDrummer;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;

import static REALDrummer.ArrayUtilities.*;
import static REALDrummer.MessageUtilities.*;

@SuppressWarnings("unchecked")
public class myList<T extends Comparable<T>> implements Comparable<myList<T>>, Cloneable {
    private T data = null;
    private myList<T> left = null, right = null, root = null;

    // DONE: implemented compareTo(myList<T>)
    // DONE: implemented next() and previous()
    // DONE: added and implemented next(int) and previous(int)
    // DONE: implemented lastIndexOf() and lastIndicesOf() methods

    public myList(T... objects) {
        if (objects.length >= 1)
            data = objects[0];

        add(objects);
    }

    // private methods
    private int add(T object, int current_index) {
        if (compareTo(object) > 0)
            if (hasRight())
                return right.add(object, current_index + (left != null ? left.length() : 0) + 1);
            else {
                right = new myList<T>(object);
                right.root = this;
                balance();
                return current_index;
            }
        else if (hasLeft())
            return left.add(object, current_index);
        else {
            left = new myList<T>(object);
            left.root = this;
            balance();
            return current_index;
        }
    }

    private void balance() {
        // search roots of this list until we find one that isn't balanced
        if (isBalanced()) {
            // try balancing from the root; if root is null, no balancing can be done
            if (hasRoot())
                root.balance();
            return;
        }

        // if the right side is the "heavy" side, shift the root to the left side and make the lowest value on the right side the new root
        if (!hasLeft() || right.length() > left.length()) {
            // shift the root to the left side
            if (!hasLeft())
                left = new myList<T>(data);
            else
                left.add(data);

            // find the lowest value on the right side
            myList<T> lowest_right = right.lowestValuedNode();
            // make the lowest value on the right side the new root
            data = lowest_right.data;
            // delete the old lowest value on the right side that is now the new root
            lowest_right.delete();

            // rebalance the right sublist; the left sublist is already rebalanced when add() is called
            right.balance();
        } // if the left side is the "heavy" side, shift the root to the right side and make the highest value on the left side the new root
        else {
            // shift the root to the right side
            if (!hasRight())
                right = new myList<T>(data);
            else
                right.add(data);

            // find the highest value on the left side
            myList<T> highest_left = left.lowestValuedNode();
            // make the highest value on the left side the new root
            data = highest_left.data;
            // delete the old highest value on the left side that is now the root
            highest_left.delete();

            // rebalance the left sublist; the right sublist is already rebalanced when add() is called
            left.balance();
        }
    }

    private int find(T object, int current_index) {
        if (data.equals(object)) {
            // find the lowest index value of that item
            myList<T> clone = clone();
            while (clone.hasLeft() && left.data.equals(object))
                clone = clone.left;
            return current_index + (clone.hasLeft() ? clone.left.length() : 0);
        } else if (compareTo(object) > 0)
            if (hasRight())
                return right.find(object, current_index + (hasLeft() ? left.length() : 0) + 1);
            else
                return -1;
        else if (hasLeft())
            return left.find(object, current_index);
        else
            return -1;
    }

    private T get(int index, int current_index) {
        int left_length = hasLeft() ? left.length() : 0;
        if (left_length + current_index > index)
            return left.get(index, current_index);
        else if (left_length + current_index == index)
            return data;
        else if (!hasRight())
            return null;
        else
            return right.get(index, current_index + left_length + 1);
    }

    private int lastIndexOf(T object, int current_index) {
        if (data.equals(object)) {
            // find the highest index value of that item
            myList<T> clone = clone();
            while (clone.hasRight() && right.data.equals(object))
                clone = clone.right;
            return current_index + (clone.hasLeft() ? clone.left.length() : 0);
        } else if (compareTo(object) > 0)
            if (hasRight())
                return right.find(object, current_index + (hasLeft() ? left.length() : 0) + 1);
            else
                return -1;
        else if (hasLeft())
            return left.find(object, current_index);
        else
            return -1;
    }

    // public methods
    public int add(T object) {
        if (isEmpty()) {
            data = object;
            return 0;
        } else
            return add(object, 0);
    }

    public int[] add(T... objects) {
        int[] indices = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            int index = add(objects[i]);
            indices[i] = index;
            /* add 1 to all indices greater than index already in the indices list to account for the fact that adding an element to the list shifts the indices of every
             * element after it in the list */
            for (int j = 0; j < i; i++)
                if (indices[j] >= index)
                    indices[j]++;
        }

        return indices;
    }

    public int[] add(Collection<T> objects) {
        return add((T[]) objects.toArray());
    }

    public int[] add(myList<T> objects) {
        return add(objects.toArray());
    }

    public void clear() {
        if (hasLeft())
            left.free();
        if (hasRight())
            right.free();
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
        return contains((T[]) objects.toArray());
    }

    public boolean[] contains(myList<T> objects) {
        return contains(objects.toArray());
    }

    public boolean containsAND(T... objects) {
        for (T object : objects)
            if (!contains(object))
                return false;
        return true;
    }

    public boolean containsAND(Collection<T> objects) {
        return containsAND((T[]) objects.toArray());
    }

    public boolean containsAND(myList<T> objects) {
        return containsAND(objects.toArray());
    }

    public boolean containsOR(T... objects) {
        for (T object : objects)
            if (contains(object))
                return true;
        return false;
    }

    public boolean containsOR(Collection<T> objects) {
        return containsOR((T[]) objects.toArray());
    }

    public boolean containsOR(myList<T> objects) {
        return containsOR(objects.toArray());
    }

    public void delete() {
        free();
    }

    public int find(T object) {
        return find(object, 0);
    }

    public int[] find(T... objects) {
        int[] indices = new int[objects.length];
        for (int i = 0; i < objects.length; i++)
            indices[i] = find(objects[i]);
        return indices;
    }

    public int[] find(Collection<T> objects) {
        return find((T[]) objects.toArray());
    }

    public int[] find(myList<T> objects) {
        return find(objects.toArray());
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

    public T get(int index) {
        return get(index, 0);
    }

    public T[] get(int... indices) {
        Object[] results = new Object[indices.length];
        for (int i = 0; i < indices.length; i++)
            results[i] = get(indices[i]);
        return (T[]) results;
    }

    public int get(T object) {
        return find(object);
    }

    public int[] get(T... objects) {
        return find(objects);
    }

    public int[] get(Collection<T> objects) {
        return find(objects);
    }

    public int[] get(myList<T> objects) {
        return find(objects);
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

        myList<T> highest_node = clone();
        while (highest_node.hasRight())
            highest_node = highest_node.right;

        return highest_node;
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

    public boolean isBalanced() {
        /* if the left list has the same number of levels as the right list, it can't be balanced any more than it already is; if the length of the list is 2 or less, then it
         * must be balanced anyway */
        int left_levels = left == null ? 0 : left.levels(), right_levels = right == null ? 0 : right.levels();
        return length() <= 2 || left_levels == right_levels || isFull() && Math.abs(left_levels - right_levels) < 2;
    }

    public boolean isEmpty() {
        return data == null && left == null && right == null;
    }

    public boolean isFull() {
        return isLeaf() || (left != null && right != null && left.isFull() && right.isFull());
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public boolean isLeft() {
        return root != null && root.left.data.equals(data);
    }

    public boolean isRight() {
        return root != null && root.right.data.equals(data);
    }

    public int lastIndexOf(T object) {
        return lastIndexOf(object, 0);
    }

    public int[] lastIndicesOf(T... objects) {
        int[] results = new int[objects.length];
        for (int i = 0; i < objects.length; i++)
            results[i] = lastIndexOf(objects[i]);
        return results;
    }

    public int[] lastIndicesOf(Collection<T> objects) {
        return lastIndicesOf((T[]) objects.toArray());
    }

    public int[] lastIndicesOf(myList<T> objects) {
        return lastIndicesOf(objects.toArray());
    }

    public myList<T> left() {
        return left;
    }

    public int length() {
        return size();
    }

    public int levels() {
        if (isEmpty())
            return 0;

        /* here, length is the length of the list, levels is the number of levels, and elements is the maximum number of elements a list of <levels> levels */
        int length = length(), levels = 1, elements = 1;
        while (length > elements) {
            elements += Math.pow(2, levels);
            levels++;
        }

        return levels;
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

        myList<T> lowest_node = clone();
        while (lowest_node.hasLeft())
            lowest_node = lowest_node.left;

        return lowest_node;
    }

    public myList<T> next() {
        // if a leaf has no root, it's a one-item myList, so there is no next
        if (isEmpty() || isLeaf() && !hasRoot())
            return null;
        else if (hasRight())
            return right.lowestValuedNode();
        // if it's the left and has no right, the next is simply the root
        else if (isLeft())
            return root;
        // if a leaf is the right, search up roots until a root is another root's left, then return the root of that root
        else if (isRight()) {
            myList<T> clone = clone();
            // first, go up the roots as long as the root is the right of another root
            while (isRight())
                clone = clone.root;
            // then, if the root is the root of the whole list (!hasRoot()), we know that there's nothing more to the right of this, so we've reached the end of the list
            if (!clone.hasRoot())
                return null;
            else
                return clone.root;
        }

        // if it gets this far, I'm not sure what happened, but send an error report
        tellOps(ChatColor.DARK_RED + "I'm not sure that I got this list's next node correctly.\nThe node's data is " + data.toString() + ".", true);
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

    public myList<T> next(int amount) {
        myList<T> clone = clone();
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
            myList<T> clone = clone();
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
        myList<T> clone = clone();
        for (int i = 0; i < amount; i++) {
            clone = clone.previous();
            if (clone == null)
                return null;
        }
        return clone;
    }

    public myList<T> sublist(int begin_index) {

    }

    public myList<T> sublist(int begin_index, int end_index) {

    }

    public void remove(int index) {

    }

    public void remove(int... indices) {

    }

    public int remove(T object) {

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

    }

    public int[] removeRepeats() {

    }

    public int retain(T object) {

    }

    public int retain(T... objects) {

    }

    public int retain(Collection<T> objects) {
        return retain((T[]) objects.toArray());
    }

    public int retain(myList<T> objects) {

    }

    public myList<T> right() {
        return right;
    }

    public myList<T> root() {
        return root;
    }

    public int size() {
        return 1 + (left != null ? left.size() : 0) + (right != null ? right.size() : 0);
    }

    public int search(T object) {
        return find(object);
    }

    public myList<T>[] split(int... indices) {

    }

    public myList<T>[] split(T object) {

    }

    public myList<T>[] split(T... objects) {

    }

    public myList<T>[] split(Collection<T> objects) {
        return split((T[]) objects.toArray());
    }

    public myList<T>[] split(myList<T> objects) {

    }

    public T[] toArray() {
        return (T[]) toArrayList().toArray();
    }

    public ArrayList<T> toArrayList() {
        // if the list is empty, return an empty ArrayList
        if (isEmpty())
            return new ArrayList<T>();

        // start here
        myList<T> list = this;

        // find the first SEQUENTIAL node, which will be at the far far left
        while (list.hasLeft())
            list = list.left;

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

    }

    public myList<T> union(Collection<T> list) {
        return union((T[]) list.toArray());
    }

    public myList<T> union(myList<T> list) {

    }

    @Override
    public myList<T> clone() {
        myList<T> list = new myList<T>(data);
        list.left = left;
        list.right = right;
        list.root = root;
        return list;
    }

    @Override
    public int compareTo(myList<T> list) {
        if (isEmpty() && list.isEmpty())
            return 0;
        else if (isEmpty())
            return -1;
        else if (list.isEmpty())
            return 1;

        // make a clone of this list to parse through for comparative testing
        myList<T> this_clone = clone(), list_clone = list.clone();

        // remove the root from each list in order to ensure that if a list is a sublist of a larger list, then only this list will be compared, not the entire list
        this_clone.root = null;
        list_clone.root = null;

        // start at the lowest nodes of each list
        this_clone = this_clone.lowestValuedNode();
        list_clone = list_clone.lowestValuedNode();

        // parse through each list and compare each element until a list ends or there is a difference in the elements
        int comparison = this_clone.data.compareTo(list_clone.data);
        while (comparison == 0 && this_clone.hasNext() && list_clone.hasNext()) {
            this_clone = this_clone.next();
            list_clone = list_clone.next();
            comparison = this_clone.data.compareTo(list_clone.data);
        }

        // if comparison still = 0, the loop must have terminated becaose one of the lists ran out of elements; in that case, return 1 or -1
        if (comparison == 0)
            if (!this_clone.hasNext())
                return -1;
            else
                return 1;
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

    @Override
    public String toString() {
        // if the list is empty, return an empty String
        if (isEmpty())
            return "";

        // start here
        myList<T> list = this;

        // find the first SEQUENTIAL node
        list = list.lowestValuedNode();

        // add the elements of the list to a new ArrayList of toString()s sequentially
        ArrayList<String> to_return = new ArrayList<String>();
        while (list != null) {
            to_return.add(list.data.toString());
            list = list.next();
        }

        // return the completed ArrayList formatted into a list with items separated by line breaks
        return writeArrayList(to_return, "\n");
    }
}
