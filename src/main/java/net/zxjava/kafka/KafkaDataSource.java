package net.zxjava.kafka;

import java.util.Properties;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

public class KafkaDataSource {
	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		Properties props = new Properties();
		props.put("bootstrap.servers", "10.90.0.13:9092");
		props.put("zookeeper.connect", "10.90.0.13:2181");
		props.put("group.id", "metric-group");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"); // key 反序列化
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("auto.offset.reset", "latest"); // value 反序列化

		DataStreamSource<String> dataStreamSource = env
				.addSource(new FlinkKafkaConsumer011<>("test_mysql.marketing.coupons_users", // kafka topic
						new SimpleStringSchema(), // String 序列化
						props))
				.setParallelism(1);

		DataStream<Integer> countStreams = dataStreamSource.map(x -> 1).timeWindowAll(Time.seconds(10))
				.reduce((x, y) -> x + y).forward();
		env.execute("count kafka data");
	}
}
