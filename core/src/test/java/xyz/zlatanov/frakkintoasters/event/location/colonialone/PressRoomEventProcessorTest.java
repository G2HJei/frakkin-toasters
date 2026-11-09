package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.deck.DiscardDownTo1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESS_ROOM;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.PANIC;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class PressRoomEventProcessorTest extends EventTestHarness<PressRoomEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(TORY_FOSTER);
        moveTo(PRESS_ROOM, TORY_FOSTER);
        player(2).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
    }

    @Test
    void shouldDraw1MutinyCardWithoutMovingToTheBrig() {
        mutinyDeck.nextCard(PANIC);

        execute(new PressRoomEvent(1, 2));

        assertEquals(1, player(2).mutinyCards().size());
        assertEquals(Optional.of(COMMAND), game.locate(SAUL_TIGH));
    }

    @Test
    void shouldKeep1OfTheTargetsMutinyCards() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        mutinyDeck.nextCard(PANIC);

        execute(new PressRoomEvent(1, 2));

        assertFollowup(new PlayerDecisionEvent<>(2, DiscardDownTo1MutinyCardEvent.class));
    }

    @Test
    void shouldLetThePlayerDiscardAMutinyCard() {
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);

        execute(new PressRoomEvent(1, 2));

        assertFollowup(
                one(
                        new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class),
                        new NoOpEvent(1)));
    }

    @Test
    void shouldKeepAndThenDiscard() {
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);
        player(2).mutinyCards().addOnTop(PANIC);

        execute(new PressRoomEvent(1, 2));

        assertFollowup(
                all(
                        single(new PlayerDecisionEvent<>(2, DiscardDownTo1MutinyCardEvent.class)),
                        one(
                                new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class),
                                new NoOpEvent(1))));
    }

    @Test
    void shouldChooseAnotherPlayer() {
        assertInvalid(new PressRoomEvent(1, 1));
    }

    @Test
    void shouldNotChooseSomeoneWhoIsNotPlaying() {
        assertInvalid(new PressRoomEvent(1, 4));
    }

    @Test
    void shouldNotChooseACylon() {
        player(3).character(CAVIL);
        assertInvalid(new PressRoomEvent(1, 3));
    }
}
