package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class GiveTitlesToNextInLineEventProcessorTest extends EventTestHarness<GiveTitlesToNextInLineEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
    }

    @Test
    void shouldGiveAllTitles() {
        president(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
        cag(WILLIAM_ADAMA);

        execute(new GiveTitlesToNextInLineEvent(1));

        assertFollowup(all(
                new DetermineNextPresidentEvent(1),
                new DetermineNextAdmiralEvent(1),
                new DetermineNextCagEvent(1)));
    }

    @Test
    void shouldGiveOnlyTitlesThePlayerHas() {
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);

        execute(new GiveTitlesToNextInLineEvent(1));

        assertFollowup(new DetermineNextAdmiralEvent(1));
    }

    @Test
    void shouldNotFollowupWhenPlayerHasNoTitles() {
        president(LAURA_ROSLIN);
        admiral(LAURA_ROSLIN);
        execute(new GiveTitlesToNextInLineEvent(1));
    }
}
