package com.example.candlesubstractor;

import com.opencsv.CSVReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;

@Component
public class Starter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(args[0]))) {
            String[] headers = reader.readNext();
            System.out.println(getColumnIndex(headers, "<DATE>"));
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
