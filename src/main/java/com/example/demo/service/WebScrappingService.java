package com.example.demo.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
	
	public String htmlService() {
		
		try (WebClient webClient = new WebClient()) {
            // Disable JavaScript to speed up page loading (optional)
            webClient.getOptions().setJavaScriptEnabled(false);

            // Fetch the web page
            webClient.getOptions().setJavaScriptEnabled(false);

            // Fetch the web page
            HtmlPage page = webClient.getPage("https://ssc.sedgwickcounty.org/propertytax/disclaimer.aspx?returnURL=/propertytax/realproperty.aspx%3fpin%3d00169581");

            HtmlForm form = null;
            for (HtmlForm htmlForm : page.getForms()) {
                if (htmlForm.getActionAttribute().contains("disclaimer.aspx")) {
                    form = htmlForm;
                    break;
                }
            }
            String result = new String();
            // Check if the form is found
            if (form != null) {
                // Find the submit button by iterating through the form's child elements
                HtmlInput submitButton = null;
                for (DomElement element : form.getChildElements()) {
                	
                	if(!element.getAttribute("style").isEmpty()) {
                		for(DomElement e: element.getChildElements()) {
                			if (e instanceof HtmlInput) {
                                HtmlInput input = (HtmlInput) e;
                                if (input.getId().equals("ctl00_mainContentPlaceHolder_acceptButton")) {
                                    submitButton = input;
                                    break;
                                }
                            }
                			
                		}
                		
                		
                	}
                
                    
                }

                // Submit the form
                if (submitButton != null) {
                    HtmlPage resultPage = submitButton.click();

                    // Process the result page as needed
                     result = resultPage.asXml();
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Submit button not found");
                }
            } else {
                System.out.println("Form not found");
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            
            return "";
        }
		
	}
	
}
