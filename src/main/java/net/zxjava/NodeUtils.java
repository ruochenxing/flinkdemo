package net.zxjava;

public class NodeUtils<T> {

	public Node prepareNodeList(T[] list) {
		if (list.length == 0) {
			return null;
		}
		// 初始化头结点
		Node headNode = new Node(list[0]);
		Node current = headNode;
		for (int i = 1; i < list.length; i++) {
			Node tempNode = new Node(list[i]);
			current.setNext(tempNode);
			current = tempNode;
		}
		return headNode;
	}

	// 建议调用之前检查是否存在环，避免无限死循环
	public static String listAllNodes(Node headNode) {
		StringBuffer sb = new StringBuffer("");
		// 单链表尾为空，直接返回空字符串
		if (headNode == null) {
			return sb.toString();
		}
		// 链表头节点
		sb.append(headNode.getValue().toString()).append(",");
		while (headNode.getNext() != null) {
			sb.append(headNode.getNext().getValue().toString()).append(",");
			headNode = headNode.getNext();
		}
		return sb.substring(0, sb.length() - 1);
	}
}
