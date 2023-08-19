package com.example.demo.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxAuctionDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String owner;
    private Integer assessment2023;
    private String halfBaths;
    private String[] geoCode;
    private String landUse;
    private Integer basementSqFt;
    private String mailingAddress;
    private Integer yearBuilt;
    private Integer finishedBasementSqFt;
    private String taxUnit;
    
    private String basementType;
    private String architecturalStyle;
    private String condition;
    private String number;
    private Integer appraisal2023;
    private String ain;
    private String ownersOfRecord;
    private String fullBaths;
    
    
    private float  redemptionCosts;
    
    private Integer livingSqFt;
    
    private String pin;
    
    private String bedrooms;
    private String marketLandSquareFeet;
    private String totalAcres2023;
    private String delinquentYears;
    private String moreDetails;
    
}
