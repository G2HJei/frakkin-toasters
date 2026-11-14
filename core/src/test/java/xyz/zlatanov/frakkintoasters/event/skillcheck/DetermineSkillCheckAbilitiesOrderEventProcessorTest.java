package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheck;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class DetermineSkillCheckAbilitiesOrderEventProcessorTest extends EventTestHarness<DetermineSkillCheckAbilitiesOrderEvent> {

    SkillCard dogfight         = new SkillCard(0, DOGFIGHT);
    SkillCard redTape          = new SkillCard(0, RED_TAPE);
    SkillCard establishNetwork = new SkillCard(1, ESTABLISH_NETWORK);
    SkillCheck skillCheck;

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        skillCheck = game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldResolveTheOnlyAbilityAndThenLookAgain() {
        val event = inTheCheck(dogfight);

        execute(event);

        assertFollowup(all(single(new ResolveSkillCheckAbilityEvent(DOGFIGHT)), single(event)));
    }

    @Test
    void shouldLetTheCurrentPlayerChooseWhichAbilityResolvesNext() {
        val event = inTheCheck(dogfight, redTape, establishNetwork);

        execute(event);

        assertFollowup(all(
                one(new ResolveSkillCheckAbilityEvent(DOGFIGHT),
                        new ResolveSkillCheckAbilityEvent(RED_TAPE),
                        new ResolveSkillCheckAbilityEvent(ESTABLISH_NETWORK)),
                single(event)));
    }

    @Test
    void shouldOfferAnAbilityOnceForSeveralCopies() {
        val event = inTheCheck(dogfight, dogfight);

        execute(event);

        assertFollowup(all(single(new ResolveSkillCheckAbilityEvent(DOGFIGHT)), single(event)));
    }

    @Test
    void shouldNotOfferAResolvedAbility() {
        val event = inTheCheck(dogfight, redTape);
        skillCheck.resolved(DOGFIGHT);

        execute(event);

        assertFollowup(all(single(new ResolveSkillCheckAbilityEvent(RED_TAPE)), single(event)));
    }

    // Rule 10.4.2: a card removed before its ability is resolved doesn't resolve it.
    @Test
    void shouldNotOfferTheAbilityOfACardRemovedBeforeItResolved() {
        val event = inTheCheck(dogfight, establishNetwork);
        skillCheck.resolved(DOGFIGHT);
        skillCheck.remove(establishNetwork, null);

        execute(event);

        assertEquals(List.of(DOGFIGHT), skillCheck.resolvedAbilities());
    }

    @Test
    void shouldOfferTheAbilityWhileACopyIsStillInTheCheck() {
        val event = inTheCheck(dogfight, establishNetwork, establishNetwork);
        skillCheck.resolved(DOGFIGHT);
        skillCheck.remove(establishNetwork, null);

        execute(event);

        assertFollowup(all(single(new ResolveSkillCheckAbilityEvent(ESTABLISH_NETWORK)), single(event)));
    }

    @Test
    void shouldEndWhenEveryAbilityResolved() {
        val event = inTheCheck(dogfight, redTape);
        skillCheck.resolved(DOGFIGHT);
        skillCheck.resolved(RED_TAPE);

        execute(event);
    }

    @Test
    void shouldNotFollowupWhenNoCardsWithAbilities() {
        execute(new DetermineSkillCheckAbilitiesOrderEvent(1, List.of()));
    }

    // Each ability records itself when it resolves.
    @Test
    void shouldRecordNothing() {
        val event = inTheCheck(dogfight, establishNetwork);

        execute(event);

        assertFollowup(all(
                one(new ResolveSkillCheckAbilityEvent(DOGFIGHT), new ResolveSkillCheckAbilityEvent(ESTABLISH_NETWORK)),
                single(event)));
        assertEquals(List.of(), journal.facts());
    }

    private DetermineSkillCheckAbilitiesOrderEvent inTheCheck(SkillCard... cards) {
        for (val card : cards) {
            skillCheck.matchingPile().addOnTop(card);
        }
        return new DetermineSkillCheckAbilitiesOrderEvent(1, List.of(cards));
    }
}
