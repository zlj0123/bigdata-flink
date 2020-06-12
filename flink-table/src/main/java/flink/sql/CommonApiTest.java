package flink.sql;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;

public class CommonApiTest {
    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.createCollectionsEnvironment();
        BatchTableEnvironment tableEnv = BatchTableEnvironment.create(env);

        //1.From DataSet
        DataSet<String> input = env.readTextFile("inout/order.csv");
        input.print();

        DataSet<Tuple4<String, String, Double, String>> orderDataSet = input.map(new MapFunction<String, Tuple4<String, String, Double, String>>() {
            @Override
            public Tuple4<String, String, Double, String> map(String s) throws Exception {
                String[] split = s.split(",");
                return new Tuple4<String, String, Double, String>(String.valueOf(split[0]),
                        String.valueOf(split[1]),
                        Double.valueOf(split[2]),
                        String.valueOf(split[3])
                );
            }
        });

        tableEnv.createTemporaryView("Orders", orderDataSet, "cID,cName,revenue,cCountry");


        // scan registered Orders table
        Table orders = tableEnv.from("Orders");
        // compute revenue for all customers from France
        Table revenue = orders
                .filter("cCountry === 'FRANCE'")
                .groupBy("cID, cName")
                .select("cID, cName, revenue.sum AS revSum");


        revenue.printSchema();
    }

    public static class Order {
        public String cID;
        public String cName;
        public Double revenue;
        public String cCountry;

        public Order(String cID, String cName, Double revenue, String cCountry) {
            this.cID = cID;
            this.cName = cName;
            this.revenue = revenue;
            this.cCountry = cCountry;
        }
    }
}
