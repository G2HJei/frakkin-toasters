package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class DetermineSkillCheckAbilitiesOrderEventProcessorTest extends EventTestHarness<DetermineSkillCheckAbilitiesOrderEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
    }

    public static Stream<Arguments> shouldFollowupWithCardEvents() {
        return Stream.of(
                arguments(INSTALL_UPGRADES, Followup.NONE),
                arguments(ALL_HANDS_ON_DECK, Followup.NONE),
                arguments(ESTABLISH_NETWORK, Followup.NONE),
                arguments(IRON_WILL, Followup.NONE),
                arguments(DOGFIGHT, single(new PlayerDecisionEvent<>(1, DogfightEvent.class))),
                arguments(FORCE_THEIR_HAND, single(new PlayerDecisionEvent<>(1, ForceTheirHandEvent.class))),
                arguments(QUICK_THINKING, single(new PlayerDecisionEvent<>(1, QuickThinkingEvent.class))),
                arguments(A_BETTER_MACHINE, single(new ABetterMachineEvent(1))),
                arguments(BAIT, single(new BaitEvent())),
                arguments(DRADIS_CONTACT, single(new DradisContactEvent())),
                arguments(EXPLOIT_A_WEAKNESS, single(new PlayerDecisionEvent<>(1, ExploitAWeaknessEvent.class))),
                arguments(PERSONAL_VICES, single(new PersonalVicesEvent(1))),
                arguments(VIOLENT_OUTBURSTS, single(new ViolentOutburstsEvent(1))),
                arguments(PROTECT_THE_FLEET, single(new PlayerDecisionEvent<>(1, ProtectTheFleetEvent.class))),
                arguments(RED_TAPE, single(new RedTapeEvent())),
                arguments(TRUST_INSTINCTS, single(new TrustInstinctsEvent()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldFollowupWithCardEvents(SkillCardType skillCardType, Followup expected) {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(new SkillCard(0, skillCardType))));
        assertFollowup(expected);
    }

    @Test
    void shouldNotFollowupWhenNoCardsWithAbilities() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of()));
    }

    @Test
    void shouldNotFollowupForceTheirHandWhenCurrentPlayerNonHuman() {
        revealCylon();
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, FORCE_THEIR_HAND))));
    }

    @Test
    void shouldFollowupWithMultipleEvents() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, FORCE_THEIR_HAND),
                new SkillCard(0, IRON_WILL),
                new SkillCard(0, RED_TAPE)
        )));
        assertFollowup(all(
                single(new PlayerDecisionEvent<>(1, ForceTheirHandEvent.class)),
                single(new RedTapeEvent())
        ));
    }

    @Test
    void shouldFollowUpOnlyOncePerSkillCardType() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(
                new SkillCard(0, DOGFIGHT),
                new SkillCard(0, DOGFIGHT)
        )));
        assertFollowup(single(new PlayerDecisionEvent<>(1, DogfightEvent.class)));
    }
}