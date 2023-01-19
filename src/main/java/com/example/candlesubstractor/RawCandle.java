package com.example.candlesubstractor;

import com.opencsv.bean.CsvBindByName;

public class RawCandle {

    @CsvBindByName(column = "<DATE>")
    private String date;

    @CsvBindByName(column = "<TIME>")
    private String time;
    @CsvBindByName(column = "<OPEN>")
    private Float open;

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public Float getOpen() {
        return open;
    }
}
