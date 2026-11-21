package xyz.zlatanov.frakkintoasters.event.location.cylon;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.crisis.DiscardSuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveSuperCrisisCardEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.HUMAN_PRISONER;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;

class CapricaPlaySuperCrisisCardEventProcessorTest extends EventTestHarness<CapricaPlaySuperCrisisCardEvent> {

    @BeforeEach
    void setUpRevealedCylonAtCaprica() {
        player(1).character(FELIX_GAETA);
        revealCylon();
        moveTo(CAPRICA, FELIX_GAETA);
        player(1).superCrisisCards().addOnTop(INBOUND_NUKES);
    }

    @Test
    void shouldPlayTheCardThenDiscardIt() {
        execute(new CapricaPlaySuperCrisisCardEvent(1, INBOUND_NUKES));

        assertTrue(player(1).superCrisisCards().isEmpty());
        assertFollowup(all(new ResolveSuperCrisisCardEvent(1, INBOUND_NUKES), new DiscardSuperCrisisCardEvent(INBOUND_NUKES)));
    }

    @Test
    void shouldRejectACardHeDoesNotHold() {
        assertInvalid(new CapricaPlaySuperCrisisCardEvent(1, HUMAN_PRISONER));
    }

    @Test
    void shouldRejectNoCard() {
        assertInvalid(new CapricaPlaySuperCrisisCardEvent(1, null));
    }

    @Test
    void shouldRejectAPlayerAwayFromCaprica() {
        moveTo(RESURRECTION_SHIP, FELIX_GAETA);
        assertInvalid(new CapricaPlaySuperCrisisCardEvent(1, INBOUND_NUKES));
    }
}
