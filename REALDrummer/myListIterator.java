package REALDrummer;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** This {@link Iterator} is able to iterate through {@link myList}s one element at a time from beginning to end. This class not only allows the use of {@link myLists}s in Java
 * for-each loops (e.g. <tt>for (Object object : objects)</tt>), but also provides a method of iterating through {@link myList}s much more efficient than retrieval by index;
 * because {@link myList}s are binary tree structures, every retrieval by index is <tt>Θ(lg(n))</tt>, but iterators allow searching from the last found node, which results in
 * <tt>Θ(1)</tt> time for iteration.
 * 
 * @author connor
 *
 * @param <T>
 *            is the type of element contained in the {@link myList} that this {@link myListIterator} is associated with. */
public class myListIterator<T> implements Iterator<T>, Cloneable {
    private myList<T> next;

    public myListIterator(myList<T> list) {
        if (list.isEmpty())
            next = null;
        else
            next = list.lowestValuedNode();
    }

    public myListIterator(myList<T> list, boolean start_from_lowest) {
        if (list == null || list.isEmpty())
            next = null;
        else if (start_from_lowest)
            next = list.lowestValuedNode();
        else
            next = list;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public T next() {
        if (next == null)
            throw new NoSuchElementException();

        T to_return = next.data();
        next = next.next();
        return to_return;
    }

    @Override
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public myListIterator<T> clone() {
        return new myListIterator<T>(next, false);
    }
}
