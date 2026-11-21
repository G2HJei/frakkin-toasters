package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class DefendPresidencyEventProcessorTest extends EventTestHarness<DefendPresidencyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        player(2).character(LAURA_ROSLIN);
        player(3).character(SAUL_TIGH);
        president(LAURA_ROSLIN);
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        ops.titles().putInPlay(QuorumCard.ACCEPT_PROPHECY, null);
    }

    @Test
    void discardingAcceptProphecyKeepsTheTitle() {
        execute(new DefendPresidencyEvent(2, true, 1));

        assertEquals(List.of(), game.quorumCardsInPlay());
        assertEquals(List.of(QuorumCard.ACCEPT_PROPHECY), quorumDeck.discardedCards());
    }

    @Test
    void keepingTheCardLetsThePlayerOfTheAdministrationChoose() {
        execute(new DefendPresidencyEvent(2, false, 1));

        assertTrue(game.inPlay(QuorumCard.ACCEPT_PROPHECY));
        assertFollowup(new PlayerDecisionEvent<>(1, MakePresidentEvent.class));
    }

    @Test
    void onlyThePresident() {
        assertInvalid(new DefendPresidencyEvent(1, true, 1));
    }
}
