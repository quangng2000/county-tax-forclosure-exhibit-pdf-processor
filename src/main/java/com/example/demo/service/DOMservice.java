package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Service
public class DOMservice {

	public String extractDOM(String PIN) {
		String result = new String();

		try (WebClient webClient = new WebClient()) {

			// Fetch the web page
			webClient.getOptions().setJavaScriptEnabled(false);
			String baseURL = "https://ssc.sedgwickcounty.org/propertytax/disclaimer.aspx?returnURL=/propertytax/realproperty.aspx%3fpin%3d";

			// Fetch the web page
			HtmlPage page = webClient.getPage(baseURL + PIN);

			HtmlForm form = null;
			for (HtmlForm htmlForm : page.getForms()) {
				if (htmlForm.getActionAttribute().contains("disclaimer.aspx")) {
					form = htmlForm;
					break;
				}
			}
			
			// Check if the form is found
			if (form != null) {
				// Find the submit button by iterating through the form's child elements
				HtmlInput submitButton = null;
				for (DomElement element : form.getChildElements()) {

					if (!element.getAttribute("style").isEmpty()) {
						for (DomElement e : element.getChildElements()) {
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
					
					
				} else {
					System.out.println("Submit button not found");
				}
			} else {
				System.out.println("Form not found");
			}

			

		} catch (Exception e) {
			e.printStackTrace();

		}
		return result;

	}
	

}
