package net.zxjava.newdemo.exactlyonce;

import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumerBase;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/**
 * 消费kafka消息，sink(自定义)到mysql中，保证kafka to mysql 的Exactly-Once
 */

public class StreamDemoKafka2Mysql {
	private static final String topic_ExactlyOnce = "mysql-exactly-once";

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// 设置并行度,为了方便测试，查看消息的顺序，这里设置为1，可以更改为多并行度
//		env.setParallelism(1);
		// checkpoint的设置
		// 每隔60s进行启动一个检查点【设置checkpoint的周期】
		env.enableCheckpointing(120000);
		// 设置模式为：exactly_one，仅一次语义
		env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
		// 确保检查点之间有1s的时间间隔【checkpoint最小间隔】
		env.getCheckpointConfig().setMinPauseBetweenCheckpoints(1000);
		// 检查点必须在60s之内完成，或者被丢弃【checkpoint超时时间】
		env.getCheckpointConfig().setCheckpointTimeout(100000);
		// 同一时间只允许进行一次检查点
		env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

		// 此处设置重启策略为：出现异常重启3次，隔5秒一次
//		env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(2, Time.seconds(2)));

		// 表示一旦Flink程序被cancel后，会保留checkpoint数据，以便根据实际需要恢复到指定的checkpoint
		env.getCheckpointConfig()
				.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
		// 设置statebackend,将检查点保存在hdfs上面，默认保存在内存中。这里先保存到本地
		StateBackend fs = new FsStateBackend("hdfs://ruochenxing4:9000/flink/checkpoints"); 
		env.setStateBackend(fs);
		// 设置kafka消费参数
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "ruochenxing4:9092");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "flink-consumer-group2");
		// kafka分区自动发现周期
		props.put(FlinkKafkaConsumerBase.KEY_PARTITION_DISCOVERY_INTERVAL_MILLIS, "3000");

		/*
		 * SimpleStringSchema可以获取到kafka消息，JSONKeyValueDeserializationSchema可以获取都消息的key,
		 * value，metadata:topic,partition，offset等信息
		 */
		FlinkKafkaConsumer011<ObjectNode> kafkaConsumer011 = new FlinkKafkaConsumer011<>(topic_ExactlyOnce,
				new JSONKeyValueDeserializationSchema(true), props);
//		kafkaConsumer011.setStartFromTimestamp(0);

		// 加入kafka数据源
		DataStreamSource<ObjectNode> streamSource = env.addSource(kafkaConsumer011);
//		streamSource.print();
		streamSource.map(new MapFunction<ObjectNode, Tuple2<String, ObjectNode>>() {

			private static final long serialVersionUID = 1239239195178569020L;

			@Override
			public Tuple2<String, ObjectNode> map(ObjectNode value) throws Exception {
				long num = value.get("value").get("value").asLong();
//				if (num == 5) {
//					throw new RuntimeException("Throw Exception");
//				}
				return new Tuple2<>("test", value);
			}
		}).keyBy(0).flatMap(new CountValueFunction()).setParallelism(2);
		// 数据传输到下游
//		streamSource.addSink(new MySqlTwoPhaseCommitSink()).name("MySqlTwoPhaseCommitSink");
		// 触发执行
		env.execute(StreamDemoKafka2Mysql.class.getName());
	}
}

/**
 * 1.kafkaSource.setCommitOffsetsOnCheckpoints(boolean);方法，是用来干什么的?
 * 
 * 官方文档有介绍，你可参考：Flink官方文档。代码中不建议将kafkaSource.setCommitOffsetsOnCheckpoints(boolean);方法设置为
 * false。此处，我们对该方法来做一个书面介绍：
 * 
 * ①你如果禁用CheckPointing，则Flink Kafka
 * Consumer依赖于内部使用的Kafka客户端的自动定期偏移量提交功能。该偏移量会被记录在 Kafka 中的 _consumer_offsets
 * 这个特殊记录偏移量的 Topic 中。
 * 
 * ②你如果启用CheckPointing，偏移量则会被记录在 StateBackend
 * 中。该方法kafkaSource.setCommitOffsetsOnCheckpoints(boolean);设置为 ture 时，偏移量会在
 * StateBackend 和 Kafka 中的 _consumer_offsets Topic 中都会记录一份；设置为 false 时，偏移量只会在
 * StateBackend 中的 存储一份。
 */
