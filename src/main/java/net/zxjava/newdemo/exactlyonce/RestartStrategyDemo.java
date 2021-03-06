package net.zxjava.newdemo.exactlyonce;

import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/**
 * flink run -p 1 -c exactlyonce1.RestartStrategyDemo first-1.0-SNAPSHOT.jar
 * 
 * 
 * bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic testTopic
 * bin/kafka-console-producer.sh --broker-list localhost:9092 --topic testTopic
 */
public class RestartStrategyDemo {

	public static void main(String[] args) throws Exception {

		/** 1.创建流运行环境 **/
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		/** 请注意此处： **/
		// 1.只有开启了CheckPointing,才会有重启策略
		env.enableCheckpointing(5000);
		// 2.默认的重启策略是：固定延迟无限重启
		// 此处设置重启策略为：出现异常重启3次，隔5秒一次
		env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(2, Time.seconds(2)));
		// 系统异常退出或人为 Cancel 掉，不删除checkpoint数据
		env.getCheckpointConfig()
				.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
		// 设置Checkpoint模式（与Kafka整合，一定要设置Checkpoint模式为Exactly_Once）
		env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

		/** 2.Source:读取 Kafka 中的消息 **/
		// Kafka props
		Properties properties = new Properties();
		// 指定Kafka的Broker地址
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "ruochenxing4:9092");
		// 指定组ID
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "testGroup");
		// 如果没有记录偏移量，第一次从最开始消费
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		// Kafka的消费者，不自动提交偏移量
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

		FlinkKafkaConsumer011<String> kafkaSource = new FlinkKafkaConsumer011("testTopic", new SimpleStringSchema(),
				properties);

		// Checkpoint成功后，还要向Kafka特殊的topic中写偏移量(此处不建议改为false )
		// 设置为false后，则不会向特殊topic中写偏移量。
		// kafkaSource.setCommitOffsetsOnCheckpoints(false);
		// 通过addSource()方式，创建 Kafka DataStream
		DataStreamSource<String> kafkaDataStream = env.addSource(kafkaSource);

		/** 3.Transformation过程 **/
		SingleOutputStreamOperator<Tuple2<String, Integer>> streamOperator = kafkaDataStream
				.map(str -> Tuple2.of(str, 1)).returns(Types.TUPLE(Types.STRING, Types.INT));

		/** 此部分读取Socket数据，只是用来人为出现异常，触发重启策略。验证重启后是否会再次去读之前已读过的数据(Exactly-Once) */
		/*************** start **************/
		// nc -l 9999
		DataStreamSource<String> socketTextStream = env.socketTextStream("10.211.55.4", 9999);

		SingleOutputStreamOperator<String> streamOperator1 = socketTextStream.map(new MapFunction<String, String>() {
			@Override
			public String map(String word) throws Exception {
				if ("error".equals(word)) {
					throw new RuntimeException("Throw Exception");
				}
				return word;
			}
		});
		/************* end **************/

		// 对元组 Tuple2 分组求和
		SingleOutputStreamOperator<Tuple2<String, Integer>> sum = streamOperator.keyBy(0).sum(1);

		/** 4.Sink过程 **/
		sum.print();

		/** 5.任务执行 **/
		env.execute("RestartStrategyDemo");
	}
}