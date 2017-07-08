package ru.arseny.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.arseny.service.EventService;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping("/minute")
    @ResponseBody
    public Long getNumberOfEventsInMinute() {
        return eventService.getNumberOfEventsInOneMinute(ZonedDateTime.now());
    }

    @RequestMapping("/hour")
    @ResponseBody
    public Long getNumberOfEventsInHour() {
        return eventService.getNumberOfEventsInOneHour(ZonedDateTime.now());
    }

    @RequestMapping("/day")
    @ResponseBody
    public Long getNumberOfEventsInDay() {
        return eventService.getNumberOfEventsInOneDay(ZonedDateTime.now());
    }

    @RequestMapping("/add")
    @ResponseBody
    public void addEvent() {
        eventService.add(ZonedDateTime.now());
    }

}
