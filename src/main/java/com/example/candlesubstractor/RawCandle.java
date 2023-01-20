package com.example.candlesubstractor;

public record RawCandle(String ticker, String per, String date, String time, String open, String high, String low,
                        String close, String vol) {
}
