package net.zxjava.state;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class CountWindowAverage extends RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>> {

	private static final long serialVersionUID = -8877892168562443728L;

	// ValueState 使用方式，第一个字段是 count，第二个字段是运行的和
	private transient ValueState<Tuple2<Long, Long>> sum;

	@Override
	public void flatMap(Tuple2<Long, Long> input, Collector<Tuple2<Long, Long>> out) throws Exception {
		// 访问状态的 value 值
		Tuple2<Long, Long> currentSum = sum.value();
		// 更新 count
		currentSum.f0 += 1;
		// 更新 sum
		currentSum.f1 += input.f1;
		// 更新状态
		sum.update(currentSum);
		// 如果 count 等于 2, 发出平均值并清除状态
		if (currentSum.f0 >= 2) {
			out.collect(new Tuple2<>(input.f0, currentSum.f1 / currentSum.f0));
			sum.clear();
		}
	}

	@Override
	public void open(Configuration config) {
		StateTtlConfig ttlConfig = StateTtlConfig.newBuilder(Time.seconds(1))// state的存活时间
				.setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)// 仅限创建和写入访问时更新
				.setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired).build();// 永远不会返回过期值

		ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<>("average", // 状态名称
				TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {
				})); // 类型信息
		descriptor.enableTimeToLive(ttlConfig); // 开启 ttl
		sum = getRuntimeContext().getState(descriptor);// 获取状态
	}
}