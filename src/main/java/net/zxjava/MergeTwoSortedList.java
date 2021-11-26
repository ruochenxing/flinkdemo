package net.zxjava;

public class MergeTwoSortedList {

	// 合并两个有序链表，输入两个链表的头结点，返回合并后的头结点
	public static Node mergeTwoSortedList(Node<Integer> head1, Node<Integer> head2) {
		// 两个链表为空，返回null
		if (head1 == null && head2 == null) {
			return null;
		}
		// 链表1为空，直接返回链表2的头节点head2
		if (head1 == null) {
			return head2;
		}
		// 链表2为空，直接返回链表2的头节点head1
		if (head2 == null) {
			return head1;
		}
		// 定义一个newHead指向合并后的头节点，current指向当前节点
		Node<Integer> newHead = new Node<>();
		Node<Integer> current = new Node<>();

		// 定义两个移动定位节点
		Node<Integer> flag1 = head1.getNext();
		Node<Integer> flag2 = head2;

		// 以链表1为标准来比较，所以需要判断头结点head1是否小于head2，如果不是，需要调整newHead和current
		if (head2.getValue().intValue() < head1.getValue().intValue()) {
			current = head2;
			flag2 = flag2.getNext();
			current.setNext(head1);
			current = head1;
			newHead = head2;
		}
		// head1小于head2，不需要做调整
		else {
			newHead = head1;
			current = head1;
		}

		// 沿着链表1开始比较
		while (flag1 != null && flag2 != null) {
			Node<Integer> next = current.getNext();
			// 当前节点在链表1上
			if (next == flag1) {
				if (next.getValue().intValue() < flag2.getValue().intValue()) {
					current = current.getNext();
					flag1 = flag1.getNext();
				} else {
					current.setNext(flag2);
					current = flag2;
					flag2 = flag2.getNext();
				}
			}
			// 当前节点在链表2上
			else if (next == flag2) {
				if (next.getValue().intValue() < flag1.getValue().intValue()) {
					current = current.getNext();
					flag2 = flag2.getNext();
				} else {
					current.setNext(flag1);
					current = flag1;
					// 如果当前节点到达链表1的尾结点跳出循环
					if (flag1.getNext() == null) {
						break;
					}
					flag1 = flag1.getNext();
				}

			}
		}
		// 当前节点到达链表1尾结点
		if (flag1.getNext() == null) {
			current.setNext(flag2);
		}
		// 链表2只有一个节点
		if (flag2 == null) {
			current.setNext(flag1);
		}
		return newHead;
	}

	public static void main(String[] args) {
		NodeUtils nodeUtils = new NodeUtils();
		Integer[] intList1 = { 2, 5 };
//        Integer[] intList2 = {1, 3, 3, 4, 6};
		Integer[] intList2 = {};
		Node<Integer> head1 = nodeUtils.prepareNodeList(intList1);
		Node<Integer> head2 = nodeUtils.prepareNodeList(intList2);
		System.out.println("list1 before merge: " + NodeUtils.listAllNodes(head1));
		System.out.println("list2 before merge: " + NodeUtils.listAllNodes(head2));
		Node<Integer> mergeHead = mergeTwoSortedList(head1, head2);
		System.out.println("list after merge list1 and list2: " + NodeUtils.listAllNodes(mergeHead));
	}

	static class NodeUtils<T> {

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

}

