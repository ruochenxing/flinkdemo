package net.zxjava.etldemo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.alibaba.fastjson.JSONObject;

public class EtlTest {

	public static void main(String[] args) {
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setParallelism(1);
		// 这里我采用eventTime
		// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		DataStream<KafkaRecord> data = env.addSource(new SourceFunction<KafkaRecord>() {

			private static final long serialVersionUID = 2259912872342956983L;

			private volatile boolean isRunning = true;

			@Override
			public void run(SourceContext<KafkaRecord> ctx) throws Exception {
				while (isRunning) {
					KafkaRecord r = new KafkaRecord();
					r.key = UUID.randomUUID().toString().replace("-", "");
					r.offset = new Random().nextInt();
					r.partition = 1;
					r.topic = "topic_kafka";
					JSONObject json = new JSONObject();
					json.put("user_id", new Random().nextInt(10));
					json.put("current_sign_in", new Date());// 最近登录时间
					List<CouponActivityDayValue> coupons = new ArrayList<>();
					CouponActivityDayValue coupon = new CouponActivityDayValue();
					coupon.setActivity_day(1);
					coupon.setCoupon_id(new Random().nextInt(1000));
					coupon.setCreate_at(new Date());
					coupon.setExpire_at(new Date());
					coupon.setIs_txj(0);
					coupons.add(coupon);
					json.put("coupons", coupons);
					r.data = json;
					ctx.collect(r);
					Thread.sleep(500);
				}
			}

			@Override
			public void cancel() {
				isRunning = false;
			}

		});

		// 添加sink
		SingleOutputStreamOperator<KafkaRecord> dataStream = data.keyBy(new KeySelector<KafkaRecord, String>() {

			private static final long serialVersionUID = -6152605669583329383L;

			@Override
			public String getKey(KafkaRecord value) throws Exception {
				JSONObject json = value.data;
				long user_id = json.getLongValue("user_id");
				return String.valueOf(user_id);
			}

		}).timeWindow(Time.seconds(15)).apply(new WindowFunction<KafkaRecord, KafkaRecord, String, TimeWindow>() {

			private static final long serialVersionUID = -6152605669583329383L;

			@Override
			public void apply(String key, TimeWindow window, Iterable<KafkaRecord> input, Collector<KafkaRecord> out)
					throws Exception {
				ArrayList<KafkaRecord> records = Lists.newArrayList(input.iterator());
				System.out.println("records = " + JSONObject.toJSONString(records));
				if (records.size() == 1) {
					KafkaRecord newRecord = records.get(0);
					out.collect(newRecord);
				} else if (records.size() > 1) {
					KafkaRecord newRecord = new KafkaRecord();
					JSONObject newData = new JSONObject();
					Set<Long> coupon_ids = new HashSet<>();
					List<CouponActivityDayValue> newCoupons = new ArrayList<>();
					for (KafkaRecord r : records) {
						newRecord.key = r.key;
						newRecord.topic = r.topic;
						newRecord.offset = r.offset;
						newRecord.partition = r.partition;
						JSONObject oldData = r.data;
						if (oldData != null && !oldData.isEmpty()) {
							if (oldData.containsKey("coupons")) {
								// 旧coupons
								List<CouponActivityDayValue> rCoupons = JSONObject.parseArray(
										oldData.getJSONArray("coupons").toJSONString(), CouponActivityDayValue.class);
								if (rCoupons != null && !rCoupons.isEmpty()) {
									newCoupons.addAll(rCoupons);
								}
								newData.putAll(oldData.getInnerMap());
							} else {
								newData.putAll(oldData.getInnerMap());
							}
						}
					}
					if (!newCoupons.isEmpty()) {
						List<CouponActivityDayValue> nCoupons = newCoupons.stream()
								.filter(c -> coupon_ids.add(c.getCoupon_id())).collect(Collectors.toList());
						newData.put("coupons", nCoupons);
					}
					newRecord.data = newData;
					out.collect(newRecord);
				}
			}
		});
		dataStream.print();
		try {
			env.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
