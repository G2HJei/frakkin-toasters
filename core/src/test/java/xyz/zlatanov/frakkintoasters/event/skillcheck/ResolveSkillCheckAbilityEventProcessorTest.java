package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.SkillCheckFact.AbilityResolved;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ability.*;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ResolveSkillCheckAbilityEventProcessorTest extends EventTestHarness<ResolveSkillCheckAbilityEvent> {

    SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    public static Stream<Arguments> shouldFollowupWithTheAbilityOfTheCard() {
        return Stream.of(
                arguments(DOGFIGHT, single(new PlayerDecisionEvent<>(1, DogfightEvent.class))),
                arguments(FORCE_THEIR_HAND, single(new PlayerDecisionEvent<>(1, ForceTheirHandEvent.class))),
                arguments(QUICK_THINKING, single(new PlayerDecisionEvent<>(1, QuickThinkingEvent.class))),
                arguments(A_BETTER_MACHINE, single(new ABetterMachineEvent())),
                arguments(BAIT, single(new BaitEvent())),
                arguments(DRADIS_CONTACT, single(new DradisContactEvent())),
                arguments(EXPLOIT_A_WEAKNESS, single(new PlayerDecisionEvent<>(1, ExploitAWeaknessEvent.class))),
                arguments(PERSONAL_VICES, single(new PersonalVicesEvent())),
                arguments(VIOLENT_OUTBURSTS, single(new ViolentOutburstsEvent())),
                arguments(PROTECT_THE_FLEET, single(new PlayerDecisionEvent<>(1, ProtectTheFleetEvent.class))),
                arguments(RED_TAPE, single(new RedTapeEvent())),
                arguments(TRUST_INSTINCTS, single(new TrustInstinctsEvent()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void shouldFollowupWithTheAbilityOfTheCard(SkillCardType type, Followup expected) {
        inTheCheck(type);

        execute(new ResolveSkillCheckAbilityEvent(type));

        assertFollowup(expected);
    }

    // The strength and result read these later.
    @ParameterizedTest
    @EnumSource(names = {"IRON_WILL", "INSTALL_UPGRADES", "ALL_HANDS_ON_DECK", "ESTABLISH_NETWORK"})
    void shouldOnlyRecordTheAbilitiesWithoutAnEvent(SkillCardType type) {
        inTheCheck(type);

        execute(new ResolveSkillCheckAbilityEvent(type));

        assertEquals(List.of(type), skillCheck.resolvedAbilities());
    }

    @Test
    void shouldRecordTheAbilityAsResolved() {
        inTheCheck(RED_TAPE);

        execute(new ResolveSkillCheckAbilityEvent(RED_TAPE));

        assertFollowup(new RedTapeEvent());
        assertEquals(List.of(new AbilityResolved(RED_TAPE)), journal.facts());
    }

    @Test
    void shouldNotFollowupForceTheirHandWhenCurrentPlayerNonHuman() {
        revealCylon();
        inTheCheck(FORCE_THEIR_HAND);

        execute(new ResolveSkillCheckAbilityEvent(FORCE_THEIR_HAND));

        assertEquals(List.of(FORCE_THEIR_HAND), skillCheck.resolvedAbilities());
    }

    @Test
    void shouldNotResolveAnAbilityTwiceInACheck() {
        inTheCheck(RED_TAPE);
        skillCheck.resolved(RED_TAPE);

        assertInvalid(new ResolveSkillCheckAbilityEvent(RED_TAPE));
    }

    @Test
    void shouldNotResolveTheAbilityOfACardNotInTheCheck() {
        assertInvalid(new ResolveSkillCheckAbilityEvent(RED_TAPE));
    }

    @Test
    void shouldNotResolveACardWithoutAnAbility() {
        inTheCheck(REPAIR);

        assertInvalid(new ResolveSkillCheckAbilityEvent(REPAIR));
    }

    @Test
    void shouldNotResolveNothing() {
        assertInvalid(new ResolveSkillCheckAbilityEvent(null));
    }

    private void inTheCheck(SkillCardType type) {
        skillCheck.matchingPile().addOnTop(new SkillCard(0, type));
    }
}
