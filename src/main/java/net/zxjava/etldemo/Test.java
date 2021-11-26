package net.zxjava.etldemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

public class Test {

	public static void main(String[] args) {
		String str = "[{\"data\":{\"user_id\":1,\"coupons\":[{\"activity_day\":1,\"coupon_id\":452,\"create_at\":1575613133185,\"expire_at\":1575613133185,\"is_txj\":0}],\"current_sign_in\":1575613133185},\"key\":\"8a8540312439452998cc91afaca7214c\",\"offset\":-936213776,\"partition\":1,\"topic\":\"topic_kafka\"},{\"data\":{\"user_id\":1,\"coupons\":[{\"activity_day\":1,\"coupon_id\":992,\"create_at\":1575613133702,\"expire_at\":1575613133702,\"is_txj\":0}],\"current_sign_in\":1575613133702},\"key\":\"9894bb1aa12b453f91c9cca84ba6262e\",\"offset\":-1760868279,\"partition\":1,\"topic\":\"topic_kafka\"}]";
		List<KafkaRecord> records = JSONObject.parseArray(str, KafkaRecord.class);
		if (records.size() == 1) {
			KafkaRecord newRecord = records.get(0);
			System.out.println(JSONObject.toJSONString(newRecord));
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
				if (oldData != null && oldData.isEmpty()) {
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
			System.out.println(JSONObject.toJSONString(newRecord));
		}
	}
}
