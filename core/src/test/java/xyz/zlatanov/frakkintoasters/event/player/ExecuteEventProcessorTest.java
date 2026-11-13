package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.GiveLoyaltyCardsEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.PassMutineerCardEvent;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class ExecuteEventProcessorTest extends EventTestHarness<ExecuteEvent> {

    private static final SkillCard LAUNCH_SCOUT_3 = new SkillCard(3, LAUNCH_SCOUT);

    @BeforeEach
    void setUp() {
        player(1).character(HELENA_CAIN);
        moveTo(AIRLOCK, HELENA_CAIN);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(3).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
        player(2).gainSkillCards(LAUNCH_SCOUT_3);
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
    }

    // "He discards his hand of Skill and Mutiny Cards and his miracle token(s)"
    @Test
    void shouldDiscardTheHandAndTheMiracleToken() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON);

        execute(new ExecuteEvent(2));

        assertNoSkillCards(2);
        assertEquals(List.of(LAUNCH_SCOUT_3), tacticsDeck.discardedCards());
        assertTrue(player(2).mutinyCards().isEmpty());
        assertEquals(List.of(WEAPONS_ARMED), mutinyDeck.discardedCards());
        assertFalse(player(2).hasMiracleToken());
        assertFollowupSelection();
    }

    // "Lose 1 morale", the character "may no longer be used during the game", the player discards his Loyalty Cards
    // and chooses a new character.
    @Test
    void shouldReplaceAnExecutedHuman() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
        val morale = galacticaBoard.morale();

        execute(new ExecuteEvent(2));

        assertEquals(morale - 1, galacticaBoard.morale());
        assertNull(player(2).character());
        assertEquals(Optional.empty(), game.locate(KARA_STARBUCK_THRACE));
        assertTrue(game.removedComponents().contains(KARA_STARBUCK_THRACE));
        assertTrue(player(2).loyaltyCards().isEmpty());
        assertEquals(List.of(NOT_CYLON), loyaltyDeck.discardedCards());
        assertFalse(game.turn().endedEarly());
        assertFollowupSelection();
    }

    // "If his character was the Mutineer, his new character immediately receives the "You Are the Mutineer" Card"
    @Test
    void shouldLetTheMutineerKeepTheCard() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON, MUTINEER);

        execute(new ExecuteEvent(2));

        assertEquals(List.of(MUTINEER), player(2).loyaltyCards().cards());
        assertFollowupSelection();
    }

    // "If the executed character was being played by the current player, that player's turn ends"
    @Test
    void shouldEndTheTurnOfTheCurrentPlayer() {
        player(1).loyaltyCards().addOnTop(NOT_CYLON);

        execute(new ExecuteEvent(1));

        assertTrue(game.turn().endedEarly());
        assertFollowup(all(
                new PlayerDecisionEvent<>(1, SelectCharacterEvent.class),
                new FinishExecutionEvent(1, HELENA_CAIN)));
    }

    // A Cylon reveals one "You Are a Cylon" card, gives his other Loyalty Cards to a human of his choice, and resurrects.
    @Test
    void shouldRevealAnExecutedCylon() {
        player(2).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, NOT_CYLON);

        execute(new ExecuteEvent(2));

        assertEquals(List.of(CYLON_DAMAGE_GALACTICA), player(2).loyaltyCards().revealedCards());
        assertTrue(player(2).isRevealedCylon());
        assertEquals(KARA_STARBUCK_THRACE, player(2).character());
        assertEquals(RESURRECTION_SHIP, locate(KARA_STARBUCK_THRACE));
        assertFollowup(all(
                one(new GiveLoyaltyCardsEvent(2, 3), new GiveLoyaltyCardsEvent(2, 1)),
                single(new GiveTitlesToNextInLineEvent(2))));
    }

    // "If his character was the Mutineer, he chooses a human player and passes him the "You Are the Mutineer" Loyalty
    // Card faceup"
    @Test
    void shouldLetACylonMutineerPassTheCard() {
        player(2).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA, MUTINEER);

        execute(new ExecuteEvent(2));

        assertFollowup(all(
                one(new PassMutineerCardEvent(2, 3), new PassMutineerCardEvent(2, 1)),
                single(new GiveTitlesToNextInLineEvent(2))));
    }

    // "If he was piloting a viper it is returned to the Reserves." (rule 6.1.1)
    @Test
    void shouldReturnTheViperOfACylonPilot() {
        player(2).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA);
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(2).pilot(viper.id());

        execute(new ExecuteEvent(2));

        assertEquals(RESURRECTION_SHIP, locate(KARA_STARBUCK_THRACE));
        assertTrue(galacticaBoard.reserves().contains(viper));
        assertFollowup(new GiveTitlesToNextInLineEvent(2));
    }

    // "If a player is a Cylon Leader, he has no Loyalty Cards to reveal, but instead proceeds to step 4, "Cylon"."
    @Test
    void shouldReturnAnInfiltratingCylonLeaderToTheResurrectionShip() {
        setUpGame(4);
        player(1).character(HELENA_CAIN);
        player(4).character(SHARON_ATHENA_AGATHON);
        ops.player(4).infiltrateGalactica(HANGAR_DECK);

        execute(new ExecuteEvent(4));

        assertFalse(player(4).isInfiltrating());
        assertEquals(RESURRECTION_SHIP, locate(SHARON_ATHENA_AGATHON));
    }

    // A revealed Cylon may ignore an execution (rule 6.1.2.1).
    @Test
    void shouldNotExecuteARevealedCylon() {
        player(2).loyaltyCards().addOnTop(CYLON_DAMAGE_GALACTICA).reveal(CYLON_DAMAGE_GALACTICA);
        assertInvalid(new ExecuteEvent(2));
    }

    // "If a human is executed and there are no available characters left to be selected, then the humans immediately
    // lose the game." (rule 7.2)
    @Test
    void shouldLetTheCylonsWinWhenNoCharacterIsLeft() {
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
        for (val character : Character.values()) {
            game.removedComponents().add(character);
        }

        execute(new ExecuteEvent(2));

        assertFollowup(new CylonsWinEvent());
    }

    private void assertFollowupSelection() {
        assertFollowup(all(
                new PlayerDecisionEvent<>(2, SelectCharacterEvent.class),
                new FinishExecutionEvent(2, KARA_STARBUCK_THRACE)));
    }
}
