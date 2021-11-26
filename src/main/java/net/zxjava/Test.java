package net.zxjava;

public class Test {

	public static void main(String[] args) {
		String fileName = "test.txt";
		String filePre = fileName.split("\\.")[0];
		String fileExt = fileName.split("\\.")[1];
		System.out.println(filePre);
		System.out.println(fileExt);
		fileName = filePre + "_" + 1 + "." + fileExt;
		System.out.println(fileName);
	}
}
