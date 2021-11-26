package net.zxjava.javademo;

import java.util.Arrays;

/**
 * 冒泡排序
 * 
 * 自上而下对相邻的两个数依次进行比较和调整，让较大的数往下沉，较小的往上冒
 */
public class BubbleSort {
	public static void main(String[] args) {
		int[] a = { 49, 38, 65, 97, 76, 13, 27, 49, 78, 34, 12, 64, 1, 8 };
		System.out.println("排序之前：");
		System.out.println(Arrays.toString(a));
		// 冒泡排序
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a.length - i - 1; j++) {// 最后i个数已经是最大的了，所以不用判断
				// 这里-i主要是每遍历一次都把最大的i个数沉到最底下去了，没有必要再替换了
				if (a[j] > a[j + 1]) {
					int temp = a[j];
					a[j] = a[j + 1];
					a[j + 1] = temp;
				}
			}
		}
		System.out.println("排序之后：");
		System.out.println(Arrays.toString(a));
	}
}