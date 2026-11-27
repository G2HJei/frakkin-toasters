package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.engine.GameEngine;
import xyz.zlatanov.frakkintoasters.engine.GiveSkillCardEvent;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingChoice;
import xyz.zlatanov.frakkintoasters.engine.PendingInput.AwaitingDecision;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptedTurns;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.constraint.ConstrainedEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EVASIVE_MANOEUVRES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class LegalAnswersEngineTest {

    // Every input of the scripted turns is among the answers, which are all taken; a choice's options have theirs.
    @Test
    void theScriptedTurnsAnswerFromTheAnswers() {
        val engine = ScriptedTurns.start();
        var decisions = 0;
        for (val step : ScriptedTurns.inputs()) {
            val input = step.apply(engine.view());
            switch (engine.pendingInput().orElseThrow()) {
                case AwaitingDecision(var decision) -> {
                    val answers = LegalAnswers.of(engine.view(), decision);
                    assertSound(engine, answers);
                    val answer = input.submit() instanceof ConstrainedEvent constrained
                            ? constrained.withConstraints(decision.eventConstraints())
                            : input.submit();
                    assertTrue(answers.covers(answer), answer + " in " + answers);
                    decisions++;
                }
                case AwaitingChoice choice -> choice.options().stream()
                        .filter(option -> LegalAnswers.decisionOf(option) != null)
                        .forEach(option -> assertFalse(LegalAnswers.forOption(engine.view(), option).isEmpty(),
                                option.toString()));
            }
            input.applyTo(engine);
        }
        assertTrue(decisions > 10);
    }

    // "Play before any die roll to add 2 to the result", or pass: "Evasive Maneuvers" is for another window.
    @Test
    void anInterruptWindowOffersTheCardsForItAndThePass() {
        val planning = new SkillCard(3, STRATEGIC_PLANNING);
        val evasive = new SkillCard(1, EVASIVE_MANOEUVRES);
        val engine = TestEngines.start(3, 42, new ScriptEvent(all(
                all(new SelectCharacterEvent(1, WILLIAM_ADAMA), new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE), new CreateDestinyDeckEvent()),
                single(new GiveSkillCardEvent(2, planning)), single(new GiveSkillCardEvent(2, evasive)),
                single(new KeepTabsOnVisitorFailEvent()), single(new CylonsWinEvent()))));
        val decision = ((AwaitingDecision) engine.pendingInput().orElseThrow()).decision();

        val answers = LegalAnswers.of(engine.view(), decision);

        assertSound(engine, answers);
        assertEquals(Set.of(PlayInterruptCardEvent.pass(2), PlayInterruptCardEvent.play(2, planning)),
                Set.copyOf(answers.events()));
        assertFalse(answers.covers(PlayInterruptCardEvent.play(2, evasive)));
    }

    private static void assertSound(GameEngine engine, LegalAnswers answers) {
        answers.events().forEach(event -> assertTaken(engine, event));
        answers.shapes().forEach(shape -> assertTaken(engine, shape.example()));
    }

    private static void assertTaken(GameEngine engine, Event event) {
        assertDoesNotThrow(() -> ProcessorRegistry.create(event).prepare(engine.view(), event), event.toString());
    }
}
