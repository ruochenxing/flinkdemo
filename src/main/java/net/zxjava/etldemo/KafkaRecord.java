package net.zxjava.etldemo;

import com.alibaba.fastjson.JSONObject;

public class KafkaRecord {
	public String key;
	public JSONObject data;
	public long offset;
	public int partition;
	public String topic;

	// region getter/setter

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String toString() {
		return "toString: " + JSONObject.toJSONString(this);
	}
	// endregion
}
