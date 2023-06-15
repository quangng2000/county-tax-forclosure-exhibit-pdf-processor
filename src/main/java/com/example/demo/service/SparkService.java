package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SparkService {

	@Autowired
	JavaSparkContext sc;
	
	@Autowired
	SparkSession spark;

	
	public List<Map<String, String>> sparkService() {
		String inputString = pdfExtract();
		JavaRDD<String> inputRDD = sc.parallelize(Arrays.asList(inputString.split("PARCEL ")));

		JavaRDD<Map<String, String>> filteredRDD = inputRDD.map(line -> line.replaceAll("Exhibit A Case 23CV163", ""))
											  .map(line -> line.replaceAll("(Updated 6/5/2023)", ""))
        		                              .filter(line -> !line.contains("REDEEMED "))
        		                              .filter(line -> !line.contains("REDEMPTION"))
        		                              .map(line -> line.replaceAll("PIN:", ":PIN:"))
        		                              .map(line -> line.replaceAll("Geocode:", ":Geocode:"))
        		                              .map(line -> line.replaceAll("Legal Description:", ":Legal Description:"))
        		                              .map(line -> line.replaceAll("Approximate Location:", ":Approximate Location:"))
        		                              .map(line -> line.replaceAll("Delinquent Years:", ":Delinquent Years:"))
        		                              .map(line -> line.replaceAll("Redemption Costs:", ":Redemption Costs:"))
        		                              .map(line -> line.replaceAll("Owners of Record:", ":Owners of Record:"))
        		                              .map(line -> line.replaceAll("\r\n \r\n()",""))
        		                              .map(line -> line.replaceAll("\r\n",""))
        		                              .map(line -> line.replace('(', ' '))
        		                              .map(line -> line.replace(')', ' '))
        		                              .map(line -> {
        		                            	  Map<String, String> data = new HashMap<>();
        		                            	  String[] tokens = line.split(":");
        		                            	  String key = null;
        		                                  for (String token : tokens) {
        		                                      String trimed = token.trim();
        		                                      if (key == null) {
        		                                          key = trimed;
        		                                      } else {
        		                                          String value = trimed;
        		                                          data.put(key, value);
        		                                          key = null;
        		                                      }
        		                                  }

        		                                  return data;
        		                              });
        		                              
        List<Map<String, String>> filteredString = filteredRDD.collect();
		
        sc.stop();
		return filteredString;
	}
	
	public String pdfExtract() {
		try (PDDocument document = PDDocument.load(new File("src/main/resources/data.pdf"))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
	}

}
