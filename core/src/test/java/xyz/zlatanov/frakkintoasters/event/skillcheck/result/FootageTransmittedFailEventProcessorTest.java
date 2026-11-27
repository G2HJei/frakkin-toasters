package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.character.Character;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_DAMAGE_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.FIGHTING_BLIND;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.INBOUND_NUKES;

class FootageTransmittedFailEventProcessorTest extends EventTestHarness<FootageTransmittedFailEvent> {

    private static final List<Character> HUMANS = List.of(WILLIAM_ADAMA, LAURA_ROSLIN, KARA_STARBUCK_THRACE, SAUL_TIGH,
            GAIUS_BALTAR, TOM_ZAREK, FELIX_GAETA);

    @Test
    void shouldGiveEachRevealedCylonSuperCrisisCard() {
        characters();
        revealCylon();
        player(3).loyaltyCards()
                .addOnTop(CYLON_DAMAGE_GALACTICA)
                .reveal(CYLON_DAMAGE_GALACTICA);
        superCrisisDeck.nextCard(FIGHTING_BLIND, INBOUND_NUKES);

        execute(new FootageTransmittedFailEvent());

        assertEquals(List.of(FIGHTING_BLIND), player(1).superCrisisCards().cards());
        assertTrue(player(2).superCrisisCards().isEmpty());
        assertEquals(List.of(INBOUND_NUKES), player(3).superCrisisCards().cards());
    }

    // A Cylon Leader counts as a revealed Cylon (rule 6.2.1), unless he is Infiltrating.
    @Test
    void shouldGiveACylonLeaderSuperCrisisCard() {
        player(2).character(CAVIL);
        characters();
        superCrisisDeck.nextCard(FIGHTING_BLIND);

        execute(new FootageTransmittedFailEvent());

        assertEquals(List.of(FIGHTING_BLIND), player(2).superCrisisCards().cards());
        assertTrue(player(1).superCrisisCards().isEmpty());
    }

    @Test
    void shouldDrawNothingWhenNoSuperCrisisCardIsLeft() {
        characters();
        revealCylon();
        superCrisisDeck.clear();

        execute(new FootageTransmittedFailEvent());

        assertTrue(player(1).superCrisisCards().isEmpty());
    }

    @Test
    void shouldDoNothingWithoutRevealedCylons() {
        characters();

        execute(new FootageTransmittedFailEvent());

        game.players().forEach(p -> assertTrue(p.superCrisisCards().isEmpty()));
    }

    private void characters() {
        val humans = HUMANS.iterator();
        for (val player : game.players()) {
            if (player.character() == null) {
                player(player.number()).character(humans.next());
            }
        }
    }
}
