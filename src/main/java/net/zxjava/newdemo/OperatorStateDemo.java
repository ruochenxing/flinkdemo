package net.zxjava.newdemo;

import java.util.ArrayList;
import java.util.List;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

// 启动flink
// flink run -p 1 -c com.demo.OperatorStateDemo first-1.0-SNAPSHOT.jar

// 通过checkpoint恢复flink
// flink run -p 1 -s hdfs://ruochenxing4:9000/flink/checkpoints/37a8e24be8146e9c064ae55c58cebbe7/chk-1052/_metadata -c com.demo.OperatorStateDemo first-1.0-SNAPSHOT.jar

// 创建savepoint
// flink savepoint 877d86aa6251e5b8a3909e84f1469ab3 hdfs://ruochenxing4:9000/flink/savepoints/
// Triggering savepoint for job 877d86aa6251e5b8a3909e84f1469ab3.
// Waiting for response...
// Savepoint completed. Path: hdfs://ruochenxing4:9000/flink/savepoints/savepoint-877d86-7da408629d2d
// You can resume your program from this savepoint with the run command.

// 取消任务 
// flink cancel 877d86aa6251e5b8a3909e84f1469ab3

// 启动任务

// flink run -p 1 -s hdfs://ruochenxing4:9000/flink/savepoints/savepoint-877d86-7da408629d2d -c com.demo.OperatorStateDemo first-1.0-SNAPSHOT.jar


// https://blog.csdn.net/chuba5570/article/details/100612614
// https://mvnrepository.com/artifact/org.apache.flink/flink-shaded-hadoop-2-uber

public class OperatorStateDemo extends RichFlatMapFunction<Tuple2<String, Long>, List<Tuple2<String, Long>>>
		implements CheckpointedFunction {

	private static final long serialVersionUID = -2222605597472871749L;

	private final int threshold;
	private transient ListState<Tuple2<String, Long>> checkpointedState;
	private List<Tuple2<String, Long>> bufferedElements;

	public OperatorStateDemo(int threshold) {
		this.threshold = threshold;
		this.bufferedElements = new ArrayList<>();
	}

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// 每 1000ms 开始一次 checkpoint
		env.enableCheckpointing(60 * 1000);
		env.setStateBackend(new FsStateBackend("hdfs://ruochenxing4:9000/flink/checkpoints"));
		// 高级选项：
		// 设置模式为精确一次 (这是默认值)
		env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
		// 确认 checkpoints 之间的时间会进行 500 ms
		env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);
		// Checkpoint 必须在一分钟内完成，否则就会被抛弃
		env.getCheckpointConfig().setCheckpointTimeout(60000);
		// 同一时间只允许一个 checkpoint 进行
		env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
		// 开启在 job 中止后仍然保留的 externalized checkpoints
		env.getCheckpointConfig().enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
		// 允许在有更近 savepoint 时回退到 checkpoint
//		env.getCheckpointConfig().setPreferCheckpointForRecovery(true);
		
		// nc -l 9000
		DataStreamSource<String> stream = env.socketTextStream("10.211.55.4", 9999);

		stream.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public void flatMap(String s, Collector<Tuple2<String, Long>> out) throws Exception {
				String[] tokens = s.toLowerCase().split("\\W+");
				out.collect(new Tuple2<String, Long>(tokens[0], Long.valueOf(tokens[1])));
			}
		}).keyBy(0).flatMap(new OperatorStateDemo(5)).print();
		env.execute(OperatorStateDemo.class.getSimpleName());
	}

	@Override
	public void flatMap(Tuple2<String, Long> value, Collector<List<Tuple2<String, Long>>> out) throws Exception {
		this.bufferedElements.add(value);
		if (this.bufferedElements.size() == threshold) {
			out.collect(bufferedElements);
			this.bufferedElements.clear();
		}
	}

	/**
	 * 进行checkpoint快照
	 * 
	 * @param context
	 * @throws Exception
	 */
	@Override
	public void snapshotState(FunctionSnapshotContext context) throws Exception {
		checkpointedState.clear();
		for (Tuple2<String, Long> element : bufferedElements) {
			checkpointedState.add(element);
		}
	}

	@Override
	public void initializeState(FunctionInitializationContext context) throws Exception {
		ListStateDescriptor<Tuple2<String, Long>> listStateDescriptor = new ListStateDescriptor("listState",
				TypeInformation.of(new TypeHint<Tuple2<String, Long>>() {
				}));
		checkpointedState = context.getOperatorStateStore().getListState(listStateDescriptor);
		// 如果是故障恢复
		if (context.isRestored()) {
			for (Tuple2<String, Long> element : checkpointedState.get()) {
				bufferedElements.add(element);
			}

			checkpointedState.clear();
		}
	}

}
