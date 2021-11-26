package net.zxjava;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MySourceFunction4 {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStream<Tuple2<Long, Long>> dataStream = env.fromElements(Tuple2.of(2L, 3L), Tuple2.of(1L, 5L),
				Tuple2.of(1L, 7L), Tuple2.of(2L, 4L), Tuple2.of(1L, 2L), Tuple2.of(3L, 2L));
		dataStream.keyBy(new KeySelector<Tuple2<Long, Long>, Long>() {

			private static final long serialVersionUID = -3984323402407505729L;

			@Override
			public Long getKey(Tuple2<Long, Long> value) throws Exception {
				return value.f0;
			}

			// reduce必须用在有界数据集上面
		}).sum(1).print();
		env.execute();
	}
}
