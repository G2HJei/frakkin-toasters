package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class SkilledPoliticianEventProcessorTest extends EventTestHarness<SkilledPoliticianEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // She isn't President: the ability doesn't need it.
    @Test
    void drawsFourQuorumCardsToResolveOne() {
        quorumDeck.nextCard(EULOGY, ESTABLISH_DOGSVILLE, EULOGY, ARREST_ORDER);

        execute(new SkilledPoliticianEvent(1));

        assertFollowup(one(
                new SkilledPoliticianChoiceEvent(1, EULOGY, List.of(ESTABLISH_DOGSVILLE, EULOGY, ARREST_ORDER)),
                new SkilledPoliticianChoiceEvent(1, ESTABLISH_DOGSVILLE, List.of(EULOGY, EULOGY, ARREST_ORDER)),
                new SkilledPoliticianChoiceEvent(1, ARREST_ORDER, List.of(EULOGY, ESTABLISH_DOGSVILLE, EULOGY))));
    }

    @Test
    void notWithoutAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new SkilledPoliticianEvent(1));
    }
}
