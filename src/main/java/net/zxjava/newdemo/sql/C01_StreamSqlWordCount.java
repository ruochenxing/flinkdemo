package net.zxjava.newdemo.sql;

import java.util.Arrays;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

// spark hadoop flink spark
public class C01_StreamSqlWordCount {

	public static void main(String[] args) throws Exception {
		// 实时DataStreamAPI
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 创建一个实时的Table执行上下文环境
		StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

		// word count spark hadoop
		DataStreamSource<String> lines = env.socketTextStream("localhost", 7777);
		SingleOutputStreamOperator<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
			private static final long serialVersionUID = -7823759483897377634L;

			@Override
			public void flatMap(String line, Collector<String> out) throws Exception {
				Arrays.stream(line.split(" ")).forEach(out::collect);

			}
		});

		// 注册成表
		tableEnv.registerDataStream("t_wordcount", words, "word");

		// 写SQL
		Table table = tableEnv.sqlQuery("SELECT word, COUNT(1) counts FROM t_wordcount GROUP BY word");

		//
		// DataStream<Tuple2<Boolean, C01_WordCount>> dataStream =
		// tableEnv.toRetractStream(table, C01_WordCount.class);
		DataStream<Tuple2<Boolean, Row>> dataStream = tableEnv.toRetractStream(table, Row.class);

		dataStream.filter(new FilterFunction<Tuple2<Boolean, Row>>() {
			private static final long serialVersionUID = -6357376528028032582L;

			@Override
			public boolean filter(Tuple2<Boolean, Row> value) throws Exception {
				return value.f0;
			}
		}).print();

		env.execute("C01_StreamSqlWordCount");
	}
}
