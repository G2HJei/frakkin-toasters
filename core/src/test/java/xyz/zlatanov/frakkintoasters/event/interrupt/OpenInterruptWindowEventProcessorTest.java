package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.*;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_6_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class OpenInterruptWindowEventProcessorTest extends EventTestHarness<OpenInterruptWindowEvent> {

    SkillCard strategicPlanning = new SkillCard(3, STRATEGIC_PLANNING);
    SkillCard calculations      = new SkillCard(3, CALCULATIONS);
    SkillCard runInterference   = new SkillCard(3, RUN_INTERFERENCE);
    SkillCard majorVictory      = new SkillCard(1, MAJOR_VICTORY);

    @BeforeEach
    void setUpCharacters() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        player(3).character(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldNotPauseWithoutHolders() {
        player(1).skillCards().addOnTop(calculations); // for another window

        execute(new OpenInterruptWindowEvent(new BeforeDieRoll()));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
        assertNull(game.turn().dieRoll().value()); // the roll it comes before has started
    }

    // "Longshot": the result Anders chose isn't modified or rerolled, so no window opens after it.
    @Test
    void shouldNotOpenAfterAFixedResult() {
        player(2).skillCards().addOnTop(calculations);
        ops.startDieRoll();
        ops.replaceDieRoll(5, true);

        execute(new OpenInterruptWindowEvent(new AfterDieRoll()));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldAskTheHoldersClockwiseFromTheCurrentPlayer() {
        game.turn().currentPlayerNumber(2);
        player(1).skillCards().addOnTop(strategicPlanning);
        player(3).skillCards().addOnTop(strategicPlanning);

        execute(new OpenInterruptWindowEvent(new BeforeDieRoll()));

        assertFollowup(all(
                new PlayerDecisionEvent<>(3, PlayInterruptCardEvent.class),
                new PlayerDecisionEvent<>(1, PlayInterruptCardEvent.class),
                new CloseInterruptWindowEvent()));
        assertEquals(Optional.of(new BeforeDieRoll()), game.turn().interruptWindow());
    }

    @Test
    void shouldNotAskARevealedCylon() {
        revealCylon();
        player(1).skillCards().addOnTop(strategicPlanning);

        execute(new OpenInterruptWindowEvent(new BeforeDieRoll()));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldNotStartADieRollAfterIt() {
        rolled(5);
        val roll = game.turn().dieRoll();

        execute(new OpenInterruptWindowEvent(new AfterDieRoll()));

        assertSame(roll, game.turn().dieRoll());
    }

    @Test
    void shouldAskForCalculationsOncePerDieRoll() {
        rolled(5);
        player(2).skillCards().addOnTop(calculations);
        ops.playOnDieRoll(CALCULATIONS, 1); // played in the window before a reroll

        execute(new OpenInterruptWindowEvent(new AfterViperAttacked(1)));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldAskForRunInterferenceOnlyAPilotWithRaidersInHisSpaceArea() {
        player(3).skillCards().addOnTop(runInterference);
        player(2).skillCards().addOnTop(runInterference);
        ops.player(3).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());
        raiderAt(GALACTICA_SPACE_4_OCLOCK);
        raiderAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new OpenInterruptWindowEvent(new BeforeRaidersActivate()));

        assertFollowup(all(new PlayerDecisionEvent<>(3, PlayInterruptCardEvent.class), new CloseInterruptWindowEvent()));
    }

    @Test
    void shouldNotAskForRunInterferenceWithoutRaidersInThePilotsSpaceArea() {
        player(3).skillCards().addOnTop(runInterference);
        ops.player(3).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());
        raiderAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new OpenInterruptWindowEvent(new BeforeRaidersActivate()));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
    }

    @Test
    void shouldAskForMajorVictoryOnlyThePlayerWhoDestroyedTheShip() {
        player(1).skillCards().addOnTop(majorVictory);
        player(2).skillCards().addOnTop(majorVictory);
        ops.actingPlayer(2);

        execute(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));

        assertFollowup(all(new PlayerDecisionEvent<>(2, PlayInterruptCardEvent.class), new CloseInterruptWindowEvent()));
    }

    @Test
    void shouldAskForMajorVictoryOnceATurn() {
        player(1).skillCards().addOnTop(majorVictory, majorVictory);
        val played = new PlayedInterruptCard(1, majorVictory, null);
        ops.interrupts().openWindow(new AfterBasestarOrCenturionDestroyed());
        ops.interrupts().play(played);
        ops.interrupts().resolve(played);
        ops.interrupts().closeWindow();

        execute(new OpenInterruptWindowEvent(new AfterBasestarOrCenturionDestroyed()));

        assertEquals(Optional.empty(), game.turn().interruptWindow());
        assertEquals(List.of(MAJOR_VICTORY), game.turn().resolvedInterruptCards());
    }

    @Test
    void shouldNotOpenAWindowWhileOneIsOpen() {
        ops.interrupts().openWindow(new BeforeDieRoll());

        assertInvalid(new OpenInterruptWindowEvent(new AfterDieRoll()));
    }
}
