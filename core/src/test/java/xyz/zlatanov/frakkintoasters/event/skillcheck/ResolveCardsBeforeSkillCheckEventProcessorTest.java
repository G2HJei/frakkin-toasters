package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class ResolveCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<ResolveCardsBeforeSkillCheckEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(CAVIL);
        player(2).character(KARA_STARBUCK_THRACE);
        player(3).character(SAMUEL_T_ANDERS);
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldFollowUpWithDestinyDeckWhenNoCardPlayed() {
        execute(new ResolveCardsBeforeSkillCheckEvent());
        assertFollowup(all(single(new PlayFromDestinyDeckEvent()), playCardsInto(), shuffleAndDivide()));
    }

    // Starting with the player to the left of the current player, and ending with the current player (rule 10.3.1).
    @Test
    void shouldLetEachPlayerPlayCardsIntoTheCheckFromTheLeftOfTheCurrentPlayer() {
        ops.changeCurrentPlayer(2);
        execute(new ResolveCardsBeforeSkillCheckEvent());
        assertFollowup(all(
                single(new PlayFromDestinyDeckEvent()),
                all(new PlayerDecisionEvent<>(3, PlayCardsIntoSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(1, PlayCardsIntoSkillCheckEvent.class),
                        new PlayerDecisionEvent<>(2, PlayCardsIntoSkillCheckEvent.class)),
                shuffleAndDivide()));
    }

    @Test
    void shouldNotPlayFromDestinyDeckWhenGutsAndInitiativeIsInPlay() {
        addPlayedBefore(1, GUTS_AND_INITIATIVE);
        execute(new ResolveCardsBeforeSkillCheckEvent());
        assertFollowup(all(playCardsInto(), shuffleAndDivide()));
    }

    @Test
    void shouldLetHumansWithLessThan4CardsDraw2() {
        addPlayedBefore(1, SUPPORT_THE_PEOPLE);
        gain4SkillCards(2);

        execute(new ResolveCardsBeforeSkillCheckEvent());

        assertFollowup(
                all(
                        one(new PlayerDecisionEvent<>(3, DrawSkillCardsEvent.class, DRAW_EXACTLY_2), new NoOpEvent(3)),
                        single(new PlayFromDestinyDeckEvent()),
                        playCardsInto(),
                        shuffleAndDivide()
                )
        );
    }

    @Test
    void shouldCombineMultiplePlayedBeforeEffects() {
        addPlayedBefore(1, GUTS_AND_INITIATIVE);
        addPlayedBefore(3, SUPPORT_THE_PEOPLE);
        gain4SkillCards(3);

        execute(new ResolveCardsBeforeSkillCheckEvent());

        assertFollowup(
                all(
                        one(new PlayerDecisionEvent<>(2, DrawSkillCardsEvent.class, DRAW_EXACTLY_2), new NoOpEvent(2)),
                        playCardsInto(),
                        shuffleAndDivide()
                )
        );
    }

    private static Followup playCardsInto() {
        return all(
                new PlayerDecisionEvent<>(2, PlayCardsIntoSkillCheckEvent.class),
                new PlayerDecisionEvent<>(3, PlayCardsIntoSkillCheckEvent.class),
                new PlayerDecisionEvent<>(1, PlayCardsIntoSkillCheckEvent.class));
    }

    private static Followup shuffleAndDivide() {
        return single(new ShuffleAndDivideCardsEvent());
    }

    private void addPlayedBefore(int playerNumber, SkillCardType skillCardType) {
        game.activeSkillCheck().playedBefore(playerNumber, List.of(new SkillCard(0, skillCardType)));
    }

    private void gain4SkillCards(int playerNumber) {
        player(playerNumber).gainSkillCards(
                new SkillCard(0, AT_ANY_COST),
                new SkillCard(0, AT_ANY_COST),
                new SkillCard(0, AT_ANY_COST),
                new SkillCard(0, AT_ANY_COST));
    }

}