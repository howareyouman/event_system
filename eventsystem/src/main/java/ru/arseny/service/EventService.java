package ru.arseny.service;

import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public interface EventService {
    Long getNumberOfEventsInOneMinute(ZonedDateTime time);
    Long getNumberOfEventsInOneHour(ZonedDateTime time);
    Long getNumberOfEventsInOneDay(ZonedDateTime time);
    void add(ZonedDateTime time);
}
