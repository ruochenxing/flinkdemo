package net.zxjava.newdemo.exactlyonce;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.alibaba.fastjson.JSON;

public class KafkaUtils {
	private static final String broker_list = "ruochenxing4:9092";
	// flink 读取kafka写入mysql exactly-once 的topic
	private static final String topic_ExactlyOnce = "mysql-exactly-once";

	public static void writeToKafka2() throws InterruptedException {
		Properties props = new Properties();
		props.put("bootstrap.servers", broker_list);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		Producer<String, String> producer = new KafkaProducer<>(props);

		try {
			for (int i = 6; i <= 10; i++) {
				MysqlExactlyOncePOJO mysqlExactlyOnce = new MysqlExactlyOncePOJO(String.valueOf(i));
				ProducerRecord<String, String> record = new ProducerRecord<>(topic_ExactlyOnce, null, null,
						JSON.toJSONString(mysqlExactlyOnce));
				producer.send(record);
				System.out.println("发送数据: " + JSON.toJSONString(mysqlExactlyOnce));
				Thread.sleep(1000);
			}
		} catch (Exception e) {

		}

		producer.flush();
	}

	public static void main(String[] args) throws InterruptedException {
		writeToKafka2();
	}

	public static class MysqlExactlyOncePOJO {
		public String value;

		public MysqlExactlyOncePOJO(String value) {
			this.value = value;
		}
	}
}
