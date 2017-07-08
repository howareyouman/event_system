package ru.arseny.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.arseny.model.Event;
import ru.arseny.service.EventService;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class EventServiceImpl implements EventService {

    private final ConcurrentLinkedDeque<Event> minuteEvents;
    private final ConcurrentLinkedDeque<Event> hourEvents;
    private final ConcurrentLinkedDeque<Event> dayEvents;


    public EventServiceImpl() {
        minuteEvents = new ConcurrentLinkedDeque<>();
        hourEvents = new ConcurrentLinkedDeque<>();
        dayEvents = new ConcurrentLinkedDeque<>();
    }


    private Long getIntervalFromDeque(ZonedDateTime time, ConcurrentLinkedDeque<Event> deque, String chronoUnit) {
        if (deque.isEmpty() ||
                ChronoUnit.valueOf(chronoUnit).between(deque.getLast().getTime(), time) > 0) {
            return 0L;
        }

        if (deque.size() == 1) {
            return deque.getLast().getNumberOfEventsBefore();
        }

        Event first = deque.getFirst();
        if (ChronoUnit.valueOf(chronoUnit).between(first.getTime(), time) <= 0) {
            return deque.getLast().getNumberOfEventsBefore();
        }

        Iterator<Event> iterator = deque.iterator();

        while (iterator.hasNext() &&
                ChronoUnit.valueOf(chronoUnit).between(first.getTime(), time) > 0) {
            first = iterator.next();
        }

        return deque.getLast().getNumberOfEventsBefore() -
                first.getNumberOfEventsBefore() + 1;
    }

    @Override
    public Long getNumberOfEventsInOneMinute(ZonedDateTime time) {
        return getIntervalFromDeque(time, minuteEvents, ChronoUnit.MINUTES.name());
    }

    @Override
    public Long getNumberOfEventsInOneHour(ZonedDateTime time) {
        return getIntervalFromDeque(time, hourEvents, ChronoUnit.HOURS.name());
    }

    @Override
    public Long getNumberOfEventsInOneDay(ZonedDateTime time) {
        return getIntervalFromDeque(time, dayEvents, ChronoUnit.DAYS.name());
    }

    private void addToMinuteQueue(ZonedDateTime time) {
        Event event = new Event(time, 1L);
        synchronized (minuteEvents) {
            clearDeque(minuteEvents, event, ChronoUnit.MINUTES.name());

            if (!minuteEvents.isEmpty()) {
                event = new Event(
                        time,
                        this.minuteEvents.getLast().getNumberOfEventsBefore() + 1
                );
            }

            minuteEvents.addLast(event);
        }
    }

    private void clearDeque(ConcurrentLinkedDeque<Event> deque, Event event, String chronoUnit) {
        while (!deque.isEmpty() &&
                ChronoUnit.valueOf(chronoUnit).between(deque.getFirst().getTime(), event.getTime()) > 0) {
            deque.removeFirst();
        }
    }

    private void addToSellDeque(ZonedDateTime time, ConcurrentLinkedDeque<Event> deque,
                                String sellChronoUnit, String dequeChronoUnit) {
        Event event = new Event(time, 1L);
        synchronized (deque) {
            clearDeque(deque, event, dequeChronoUnit);

            if (!deque.isEmpty()) {
                Event last = deque.getLast();

                if (ChronoUnit.valueOf(sellChronoUnit).between(last.getTime(), event.getTime()) > 0) {
                    deque.addLast(
                            new Event(event.getTime(), last.getNumberOfEventsBefore() + 1)
                    );
                } else {
                    deque.getLast().addOneEventBefore();
                }
            } else {
                deque.addLast(event);
            }
        }
    }

    @Override
    public void add(ZonedDateTime time) {
        addToMinuteQueue(time);
        addToSellDeque(time, hourEvents, ChronoUnit.SECONDS.name(), ChronoUnit.HOURS.name());
        addToSellDeque(time, dayEvents, ChronoUnit.MINUTES.name(), ChronoUnit.DAYS.name());
    }


    @Scheduled(cron = "0 * * * * *")
    private void clearMinuteDeque() {
        Event event = new Event();

        synchronized (minuteEvents) {
            clearDeque(minuteEvents, event, ChronoUnit.MINUTES.name());
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    private void clearHourDeque() {
        Event event = new Event();

        synchronized (hourEvents) {
            clearDeque(hourEvents, event, ChronoUnit.HOURS.name());
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void clearDayDeque() {
        Event event = new Event();

        synchronized (dayEvents) {
            clearDeque(dayEvents, event, ChronoUnit.DAYS.name());
        }
    }
}
