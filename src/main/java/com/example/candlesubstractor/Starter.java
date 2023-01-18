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
            String h = getColumnIndex(headers);
            System.out.println(h);
        }
    }

    private String getColumnIndex(String[] headers) {
        int index = 0;
        String h = null;
        for (String header : headers) {
            if (header.equals("<DATE>")) {
                System.out.println("index = " + index);
                h = header;
            }
            index++;
        }
        return h;
    }
}
