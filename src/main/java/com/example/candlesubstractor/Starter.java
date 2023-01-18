package com.example.candlesubstractor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class Starter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        List<LocalDateTime> first = readDateAndTimeFromFile(args[0]);
        List<LocalDateTime> second = readDateAndTimeFromFile(args[1]);
        System.out.println(second);
    }

    private List<LocalDateTime> readDateAndTimeFromFile(String fileName) throws IOException, CsvException {
        List<LocalDateTime> dates;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            String[] headers = reader.readNext();
            int dateColumnIndex = getColumnIndex(headers, "<DATE>");
            int timeColumnIndex = getColumnIndex(headers, "<TIME>");
            dates = readLocalDateTimes(reader, dateColumnIndex, timeColumnIndex);
        }
        return dates;
    }

    private List<LocalDateTime> readLocalDateTimes(CSVReader reader, int dateColumnIndex, int timeColumnIndex) throws IOException, CsvException {
        return reader.readAll().stream()
                .map(columns -> new Pair(columns[dateColumnIndex], columns[timeColumnIndex]))
                .map(pair -> {
                            LocalDate date = LocalDate.parse(pair.left, DateTimeFormatter.ofPattern("yyyyMMdd"));
                            long dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            long timeMillis = Long.parseLong(pair.right);
                            Instant commonInstant = Instant.ofEpochMilli(dateMillis + timeMillis);
                            return LocalDateTime.ofInstant(commonInstant, ZoneId.systemDefault());
                        }
                )
                .collect(toList());
    }

    private record Pair(String left, String right) {
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
