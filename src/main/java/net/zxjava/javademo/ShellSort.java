package net.zxjava.javademo;

import java.util.Arrays;

/**
 * 把较大的数据集合分割成若干个小组（逻辑上分组），然后对每一个小组分别进行插入排序，此时，插入排序所作用的数据量比较小（每一个小组），插入的效率比较高
 * 
 * https://blog.csdn.net/qq_39207948/article/details/80006224
 */
public class ShellSort {

	public static void main(String[] args) {
		int[] a = { 49, 38, 65, 97, 76, 13, 27, 49, 78, 34, 12, 64, 1 };
		System.out.println("排序之前：");
		System.out.println(Arrays.toString(a));
		// 希尔排序
		int d = a.length;
		while (true) {
			d = d / 2;
			for (int x = 0; x < d; x++) {
				for (int i = x + d; i < a.length; i = i + d) {
					int temp = a[i];
					int j;
					for (j = i - d; j >= 0 && a[j] > temp; j = j - d) {
						a[j + d] = a[j];
						System.out.println("\t" + Arrays.toString(a));
					}
					a[j + d] = temp;
					System.out.println("\t" + Arrays.toString(a));
				}
				System.out.println("\t\t" + Arrays.toString(a));
			}
			if (d == 1) {
				break;
			}
		}
		System.out.println();
		System.out.println("排序之后：");
		System.out.println(Arrays.toString(a));
	}
}