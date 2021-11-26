package net.zxjava;

/*
自己定义链表元素
*/
public class Node<T> {
	private T value;
	private Node next;

	public Node() {

	}

	public Node(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public Node getNext() {
		return next;
	}

	public void setNext(Node next) {
		this.next = next;
	}

}