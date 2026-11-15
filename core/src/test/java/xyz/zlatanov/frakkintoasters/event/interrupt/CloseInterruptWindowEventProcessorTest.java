package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.*;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class CloseInterruptWindowEventProcessorTest extends EventTestHarness<CloseInterruptWindowEvent> {

    @BeforeEach
    void setUpCharacters() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LOUANNE_KAT_KATRAINE);
        player(3).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldCloseTheWindowWhenEveryonePassed() {
        ops.interrupts().openWindow(new BeforeRaidersActivate());

        execute(new CloseInterruptWindowEvent());

        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldAdd2WithStrategicPlanningAndDiscardIt() {
        ops.startDieRoll();
        val card = open(new BeforeDieRoll(), 2, STRATEGIC_PLANNING, null);

        execute(new CloseInterruptWindowEvent());

        assertEquals(List.of(STRATEGIC_PLANNING), game.turn().dieRoll().cards());
        assertEquals(2, game.turn().dieRoll().modifier());
        assertEquals(List.of(card.card()), tacticsDeck.discardedCards());
        assertEquals(List.of(STRATEGIC_PLANNING), game.turn().resolvedInterruptCards());
        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldAddOrSubtract1WithCalculations() {
        rolled(4);
        open(new AfterDieRoll(), 2, CALCULATIONS, -1);

        execute(new CloseInterruptWindowEvent());

        assertEquals(3, game.turn().dieRoll().result());
    }

    @Test
    void shouldLetTheCurrentPlayerSettleConflictingCards() {
        ops.startDieRoll();
        ops.interrupts().openWindow(new BeforeDieRoll());
        play(2, STRATEGIC_PLANNING, null);
        play(3, STRATEGIC_PLANNING, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(all(new PlayerDecisionEvent<>(1, TiebreakInterruptCardsEvent.class), new CloseInterruptWindowEvent()));
        assertEquals(2, game.turn().windowCards().size());
    }

    @Test
    void shouldResolveRunInterferenceInEachPilotsSpaceArea() {
        pilotWithRaiders(2, GALACTICA_SPACE_4_OCLOCK, 5);
        pilotWithRaiders(3, GALACTICA_SPACE_6_OCLOCK, 2);
        ops.interrupts().openWindow(new BeforeRaidersActivate());
        play(2, RUN_INTERFERENCE, null);
        play(3, RUN_INTERFERENCE, null);

        execute(new CloseInterruptWindowEvent());

        // "The first 4 raiders", out of those that activate there
        assertEquals(Map.of(GALACTICA_SPACE_4_OCLOCK, 4, GALACTICA_SPACE_6_OCLOCK, 2), game.turn().raidersToMiss());
    }

    @Test
    void shouldConflictRunInterferenceInTheSameSpaceArea() {
        pilotWithRaiders(2, GALACTICA_SPACE_4_OCLOCK, 1);
        pilotWithRaiders(3, GALACTICA_SPACE_4_OCLOCK, 1);
        ops.interrupts().openWindow(new BeforeRaidersActivate());
        play(2, RUN_INTERFERENCE, null);
        play(3, RUN_INTERFERENCE, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(all(new PlayerDecisionEvent<>(1, TiebreakInterruptCardsEvent.class), new CloseInterruptWindowEvent()));
    }

    @Test
    void shouldRerollWithEvasiveManoeuvresAndSubtract2ForAPilotedViper() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(3).pilot(viper.id());
        rolled(7);
        val window = new AfterViperAttacked(viper.id());
        open(window, 2, EVASIVE_MANOEUVRES, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(all(new RollDieEvent(), new OpenInterruptWindowEvent(window)));
        assertEquals(-2, game.turn().dieRoll().modifier());
    }

    @Test
    void shouldSubtract2OnceForTwoRerolls() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        ops.player(3).pilot(viper.id());
        rolled(7);
        ops.playOnDieRoll(EVASIVE_MANOEUVRES, -2);
        open(new AfterViperAttacked(viper.id()), 2, EVASIVE_MANOEUVRES, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(all(new RollDieEvent(), new OpenInterruptWindowEvent(new AfterViperAttacked(viper.id()))));
        assertEquals(-2, game.turn().dieRoll().modifier());
        assertEquals(List.of(EVASIVE_MANOEUVRES, EVASIVE_MANOEUVRES), game.turn().dieRoll().cards());
    }

    @Test
    void shouldRerollWithEvasiveManoeuvresWithoutSubtractingForAnUnmannedViper() {
        val viper = viperAt(GALACTICA_SPACE_4_OCLOCK);
        rolled(7);
        open(new AfterViperAttacked(viper.id()), 2, EVASIVE_MANOEUVRES, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(all(new RollDieEvent(), new OpenInterruptWindowEvent(new AfterViperAttacked(viper.id()))));
        assertEquals(0, game.turn().dieRoll().modifier());
    }

    @Test
    void shouldMarkTheAttackRollForBestOfTheBest() {
        ops.startDieRoll();
        open(new BeforePilotedViperAttack(), 2, BEST_OF_THE_BEST, null);

        execute(new CloseInterruptWindowEvent());

        assertEquals(List.of(BEST_OF_THE_BEST), game.turn().dieRoll().cards());
        assertEquals(0, game.turn().dieRoll().modifier());
    }

    @Test
    void shouldRollForMajorVictory() {
        open(new AfterBasestarOrCenturionDestroyed(), 1, MAJOR_VICTORY, null);

        execute(new CloseInterruptWindowEvent());

        assertFollowup(rollDie(new MajorVictoryResultEvent()));
        assertEquals(List.of(MAJOR_VICTORY), game.turn().resolvedInterruptCards());
    }

    @Test
    void shouldCloseOnlyAnOpenWindow() {
        assertInvalid(new CloseInterruptWindowEvent());
    }

    private PlayedInterruptCard open(InterruptWindow window, int playerNumber, SkillCardType type, Integer modifier) {
        ops.interrupts().openWindow(window);
        return play(playerNumber, type, modifier);
    }

    private PlayedInterruptCard play(int playerNumber, SkillCardType type, Integer modifier) {
        val card = new SkillCard(3, type);
        player(playerNumber).skillCards().addOnTop(card);
        val played = new PlayedInterruptCard(playerNumber, card, modifier);
        ops.interrupts().play(played);
        return played;
    }

    private void pilotWithRaiders(int playerNumber, Location area, int raiders) {
        ops.player(playerNumber).pilot(viperAt(area).id());
        for (int i = 0; i < raiders; i++) {
            raiderAt(area);
        }
    }
}
