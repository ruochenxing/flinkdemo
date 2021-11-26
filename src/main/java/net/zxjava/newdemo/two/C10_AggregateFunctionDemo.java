package net.zxjava.newdemo.two;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// AggregateFunction 比 ReduceFunction 更加的通用，它有三个参数，一个输入类型（IN），一个累加器（ACC），一个输出类型（OUT）。
// 输入类型，就是输入流的类型。接口中有一个方法，可以把输入的元素和累加器累加。并且可以初始化一个累加器，然后把两个累加器合并成一个累加器，获得输出结果。

public class C10_AggregateFunctionDemo {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//		DataStreamSource<Tuple3<String, String, Integer>> input = env.fromElements(ENGLISH);
		// class1 zhangsan 100
		// class1 lisi 30
		// class1 wangwu 70
		
		DataStreamSource<String> input = env.socketTextStream("localhost", 7777);
		SingleOutputStreamOperator<Tuple3<String, String, Integer>> words = input.map(w -> {
			String[] arr = w.split("\\s+");
			return Tuple3.of(arr[0], arr[1], Integer.valueOf(arr[2]));
		}).returns(Types.TUPLE(Types.STRING, Types.STRING, Types.INT));
		// AggregateFunction 比 ReduceFunction
		// 更加的通用，它有三个参数，一个输入类型（IN），一个累加器（ACC），一个输出类型（OUT）
		words.keyBy(x -> x.f0).countWindow(3).aggregate(
				new AggregateFunction<Tuple3<String, String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
					private static final long serialVersionUID = 5963834118501451321L;

					// 创建累加器操作：初始化中间值
					@Override
					public Tuple2<String, Integer> createAccumulator() {
						return Tuple2.of("class1", 1000);
					}

					// 累加器操作
					@Override
					public Tuple2<String, Integer> add(Tuple3<String, String, Integer> value1,
							Tuple2<String, Integer> value2) {
						return Tuple2.of(value1.f0, value1.f2 + value2.f1);
					}

					// 获取结果
					@Override
					public Tuple2<String, Integer> getResult(Tuple2<String, Integer> value) {
						return Tuple2.of(value.f0, value.f1);
					}

					// 累加器合并操作，只有会话窗口的时候才会调用！
					@Override
					public Tuple2<String, Integer> merge(Tuple2<String, Integer> value, Tuple2<String, Integer> acc1) {
						return Tuple2.of(value.f0, value.f1 + acc1.f1);
					}
				}).print("aggregate累加");

		env.execute();
	}

	@SuppressWarnings("unchecked")
	public static final Tuple3<String, String, Integer>[] ENGLISH = new Tuple3[] {
			// 班级 姓名 成绩
			Tuple3.of("class1", "张三", 100), Tuple3.of("class1", "李四", 30), Tuple3.of("class1", "王五", 70),
			Tuple3.of("class2", "赵六", 50), Tuple3.of("class2", "小七", 40), Tuple3.of("class2", "小八", 10), };

}
