package com.example.candlesubstractor;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.example.candlesubstractor.Parser.parseToRawCandle;

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
                    Scanner scanner = new Scanner(reader)
            ) {
                scanner.next();//skip headers
                while (intersectionIterator.hasNext()) {
                    handleIntersectionDate(scanner)
                            .accept(intersectionIterator.next());
                }
            }
        }
    }

    private Consumer<LocalDateTime> handleIntersectionDate(Scanner scanner) {
        return intersectionDate -> {
            while (scanner.hasNext()) {
                RawCandle firstRawCandle = parseToRawCandle(scanner.next());
                LocalDateTime date = convertToDate(firstRawCandle.date(), firstRawCandle.time());
                if (date.equals(intersectionDate)) {
                    System.out.println("equals " + date);
                    break;
                }
            }
        };
    }

    private List<LocalDateTime> readDateAndTimeFromFile(String fileName) throws IOException {
        List<LocalDateTime> dates;
        try (Scanner scanner = new Scanner(new FileReader(fileName))) {
            String[] headers = Parser.parseCells(scanner.next());
            int dateColumnIndex = getColumnIndex(headers, "<DATE>");
            int timeColumnIndex = getColumnIndex(headers, "<TIME>");
            dates = readLocalDateTimes(scanner, dateColumnIndex, timeColumnIndex);
        }
        return dates;
    }

    private List<LocalDateTime> readLocalDateTimes(Scanner scanner, int dateColumnIndex, int timeColumnIndex) {
        List<LocalDateTime> dateTimeList = new ArrayList<>();
        while (scanner.hasNext()) {
            RawCandle rawCandle = parseToRawCandle(scanner.next());
            dateTimeList.add(convertToDate(rawCandle.date(), rawCandle.time()));
        }
        return dateTimeList;
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
