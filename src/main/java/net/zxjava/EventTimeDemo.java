package net.zxjava;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class EventTimeDemo {

	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);
		// 这里我采用eventTime
		// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		DataStream<Student> data = env.addSource(new SourceFunction<Student>() {

			private static final long serialVersionUID = 9216577342065994290L;

			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<Student> ctx) throws Exception {
				int count = 0;
				int random = 0;
				while (isRunning) {
					Student s = new Student("name", System.currentTimeMillis() - random * 1000, 1, random);
					ctx.collect(s);
					count++;
					Thread.sleep(1000);
					if (count % 5 == 0) {
						random = new Random().nextInt(10);
					} else {
						random = 0;
					}
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}

		});

		DataStream<Student> streamResult = data
				.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<Student>() {// 定时提取watermark
																								// 这个具体由ExecutionConfig.setAutoWatermarkInterval设置
					private static final long serialVersionUID = 6287140203044279639L;
					private final long maxTimeLag = 7000L; // 延迟时间
					private long currentMaxTimestamp;

					// extractTimestamp方法，在每一个event到来之后就会被调用，这里其实就是为了设置watermark的值
					@Override
					public long extractTimestamp(Student element, long previousElementTimestamp) {
						long timestamp = element.getTime();// event time
						// currentMaxTimestamp是截至当前所有的event time的最大值
//		 在水印的处理中，我们一般取事件时间序列的最大值作为水印的生成时间参考。
						currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
						SimpleDateFormat format = new SimpleDateFormat("mm:ss.SSS");
						System.out.println(
								element + ", watermark=" + format.format(new Date(currentMaxTimestamp - maxTimeLag)));
						return timestamp;
					}

					// 而getCurrentWatermarker会被定时调用
					@Override
					public Watermark getCurrentWatermark() {
						return new Watermark(currentMaxTimestamp - maxTimeLag);
					}
				});
		data.keyBy(new KeySelector<Student, String>() {

			private static final long serialVersionUID = -3134212429010728114L;

			@Override
			public String getKey(Student value) throws Exception {
				return value.getName();
			}

			// window根据自然时间进行划分，[2019-11-28 00:00:00,2019-11-28 00:00:10),...,[2019-11-28
			// 18:53:00,2019-11-28 18:53:10)，[2019-11-28 18:53:10,2019-11-28 18:53:20)...

			// 当一个event time=2019-11-28 00:00:01的消息到来时，就会生成[2019-11-28 00:00:00,2019-11-28
			// 00:00:10)这个窗口(而不是[2019-11-28 00:00:01,2019-11-28 00:00:11)这样的窗口)
			// 当watermark(可以翻译成水位线，只会不断上升，不会下降，用来保证在水位线之前的数据一定都到达)超过窗口的endTime时，触发窗口计算
		}).timeWindow(Time.seconds(10)).apply(new WindowFunction<Student, List<Student>, String, TimeWindow>() {

			private static final long serialVersionUID = -480515632261745005L;

			@Override
			public void apply(String key, TimeWindow window, Iterable<Student> input, Collector<List<Student>> out)
					throws Exception {
				List<Student> result = new ArrayList<>();
				input.forEach(s -> result.add(s));
				System.out.println("----->" + result.toString());
				out.collect(result);
			}
		}).print();
		env.execute();
	}

	public static class Student {
		private String name;
		private long time;
		private int count;
		private int random;

		// 这个必须要有
		public Student() {
		}

		public Student(String name, long time, int count, int random) {
			this.name = name;
			this.time = time;
			this.count = count;
			this.random = random;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getRandom() {
			return random;
		}

		public void setRandom(int random) {
			this.random = random;
		}

		@Override
		public String toString() {
			return "Student [name=" + name + ", time=" + new SimpleDateFormat("mm:ss.SSS").format(new Date(time))
					+ ", count=" + count + ", random=" + random + "]["
					+ new SimpleDateFormat("mm:ss.SSS").format(new Date()) + "]";
		}
	}
}
