package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class AbusePowerEventProcessorTest extends EventTestHarness<AbusePowerEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK_ALT);
        player(2).character(LAURA_ROSLIN);
        moveTo(WEAPONS_CONTROL, TOM_ZAREK_ALT);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        president(LAURA_ROSLIN);
    }

    // "Blackmail" can't be played: the President holds no Skill Card.
    @Test
    void drawsFourMutinyCardsToPlayOne() {
        mutinyDeck.nextCard(FEED_THE_PEOPLE, BLACKMAIL, PANIC, FEED_THE_PEOPLE);

        execute(new AbusePowerEvent(1));

        assertEquals(0, player(1).mutinyCards().size());
        assertFollowup(one(
                new AbusePowerChoiceEvent(1, FEED_THE_PEOPLE, List.of(BLACKMAIL, PANIC, FEED_THE_PEOPLE)),
                new AbusePowerChoiceEvent(1, PANIC, List.of(FEED_THE_PEOPLE, BLACKMAIL, FEED_THE_PEOPLE))));
    }

    @Test
    void withNothingToPlayAllGoToTheBottom() {
        mutinyDeck.nextCard(BLACKMAIL, BLACKMAIL, BLACKMAIL, BLACKMAIL);

        execute(new AbusePowerEvent(1));

        assertEquals(BLACKMAIL, game.decks().deck(xyz.zlatanov.frakkintoasters.state.deck.DeckId.MUTINY).bottom());
    }

    @Test
    void notWithoutAMiracleToken() {
        ops.player(1).loseMiracleTokens();

        assertInvalid(new AbusePowerEvent(1));
    }
}
