package org.infy.model;

import java.util.HashMap;
import java.util.Map;

public record CsvStats(
    int totalRows,
    int totalColumns,
    Map<String, Integer> blankFieldCounts,
    Map<String, Double> blankFieldPercentages
) {
    public CsvStats {
        blankFieldCounts = new HashMap<>(blankFieldCounts);
        blankFieldPercentages = new HashMap<>(blankFieldPercentages);
    }

    public static CsvStats create(int rows, int cols, Map<String, Integer> blanks) {
        Map<String, Double> percentages = new HashMap<>();
        blanks.forEach((field, count) -> 
            percentages.put(field, calculatePercentage(count, rows))
        );
        return new CsvStats(rows, cols, blanks, percentages);
    }

    private static double calculatePercentage(int count, int total) {
        return total == 0 ? 0 : (count * 100.0) / total;
    }
} 