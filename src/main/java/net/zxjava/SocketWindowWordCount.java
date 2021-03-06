package net.zxjava;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class SocketWindowWordCount {

	public static void main(String[] args) throws Exception {
		// get the execution environment
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// get input data by connecting to the socket
		DataStream<String> text = env.socketTextStream("localhost", 7777, "\n");
		// parse the data, group it, window it, and aggregate the counts
		DataStream<WordWithCount> windowCounts = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
			private static final long serialVersionUID = -8965776461114876790L;

			@Override
			public void flatMap(String value, Collector<WordWithCount> out) {
				for (String word : value.split("\\s")) {
					out.collect(new WordWithCount(word, 1L));
				}
			}
		}).keyBy("word").timeWindow(Time.seconds(15), Time.seconds(1)).reduce(new ReduceFunction<WordWithCount>() {
			private static final long serialVersionUID = -3430643989764185350L;

			@Override
			public WordWithCount reduce(WordWithCount a, WordWithCount b) {
				return new WordWithCount(a.word, a.count + b.count);
			}
		});
		// print the results with a single thread, rather than in parallel
		windowCounts.print();
		env.execute("Socket Window WordCount");
	}

	// Data type for words with count
	public static class WordWithCount {

		public String word;
		public long count;

		public WordWithCount() {
		}

		public WordWithCount(String word, long count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public String toString() {
			return word + " : " + count;
		}
	}
}