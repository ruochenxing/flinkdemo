package net.zxjava.customize;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MySqlDataSource {

	public static void main(String[] args) {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.addSource(new SourceFromMySQL()).print();
		try {
			env.execute("Flink add data sourc");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
