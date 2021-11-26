package net.zxjava.newdemo.three;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class C05_Task_SubTask_ChainTest {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<String> word = lines.flatMap(new FlatMapFunction<String, String>() {
			private static final long serialVersionUID = 3846061303912339627L;

			@Override
			public void flatMap(String line, Collector<String> out) throws Exception {
				String[] words = line.split(" ");
				for (String word : words) {
					out.collect(word);
				}
			}
		});

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = word
				.map(new MapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 4829442700222737270L;

					@Override
					public Tuple2<String, Integer> map(String value) throws Exception {
						return Tuple2.of(value, 1);
					}
				});

		SingleOutputStreamOperator<Tuple2<String, Integer>> filtered = wordAndOne
				.filter(new FilterFunction<Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 4048619793975798977L;

					@Override
					public boolean filter(Tuple2<String, Integer> value) throws Exception {
						return value.f0.startsWith("h");
					}
				});
		// .disableChaining(); // 将这个算子单独划分处理，生成一个Task，跟其他的算子不再有Operator Chain;
		// 比如：CPU、内存密集型，算法逻辑复杂的操作单独划分成Task，独享硬件资源
		// .startNewChain();
		// 从该算子开始，开启一个新的链；从这个算子之前，发生redistributing
		// 需要使用Flink web 查看Show Plan

		filtered.print();

		env.execute("C05_Task_SubTask_ChainTest");
	}

}
