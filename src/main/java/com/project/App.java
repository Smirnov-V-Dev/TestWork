package com.project;


import com.project.model.ErrorResult;
import com.project.model.SearchInput;
import com.project.model.StatInput;
import com.project.service.SearchService;
import com.project.service.StatService;
import com.project.util.DatabaseConfig;
import com.project.util.JsonUtils;

import java.io.File;

public class App {
    public static void main(String[] args) {
        if (args.length != 3) {
            ErrorResult error = new ErrorResult("Неправильное количество аргументов. Ожидается: тип операции, путь к входному файлу, путь к файлу результата.");
            writeError(error, args.length > 2 ? new File(args[2]) : null);
            return;
        }


        String operationType = args[0];
        String inputPath = args[1];
        String outputPath = args[2];

        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        DatabaseConfig databaseConfig = new DatabaseConfig();

        try {
            switch (operationType) {
                case "search":
                    SearchInput searchInput = JsonUtils.readJson(inputFile, SearchInput.class);
                    SearchService searchService = new SearchService(databaseConfig);
                    Object searchResult = searchService.executeSearch(searchInput);
                    JsonUtils.writeJson(outputFile, searchResult);
                    break;
                case "stat":
                    StatInput statInput = JsonUtils.readJson(inputFile, StatInput.class);
                    StatService statService = new StatService(databaseConfig);
                    Object statResult = statService.generateStatistics(statInput);
                    JsonUtils.writeJson(outputFile, statResult);
                    break;
                default:
                    ErrorResult error = new ErrorResult("Неизвестный тип операции: " + operationType);
                    JsonUtils.writeJson(outputFile, error);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResult error = new ErrorResult(e.getMessage());
            writeError(error, outputFile);
        }
    }

    private static void writeError(ErrorResult error, File outputFile) {
        if (outputFile != null) {
            try {
                JsonUtils.writeJson(outputFile, error);
            } catch (Exception ex) {
                ex.printStackTrace();
                // Если не удалось записать в файл, выводим в консоль
                System.err.println(error.getMessage());
            }
        } else {
            // Если нет файла для записи, выводим в консоль
            System.err.println(error.getMessage());
        }
    }
}