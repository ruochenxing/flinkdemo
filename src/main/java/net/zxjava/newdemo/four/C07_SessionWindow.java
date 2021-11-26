package net.zxjava.newdemo.four;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

// 距离上一次会话时间超过时间，触发

public class C07_SessionWindow {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// spark,3
		// hadoop,2
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndCount = lines
				.map(new MapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 1720965178648045363L;

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
		WindowedStream<Tuple2<String, Integer>, Tuple, TimeWindow> window = keyed
				.window(ProcessingTimeSessionWindows.withGap(Time.seconds(5)));

		SingleOutputStreamOperator<Tuple2<String, Integer>> summed = window.sum(1);

		summed.print();

		env.execute("C07_SessionWindow");
	}
}
