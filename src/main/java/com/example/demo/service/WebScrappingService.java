//package com.example.demo.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.example.demo.dto.GeoDto;
//
//@Service
//public class WebScrappingService {
//	
//	
//	public String webScrapping() {
//		RestTemplate restTemplate = new RestTemplate();
//        String apiUrl = "https://nominatim.openstreetmap.org/search?format=json&q={address}";
//        String address =formatAddress("11001 E MOUNT VERNON CT WICHITA KS 67207-7712");
//        GeoDto[] obj = restTemplate.getForObject(apiUrl, GeoDto[].class, address);
//		
//		
//		
//		
//		return obj[0].getLat();
//	}
//	
//	public String formatAddress(String originalAddress) {
//        int lastSpaceIndex = originalAddress.lastIndexOf(" ");
//        if (lastSpaceIndex != -1) {
//            String formattedAddress = originalAddress.substring(0, lastSpaceIndex);
//            return formattedAddress;
//        } else {
//            return originalAddress; // Return original if no space found
//        }
//    }
//	
//}
