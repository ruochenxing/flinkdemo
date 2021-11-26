package net.zxjava.newdemo.three;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 结果有问题，并没有保存结果到文件中
 * */
public class C04_WriteAsCsvSink {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = lines
				.flatMap(new FlatMapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = -157684009536333142L;

					@Override
					public void flatMap(String line, Collector<Tuple2<String, Integer>> out) throws Exception {
						String[] words = line.split(" ");
						for (String word : words) {
							out.collect(Tuple2.of(word, 1));
						}
					}
				});

		SingleOutputStreamOperator<Tuple2<String, Integer>> summed = wordAndOne.keyBy(0).sum(1);
		summed.print();
		// 如果数据不是Tuple类型，writeAsCsv是无法正常保存
		summed.writeAsCsv("out_dir1.csv", FileSystem.WriteMode.NO_OVERWRITE);

		env.execute("C02_AddSinkDemo");
	}
}
