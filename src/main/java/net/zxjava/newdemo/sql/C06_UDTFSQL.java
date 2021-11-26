package net.zxjava.newdemo.sql;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

public class C06_UDTFSQL {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        DataStreamSource<String> socketTextStream = env.socketTextStream("localhost", 7777);

        // hello tom jerry tom
        tableEnv.registerDataStream("t_lines", socketTextStream, "line");

        tableEnv.registerFunction("split", new C06_Split("\\W+"));

        //Table table = tableEnv.sqlQuery(
        //        "SELECT word, line FROM t_lines, LATERAL TABLE(split(line)) as T(word)");

        // 左表关联右表
        Table table = tableEnv.sqlQuery(
                "SELECT word FROM t_lines, LATERAL TABLE(split(line)) as T(word)");

        tableEnv.toAppendStream(table, Row.class).print();

        env.execute("C06_UDTFSQL");
    }
}
