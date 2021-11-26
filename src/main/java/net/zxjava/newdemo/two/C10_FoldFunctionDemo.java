package net.zxjava.newdemo.two;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * Fold 算子适合需要从指定数据开始累计的场景；
 * 
 * FoldFunction定义了如何将窗口中的输入元素与外部的元素合并的逻辑
 * 
 */
public class C10_FoldFunctionDemo {
	private static final String[] TYPE = { "苹果", "梨", "西瓜", "葡萄", "火龙果" };

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 添加自定义数据源,每秒发出一笔订单信息{商品名称,商品数量}
		DataStreamSource<Tuple2<String, Integer>> orderSource = env
				.addSource(new SourceFunction<Tuple2<String, Integer>>() {
					private static final long serialVersionUID = -3852415133578138848L;
					private volatile boolean isRunning = true;
					private final Random random = new Random();

					@Override
					public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {
						while (isRunning) {
							TimeUnit.SECONDS.sleep(1);
							String type = TYPE[random.nextInt(TYPE.length)];
							System.out.println("---------------");
							System.out.println(type);
							ctx.collect(Tuple2.of(type, 1));
						}
					}

					@Override
					public void cancel() {
						isRunning = false;
					}

				}, "order-info");
		// 这里只为将DataStream → KeyedStream,用空字符串做分区键。所有数据为相同分区
		orderSource.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
			private static final long serialVersionUID = -7219765853499523461L;

			@Override
			public String getKey(Tuple2<String, Integer> value) throws Exception {
				return "";
//				return value.f0;
			}

		})
				// 这里用HashMap做暂存器 初始值
				.fold(new HashMap<String, Integer>(),
						new FoldFunction<Tuple2<String, Integer>, Map<String, Integer>>() {
							private static final long serialVersionUID = -9063622346431736711L;

							@Override
							public Map<String, Integer> fold(Map<String, Integer> accumulator,
									Tuple2<String, Integer> value) throws Exception {
								accumulator.put(value.f0, accumulator.getOrDefault(value.f0, 0) + value.f1);
								return accumulator;
							}
						})
				.print();

		env.execute("Flink Streaming Java API Skeleton");
	}

}