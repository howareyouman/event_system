package ru.arseny.model;

import java.time.ZonedDateTime;

public class Event {
    private ZonedDateTime time;
    private Long numberOfEventsBefore;

    public Event() {
        this.time = ZonedDateTime.now();
        this.numberOfEventsBefore = 0L;
    }

    public Event(ZonedDateTime zonedDateTime, Long numberOfEventsBefore) {
        this.time = zonedDateTime;
        this.numberOfEventsBefore = numberOfEventsBefore;
    }

    public void addOneEventBefore() {
        this.numberOfEventsBefore++;
    }

    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    public Long getNumberOfEventsBefore() {
        return numberOfEventsBefore;
    }

    public void setNumberOfEventsBefore(Long numberOfEventsBefore) {
        this.numberOfEventsBefore = numberOfEventsBefore;
    }
}
