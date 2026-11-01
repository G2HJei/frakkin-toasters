package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.officials.GiveTitlesToNextInLineEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.*;

class ResolveConsequencesEventProcessorTest extends EventTestHarness<ResolveConsequencesEvent> {

    public static Stream<Arguments> shouldFollowupWithConsequence() {
        return Stream.of(
                arguments(DIVISIVE_BEHAVIOR, single(new PlayerDecisionEvent<>(1, SendToSickbayEvent.class))),
                arguments(HAUNTED_BY_THE_PAST, single(new GiveTitlesToNextInLineEvent(1))),
                arguments(IN_THE_RING, single(new PlayerDecisionEvent<>(1, SendToSickbayEvent.class))),
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

    @Test
    void shouldNotFollowupWhenSkillCheckHasNoConsequence() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        execute(new ResolveConsequencesEvent());
    }
}
