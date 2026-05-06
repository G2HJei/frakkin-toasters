package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class FollowupTest {

    @Test
    void shouldReturnSingleWhenAllOfWithOneEvent() {
        Event event = mock(Event.class);
        Followup result = Followup.all(event);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnSingleWhenOneOfWithOneEvent() {
        Event event = mock(Event.class);
        Followup result = Followup.one(event);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnOriginalWhenAllOfWithOneFollowup() {
        Followup followup = new Followup.Single(mock(Event.class));
        Followup result = Followup.all(followup);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnOriginalWhenOneOfWithOneFollowup() {
        Followup followup = new Followup.Single(mock(Event.class));
        Followup result = Followup.one(followup);

        assertInstanceOf(Followup.Single.class, result);
    }
}
