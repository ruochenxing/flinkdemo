package net.zxjava.etldemo;

import java.util.Date;

public class CouponActivityDayValue {

	private long coupon_id;// 代金券ID
	private int activity_day;// 激活剩余天数(有效日)
	private int is_txj = 0;// 是否是淘新机的代金券
	private Date create_at;// 领券时间
	private Date expire_at;// 过期时间

	public long getCoupon_id() {
		return coupon_id;
	}

	public void setCoupon_id(long coupon_id) {
		this.coupon_id = coupon_id;
	}

	public int getActivity_day() {
		return activity_day;
	}

	public void setActivity_day(int activity_day) {
		this.activity_day = activity_day;
	}

	public int getIs_txj() {
		return is_txj;
	}

	public void setIs_txj(int is_txj) {
		this.is_txj = is_txj;
	}

	public Date getCreate_at() {
		return create_at;
	}

	public void setCreate_at(Date create_at) {
		this.create_at = create_at;
	}

	public Date getExpire_at() {
		return expire_at;
	}

	public void setExpire_at(Date expire_at) {
		this.expire_at = expire_at;
	}
}
