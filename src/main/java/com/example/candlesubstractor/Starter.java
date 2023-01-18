package com.example.candlesubstractor;

import com.opencsv.CSVReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class Starter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(args[0]))) {
            String[] headers = reader.readNext();
            int dateColumnIndex = getColumnIndex(headers, "<DATE>");
            System.out.println(dateColumnIndex);
            List<LocalDate> dates = reader.readAll().stream()
                    .map(columns -> columns[dateColumnIndex])
                    .map(dateString -> LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .collect(toList());
            System.out.println(dates);
        }
    }

    private int getColumnIndex(String[] headers, String columnName) {
        int index = 0;
        for (String header : headers) {
            if (header.equals(columnName)) {
                return index;
            }
            index++;
        }
        throw new IllegalStateException("Didn't find column " + columnName);
    }
}
