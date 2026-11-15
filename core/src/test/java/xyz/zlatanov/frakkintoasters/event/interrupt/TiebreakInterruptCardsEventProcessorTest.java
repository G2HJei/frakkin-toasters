package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterViperAttacked;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EVASIVE_MANOEUVRES;

class TiebreakInterruptCardsEventProcessorTest extends EventTestHarness<TiebreakInterruptCardsEvent> {

    SkillCard calculations = new SkillCard(3, CALCULATIONS);
    SkillCard evasive      = new SkillCard(1, EVASIVE_MANOEUVRES);

    PlayedInterruptCard plusOne;
    PlayedInterruptCard minusOne;
    PlayedInterruptCard reroll;

    @BeforeEach
    void setUpConflict() {
        player(1).character(WILLIAM_ADAMA);
        player(2).character(LAURA_ROSLIN);
        player(3).character(KARA_STARBUCK_THRACE);
        player(1).skillCards().addOnTop(calculations, evasive);
        player(2).skillCards().addOnTop(calculations);
        rolled(4);
        ops.interrupts().openWindow(new AfterViperAttacked(1));
        plusOne = play(new PlayedInterruptCard(1, calculations, 1));
        minusOne = play(new PlayedInterruptCard(2, calculations, -1));
        reroll = play(new PlayedInterruptCard(1, evasive, null));
    }

    @Test
    void shouldReturnTheCardsThatDoNotStand() {
        execute(new TiebreakInterruptCardsEvent(minusOne));

        assertEquals(List.of(minusOne, reroll), game.turn().windowCards());
        assertSkillCards(1, calculations);
        assertSkillCards(2);
    }

    @Test
    void shouldKeepACardOfEachConflict() {
        assertInvalid(new TiebreakInterruptCardsEvent());
    }

    @Test
    void shouldKeepOneCardOfAConflict() {
        assertInvalid(new TiebreakInterruptCardsEvent(plusOne, minusOne));
    }

    @Test
    void shouldKeepOnlyConflictingCards() {
        assertInvalid(new TiebreakInterruptCardsEvent(plusOne, reroll));
    }

    private PlayedInterruptCard play(PlayedInterruptCard card) {
        ops.interrupts().play(card);
        return card;
    }
}
