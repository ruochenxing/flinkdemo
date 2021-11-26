package net.zxjava.state;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class Main {

	public static void main(String[] args) {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);
		env.fromElements(Tuple2.of(1L, 3L), Tuple2.of(1L, 5L), Tuple2.of(1L, 7L), Tuple2.of(1L, 4L), Tuple2.of(1L, 2L),
				Tuple2.of(1L, 2L)).keyBy(0).flatMap(new CountWindowAverage()).print();
		// 结果会打印出 (1,4) 和 (1,5)
		try {
			env.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
