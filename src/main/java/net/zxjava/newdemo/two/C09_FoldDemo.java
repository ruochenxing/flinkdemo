package net.zxjava.newdemo.two;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * Fold 算子适合需要从指定数据开始累计的场景；
 * 
 * FoldFunction定义了如何将窗口中的输入元素与外部的元素合并的逻辑
 * 
 */
public class C09_FoldDemo {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 直接输入的就是单词
		DataStreamSource<String> words = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = words.map(w -> Tuple2.of(w, 1))
				.returns(Types.TUPLE(Types.STRING, Types.INT));

		// 在java,认为元组是一个特殊的集合，脚标是从0开始
		KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndOne.keyBy(0);

		/*
		 * SingleOutputStreamOperator<Integer> folded = keyed.fold(1000, new
		 * FoldFunction<Tuple2<String, Integer>, Integer>() {
		 * 
		 * @Override public Integer fold(Integer accumulator, Tuple2<String, Integer>
		 * value) throws Exception { return accumulator + value.f1; } });
		 * 
		 * folded.print();
		 */
		String initValue = "";
		SingleOutputStreamOperator<String> result = keyed.fold(initValue,
				new FoldFunction<Tuple2<String, Integer>, String>() {
					@Override
					public String fold(String accumulator, Tuple2<String, Integer> value) throws Exception {
						String word = "";
						int sum = 0;
						if (accumulator.equals("")) {
							word = value.f0;
							sum += value.f1;
						} else {
							String[] fields = accumulator.split("-");
							word = fields[0];
							sum = Integer.parseInt(fields[1]) + value.f1;
						}

						return word + "-" + sum;
					}
				});

		result.print();

		env.execute("C09_FoldDemo");

	}

}
