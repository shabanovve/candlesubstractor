package com.example.candlesubstractor;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

@SuppressWarnings({"unused"})
@Component
public class Starter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        List<LocalDateTime> first = readDateAndTimeFromFile(args[0]);
        List<LocalDateTime> second = readDateAndTimeFromFile(args[1]);
        List<LocalDateTime> intersection = first.stream()
                .distinct().filter(second::contains).toList();
        System.out.println("first list amount = " + first.size());
        System.out.println("second list amount = " + second.size());
        System.out.println("Intersection amount = " + intersection.size());

        Iterator<LocalDateTime> intersectionIterator = intersection.iterator();
        try (Reader reader = Files.newBufferedReader(Paths.get(args[0]))) {
            try (
                    CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()
            ) {
                ColumnPositionMappingStrategy<RawCandle> strat = new ColumnPositionMappingStrategy<RawCandle>();
                strat.setType(RawCandle.class);
                String[] columns = new String[]{"ticker", "per", "date", "time", "open", "high", "low", "close", "vol"};
                strat.setColumnMapping(columns);

                CsvToBean<RawCandle> csvToBean = new CsvToBeanBuilder<RawCandle>(csvReader)
                        .withType(RawCandle.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withMappingStrategy(strat)
                        .build();
                Iterator<RawCandle> firstCsvIterator = csvToBean.iterator();
                while (intersectionIterator.hasNext()) {
                    handleIntersectionDate(firstCsvIterator)
                            .accept(intersectionIterator.next());
                }
            }
        }
    }

    private Consumer<LocalDateTime> handleIntersectionDate(Iterator<RawCandle> iterator) {
        return intersectionDate -> {
            while (iterator.hasNext()) {
                RawCandle firstRawCandle = iterator.next();
                LocalDateTime date = convertToDate(firstRawCandle.getDate(), firstRawCandle.getTime());
                if (date.equals(intersectionDate)) {
                    System.out.println("equals " + date);
                    break;
                }
            }
        };
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
                .map(pair -> convertToDate(pair.left, pair.right))
                .collect(toList());
    }

    private LocalDateTime convertToDate(String dateString, String timeString) {
        LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
        long dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long timeMillis = Long.parseLong(timeString);
        Instant commonInstant = Instant.ofEpochMilli(dateMillis + timeMillis);
        return LocalDateTime.ofInstant(commonInstant, ZoneId.systemDefault());
    }

    @SuppressWarnings("unused")
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
