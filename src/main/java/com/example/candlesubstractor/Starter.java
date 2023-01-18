package com.example.candlesubstractor;

import com.opencsv.CSVReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

@Component
public class Starter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(args[0]))) {
            List<String[]> rows = reader.readAll();
            rows.forEach(row -> System.out.println(Arrays.toString(row)));
        }
    }
}
