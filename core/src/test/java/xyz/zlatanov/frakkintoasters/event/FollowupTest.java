package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class FollowupTest {

    @Test
    void shouldReturnSingleWhenAllOfWithOneEvent() {
        val event = mock(Event.class);
        val result = Followup.all(event);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnSingleWhenOneOfWithOneEvent() {
        val event = mock(Event.class);
        val result = Followup.one(event);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnOriginalWhenAllOfWithOneFollowup() {
        val followup = new Followup.Single(mock(Event.class));
        val result = Followup.all(followup);

        assertInstanceOf(Followup.Single.class, result);
    }

    @Test
    void shouldReturnOriginalWhenOneOfWithOneFollowup() {
        val followup = new Followup.Single(mock(Event.class));
        val result = Followup.one(followup);

        assertInstanceOf(Followup.Single.class, result);
    }
}
