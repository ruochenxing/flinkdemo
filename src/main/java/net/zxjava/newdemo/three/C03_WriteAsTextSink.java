package net.zxjava.newdemo.three;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class C03_WriteAsTextSink {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);

		lines.writeAsText("out_dir");// 默认并行度8时，文件夹不能已经存在，并行度为1时，代表一个文件

		env.execute("C03_WriteAsTextSink");
	}
}
