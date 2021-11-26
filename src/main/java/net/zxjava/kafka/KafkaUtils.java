package net.zxjava.kafka;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * 往kafka中写数据 可以使用这个main函数进行测试一下
 */
public class KafkaUtils {
	public static final String broker_list = "10.90.0.13:9092";
	public static final String topic = "test_12_postgres.public.users"; // kafka topic，Flink 程序中需要和这个统一

	public static void writeToKafka(String message) throws InterruptedException {
		Properties props = new Properties();
		props.put("bootstrap.servers", broker_list);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // key 序列化
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); // value 序列化
		KafkaProducer<String, String> producer = new KafkaProducer<>(props);
		// Metric metric = new Metric();
		// metric.setTimestamp(System.currentTimeMillis());
		// metric.setName("mem");
		// Map<String, String> tags = new HashMap<>();
		// Map<String, Object> fields = new HashMap<>();
		// tags.put("cluster", "zhisheng");
		// tags.put("host_ip", "101.147.022.106");
		// fields.put("used_percent", 90d);
		// fields.put("max", 27244873d);
		// fields.put("used", 17244873d);
		// fields.put("init", 27244873d);
		// metric.setTags(tags);
		// metric.setFields(fields);
		ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, message);
		producer.send(record);
		System.out.println("发送数据: " + message);
		producer.flush();
		producer.close();
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		writeToKafka(
				"{\"payload\":{\"before\":null,\"after\":{\"id\":1358928656,\"mobile\":\"18779187305\",\"uuid\":\"1123412341234\",\"created_at\":1575444207000},\"source\":{\"version\":\"0.9.5.Final\",\"connector\":\"mysql\",\"name\":\"pro_mysql\",\"server_id\":35571072,\"ts_sec\":1575444209,\"gtid\":\"598ba183-5eea-11e8-bf7c-7cd30ae00cd2:22284893\",\"file\":\"mysql-bin.002097\",\"pos\":10827999,\"row\":8,\"snapshot\":false,\"thread\":11363057,\"db\":\"marketing\",\"table\":\"users\",\"query\":null},\"op\":\"c\",\"ts_ms\":1575444209045}}");
	}
}