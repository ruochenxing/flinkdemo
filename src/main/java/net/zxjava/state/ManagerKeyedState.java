package net.zxjava.state;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class ManagerKeyedState {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);

		env.fromElements(Tuple2.of(1L, 3L), Tuple2.of(1L, 5L), Tuple2.of(1L, 7L), Tuple2.of(1L, 4L), Tuple2.of(1L, 2L))
				.keyBy(0).flatMap(new RichFlatMapFunction<Tuple2<Long, Long>, Tuple2<Long, Long>>() {
					private static final long serialVersionUID = 880727045606970115L;
					private ValueState<Tuple2<Long, Long>> state;

					@Override
					public void open(Configuration parameters) throws Exception {
						super.open(parameters);
						ValueStateDescriptor<Tuple2<Long, Long>> descriptor = new ValueStateDescriptor<Tuple2<Long, Long>>(
								"", TypeInformation.of(new TypeHint<Tuple2<Long, Long>>() {
								}));

						state = getRuntimeContext().getState(descriptor);
					}

					@Override
					public void flatMap(Tuple2<Long, Long> value, Collector<Tuple2<Long, Long>> out) throws Exception {
						Tuple2<Long, Long> currentSum = state.value();
						System.out.println("currentSum: " + currentSum + "  value: " + value);
						if (currentSum == null) {
							currentSum = new Tuple2<Long, Long>();
							currentSum.f0 = 0L;
							currentSum.f1 = 0L;
						}
						currentSum.f0 += 1;
						currentSum.f1 += value.f1;

						state.update(currentSum);

						if (currentSum.f0 >= 2) {
							out.collect(Tuple2.of(value.f0, currentSum.f1 / currentSum.f0));
							state.clear();
						}
					}
				}).print();

		env.execute("ManagerKeyedState");
	}
}