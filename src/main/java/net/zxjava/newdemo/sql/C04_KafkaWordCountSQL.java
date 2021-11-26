package net.zxjava.newdemo.sql;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.table.descriptors.Json;
import org.apache.flink.table.descriptors.Kafka;
import org.apache.flink.table.descriptors.Schema;
import org.apache.flink.types.Row;

public class C04_KafkaWordCountSQL {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);

        tEnv.connect(new Kafka()
                .version("universal")
                .topic("json-input")
                .startFromEarliest()
                .property("bootstrap.servers", "localhost:9092")
        ).withFormat(new Json().deriveSchema()).withSchema(new Schema()
            .field("name", DataTypes.STRING())
                .field("gender", DataTypes.STRING())
        ).inAppendMode().registerTableSource("kafkaSource");

        Table result = tEnv.scan("kafkaSource")
                .groupBy("gender")
                .select("gender, count(1) as counts");

        tEnv.toRetractStream(result, Row.class).print();

        env.execute("C04_KafkaWordCountSQL");
    }
}
