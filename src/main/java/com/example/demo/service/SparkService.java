package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.GeoDto;
import com.example.demo.dto.TaxAuctionDto;
import com.example.demo.repo.TaxAuctionRepo;

import scala.Tuple2;

@Service
public class SparkService {

	@Autowired
	JavaSparkContext sc;

	@Autowired
	SparkSession spark;

	@Autowired
	DOMservice service;

	@Autowired
	TaxAuctionRepo repo;

	public List<TaxAuctionDto> sparkService() {
		RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://nominatim.openstreetmap.org/search?format=json&q={address}";
		String inputString = pdfExtract();
		List<String> inputList = Arrays.asList(inputString.split("PARCEL "));
		String[] header = inputList.get(0).split("\r\n");
		header[1] = header[1].substring(1, header[1].length() - 2);
		JavaRDD<String> inputRDD = sc.parallelize(inputList);

		JavaRDD<Map<String, String>> filteredRDD = inputRDD.map(line -> line.replaceAll(header[0], ""))
				.map(line -> line.replaceAll(header[1], "")).filter(line -> !line.contains("REDEEMED ")).filter(line -> !line.contains("DROPPED"))
				.filter(line -> !line.contains("REDEMPTION")).map(line -> line.replaceAll("PIN:", ":PIN:"))
				.map(line -> line.replaceAll("Geocode:", ":Geocode:"))
				.map(line -> line.replaceAll("Legal Description:", ":Legal Description:"))
				.map(line -> line.replaceAll("Approximate Location:", ":Approximate Location:"))
				.map(line -> line.replaceAll("Delinquent Years:", ":Delinquent Years:"))
				.map(line -> line.replaceAll("Redemption Costs:", ":Redemption Costs:"))
				.map(line -> line.replaceAll("Owners of Record:", ":Owners of Record:"))
				.map(line -> line.replaceAll("\r\n \r\n()", "")).map(line -> line.replaceAll("\r\n", ""))
				.map(line -> line.replace('(', ' ')).map(line -> line.replace(')', ' ')).map(line -> {
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

		List<String> results = new ArrayList<>();
		for (Map<String, String> e : filteredString) {
			if (!e.isEmpty()) {
				String data = service.extractDOM(e.get("PIN"));

				results.add(data);

			}

		}

		JavaRDD<String> domRDD = sc.parallelize(results);

		JavaRDD<Map<String, String>> enrichRDD = domRDD.map(line -> {

			Map<String, String> map = new HashMap<>();

			Document doc = Jsoup.parse(line);

			Elements tables = doc.select("table.dense.compactTable.compact");

			// Create a map to store the key-value pairs

			// Iterate over the tables
			for (Element table : tables) {
				Elements rows = table.select("tbody tr");

				// Iterate over the rows and extract the key-value pairs
				for (Element row : rows) {
					Element keyElement = row.selectFirst("th");
					Element valueElement = row.selectFirst("td");

					if (keyElement != null && valueElement != null) {
						String key = keyElement.text();
						String value = valueElement.text();

						if (key.equals("More Details")) {
							Element linkElement = valueElement.selectFirst("a");
							if (linkElement != null) {
								String hyperlink = linkElement.attr("href");
								value = hyperlink;
							}
						}

						map.put(key, value);
					}
				}
			}

			return map;
		});

		JavaRDD<Map<String, String>> combinedRDD = enrichRDD.union(filteredRDD).mapToPair(map -> {
			String key = map.get("PIN"); // Replace "key" with the actual key in your map
			return new Tuple2<>(key, map);
		}).reduceByKey((map1, map2) -> {
			map2.putAll(map1);
			return map2;
		}).map(tuple -> tuple._2).filter(map -> map != null).filter(map -> !map.isEmpty());

		List<TaxAuctionDto> records = new ArrayList();

		for (Map<String, String> data : combinedRDD.collect()) {

			if (!data.isEmpty()) {
				TaxAuctionDto dto = new TaxAuctionDto();
				dto.setOwner(data.getOrDefault("Owner", null));
				dto.setAssessment2023(parseInteger(removeCurrencySymbol(data.getOrDefault("2023 Assessment", null))));
				dto.setHalfBaths(data.getOrDefault("Half Baths", null));

				dto.setLandUse(data.getOrDefault("Land Use", null));
				dto.setBasementSqFt(parseInteger(removeCurrencySymbol(data.getOrDefault("Basement Sq. Ft.", null))));
				dto.setMailingAddress(data.getOrDefault("Mailing Address", null));
				dto.setYearBuilt(parseInteger(data.getOrDefault("Year Built", null)));
				dto.setFinishedBasementSqFt(parseInteger(removeCurrencySymbol(data.getOrDefault("Finished Basement Sq. Ft.", null))));
				dto.setTaxUnit(data.getOrDefault("Tax Unit", null));
				
				dto.setBasementType(data.getOrDefault("Basement Type", null));
				dto.setArchitecturalStyle(data.getOrDefault("Architectural Style", null));
				dto.setCondition(data.getOrDefault("Condition", null));
				String address = data.getOrDefault("Mailing Address", null);
				
				GeoDto[] obj = restTemplate.getForObject(apiUrl, GeoDto[].class, formatAddress(address));
				String[] arr = new String[2];
				if(obj.length > 0) {
					 arr[0] =obj[0].getLat();
					 arr[1] = obj[0].getLon();
				}
				
				
				
				dto.setGeoCode(arr);
				dto.setNumber(data.getOrDefault("NUMBER", null));
				dto.setAppraisal2023(parseInteger(removeCurrencySymbol(data.getOrDefault("2023 Appraisal", null))));
				dto.setAin(data.getOrDefault("AIN", null));
				dto.setOwnersOfRecord(data.getOrDefault("Owners of Record", null));
				dto.setFullBaths(data.getOrDefault("Full Baths", null));
				dto.setRedemptionCosts(parseFloat(data.getOrDefault("Redemption Costs", "$999.1").replace("$", "").replace(",", "")));
				dto.setLivingSqFt(parseInteger(removeCurrencySymbol(data.getOrDefault("Living Sq. Ft.", null))));
				
				dto.setPin(data.getOrDefault("PIN", null));
				
				dto.setBedrooms(data.getOrDefault("Bedrooms", null));
				dto.setMarketLandSquareFeet(data.getOrDefault("Market Land Square Feet", null));
				dto.setTotalAcres2023(data.getOrDefault("2023 Total Acres", null));
				dto.setDelinquentYears(data.getOrDefault("Delinquent Years", null));
				dto.setMoreDetails(data.getOrDefault("More Details", null));
				
				records.add(dto);

			}

		}

		repo.saveAll(records);

		return records;
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

	public Integer parseInteger(String value) {
		if (value != null && !value.isEmpty()) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				// Handle parsing exception, if required
			}
		}
		return null;
	}

	public float parseFloat(String value) {
		
		
		if(value != null && value.chars().filter(ch -> ch == '.').count() > 1) {
			value = value.replaceFirst("\\.", "");
			System.out.println(value);
		}
		
		
		if (value != null && !value.isEmpty()) {
			try {
				
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				// Handle parsing exception, if require
				
				
				
			}
		}
		return Float.parseFloat("10");
	}

	public String removeCurrencySymbol(String value) {
		if (value != null) {
			return value.replace("$", "").replace(",", "");
		}
		return value;
	}
	public String formatAddress(String originalAddress) {
        int lastSpaceIndex = originalAddress.lastIndexOf(" ");
        if (lastSpaceIndex != -1) {
            String formattedAddress = originalAddress.substring(0, lastSpaceIndex);
            return formattedAddress;
        } else {
            return originalAddress; // Return original if no space found
        }
    }

}