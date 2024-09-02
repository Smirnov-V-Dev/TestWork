package com.project.service;

import com.project.model.StatInput;
import com.project.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatService {
    private final DatabaseConfig databaseConfig;

    public StatService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public Object generateStatistics(StatInput statInput) throws SQLException, ParseException {
        Date startDate = dateFormat.parse(statInput.getStartDate());
        Date endDate = dateFormat.parse(statInput.getEndDate());

        List<CustomerStat> customerStats = new ArrayList<>();
        double totalExpenses = 0.0;
        int totalDays = calculateBusinessDays(startDate, endDate);

        try (Connection conn = databaseConfig.getConnection()) {
            String query = "SELECT c.first_name, c.last_name, pr.name, pr.price " +
                    "FROM customers c " +
                    "JOIN purchases p ON c.id = p.customer_id " +
                    "JOIN products pr ON p.product_id = pr.id " +
                    "WHERE p.purchase_date BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setDate(1, new java.sql.Date(startDate.getTime()));
                stmt.setDate(2, new java.sql.Date(endDate.getTime()));
                try (ResultSet rs = stmt.executeQuery()) {
                    // Сбор данных
                    Map<String, CustomerStat> customerMap = new HashMap<>();
                    while (rs.next()) {
                        String fullName = rs.getString("last_name") + " " + rs.getString("first_name");
                        String productName = rs.getString("name");
                        double price = rs.getDouble("price");

                        customerMap.putIfAbsent(fullName, new CustomerStat(fullName));
                        CustomerStat customerStat = customerMap.get(fullName);
                        customerStat.addPurchase(productName, price);
                        totalExpenses += price;
                    }

                    // Преобразование в список
                    customerStats.addAll(customerMap.values());

                    // Сортировка по общей стоимости покупок по убыванию
                    customerStats.sort(Comparator.comparingDouble(CustomerStat::getTotalExpenses).reversed());
                }
            }
        }

        // Вычисление средней стоимости
        double avgExpenses = customerStats.isEmpty() ? 0.0 : totalExpenses / customerStats.size();

        return new StatResult("stat", totalDays, customerStats, totalExpenses, avgExpenses);
    }

    private int calculateBusinessDays(Date start, Date end) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);

        int businessDays = 0;

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            return 0;
        }

        do {
            int dayOfWeek = startCal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                businessDays++;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis());

        return businessDays;
    }

    // Внутренний класс для статистики по покупателю
    public static class CustomerStat {
        private String name;
        private List<PurchaseStat> purchases;
        private double totalExpenses;
        private Map<String, Double> purchaseMap;

        public CustomerStat(String name) {
            this.name = name;
            this.purchases = new ArrayList<>();
            this.purchaseMap = new HashMap<>();
            this.totalExpenses = 0.0;
        }

        public void addPurchase(String productName, double price) {
            purchaseMap.put(productName, purchaseMap.getOrDefault(productName, 0.0) + price);
            totalExpenses += price;
        }

        public void finalizePurchases() {
            for (Map.Entry<String, Double> entry : purchaseMap.entrySet()) {
                purchases.add(new PurchaseStat(entry.getKey(), entry.getValue()));
            }
            // Сортировка по суммарной стоимости по убыванию
            purchases.sort(Comparator.comparingDouble(PurchaseStat::getExpenses).reversed());
        }

        // Геттеры и сеттеры
        public String getName() {
            return name;
        }

        public List<PurchaseStat> getPurchases() {
            return purchases;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }
    }

    // Внутренний класс для статистики по покупке
    public static class PurchaseStat {
        private String name;
        private double expenses;

        public PurchaseStat(String name, double expenses) {
            this.name = name;
            this.expenses = expenses;
        }

        // Геттеры и сеттеры
        public String getName() {
            return name;
        }

        public double getExpenses() {
            return expenses;
        }
    }

    // Класс для формирования итогового JSON-результата статистики
    public static class StatResult {
        private String type;
        private int totalDays;
        private List<CustomerStat> customers;
        private double totalExpenses;
        private double avgExpenses;

        public StatResult(String type, int totalDays, List<CustomerStat> customers, double totalExpenses, double avgExpenses) {
            this.type = type;
            this.totalDays = totalDays;
            this.customers = customers;
            this.totalExpenses = totalExpenses;
            this.avgExpenses = avgExpenses;

            // Финализация данных по покупкам
            for (CustomerStat cs : this.customers) {
                cs.finalizePurchases();
            }
        }

        // Геттеры и сеттеры
        public String getType() {
            return type;
        }

        public int getTotalDays() {
            return totalDays;
        }

        public List<CustomerStat> getCustomers() {
            return customers;
        }

        public double getTotalExpenses() {
            return totalExpenses;
        }

        public double getAvgExpenses() {
            return avgExpenses;
        }
    }
}
