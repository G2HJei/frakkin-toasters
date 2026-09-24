package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.DamageViperAndRemoveCardFromSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.ForceTheirHandEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.QuickThinkingEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class DetermineSkillCheckAbilitiesOrderEventProcessorTest extends EventTestHarness<DetermineSkillCheckAbilitiesOrderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    public static Stream<Arguments> shouldFollowupWithCardEvents() {
        return Stream.of(
                argumentSet("No cards with abilities", List.of(), Followup.NONE),
                argumentSet("Install upgrades", List.of(INSTALL_UPGRADES), Followup.NONE),
                argumentSet("All hands on deck", List.of(ALL_HANDS_ON_DECK), Followup.NONE),
                argumentSet("Dogfight", List.of(DOGFIGHT), one(
                        new PlayerDecisionEvent<>(1, DamageViperAndRemoveCardFromSkillCheckEvent.class),
                        new NoOpEvent(1))),
                argumentSet("Force their hand", List.of(FORCE_THEIR_HAND), one(
                        new PlayerDecisionEvent<>(1, ForceTheirHandEvent.class))),
                argumentSet("Quick thinking", List.of(QUICK_THINKING), one(
                        new PlayerDecisionEvent<>(1, QuickThinkingEvent.class),
                        new NoOpEvent(1)))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldFollowupWithCardEvents(List<SkillCardType> skillCardTypes, Followup expected) {
        val skillCards = skillCardTypes.stream().map(c -> new SkillCard(0, c)).toList();
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, skillCards));
        assertFollowup(expected);
    }

    @Test
    void shouldNotFollowupForceTheirHandWhenCurrentPlayerNonHuman() {
        revealCylon();
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, FORCE_THEIR_HAND))));
    }
}