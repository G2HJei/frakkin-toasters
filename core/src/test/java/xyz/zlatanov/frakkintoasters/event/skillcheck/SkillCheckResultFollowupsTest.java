package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.*;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.*;

class SkillCheckResultFollowupsTest extends TestHarness {

    private static final int ADMIRAL   = 1;
    private static final int PRESIDENT = 2;
    private static final int OTHER     = 3;

    @BeforeEach
    void setUp() {
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(OTHER).character(KARA_STARBUCK_THRACE);
        moveTo(Location.ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(Location.PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(Location.HANGAR_DECK, KARA_STARBUCK_THRACE);
        admiral(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
    }

    @ParameterizedTest
    @EnumSource(SkillCheckType.class)
    void shouldBuildFollowupForEveryResult(SkillCheckType type) {
        val results = type.partialPass() == null ? List.of(PASS, FAIL) : List.of(PASS, PARTIAL_PASS, FAIL);
        for (val result : results) {
            game.startSkillCheck(type).targetPlayerNumber(OTHER).result(result);
            assertNotNull(new SkillCheckResultFollowups(game).followup(), type + " " + result);
        }
    }

    @Test
    void shouldNotHavePartialPassWithoutPartialPassNumber() {
        game.startSkillCheck(COLONIAL_DAY).result(PARTIAL_PASS);
        assertThrows(FrakCallTheAdmiralException.class, () -> new SkillCheckResultFollowups(game).followup());
    }

    @Test
    void shouldHaveNoFollowupForNoEffect() {
        assertEquals(NONE, followup(DIVISIVE_BEHAVIOR, PASS));
        assertEquals(NONE, followup(MANDATORY_TESTING, PARTIAL_PASS));
        assertEquals(NONE, followup(ADMIRALS_QUARTERS, FAIL));
    }

    @Test
    void shouldComposeGenericEvents() {
        assertEquals(all(new LoseResourceEvent(MORALE, 1), new LoseResourceEvent(FOOD, 1)),
                followup(PRISON_LABOR, FAIL));
    }

    @Test
    void shouldResolvePartialPass() {
        assertEquals(single(new LoseResourceEvent(MORALE, 1)), followup(ELECTIONS_LOOM, PARTIAL_PASS));
    }

    @Test
    void shouldLetThePresidentDiscard() {
        assertEquals(all(new LoseResourceEvent(MORALE, 1),
                        new PlayerDecisionEvent<>(PRESIDENT, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_4)),
                followup(ELECTIONS_LOOM, FAIL));
    }

    @Test
    void shouldLetThePresidentChooseTheNextPresident() {
        assertEquals(all(new LoseResourceEvent(POPULATION, 1),
                        new PlayerDecisionEvent<>(PRESIDENT, MakePresidentEvent.class)),
                followup(PRISONER_REVOLT, FAIL));
    }

    @Test
    void shouldLetTheAdmiralOptionallySendToBrig() {
        assertEquals(one(new PlayerDecisionEvent<>(ADMIRAL, SendToBrigEvent.class), new NoOpEvent(ADMIRAL)),
                followup(FAMILIAR_FACE, PASS));
    }

    @Test
    void shouldSendEveryCharacterInTheLocation() {
        moveTo(Location.COMMAND, WILLIAM_ADAMA);
        moveTo(Location.COMMAND, KARA_STARBUCK_THRACE);

        assertEquals(all(single(new LoseResourceEvent(MORALE, 1)),
                        all(new SendToSickbayEvent(ADMIRAL), new SendToSickbayEvent(OTHER))),
                followup(SECURITY_BREACH, FAIL));
    }

    @Test
    void shouldSkipTheLocationEffectWhenNobodyIsThere() {
        assertEquals(single(new LoseResourceEvent(MORALE, 1)), followup(SECURITY_BREACH, FAIL));
    }

    @Test
    void shouldLose1MoraleOnLowSuppliesWhenFoodIs6OrMore() {
        galacticaBoard.decreaseFood(2); // 6 food left

        assertEquals(single(new LoseResourceEvent(MORALE, 1)), followup(LOW_SUPPLIES, FAIL));
    }

    @Test
    void shouldLose2MoraleOnLowSuppliesWhenFoodIsBelow6() {
        galacticaBoard.decreaseFood(3); // 5 food left

        assertEquals(single(new LoseResourceEvent(MORALE, 2)), followup(LOW_SUPPLIES, FAIL));
    }

    @Test
    void shouldSendTheChosenCharacterToTheBrig() {
        game.startSkillCheck(ADMIRALS_QUARTERS).targetPlayerNumber(OTHER).result(PASS);

        assertEquals(single(new SendToBrigEvent(OTHER)), new SkillCheckResultFollowups(game).followup());
    }

    @Test
    void shouldGiveThePresidentTitleToTheChosenCharacter() {
        game.startSkillCheck(ADMINISTRATION).targetPlayerNumber(OTHER).result(PASS);

        assertEquals(single(new MakePresidentEvent(KARA_STARBUCK_THRACE)),
                new SkillCheckResultFollowups(game).followup());
    }

    @Test
    void shouldResolveEachPlayerStartingWithTheCurrentPlayer() {
        game.currentPlayerNumber(2);

        assertEquals(all(single(new LoseResourceEvent(MORALE, 1)),
                        all(discardAndDrawTreachery(2), discardAndDrawTreachery(3), discardAndDrawTreachery(1))),
                followup(UNSETTLING_STORIES, FAIL));
    }

    @Test
    void shouldOnlyGiveMutinyCardsToPlayersWithoutOne() {
        player(PRESIDENT).mutinyCards().addOnTop(WEAPONS_ARMED);

        assertEquals(all(single(new LoseResourceEvent(MORALE, 1)),
                        all(new DrawMutinyCardEvent(ADMIRAL), new DrawMutinyCardEvent(OTHER))),
                followup(INSUBORDINATE_CREW, FAIL));
    }

    @Test
    void shouldLookAtLoyaltyCardOfPresidentOrAdmiral() {
        game.currentPlayerNumber(OTHER);

        assertEquals(all(single(new LoseResourceEvent(MORALE, 1)),
                        one(new LookAtLoyaltyCardEvent(OTHER, PRESIDENT), new LookAtLoyaltyCardEvent(OTHER, ADMIRAL))),
                followup(CYLON_SCREENINGS, FAIL));
    }

    @Test
    void shouldNotLookAtOwnLoyaltyCard() {
        game.currentPlayerNumber(ADMIRAL);

        assertEquals(all(single(new LoseResourceEvent(MORALE, 1)),
                        single(new LookAtLoyaltyCardEvent(ADMIRAL, PRESIDENT))),
                followup(CYLON_SCREENINGS, FAIL));
    }

    private Followup followup(SkillCheckType type, SkillCheckResult result) {
        game.startSkillCheck(type).result(result);
        return new SkillCheckResultFollowups(game).followup();
    }

    private static Followup discardAndDrawTreachery(int playerNumber) {
        return all(new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_1),
                new DrawSkillCardsOfColorEvent(playerNumber, TREACHERY, 1));
    }
}
