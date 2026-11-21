package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.FeedThePeopleEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.WEAPONS_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.TOM_ZAREK_ALT;

class AbusePowerChoiceEventProcessorTest extends EventTestHarness<AbusePowerChoiceEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK_ALT);
        moveTo(WEAPONS_CONTROL, TOM_ZAREK_ALT);
    }

    // A second Mutiny Card in his hand doesn't send him to the "Brig": he only holds it to play it.
    @Test
    void holdsTheChosenCardToPlayIt() {
        player(1).mutinyCards().addOnTop(PANIC);

        execute(new AbusePowerChoiceEvent(1, FEED_THE_PEOPLE, List.of(PANIC, BLACKMAIL)));

        assertEquals(List.of(FEED_THE_PEOPLE, PANIC), player(1).mutinyCards().cards());
        assertEquals(WEAPONS_CONTROL, locate(TOM_ZAREK_ALT));
        assertEquals(BLACKMAIL, game.decks().deck(DeckId.MUTINY).bottom());
        assertFollowup(new PlayerDecisionEvent<>(1, FeedThePeopleEvent.class));
    }
}
