package ru.arseny.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.arseny.application.AppConfig;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventServiceHourTest {

    @Autowired
    private EventService eventService;

    private static boolean initialized = false;

    @Before
    public void beforeTest() {
        if (!initialized) {
            eventService.add(ZonedDateTime.now());
            eventService.add(ZonedDateTime.now().plusMinutes(10));
            eventService.add(ZonedDateTime.now().plusMinutes(15));
            initialized = true;
        }
    }

    @Test
    public void testMultipleMinutes() {
        ZonedDateTime nextToNow = ZonedDateTime.now().plusMinutes(20);
        Long correct = 3L;
        assertEquals(eventService.getNumberOfEventsInOneHour(nextToNow), correct);
    }

    @Test
    public void testZeroEventsHour() {
        ZonedDateTime nextYear = ZonedDateTime.now().plusYears(1L);
        Long correct = 0L;
        assertEquals(eventService.getNumberOfEventsInOneHour(nextYear), correct);
    }
}

