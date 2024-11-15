package org.infy.model;

import java.time.Duration;

public record ParsingStats(
    long totalRecords,
    long validRecords,
    long invalidRecords,
    Duration processingTime,
    long memoryUsed
) {} 