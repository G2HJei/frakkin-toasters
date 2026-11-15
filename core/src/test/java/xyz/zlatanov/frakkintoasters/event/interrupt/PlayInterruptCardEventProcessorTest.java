package xyz.zlatanov.frakkintoasters.event.interrupt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class PlayInterruptCardEventProcessorTest extends EventTestHarness<PlayInterruptCardEvent> {

    SkillCard strategicPlanning = new SkillCard(3, STRATEGIC_PLANNING);
    SkillCard calculations      = new SkillCard(3, CALCULATIONS);

    @BeforeEach
    void setUpWindow() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        player(2).skillCards().addOnTop(strategicPlanning, calculations);
        ops.interrupts().openWindow(new BeforeDieRoll());
    }

    @Test
    void shouldPlayTheCardFaceDownIntoTheWindow() {
        execute(PlayInterruptCardEvent.play(2, strategicPlanning));

        assertEquals(List.of(new PlayedInterruptCard(2, strategicPlanning, null)), game.turn().windowCards());
        assertSkillCards(2, calculations);
    }

    @Test
    void shouldPass() {
        execute(PlayInterruptCardEvent.pass(2));

        assertEquals(List.of(), game.turn().windowCards());
        assertSkillCards(2, strategicPlanning, calculations);
    }

    @Test
    void shouldPlayACardTheWindowTakes() {
        assertInvalid(new PlayInterruptCardEvent(2, calculations, 1));
    }

    @Test
    void shouldPlayACardFromTheHand() {
        assertInvalid(PlayInterruptCardEvent.play(1, strategicPlanning));
    }

    @Test
    void shouldNotPlayAsARevealedCylon() {
        player(1).skillCards().addOnTop(strategicPlanning);
        revealCylon();

        assertInvalid(PlayInterruptCardEvent.play(1, strategicPlanning));
    }

    @Test
    void shouldPlayOnlyIntoAnOpenWindow() {
        ops.interrupts().closeWindow();

        assertInvalid(PlayInterruptCardEvent.pass(2));
    }

    @Test
    void shouldAddOrSubtract1WithCalculations() {
        reopen();

        execute(new PlayInterruptCardEvent(2, calculations, -1));

        assertEquals(List.of(new PlayedInterruptCard(2, calculations, -1)), game.turn().windowCards());
    }

    @Test
    void shouldNameWhatCalculationsAdds() {
        reopen();

        assertInvalid(PlayInterruptCardEvent.play(2, calculations));
    }

    @Test
    void shouldAddOrSubtractOnly1WithCalculations() {
        reopen();

        assertInvalid(new PlayInterruptCardEvent(2, calculations, 2));
    }

    @Test
    void shouldNameAModifierOnlyForCalculations() {
        assertInvalid(new PlayInterruptCardEvent(2, strategicPlanning, 1));
    }

    @Test
    void shouldNameNoModifierToPass() {
        assertInvalid(new PlayInterruptCardEvent(2, null, 1));
    }

    private void reopen() {
        ops.interrupts().closeWindow();
        rolled(4);
        ops.interrupts().openWindow(new AfterDieRoll());
    }
}
