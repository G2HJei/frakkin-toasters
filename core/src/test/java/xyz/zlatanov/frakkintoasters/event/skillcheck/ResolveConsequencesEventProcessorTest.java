package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.*;

class ResolveConsequencesEventProcessorTest extends EventTestHarness<ResolveConsequencesEvent> {

    public static Stream<Arguments> shouldFollowupWithConsequence() {
        return Stream.of(
                arguments(HAUNTED_BY_THE_PAST, single(new GiveTitlesToNextInLineEvent(1))),
                arguments(POWER_FAILURE, single(new DamageGalacticaEvent())),
                arguments(THREAT_OF_SUPER_NOVA, single(new ActivateCylonBasestarsEvent())),
                arguments(FIRE_ALL_MISSILES, all(new DamageGalacticaEvent(), new DamageGalacticaEvent()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldFollowupWithConsequence(SkillCheckType skillCheckType, Followup expected) {
        game.startSkillCheck(skillCheckType);
        execute(new ResolveConsequencesEvent());
        assertFollowup(expected);
    }

    // The character already in "Sickbay" can't be chosen (rule 9.3.2).
    @ParameterizedTest
    @EnumSource(value = SkillCheckType.class, names = {"DIVISIVE_BEHAVIOR", "IN_THE_RING"})
    void shouldLetTheCurrentPlayerChooseACharacterForSickbay(SkillCheckType skillCheckType) {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(2).character(GAIUS_BALTAR);
        moveTo(SICKBAY, GAIUS_BALTAR);
        player(3).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
        game.startSkillCheck(skillCheckType);

        execute(new ResolveConsequencesEvent());

        assertFollowup(one(new SendToSickbayEvent(1), new SendToSickbayEvent(3)));
    }

    @Test
    void shouldNotFollowupWhenSkillCheckHasNoConsequence() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        execute(new ResolveConsequencesEvent());
    }
}
