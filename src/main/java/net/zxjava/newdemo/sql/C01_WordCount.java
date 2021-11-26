package net.zxjava.newdemo.sql;

public class C01_WordCount {
	public String word;
	public Long counts;

	public C01_WordCount() {
	}

	public C01_WordCount(String word, Long counts) {
		this.word = word;
		this.counts = counts;
	}

	@Override
	public String toString() {
		return "WordCount{" + "word='" + word + '\'' + ", counts='" + counts + '\'' + '}';
	}

}
