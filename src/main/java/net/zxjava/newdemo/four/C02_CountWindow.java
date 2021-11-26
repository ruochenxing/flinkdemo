package net.zxjava.newdemo.four;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;

/**
 * 分组后再调用 CountWindow，每一个组达到一定的条数才会触发窗口执行
 */
public class C02_CountWindow {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// spark,3
		// hadoop,2
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndCount = lines
				.map(new MapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = -1822736336861722571L;

					@Override
					public Tuple2<String, Integer> map(String value) throws Exception {
						String[] fields = value.split(",");
						String word = fields[0];
						Integer count = Integer.parseInt(fields[1]);
						return Tuple2.of(word, count);
					}
				});

		// 先分组，再划分窗口
		KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndCount.keyBy(0);

		// 划分窗口
		WindowedStream<Tuple2<String, Integer>, Tuple, GlobalWindow> window = keyed.countWindow(5);

		SingleOutputStreamOperator<Tuple2<String, Integer>> summed = window.sum(1);

		summed.print();

		env.execute("C02_CountWindow");
	}
}
