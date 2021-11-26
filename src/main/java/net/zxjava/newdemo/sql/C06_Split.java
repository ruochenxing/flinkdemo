package net.zxjava.newdemo.sql;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.table.functions.TableFunction;
import org.apache.flink.types.Row;

public class C06_Split extends TableFunction<Row> {

	private static final long serialVersionUID = 8136262059740712143L;
	private String separator = ",";

	public C06_Split(String separator) {
		this.separator = separator;
	}

	public void eval(String line) {
		for (String s : line.split(separator)) {
			collect(Row.of(s));
		}
	}

	@Override
	public TypeInformation<Row> getResultType() {
		return Types.ROW(Types.STRING);
	}
}
