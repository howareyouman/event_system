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


    private Long getIntervalFromDeque(ZonedDateTime time, ConcurrentLinkedDeque<Event> deque, ChronoUnit chronoUnit) {
        if (deque.isEmpty() || chronoUnit.between(deque.getLast().getTime(), time) > 0) {
            return 0L;
        }

        if (deque.size() == 1) {
            return deque.getLast().getNumberOfEventsBefore();
        }

        Event first = deque.getFirst();
        if (chronoUnit.between(first.getTime(), time) <= 0) {
            return deque.getLast().getNumberOfEventsBefore();
        }

        Iterator<Event> iterator = deque.iterator();

        while (iterator.hasNext() &&
                chronoUnit.between(first.getTime(), time) > 0) {
            first = iterator.next();
        }

        return deque.getLast().getNumberOfEventsBefore() -
                first.getNumberOfEventsBefore() + 1;
    }

    @Override
    public Long getNumberOfEventsInOneMinute(ZonedDateTime time) {
        return getIntervalFromDeque(time, minuteEvents, ChronoUnit.MINUTES);
    }

    @Override
    public Long getNumberOfEventsInOneHour(ZonedDateTime time) {
        return getIntervalFromDeque(time, hourEvents, ChronoUnit.HOURS);
    }

    @Override
    public Long getNumberOfEventsInOneDay(ZonedDateTime time) {
        return getIntervalFromDeque(time, dayEvents, ChronoUnit.DAYS);
    }

    private void addToMinuteQueue(ZonedDateTime time) {
        synchronized (minuteEvents) {
            clearDeque(minuteEvents, time, ChronoUnit.MINUTES);
            Event event;
            if (!minuteEvents.isEmpty()) {
                event = new Event(
                        time,
                        this.minuteEvents.getLast().getNumberOfEventsBefore() + 1
                );
            } else  {
               event = new Event(time, 1L);
            }

            minuteEvents.addLast(event);
        }
    }

    private void clearDeque(ConcurrentLinkedDeque<Event> deque, ZonedDateTime time, ChronoUnit chronoUnit) {
        while (!deque.isEmpty() && chronoUnit.between(deque.getFirst().getTime(), time) > 0) {
            deque.removeFirst();
        }
    }

    private void addToCellDeque(ZonedDateTime time, ConcurrentLinkedDeque<Event> deque,
                                ChronoUnit sellChronoUnit, ChronoUnit dequeChronoUnit) {
        synchronized (deque) {
            clearDeque(deque, time, dequeChronoUnit);
            Event event = new Event(time, 1L);
            if (!deque.isEmpty()) {
                Event last = deque.getLast();

                if (sellChronoUnit.between(last.getTime(), event.getTime()) > 0) {
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
        addToCellDeque(time, hourEvents, ChronoUnit.SECONDS, ChronoUnit.HOURS);
        addToCellDeque(time, dayEvents, ChronoUnit.MINUTES, ChronoUnit.DAYS);
    }


    @Scheduled(cron = "0 * * * * *")
    private void clearMinuteDeque() {
        synchronized (minuteEvents) {
            clearDeque(minuteEvents, ZonedDateTime.now(), ChronoUnit.MINUTES);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    private void clearHourDeque() {
        synchronized (hourEvents) {
            clearDeque(hourEvents, ZonedDateTime.now(), ChronoUnit.HOURS);
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void clearDayDeque() {
        synchronized (dayEvents) {
            clearDeque(dayEvents, ZonedDateTime.now(), ChronoUnit.DAYS);
        }
    }
}
