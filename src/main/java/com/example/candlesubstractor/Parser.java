package com.example.candlesubstractor;

public class Parser {

    public static final int TICKET = 0;
    public static final int PER = 1;
    public static final int DATE = 2;
    public static final int TIME = 3;
    public static final int OPEN = 4;
    public static final int HIGH = 5;
    public static final int LOW = 6;
    public static final int CLOSE = 7;
    public static final int VOL = 8;

    public static RawCandle parseToRawCandle(String row) {
        String[] cells = parseCells(row);
        return new RawCandle(
                cells[TICKET],
                cells[PER],
                cells[DATE],
                cells[TIME],
                cells[OPEN],
                cells[HIGH],
                cells[LOW],
                cells[CLOSE],
                cells[VOL]
        );
    }

    public static String[] parseCells(String row) {
        return row.split(",");

    }
}
