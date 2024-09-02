package com.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchCriteria {
    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("productName")
    private String productName;

    @JsonProperty("minTimes")
    private Integer minTimes;

    @JsonProperty("minExpenses")
    private Double minExpenses;

    @JsonProperty("maxExpenses")
    private Double maxExpenses;

    @JsonProperty("badCustomers")
    private Integer badCustomers;


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getMinTimes() {
        return minTimes;
    }

    public void setMinTimes(Integer minTimes) {
        this.minTimes = minTimes;
    }

    public Double getMinExpenses() {
        return minExpenses;
    }

    public void setMinExpenses(Double minExpenses) {
        this.minExpenses = minExpenses;
    }

    public Double getMaxExpenses() {
        return maxExpenses;
    }

    public void setMaxExpenses(Double maxExpenses) {
        this.maxExpenses = maxExpenses;
    }

    public Integer getBadCustomers() {
        return badCustomers;
    }

    public void setBadCustomers(Integer badCustomers) {
        this.badCustomers = badCustomers;
    }
}