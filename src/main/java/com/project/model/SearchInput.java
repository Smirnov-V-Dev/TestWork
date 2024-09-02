package com.project.model;

import java.util.List;

public class SearchInput {
    private List<SearchCriteria> criterias;


    public List<SearchCriteria> getCriterias() {
        return criterias;
    }

    public void setCriterias(List<SearchCriteria> criterias) {
        this.criterias = criterias;
    }
}