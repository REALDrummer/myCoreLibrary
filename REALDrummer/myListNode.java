package REALDrummer;

public class myListNode<T extends Comparable<T>> {
    T data;
    byte number = 1;
    public myListNode<T> root, left, right;

    public myListNode(T data) {

    }

    public myListNode(T data, myListNode<T> left) {

    }

    public myListNode(T data, myListNode<T> left, myListNode<T> right) {

    }

    @Override
    public boolean equals(Object object) {
        return object instanceof myListNode<?> && ((myListNode<?>) object).data.equals(data);
    }

    @Override
    public String toString() {
        return data.toString() + "(" + number + ")";
    }
}
