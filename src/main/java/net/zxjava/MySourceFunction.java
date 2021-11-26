package net.zxjava;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * 自定义SourceFunction
 */
public class MySourceFunction {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<String> dataStream = env.addSource(new SourceFunction<String>() {

			private static final long serialVersionUID = 4498261897777455721L;

			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<String> ctx) throws Exception {
				while (isRunning) {
					Thread.sleep(1000);
					ctx.collect(String.valueOf(Math.round((Math.random() * 100))));
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}
		});
		dataStream.keyBy(new KeySelector<String, Long>() {

			private static final long serialVersionUID = -4650957976903937368L;

			@Override
			public Long getKey(String value) throws Exception {
				return Long.valueOf(value);
			}

		}).timeWindow(Time.seconds(10)).max(0).print();
		env.execute();
	}
}
