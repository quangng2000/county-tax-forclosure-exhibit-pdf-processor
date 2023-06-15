package com.example.demo.config;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

	@Value("${spark.app.name}")
	private String appName;
	@Value("${spark.master}")
	private String masterUri;
	//@Value("${spark.home}")
	//private String sparkHome;

	@Bean
	public SparkConf conf() {
		return new SparkConf().setAppName("startingSpark").setMaster("local[*]");
	}

	@Bean
	public JavaSparkContext sc() {
		return new JavaSparkContext(conf());
	}
	
	@Bean
    public SparkSession sparkSession() {
         // Set the Spark master URL as per your requirements

        return SparkSession.builder().appName("testingSql").master("local[*]")
                .config("spark.sql.warehouse.dir","file:///c:/tmp/")
                .getOrCreate();
    }

}
