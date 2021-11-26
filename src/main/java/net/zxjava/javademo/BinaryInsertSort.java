package net.zxjava.javademo;

import java.util.Arrays;

/**
 * 二分法插入排序的思想和直接插入一样，只是找合适的插入位置的方式不同，这里是按二分法找到合适的位置，可以减少比较的次数。
 * */
public class BinaryInsertSort {

	public static void main(String[] args) {
		int[] a = { 49, 38, 65, 97, 176, 213, 227, 49, 78, 34, 12, 164, 11, 18, 1 };
		System.out.println("排序之前：");
		System.out.println(Arrays.toString(a));
		// 二分插入排序
		sort(a);
		System.out.println();
		System.out.println("排序之后：");
		System.out.println(Arrays.toString(a));
	}

	private static void sort(int[] a) {
		for (int i = 0; i < a.length; i++) {
			System.out.println("i=" + i);
			int temp = a[i];
			int left = 0;
			int right = i - 1;
			int mid = 0;
			while (left <= right) {
				mid = (left + right) / 2;
				if (temp < a[mid]) {
					right = mid - 1;
				} else {
					left = mid + 1;
				}
			}
			for (int j = i - 1; j >= left; j--) {
				a[j + 1] = a[j];
				System.out.println("\t" + Arrays.toString(a));
			}
			if (left != i) {
				a[left] = temp;
			}
			System.out.println("\t\t" + Arrays.toString(a));
		}
	}

}