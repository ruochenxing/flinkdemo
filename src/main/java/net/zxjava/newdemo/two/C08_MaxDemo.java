package net.zxjava.newdemo.two;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// 取当最当前key最大值
public class C08_MaxDemo {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// spark,10
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndNum = lines
				.map(new MapFunction<String, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 9034880513706949270L;

					@Override
					public Tuple2<String, Integer> map(String line) throws Exception {
						String[] fields = line.split(",");
						String word = fields[0];
						int num = Integer.parseInt(fields[1]);
						return Tuple2.of(word, num);
					}
				});

		KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndNum.keyBy(0);
		SingleOutputStreamOperator<Tuple2<String, Integer>> res = keyed.max(1);

		res.print();

		env.execute("C08_MaxDemo");

	}
}
