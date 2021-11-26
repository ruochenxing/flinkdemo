package net.zxjava.newdemo.exactlyonce;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
//import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
//import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.util.Collector;

// flink run -p 1 -c exactlyonce.StreamDemoKafka2Mysql first-1.0-SNAPSHOT.jar
// 获取最后一次checkpoint路径
// hdfs://ruochenxing4:9000/flink/checkpoints/aaefe0aac6a8d30576ac8f983b6346ae/chk-1
// 使用checkpoint 恢复
// flink run -p 1 -s hdfs://ruochenxing4:9000/flink/checkpoints/9b3cb0a97db74587d7bc12f30afac3c9/chk-21/_metadata -c exactlyonce.StreamDemoKafka2Mysql first-1.0-SNAPSHOT.jar
public class CountValueFunction extends RichFlatMapFunction<Tuple2<String, ObjectNode>, Long> {

	private static final long serialVersionUID = 5672847598958637454L;

	// ValueState 使用方式，第一个字段是 count，第二个字段是运行的和
	private transient ValueState<Long> sumState;

	@Override
	public void flatMap(Tuple2<String, ObjectNode> value, Collector<Long> out) throws Exception {
		// 访问状态的 value 值
		Long currentSum = sumState.value();
		// f1 =
		// {"value":{"value":"5"},"metadata":{"offset":74,"topic":"mysql-exactly-once","partition":0}}
		long v = value.f1.get("value").get("value").asLong();
		if (currentSum == null) {
			currentSum = 0L;
		}
		currentSum += v;
		sumState.update(currentSum);
		System.out.println(currentSum);
	}

	@Override
	public void open(Configuration parameters) throws Exception {
//		StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.seconds(100))// state的存活时间
//				.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)// 仅限创建和写入访问时更新
//				.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();// 永远不会返回过期值

		ValueStateDescriptor<Long> descriptor = new ValueStateDescriptor<>("sumState", // 状态名称
				TypeInformation.of(new TypeHint<Long>() {
				})); // 类型信息
//		descriptor.enableTimeToLive(ttlConfig); // 开启 ttl
		sumState = getRuntimeContext().getState(descriptor);// 获取状态
	}

}
