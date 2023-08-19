package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.GeoDto;
import com.example.demo.dto.TaxAuctionDto;
import com.example.demo.service.SparkService;
import com.example.demo.service.WebScrappingService;

@RestController
public class SparkController {

	@Autowired
	SparkService service;
	
	@Autowired
	WebScrappingService webService;

	
	
	@RequestMapping(method = RequestMethod.GET, path = "/api")
	public List<TaxAuctionDto> sqlController() {
        
		

        return service.sparkService();
    }
	@RequestMapping(method = RequestMethod.GET, path = "/extract")
	public String extractController() {
        
		

        return webService.webScrapping();
    }
	
	
	

}
