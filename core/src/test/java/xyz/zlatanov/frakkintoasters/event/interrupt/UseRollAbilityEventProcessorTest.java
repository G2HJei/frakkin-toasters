package xyz.zlatanov.frakkintoasters.event.interrupt;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterFtlControlRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class UseRollAbilityEventProcessorTest extends EventTestHarness<UseRollAbilityEvent> {

    SkillCard consolidatePower = new SkillCard(2, CONSOLIDATE_POWER);

    @Test
    void shouldPutTheAbilityIntoTheWindowInSecret() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        rolledThenOpen(new AfterDieRoll());

        execute(UseRollAbilityEvent.use(1, ECO_OFFICER));

        assertEquals(List.of(new UsedRollAbility(1, ECO_OFFICER, null, null)), game.turn().windowAbilities());
        assertEquals(List.of(), game.turn().dieRoll().abilities()); // it takes effect when the window closes
    }

    @Test
    void shouldAcceptAPass() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        rolledThenOpen(new AfterDieRoll());

        execute(UseRollAbilityEvent.pass(1));

        assertEquals(List.of(), game.turn().windowAbilities());
    }

    @Test
    void shouldPassWithoutACard() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        rolledThenOpen(new AfterDieRoll());

        assertInvalid(new UseRollAbilityEvent(1, null, consolidatePower, null));
    }

    @Test
    void shouldUseOnlyAnAbilityOfHisCharacter() {
        characters(KARL_HELO_AGATHON, KARL_HELO_AGATHON_ALT, WILLIAM_ADAMA);
        rolledThenOpen(new AfterDieRoll());

        assertInvalid(UseRollAbilityEvent.use(2, ECO_OFFICER));
    }

    @Test
    void shouldRerollWithEcoOfficerOnceATurn() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        rolledThenOpen(new AfterDieRoll());
        ops.player(1).useAbility(ECO_OFFICER);

        assertInvalid(UseRollAbilityEvent.use(1, ECO_OFFICER));
    }

    @Test
    void shouldNotRerollWithEcoOfficerOnAnotherPlayersTurn() {
        characters(WILLIAM_ADAMA, KARL_HELO_AGATHON, LAURA_ROSLIN);
        rolledThenOpen(new AfterDieRoll());

        assertInvalid(UseRollAbilityEvent.use(2, ECO_OFFICER));
    }

    @Test
    void shouldNotRerollBeforeTheRoll() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());

        assertInvalid(UseRollAbilityEvent.use(1, ECO_OFFICER));
    }

    @Test
    void shouldNotRerollAFaceAnAbilitySet() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        ops.startDieRoll();
        ops.replaceDieRoll(4, false);
        ops.interrupts().openWindow(new AfterDieRoll());

        assertInvalid(UseRollAbilityEvent.use(1, ECO_OFFICER));
    }

    @Test
    void shouldRerollWithFtlOperatorOnTheRollOfFtlControl() {
        characters(WILLIAM_ADAMA, FELIX_GAETA, LAURA_ROSLIN);
        rolledThenOpen(new AfterFtlControlRoll());

        execute(UseRollAbilityEvent.use(2, FTL_OPERATOR));

        assertEquals(List.of(new UsedRollAbility(2, FTL_OPERATOR, null, null)), game.turn().windowAbilities());
    }

    @Test
    void shouldNotRerollWithFtlOperatorAnotherRoll() {
        characters(WILLIAM_ADAMA, FELIX_GAETA, LAURA_ROSLIN);
        rolledThenOpen(new AfterDieRoll());

        assertInvalid(UseRollAbilityEvent.use(2, FTL_OPERATOR));
    }

    @Test
    void shouldRerollWithFtlOperatorOnceARoll() {
        characters(WILLIAM_ADAMA, FELIX_GAETA, LAURA_ROSLIN);
        rolledThenOpen(new AfterFtlControlRoll());
        ops.useAbilityOnDieRoll(FTL_OPERATOR);

        assertInvalid(UseRollAbilityEvent.use(2, FTL_OPERATOR));
    }

    @Test
    void shouldChooseTheResultWithLongshotOnHisTurn() {
        characters(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());

        execute(new UseRollAbilityEvent(1, LONGSHOT, null, 8));

        assertEquals(List.of(new UsedRollAbility(1, LONGSHOT, null, 8)), game.turn().windowAbilities());
    }

    @Test
    void shouldChooseAResultOfTheDie() {
        characters(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());

        assertInvalid(new UseRollAbilityEvent(1, LONGSHOT, null, 9));
    }

    @Test
    void shouldChooseAResult() {
        characters(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());

        assertInvalid(UseRollAbilityEvent.use(1, LONGSHOT));
    }

    @Test
    void shouldNotChooseTheResultOnAnotherPlayersTurn() {
        characters(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());
        game.turn().currentPlayerNumber(2);

        assertInvalid(new UseRollAbilityEvent(1, LONGSHOT, null, 5));
    }

    @Test
    void shouldNotChooseTheResultWithoutAMiracleToken() {
        characters(SAMUEL_T_ANDERS, LAURA_ROSLIN, WILLIAM_ADAMA);
        startedThenOpen(new BeforeDieRoll());
        ops.player(1).loseMiracleTokens();

        assertInvalid(new UseRollAbilityEvent(1, LONGSHOT, null, 5));
    }

    @Test
    void shouldNameTheCardDiscardedForHotshotInHerActionStep() {
        katHoldsACardIn(ACTION);

        execute(new UseRollAbilityEvent(1, HOTSHOT, consolidatePower, null));

        assertEquals(List.of(new UsedRollAbility(1, HOTSHOT, consolidatePower, null)), game.turn().windowAbilities());
        assertEquals(List.of(consolidatePower), player(1).skillCards().cards()); // she discards it when the window closes
    }

    @Test
    void shouldDiscardACardForHotshot() {
        katHoldsACardIn(ACTION);

        assertInvalid(UseRollAbilityEvent.use(1, HOTSHOT));
    }

    @Test
    void shouldDiscardACardOfHerHandForHotshot() {
        katHoldsACardIn(ACTION);

        assertInvalid(new UseRollAbilityEvent(1, HOTSHOT, new SkillCard(5, CONSOLIDATE_POWER), null));
    }

    @Test
    void shouldNotUseHotshotOutOfHerActionStep() {
        katHoldsACardIn(MOVEMENT);

        assertInvalid(new UseRollAbilityEvent(1, HOTSHOT, consolidatePower, null));
    }

    @Test
    void shouldNotUseHotshotWhileAnotherPlayerActsInHerActionStep() {
        katHoldsACardIn(ACTION);
        ops.actingPlayer(2);

        assertInvalid(new UseRollAbilityEvent(1, HOTSHOT, consolidatePower, null));
    }

    @Test
    void shouldNeedAnOpenWindow() {
        characters(KARL_HELO_AGATHON, LAURA_ROSLIN, WILLIAM_ADAMA);
        rolled(3);

        assertInvalid(UseRollAbilityEvent.pass(1));
    }

    private void characters(Character... characters) {
        for (int i = 0; i < characters.length; i++) {
            player(i + 1).character(characters[i]);
        }
    }

    private void rolledThenOpen(InterruptWindow window) {
        rolled(3);
        ops.interrupts().openWindow(window);
    }

    private void startedThenOpen(InterruptWindow window) {
        ops.startDieRoll();
        ops.interrupts().openWindow(window);
    }

    private void katHoldsACardIn(GameStep step) {
        characters(LOUANNE_KAT_KATRAINE, LAURA_ROSLIN, WILLIAM_ADAMA);
        player(1).skillCards().addOnTop(consolidatePower);
        ops.changeStep(step);
        startedThenOpen(new BeforeDieRoll());
    }
}
