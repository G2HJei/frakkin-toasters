package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;

import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DECLARE_MARTIAL_LAW;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_JUMP;

class ResolveCrisisSectionThenEventProcessorTest extends EventTestHarness<ResolveCrisisSectionThenEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(ANASTASIA_DEE_DUALLA);
        president(LAURA_ROSLIN);
        admiral(ANASTASIA_DEE_DUALLA);
    }

    // "Declare Martial Law": -1 morale, then the Admiral becomes the President.
    @Test
    void theAdmiralBecomesPresident() {
        execute(new ResolveCrisisSectionThenEvent(DECLARE_MARTIAL_LAW, TOP));

        assertFollowup(new MakePresidentEvent(ANASTASIA_DEE_DUALLA));
    }

    // The morale loss executed "Emotionally Fragile" Dee, the Admiral, before the title came (F-7c5468).
    @Test
    void nothingForAnAdmiralWhoWasExecuted() {
        ops.player(2).removeCharacter();

        execute(new ResolveCrisisSectionThenEvent(DECLARE_MARTIAL_LAW, TOP));
    }

    @Test
    void onlyForASectionInTwoParts() {
        assertInvalid(new ResolveCrisisSectionThenEvent(RIOTS_JUMP, TOP));
    }

    @Test
    void onlyTheTopOfTheCard() {
        assertInvalid(new ResolveCrisisSectionThenEvent(DECLARE_MARTIAL_LAW, BOTTOM));
    }
}
