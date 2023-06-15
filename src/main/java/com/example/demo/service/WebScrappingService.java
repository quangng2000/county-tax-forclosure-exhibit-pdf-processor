package com.example.demo.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class WebScrappingService {
	
	
	public Map<String, String> webScrapping() {
		String url = "https://www.countyoffice.org/property-records-search/?q=1014-s-rue-st-wichita-ks-67207-c39";
		Map<String, String> characteristics = new HashMap<>();
		try {
			 
			
			Document doc = Jsoup.connect(url).get();

	        // Find the table with class "dense compactTable compact" and get the rows
			Elements tables = doc.select("table.table.table-striped.property-details");

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
			            characteristics.put(key, value);
			        }
			    }
			}
	        
	        
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return characteristics; 
	}

}
