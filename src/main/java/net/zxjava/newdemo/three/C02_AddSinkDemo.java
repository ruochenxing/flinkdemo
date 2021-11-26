package net.zxjava.newdemo.three;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.util.Collector;

public class C02_AddSinkDemo {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = lines
				.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 5295443671001064121L;

					@Override
					public void flatMap(String line, Collector<Tuple2<String, Integer>> out) throws Exception {
						String[] words = line.split(" ");
						for (String word : words) {
							out.collect(Tuple2.of(word, 1));
						}
					}
				});

		SingleOutputStreamOperator<Tuple2<String, Integer>> summed = wordAndOne.keyBy(0).sum(1);

		summed.addSink(new RichSinkFunction<Tuple2<String, Integer>>() {
			private static final long serialVersionUID = -5644556875404555637L;

			@Override
			public void invoke(Tuple2<String, Integer> value, Context context) throws Exception {
				// 通过功能更加丰富的RichSinkFunction，可以通过getRuntimeContext可以拿到subTaskIndex
				int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
				System.out.println(indexOfThisSubtask + " > " + value);
			}
		});

		env.execute("C02_AddSinkDemo");
	}
}
