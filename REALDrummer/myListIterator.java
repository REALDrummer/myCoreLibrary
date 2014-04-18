package REALDrummer;

import java.util.Iterator;

public class myListIterator<T extends Comparable<? super T>> implements Iterator<T> {
    myList<T> list;

    public myListIterator(myList<T> list) {
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return list.hasNext();
    }

    @Override
    public T next() {
        myList<T> next = list.next();
        if (next == null)
            return null;
        else
            return next.data();
    }

    @Override
    public void remove() {
        list.remove();
    }

}
