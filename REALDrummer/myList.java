package REALDrummer;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class myList<T extends Comparable<T>> implements Comparable<myList<T>> {
    public int length = 0;
    public myListNode<T> root = null;

    public myList(T... objects) {
        add(objects);
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

    public void clear() {

    }

    public boolean contains(T object) {

    }

    public boolean[] contains(T... objects) {

    }

    public boolean[] contains(Collection<T> objects) {

    }

    public boolean[] contains(myList<T> objects) {

    }

    public boolean containsAND(T... objects) {

    }

    public boolean containsAND(Collection<T> objects) {

    }

    public boolean containsAND(myList<T> objects) {

    }

    public boolean containsOR(T... objects) {

    }

    public boolean containsOR(Collection<T> objects) {

    }

    public boolean containsOR(myList<T> objects) {

    }

    public int find(T object) {

    }

    public int[] find(T... objects) {

    }

    public int[] find(Collection<T> objects) {

    }

    public int[] find(myList<T> objects) {

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

    public int has(int index) {

    }

    public myList<T> intersect(T... list) {

    }

    public myList<T> intersect(Collection<T> list) {

    }

    public myList<T> intersect(myList<T> list) {

    }

    public boolean isEmpty() {
        return length == 0;
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

    public void retainAll(T... objects) {

    }

    public void retainAll(Collection<T> objects) {

    }

    public void retainAll(myList<T> objects) {

    }

    public int search(T object) {
        return find(object);
    }

    public T[] toArray() {
        return (T[]) toArrayList().toArray();
    }

    public ArrayList<T> toArrayList() {

    }

    public myList<T> union(T... list) {

    }

    public myList<T> union(Collection<T> list) {

    }

    public myList<T> union(myList<T> list) {

    }

    @Override
    public int compareTo(myList<T> list) {
        if (isEmpty() && list.isEmpty())
            return 0;
        else if (isEmpty())
            return -1;
        else if (list.isEmpty())
            return 1;
        else
            return get(0).compareTo(list.get(0));
    }

    @Override
    public boolean equals(Object object) {

    }

    @Override
    public String toString() {

    }
}
