package REALDrummer;

import java.util.ArrayList;
import java.util.Collection;

import static REALDrummer.ArrayUtilities.writeArrayList;

@SuppressWarnings("unchecked")
public class myList<T extends Comparable<T>> implements Comparable<myList<T>>, Cloneable {
    private T data;
    private byte number_of;
    private myList<T> left, right, root;

    public myList(T data, T... objects) {
        this.data = data;
        number_of = 1;
        left = null;
        right = null;
        root = null;

        add(objects);
    }

    private void balance() {

    }

    public int add(T object) {

    }

    public int[] add(T... objects) {
        int[] indices = new int[objects.length];
        for (int i = 0; i < objects.length; i++)
            indices[i] = add(objects[i]);
        return indices;
    }

    public int[] add(Collection<T> objects) {
        return add((T[]) objects.toArray());
    }

    public int[] add(myList<T> objects) {

    }

    public void clear() {

    }

    public boolean contains(T object) {

    }

    public boolean[] contains(T... objects) {

    }

    public boolean[] contains(Collection<T> objects) {
        return contains((T[]) objects.toArray());
    }

    public boolean[] contains(myList<T> objects) {

    }

    public boolean containsAND(T... objects) {

    }

    public boolean containsAND(Collection<T> objects) {
        return containsAND((T[]) objects.toArray());
    }

    public boolean containsAND(myList<T> objects) {

    }

    public boolean containsOR(T... objects) {

    }

    public boolean containsOR(Collection<T> objects) {
        return containsOR((T[]) objects.toArray());
    }

    public boolean containsOR(myList<T> objects) {

    }

    public void delete() {
        free();
    }

    public int find(T object) {

    }

    public int[] find(T... objects) {

    }

    public int[] find(Collection<T> objects) {
        return find((T[]) objects.toArray());
    }

    public int[] find(myList<T> objects) {

    }

    public void free() {
        // free the left and the right of this list first
        if (left != null)
            left.free();
        if (right != null)
            right.free();

        // also remove the root's pointer that points to this node
        if (root != null)
            if (compareTo(root) < 0)
                root.left = null;
            else
                root.right = null;

        // finally, set everything to null/0 and leave the garbage collector to finish up
        data = null;
        number_of = 0;
        left = null;
        right = null;
        root = null;
    }

    public T get(int index) {

    }

    public T[] get(int... indices) {

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

    }

    public myList<T> intersect(Collection<T> list) {
        return intersect((T[]) list.toArray());
    }

    public myList<T> intersect(myList<T> list) {

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

    public myListIterator<T> iterator() {

    }

    public int lastIndexOf(T object) {

    }

    public int[] lastIndicesOf(T... objects) {

    }

    public int[] lastIndicesOf(Collection<T> objects) {
        return lastIndicesOf((T[]) objects.toArray());
    }

    public int[] lastIndicesOf(myList<T> objects) {

    }

    public myList<T> left() {
        return left;
    }

    public int length() {
        return size();
    }

    public myList<T> next() {

    }

    public myList<T> previous() {

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
        list.number_of = number_of;
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

        // TODO
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

        // find the first SEQUENTIAL node, which will be at the far far left
        while (list.hasLeft())
            list = list.left;

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
