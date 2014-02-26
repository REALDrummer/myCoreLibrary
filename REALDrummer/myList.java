package REALDrummer;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class myList<T extends Comparable<T>> implements Comparable<myList<T>>, Cloneable {
    private int length = 0;
    private myListNode<T> root = null;

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

    private void balance() {
        
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

    public int find(T object) {

    }

    public int[] find(T... objects) {

    }

    public int[] find(Collection<T> objects) {
        return find((T[]) objects.toArray());
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

    }

    public myList<T> intersect(myList<T> list) {

    }

    public boolean isEmpty() {
        return length == 0;
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

    public int length() {
        return length;
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

    public void retainAll(T... objects) {

    }

    public void retainAll(Collection<T> objects) {

    }

    public void retainAll(myList<T> objects) {

    }

    public int size() {
        return length;
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

    }

    public myList<T>[] split(myList<T> objects) {

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
    public myList<T> clone() {

    }

    @Override
    public int compareTo(myList<T> list) {
        if (isEmpty() && list.isEmpty())
            return 0;
        else if (isEmpty())
            return -1;
        else if (list.isEmpty())
            return 1;
        
        
    }

    @Override
    public boolean equals(Object object) {

    }

    @Override
    public String toString() {
        
    }
}
