package net.zxjava.newdemo.two;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.util.Collector;

// ProcessWindowFunction 有一个 Iterable 迭代器，用来获得窗口中所有的元素。
// 有一个上下文对象用来获得时间和状态信息，比其他的窗口函数有更大的灵活性。
// 但是这样做损耗了一部分性能和资源，因为元素不能增量聚合，相反 ，在触发窗口计算时，Flink 需要在内部缓存窗口的所有元素。
// https://www.cnblogs.com/-courage/p/14674309.html
/**
 * ProcessWindowFunction 相较于其他的 Window Function, 可以实现一些更复杂的计算, 比如基于整个窗口做某些指标计算
 * 或者需要操作窗口中的状态数据和窗口元数据. Flink 提供了 ProcessWindowFunction 这个抽象类,
 * 继承此类就可以实现ProcessWindowFunction, 其中, 必须要实现 process( ) 方法,
 * 这是处理窗口数据的主要方法.还在一下跟窗口数据相关的方法可以有选择的实现.
 */
public class C10_ProcessWindowFunctionDemo {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<Tuple3<String, String, Integer>> input = env.fromElements(ENGLISH);

		// public abstract class ProcessWindowFunction<IN, OUT, KEY, W extends Window>
		// extends AbstractRichFunction ...

		input.keyBy(x -> x.f0).countWindow(2).process(
				new ProcessWindowFunction<Tuple3<String, String, Integer>, Tuple3<String, String, Integer>, String, GlobalWindow>() {
					private static final long serialVersionUID = 8163489061958144183L;

					@Override
					public void process(String s, // 参数1：key
							Context context, // 参数2：上下文对象
							Iterable<Tuple3<String, String, Integer>> iterable, // 参数3：这个窗口的所有元素
							// 参数4：收集器，用于向下游传递数据
							Collector<Tuple3<String, String, Integer>> collector) throws Exception {
						System.out.println(context.window().maxTimestamp());
						int sum = 0;
						String name = "";
						for (Tuple3<String, String, Integer> tuple3 : iterable) {
							sum += tuple3.f2;
							name = tuple3.f1;
						}

						collector.collect(Tuple3.of(s, name, sum));
					}
				}).print();

	}

	@SuppressWarnings("unchecked")
	public static final Tuple3<String, String, Integer>[] ENGLISH = new Tuple3[] {
			// 班级 姓名 成绩
			Tuple3.of("class1", "张三", 100), Tuple3.of("class1", "李四", 30), Tuple3.of("class1", "王五", 70),
			Tuple3.of("class2", "赵六", 50), Tuple3.of("class2", "小七", 40), Tuple3.of("class2", "小八", 10), };
}
