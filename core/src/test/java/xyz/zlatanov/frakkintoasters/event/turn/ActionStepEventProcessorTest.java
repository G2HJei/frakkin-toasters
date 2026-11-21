package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;

class ActionStepEventProcessorTest extends EventTestHarness<ActionStepEvent> {

    private static final SkillCard BUILD_NUKE_6 = new SkillCard(6, BUILD_NUKE);

    @Test
    void theCurrentPlayerMayActivateTheirLocation() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);

        execute(new ActionStepEvent());

        assertEquals(ACTION, game.turn().step());
        assertFollowup(one(new PlayerDecisionEvent<>(1, ActionEvent.class), new NoOpEvent(1)));
    }

    // "Activate his Viper: If the player is piloting a viper, he may activate it to move or to attack a Cylon ship."
    @Test
    void aPilotMayActivateTheirViper() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());

        execute(new ActionStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class), new NoOpEvent(1)));
    }

    // A pilot may take the normal actions of the Action step, e.g. play a Skill Card, or activate his viper instead
    // (rule 11.2.1).
    @Test
    void aPilotWithAnActionCardMayPlayItInstead() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());
        player(1).gainSkillCards(BUILD_NUKE_6);

        execute(new ActionStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class),
                new PlayerDecisionEvent<>(1, ActionEvent.class),
                new NoOpEvent(1)));
    }

    @Test
    void aCharacterInSickbayMayPlayAnActionCard() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(BUILD_NUKE_6);

        execute(new ActionStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActionEvent.class), new NoOpEvent(1)));
    }

    // A card whose effect can't happen isn't offered: the Admiral holds 3 nukes at most (rule 11.6.1).
    @Test
    void aCardThatCannotBePlayedGivesNoAction() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(BUILD_NUKE_6);
        ops.gainNuke();

        execute(new ActionStepEvent());
    }

    // A revealed Cylon may only take the action of his location (rule 6.1.3.3).
    @Test
    void aRevealedCylonCannotPlayAnActionCard() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(BUILD_NUKE_6);
        revealCylon();

        execute(new ActionStepEvent());
    }

    @Test
    void strandedHeloTakesNoActions() {
        player(1).character(KARL_HELO_AGATHON);

        execute(new ActionStepEvent());

        assertEquals(ACTION, game.turn().step());
    }

    // Choosing to act would leave him without a legal answer.
    @Test
    void aCharacterInSickbayHasNoActionToTake() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);

        execute(new ActionStepEvent());

        assertEquals(ACTION, game.turn().step());
    }

    @Test
    void aDamagedLocationHasNoActionToTake() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        galacticaBoard.damage(HANGAR_DECK);

        execute(new ActionStepEvent());
    }

    @Test
    void ftlControlHasNoActionToTakeInTheRedZone() {
        player(1).character(FELIX_GAETA);
        moveTo(FTL_CONTROL, FELIX_GAETA);

        execute(new ActionStepEvent());
    }

    @Test
    void ftlControlHasAnActionToTakeOutOfTheRedZone() {
        player(1).character(FELIX_GAETA);
        moveTo(FTL_CONTROL, FELIX_GAETA);
        galacticaBoard.advanceJumpPreparation().advanceJumpPreparation().advanceJumpPreparation();

        execute(new ActionStepEvent());

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActionEvent.class), new NoOpEvent(1)));
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new ActionStepEvent());

        assertEquals(SETUP, game.turn().step());
    }
}
