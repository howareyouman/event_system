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
public class EventServiceMinuteTest {

    @Autowired
    private EventService eventService;

    private static boolean initialized = false;

    @Before
    public void beforeTest() {
        if (!initialized) {
            eventService.add(ZonedDateTime.now());
            eventService.add(ZonedDateTime.now().plusSeconds(10));
            eventService.add(ZonedDateTime.now().plusSeconds(15));
            initialized = true;
        }
    }

    @Test
    public void testOneMinute() {
        ZonedDateTime nextToNow = ZonedDateTime.now().plusSeconds(20);
        Long correct = 3L;
        assertEquals(eventService.getNumberOfEventsInOneMinute(nextToNow), correct);
    }

    @Test
    public void testZeroEventsDay() {
        ZonedDateTime nextToNow = ZonedDateTime.now().plusYears(1L);
        Long correct = 0L;
        assertEquals(eventService.getNumberOfEventsInOneMinute(nextToNow), correct);
    }
}
