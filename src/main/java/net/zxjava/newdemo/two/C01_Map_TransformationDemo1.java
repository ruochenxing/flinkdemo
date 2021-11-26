package net.zxjava.newdemo.two;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * map
 * <p>
 * 对DataStream进行操作，返回一个新的DataStream
 */
public class C01_Map_TransformationDemo1 {
	public static void main(String[] args) throws Exception {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<Integer> nums = env.fromElements(1, 2, 3, 4, 5);

		// 方式一：使用匿名内部类
		// map方法是一个Transformation，功能：做映射。按tab键，会自动补全
		/*
		 * SingleOutputStreamOperator<Integer> res = nums.map(new MapFunction<Integer,
		 * Integer>() {
		 * 
		 * @Override public Integer map(Integer integer) throws Exception { return
		 * integer * 2; } });
		 */
		// 方式二：使用lambda表达式
		// SingleOutputStreamOperator<Integer> res = nums.map(i -> i *
		// 2).returns(Integer.class);
		SingleOutputStreamOperator<Integer> res = nums.map(i -> i * 2);
		// 方式三：传入功能更加强大的RichMapFunction
		// 使用RichXXX_Function，里面含有open，close方法，比如后续读取数据库的前后操作就可以使用open，close
		SingleOutputStreamOperator<Integer> map = nums.map(new RichMapFunction<Integer, Integer>() {
			private static final long serialVersionUID = -7224085399545126961L;

			// open，在构造方法之后，map方法执行之前，执行一次，Configuration可以拿到全局配置
			// 用来初始化一下连接，或者初始化或恢复state
			@Override
			public void open(Configuration parameters) throws Exception {
				super.open(parameters);
			}

			// 销毁之前，执行一次，通常是做资源释放
			@Override
			public void close() throws Exception {
				super.close();
			}

			@Override
			public Integer map(Integer integer) throws Exception {
				return integer * 10;
			}
			// close
		});
		// Sink
		res.print();
		System.out.println("==================================");
		map.print();
		env.execute("C01_TransformationDemo1");
	}
}
