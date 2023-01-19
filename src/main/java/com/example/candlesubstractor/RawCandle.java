package com.example.candlesubstractor;

import com.opencsv.bean.CsvBindByName;

public class RawCandle {
    @CsvBindByName(column = "<OPEN>")
    private Float open;

    @Override
    public String toString() {
        return "<OPEN> = " + this.open;
    }
}
