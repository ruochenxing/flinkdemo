package net.zxjava;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;

public class TimeWindowDemo2 {

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
		dataStream.map(x -> 1).timeWindowAll(Time.seconds(1)).reduce((x, y) -> x + y).addSink(new SinkFunction<Integer>() {

			private static final long serialVersionUID = 2902933031185771809L;

			@Override
			public void invoke(Integer value) throws Exception {
				if(value < 5100000) {
					System.out.println("");
				}
			}
			
		});
		env.execute();
	}
}
