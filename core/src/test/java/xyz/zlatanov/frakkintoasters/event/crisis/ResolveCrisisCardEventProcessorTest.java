package xyz.zlatanov.frakkintoasters.event.crisis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

class ResolveCrisisCardEventProcessorTest extends EventTestHarness<ResolveCrisisCardEvent> {

    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(FELIX_GAETA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    @Test
    void shouldLetThePresidentChoose() {
        execute(new ResolveCrisisCardEvent(FOOD_SHORTAGE));

        assertChoice(PRESIDENT, FOOD_SHORTAGE);
    }

    @Test
    void shouldLetTheAdmiralChoose() {
        execute(new ResolveCrisisCardEvent(RIOTS_JUMP));

        assertChoice(ADMIRAL, RIOTS_JUMP);
    }

    @Test
    void shouldLetTheCagChoose() {
        execute(new ResolveCrisisCardEvent(RAIDERS_INBOUND));

        assertChoice(CAG, RAIDERS_INBOUND);
    }

    private void assertChoice(int chooser, CrisisCard card) {
        assertFollowup(one(new ChooseCrisisSectionEvent(chooser, card, TOP),
                new ChooseCrisisSectionEvent(chooser, card, BOTTOM)));
    }
}
