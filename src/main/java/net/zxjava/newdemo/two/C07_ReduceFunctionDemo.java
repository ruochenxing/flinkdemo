package net.zxjava.newdemo.two;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 对输入的两个相同类型的元素按照指定的计算方式进行聚合, 通过实现 ReduceFunction 接口就可以在reduce( ) 函数内部进行聚合操作了.
 */
public class C07_ReduceFunctionDemo {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 直接输入的就是单词
		DataStreamSource<String> words = env.socketTextStream("localhost", 7777);

		SingleOutputStreamOperator<Tuple2<String, Integer>> wordAndOne = words.map(w -> Tuple2.of(w, 1))
				.returns(Types.TUPLE(Types.STRING, Types.INT));

		// 在java，认为元素是一个特殊的集合，脚标是从0开始；因为Flink底层源码是java编写的
		KeyedStream<Tuple2<String, Integer>, Tuple> keyed = wordAndOne.keyBy(0);
		// 让两个元素结合起来，产生一个相同类型的元素，它是增量的，放在KeyBy函数之后
		SingleOutputStreamOperator<Tuple2<String, Integer>> reduced = keyed
				.reduce(new ReduceFunction<Tuple2<String, Integer>>() {
					private static final long serialVersionUID = -4183231741254639678L;

					@Override
					public Tuple2<String, Integer> reduce(Tuple2<String, Integer> v1, Tuple2<String, Integer> v2)
							throws Exception {
						v1.f1 = v1.f1 + v2.f1;
						return v1;
					}
				});

		reduced.print();

		env.execute("C07_ReduceDemo");
	}
}
