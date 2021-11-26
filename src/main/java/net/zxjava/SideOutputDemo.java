package net.zxjava;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

public class SideOutputDemo {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<Long> dataStream = env.addSource(new SourceFunction<Long>() {

			private static final long serialVersionUID = 2963932775740265731L;
			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<Long> ctx) throws Exception {
				while (isRunning) {
					ctx.collect(Math.round(Math.random() * 100));
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}
		});
		OutputTag<Long> even = new OutputTag<Long>("even") {
			private static final long serialVersionUID = -4924115047479130391L;
		};
		OutputTag<Long> odd = new OutputTag<Long>("odd") {
			private static final long serialVersionUID = -7071693616285238379L;
		};
		SingleOutputStreamOperator<Long> mainDataStream = dataStream.process(new ProcessFunction<Long, Long>() {
			private static final long serialVersionUID = -5285175128125579385L;

			@Override
			public void processElement(Long value, ProcessFunction<Long, Long>.Context ctx, Collector<Long> out)
					throws Exception {
				// 将数据发送到常规输出中
				out.collect(value);
				// 将数据发送到侧输出中
				if (value % 2 != 0) {
					ctx.output(odd, value);
				} else {
					ctx.output(even, value);
				}
			}
		});
		DataStream<Long> evenStream = mainDataStream.getSideOutput(even);
		DataStream<Long> oddStream = mainDataStream.getSideOutput(odd);
		oddStream.print();
		evenStream.print();
		env.execute();
	}
}
