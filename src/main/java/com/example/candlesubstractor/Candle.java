package com.example.candlesubstractor;

import java.time.LocalDateTime;

public record Candle(String ticker, String per, LocalDateTime dateTime, Float open, Float high, Float low,
                     Float close, Float vol) {
}
