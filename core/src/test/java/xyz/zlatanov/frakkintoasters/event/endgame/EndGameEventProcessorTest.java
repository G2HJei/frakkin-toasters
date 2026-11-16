package xyz.zlatanov.frakkintoasters.event.endgame;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.Team.CYLONS;
import static xyz.zlatanov.frakkintoasters.state.Team.HUMANS;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.MotiveCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

// "If the humans may have won, all unrevealed Personal Goal Loyalty Cards have to be examined." (rule 16.2)
class EndGameEventProcessorTest extends EventTestHarness<EndGameEvent> {

    @BeforeEach
    void players() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(1).loyaltyCards().addOnTop(NOT_CYLON);
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
        player(3).loyaltyCards().addOnTop(NOT_CYLON);
    }

    @Test
    void theHumansWinWhenNoResourceIsDepleted() {
        execute(new EndGameEvent());

        assertEquals(Optional.of(HUMANS), game.outcome());
        assertEquals(List.of(1, 2, 3), game.winners());
    }

    // "Each unrevealed Personal Goal Loyalty Card belonging to a human player at the end of the game reduces the resource
    // listed on the card."
    @Test
    void anUnrevealedPersonalGoalReducesItsResource() {
        player(1).loyaltyCards().addOnTop(NOT_CYLON_SELFISH); // fuel
        val fuel = galacticaBoard.fuel();

        execute(new EndGameEvent());

        assertEquals(fuel - 1, galacticaBoard.fuel());
        assertEquals(Optional.of(HUMANS), game.outcome());
    }

    // "If, however, any resource has been reduced to 0, then the victory goes to the Cylons."
    @Test
    void aResourceReducedToZeroGivesTheCylonsTheVictory() {
        player(1).loyaltyCards().addOnTop(NOT_CYLON_SELFISH);
        player(2).loyaltyCards().addOnTop(NOT_CYLON_SACRIFICE); // both fuel
        galacticaBoard.decreaseFuel(galacticaBoard.fuel() - 2);

        execute(new EndGameEvent());

        assertEquals(0, galacticaBoard.fuel());
        assertEquals(Optional.of(CYLONS), game.outcome());
    }

    // "If a revealed Cylon player has an unrevealed Personal Goal Card at the end of the game, do not reduce the resource."
    @Test
    void aRevealedCylonDoesNotReduceAnything() {
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG, NOT_CYLON_SELFISH).reveal(CYLON_SEND_TO_BRIG);
        val fuel = galacticaBoard.fuel();

        execute(new EndGameEvent());

        assertEquals(fuel, galacticaBoard.fuel());
    }

    // An unrevealed Cylon is a human until he reveals, and wins with the Cylons.
    @Test
    void theHumansWinAndTheHiddenCylonLoses() {
        player(3).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG);

        execute(new EndGameEvent());

        assertEquals(Optional.of(HUMANS), game.outcome());
        assertEquals(List.of(1, 2), game.winners());
    }

    // "A Cylon Leader wins along with the winning team if at the end of the game he has no more than one Motive Card that
    // he has not revealed and has revealed at least two Motive Cards with an allegiance of the winning team." (rule 16.3.2)
    @Test
    void aCylonLeaderWinsWithTheHumansByHisMotiveCards() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(4).character(SHARON_ATHENA_AGATHON);
        player(4).motiveCards().addOnTop(IMPROVE_EFFICIENCY, PRESSURE_THEIR_LEADERS, KEEP_THEM_DOCILE, SUBJECTS_TO_STUDY);
        galacticaBoard.decreaseMorale(galacticaBoard.morale() - 5); // "morale is 5 or less"
        galacticaBoard.decreaseFood(galacticaBoard.food() - 4);     // "food is 4 or less"

        execute(new EndGameEvent());

        assertEquals(Optional.of(HUMANS), game.outcome());
        // three cards could be revealed, two of them human; only "Improve Efficiency" stays hidden
        assertEquals(3, game.player(4).motiveCards().revealedCards().size());
        assertTrue(game.winners().contains(4));
    }

    @Test
    void aCylonLeaderWithoutEnoughMotiveCardsLoses() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(4).character(SHARON_ATHENA_AGATHON);
        player(4).motiveCards().addOnTop(IMPROVE_EFFICIENCY, MAKE_AN_ALLY, END_THE_CHASE, SAVOR_THEIR_DEMISE);

        execute(new EndGameEvent());

        assertEquals(Optional.of(HUMANS), game.outcome());
        assertFalse(game.winners().contains(4));
    }
}
