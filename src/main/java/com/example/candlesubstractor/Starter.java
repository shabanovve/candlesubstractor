package com.example.candlesubstractor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
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
        try (
                Reader firstFileReader = Files.newBufferedReader(Paths.get(args[0]));
                Scanner firstFileScanner = new Scanner(firstFileReader);
                Reader secondFileReader = Files.newBufferedReader(Paths.get(args[1]));
                Scanner secondFileScanner = new Scanner(secondFileReader);
                BufferedWriter writer = new BufferedWriter(new FileWriter("result.csv"))
        ) {
            String header = firstFileScanner.next();//skip headers
            writer.write(header + "\n");//write header
            secondFileScanner.next();//skip headers
            while (intersectionIterator.hasNext()) {
                handleIntersectionDate(firstFileScanner, secondFileScanner, writer)
                        .accept(intersectionIterator.next());
            }
        }
    }

    private Consumer<LocalDateTime> handleIntersectionDate(Scanner scanner, Scanner secondFileScanner, BufferedWriter writer) {
        return intersectionDate -> {
            while (scanner.hasNext()) {
                RawCandle firstRawCandle = parseToRawCandle(scanner.next());
                LocalDateTime date = convertToDate(firstRawCandle.date(), firstRawCandle.time());
                if (date.equals(intersectionDate)) {
                    Candle firstCandle = toCandle(firstRawCandle, date);
                    handleFirstCandle(secondFileScanner, intersectionDate, writer).accept(firstCandle);
                    break;
                }
            }
        };
    }

    private Candle toCandle(RawCandle rawCandle, LocalDateTime date) {
        return new Candle(
                rawCandle.ticker(),
                rawCandle.per(),
                date,
                Float.valueOf(rawCandle.open()),
                Float.valueOf(rawCandle.high()),
                Float.valueOf(rawCandle.low()),
                Float.valueOf(rawCandle.close()),
                Float.valueOf(rawCandle.vol())
        );
    }

    private Consumer<Candle> handleFirstCandle(Scanner secondFileScanner, LocalDateTime intersectionDate, BufferedWriter writer) {
        return firstCandle -> {
            while (secondFileScanner.hasNext()) {
                RawCandle secondRawCandle = parseToRawCandle(secondFileScanner.next());
                LocalDateTime date = convertToDate(secondRawCandle.date(), secondRawCandle.time());
                if (intersectionDate.equals(date)) {
                    Candle secondCandle = toCandle(secondRawCandle, intersectionDate);
                    handleSecondCandle(firstCandle, intersectionDate, secondRawCandle.date(), secondRawCandle.time(), writer)
                            .accept(secondCandle);
                    break;
                }
            }
        };
    }

    private Consumer<Candle> handleSecondCandle(
            Candle firstCandle, LocalDateTime intersectionDate, String dateSting, String dateTime, BufferedWriter writer
    ) {
        return secondCandle -> {
            Candle resultCandle = new Candle(
                    "result",
                    firstCandle.per(),
                    intersectionDate,
                    firstCandle.open() - secondCandle.open(),
                    firstCandle.high() - secondCandle.high(),
                    firstCandle.low() - secondCandle.low(),
                    firstCandle.close() - secondCandle.close(),
                    firstCandle.vol() - secondCandle.vol()
            );
            RawCandle resultRawCandle = toRawCandle(secondCandle, dateSting, dateTime);
            try {
                writer.write(toRawString(resultRawCandle));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private String toRawString(RawCandle rawCandle) {
        //<TICKER>,<PER>,<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
        return rawCandle.ticker() + "," +
                rawCandle.per() + "," +
                rawCandle.date() + "," +
                rawCandle.time() + "," +
                rawCandle.open() + "," +
                rawCandle.high() + "," +
                rawCandle.low() + "," +
                rawCandle.close() + "," +
                rawCandle.vol() + "\n";
    }

    private RawCandle toRawCandle(Candle candle, String dateSting, String dateTime) {
        return new RawCandle(
                "RESULT",
                candle.per(),
                dateSting,
                dateTime,
                candle.open().toString(),
                candle.high().toString(),
                candle.low().toString(),
                candle.close().toString(),
                candle.vol().toString()
        );
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
