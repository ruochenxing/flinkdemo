package net.zxjava;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * 自定义SourceFunction-》keyBy
 */
public class MySourceFunction2 {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<Tuple2<String, Integer>> dataStream = env.addSource(new SourceFunction<Tuple2<String, Integer>>() {

			private static final long serialVersionUID = 2963932775740265731L;
			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<Tuple2<String, Integer>> ctx) throws Exception {
				while (isRunning) {
					ctx.collect(Tuple2.of(String.valueOf(Math.floor(Math.random() * 100)), 1));
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}
		});
		dataStream.keyBy(0).print();
		env.execute();
	}
}
