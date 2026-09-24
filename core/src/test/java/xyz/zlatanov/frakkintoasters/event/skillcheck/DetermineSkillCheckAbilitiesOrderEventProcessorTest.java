package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
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

import static org.junit.jupiter.params.provider.Arguments.argumentSet;
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
                argumentSet("No cards with abilities", List.of(), Followup.NONE),
                argumentSet("Install upgrades", List.of(INSTALL_UPGRADES), Followup.NONE),
                argumentSet("All hands on deck", List.of(ALL_HANDS_ON_DECK), Followup.NONE),
                argumentSet("Establish network", List.of(ESTABLISH_NETWORK), Followup.NONE),
                argumentSet("Iron will", List.of(IRON_WILL), Followup.NONE),
                argumentSet("Dogfight", List.of(DOGFIGHT), single(new PlayerDecisionEvent<>(1, DogfightEvent.class))),
                argumentSet("Force their hand", List.of(FORCE_THEIR_HAND), single(new PlayerDecisionEvent<>(1, ForceTheirHandEvent.class))),
                argumentSet("Quick thinking", List.of(QUICK_THINKING), single(new PlayerDecisionEvent<>(1, QuickThinkingEvent.class))),
                argumentSet("A better machine", List.of(A_BETTER_MACHINE), single(new ABetterMachineEvent(1))),
                argumentSet("Bait", List.of(BAIT), single(new BaitEvent())),
                argumentSet("Dradis contact", List.of(DRADIS_CONTACT), single(new DradisContactEvent())),
                argumentSet("Exploit a weakness", List.of(EXPLOIT_A_WEAKNESS), single(new PlayerDecisionEvent<>(1, ExploitAWeaknessEvent.class))),
                argumentSet("Personal vices", List.of(PERSONAL_VICES), single(new PersonalVicesEvent(1))),
                argumentSet("Violent outbursts", List.of(VIOLENT_OUTBURSTS), single(new ViolentOutburstsEvent(1))),
                argumentSet("Protect the fleet", List.of(PROTECT_THE_FLEET), single(new PlayerDecisionEvent<>(1, ProtectTheFleetEvent.class))),
                argumentSet("Red tape", List.of(RED_TAPE), single(new RedTapeEvent())),
                argumentSet("Trust instincts", List.of(TRUST_INSTINCTS), single(new TrustInstinctsEvent()))

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