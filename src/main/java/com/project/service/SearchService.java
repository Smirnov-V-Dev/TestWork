package com.project.service;

import com.project.model.Customer;
import com.project.model.SearchCriteria;
import com.project.model.SearchInput;
import com.project.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchService {
    private final DatabaseConfig databaseConfig;
    public SearchService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }



    public Object executeSearch(SearchInput searchInput) throws SQLException {
        List<CriteriaResult> results = new ArrayList<>();

        try (Connection conn = databaseConfig.getConnection()) {
            for (SearchCriteria criteria : searchInput.getCriterias()) {
                if (criteria.getLastName() != null) {
                    List<Customer> customers = searchByLastName(conn, criteria.getLastName());
                    results.add(new CriteriaResult(criteria, customers));
                } else if (criteria.getProductName() != null && criteria.getMinTimes() != null) {
                    List<Customer> customers = searchByProductAndMinTimes(conn, criteria.getProductName(), criteria.getMinTimes());
                    results.add(new CriteriaResult(criteria, customers));
                } else if (criteria.getMinExpenses() != null && criteria.getMaxExpenses() != null) {
                    List<Customer> customers = searchByExpensesRange(conn, criteria.getMinExpenses(), criteria.getMaxExpenses());
                    results.add(new CriteriaResult(criteria, customers));
                } else if (criteria.getBadCustomers() != null) {
                    List<Customer> customers = searchBadCustomers(conn, criteria.getBadCustomers());
                    results.add(new CriteriaResult(criteria, customers));
                } else {
                    // Неизвестный критерий
                    throw new IllegalArgumentException("Неизвестный критерий поиска: " + criteria);
                }
            }
        }

        return new SearchResult("search", results);
    }

    private List<Customer> searchByLastName(Connection conn, String lastName) throws SQLException {
        String query = "SELECT first_name, last_name FROM customers WHERE last_name = ?";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, lastName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(rs.getString("first_name"), rs.getString("last_name")));
                }
            }
        }
        return customers;
    }

    private List<Customer> searchByProductAndMinTimes(Connection conn, String productName, int minTimes) throws SQLException {
        String query = "SELECT c.first_name, c.last_name " +
                "FROM customers c " +
                "JOIN purchases p ON c.id = p.customer_id " +
                "JOIN products pr ON p.product_id = pr.id " +
                "WHERE pr.name = ? " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "HAVING COUNT(p.id) >= ?";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productName);
            stmt.setInt(2, minTimes);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(rs.getString("first_name"), rs.getString("last_name")));
                }
            }
        }
        return customers;
    }

    private List<Customer> searchByExpensesRange(Connection conn, double minExpenses, double maxExpenses) throws SQLException {
        String query = "SELECT c.first_name, c.last_name " +
                "FROM customers c " +
                "JOIN purchases p ON c.id = p.customer_id " +
                "JOIN products pr ON p.product_id = pr.id " +
                "GROUP BY c.id, c.first_name, c.last_name " +
                "HAVING SUM(pr.price) BETWEEN ? AND ?";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, minExpenses);
            stmt.setDouble(2, maxExpenses);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(rs.getString("first_name"), rs.getString("last_name")));
                }
            }
        }
        return customers;
    }

    private List<Customer> searchBadCustomers(Connection conn, int badCustomersLimit) throws SQLException {
        String subquery = "SELECT COUNT(p.id) AS purchase_count FROM purchases p WHERE p.customer_id = c.id";
        String query = "SELECT c.first_name, c.last_name " +
                "FROM customers c " +
                "ORDER BY (" + subquery + ") ASC " +
                "LIMIT ?";
        List<Customer> customers = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, badCustomersLimit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(rs.getString("first_name"), rs.getString("last_name")));
                }
            }
        }
        return customers;
    }

    // Внутренний класс для хранения результатов по критериям
    public static class CriteriaResult {
        private SearchCriteria criteria;
        private List<Customer> results;

        public CriteriaResult(SearchCriteria criteria, List<Customer> results) {
            this.criteria = criteria;
            this.results = results;
        }

        // Геттеры и сеттеры
        public SearchCriteria getCriteria() {
            return criteria;
        }

        public void setCriteria(SearchCriteria criteria) {
            this.criteria = criteria;
        }

        public List<Customer> getResults() {
            return results;
        }

        public void setResults(List<Customer> results) {
            this.results = results;
        }
    }

    // Класс для формирования итогового JSON-результата поиска
    public static class SearchResult {
        private String type;
        private List<CriteriaResult> results;

        public SearchResult(String type, List<CriteriaResult> results) {
            this.type = type;
            this.results = results;
        }

        // Геттеры и сеттеры
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<CriteriaResult> getResults() {
            return results;
        }

        public void setResults(List<CriteriaResult> results) {
            this.results = results;
        }
    }
}