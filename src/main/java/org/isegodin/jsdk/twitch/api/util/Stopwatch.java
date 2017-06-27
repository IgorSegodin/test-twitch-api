package org.isegodin.jsdk.twitch.api.util;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * @author isegodin
 */
public class Stopwatch {

    private final LocalTime startTime;

    private Stopwatch(LocalTime startTime) {
        this.startTime = startTime;
    }

    public static Stopwatch start() {
        return new Stopwatch(LocalTime.now());
    }

    public boolean isElapsedMillis(long millis) {
        return ChronoUnit.MILLIS.between(startTime, LocalTime.now()) >= millis;
    }
}
