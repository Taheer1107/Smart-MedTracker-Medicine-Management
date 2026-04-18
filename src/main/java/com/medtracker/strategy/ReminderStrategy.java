package com.medtracker.strategy;

import java.time.LocalDateTime;

public interface ReminderStrategy {

    LocalDateTime calculateNextReminder(LocalDateTime current);
}

package com.medtracker.strategy;

import java.time.LocalDateTime;

public class DailyReminderStrategy implements ReminderStrategy {

    @Override
    public LocalDateTime calculateNextReminder(LocalDateTime current) {
        return current.plusDays(1);
    }
}