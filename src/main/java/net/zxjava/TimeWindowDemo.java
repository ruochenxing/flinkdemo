package net.zxjava;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class TimeWindowDemo {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<Tuple2<Long, Integer>> data = env.addSource(new SourceFunction<Tuple2<Long, Integer>>() {

			private static final long serialVersionUID = 2963932775740265731L;
			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<Tuple2<Long, Integer>> ctx) throws Exception {
				while (isRunning) {
					Thread.sleep(100);
					ctx.collect(Tuple2.of(Double.valueOf(Math.floor(Math.random() * 100)).longValue(), 1));
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}
		});
		// data.keyBy(1).timeWindow(Time.seconds(10)) // tumbling time window 每分钟统计一次数量和
		// .sum(1).print();
		// dataStream.keyBy(1).timeWindow(Time.minutes(1), Time.seconds(30)) //sliding
		// time window 每隔 30s
		// 统计过去一分钟的数量和
		data.keyBy(1).countWindow(100).sum(1).print();
		env.execute();
	}
}
