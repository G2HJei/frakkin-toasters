package xyz.zlatanov.frakkintoasters.event.skillcheck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class PlayCardsIntoSkillCheckEventProcessorTest extends EventTestHarness<PlayCardsIntoSkillCheckEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.skillCheck()
                .createDestinyDeck()
                .start(ADMIRALS_QUARTERS)
                .playFromDestinyDeck(2);
    }

    @Test
    void shouldAllowUpToOneCardForCharactersInBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(new SkillCard(1, DOGFIGHT), new SkillCard(2, EXECUTIVE_ORDER));
        assertInvalid(new PlayCardsIntoSkillCheckEvent(1, List.of(
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER))));
    }

    @Test
    void shouldAllowUpToOneCardForRevealedCylon() {
        revealCylon();
        player(1).gainSkillCards(new SkillCard(1, DOGFIGHT), new SkillCard(2, EXECUTIVE_ORDER));
        assertInvalid(new PlayCardsIntoSkillCheckEvent(1, List.of(
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER))));
    }

    @Test
    void shouldAllowUpToTwoCardsForInfiltrator() {
        player(2).character(DANNA_BIERS).infiltrateGalactica();
        moveTo(ADMINISTRATION, DANNA_BIERS);
        player(2).gainSkillCards(new SkillCard(1, DOGFIGHT), new SkillCard(2, EXECUTIVE_ORDER), new SkillCard(3, FULL_THROTTLE));
        assertInvalid(new PlayCardsIntoSkillCheckEvent(2, List.of(
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER),
                new SkillCard(3, FULL_THROTTLE))));
    }

    @Test
    void shouldNotLimitCardsOfCharacterNotOnTheBoard() {
        val firstCard = new SkillCard(1, DOGFIGHT);
        val secondCard = new SkillCard(2, EXECUTIVE_ORDER);
        player(2).character(KARL_HELO_AGATHON);
        player(2).skillCards().addOnTop(firstCard);
        player(2).skillCards().addOnTop(secondCard);

        execute(new PlayCardsIntoSkillCheckEvent(2, List.of(firstCard, secondCard)));

        assertTrue(game.activeSkillCheck().cards().cards().containsAll(List.of(firstCard, secondCard)));
    }

    @Test
    void shouldContributeToActiveSkillCheck() {
        val contribution = new SkillCard(0, CALCULATIONS);
        player(1).skillCards().addOnTop(contribution);

        execute(new PlayCardsIntoSkillCheckEvent(1, List.of(contribution)));

        assertTrue(game.activeSkillCheck().cards().cards().contains(contribution));
    }

    // "Investigative Committee": everyone learns the cards and who played them.
    @Test
    void anInvestigatedCheckShowsTheCards() {
        val check = game.activeSkillCheck();
        check.playedBefore(2, List.of(new SkillCard(3, INVESTIGATIVE_COMMITTEE)));
        check.revealPlayedBefore();
        val card = new SkillCard(1, CALCULATIONS);
        player(1).gainSkillCards(card);

        execute(new PlayCardsIntoSkillCheckEvent(1, List.of(card)));

        assertEquals(List.of(card), check.shownCards(1));
        assertTrue(check.cards().cards().contains(card)); // in the pile like any other
    }

    // The President may look at what the holder of "Probation" put in.
    @Test
    void thePresidentMayUseProbation() {
        player(2).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        ops.titles().putInPlay(QuorumCard.PROBATION, 1);
        val card = new SkillCard(1, CALCULATIONS);
        player(1).gainSkillCards(card);

        execute(new PlayCardsIntoSkillCheckEvent(1, List.of(card)));

        assertFollowup(one(new UseProbationEvent(2, 1, 1), new NoOpEvent(2)));
    }

    @Test
    void probationAsksNothingWhenTheHolderPutsNothingIn() {
        player(2).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        ops.titles().putInPlay(QuorumCard.PROBATION, 1);

        execute(new PlayCardsIntoSkillCheckEvent(1, List.of()));
    }

    @Test
    void otherwiseTheCardsStayHidden() {
        val card = new SkillCard(1, CALCULATIONS);
        player(1).gainSkillCards(card);

        execute(new PlayCardsIntoSkillCheckEvent(1, List.of(card)));

        assertEquals(List.of(), game.activeSkillCheck().shownCards(1));
    }

    @Test
    void shouldPlayCardsInHand() {
        player(1).gainSkillCards(new SkillCard(1, DOGFIGHT));
        assertInvalid(new PlayCardsIntoSkillCheckEvent(1, List.of(new SkillCard(1, DOGFIGHT), new SkillCard(1, DOGFIGHT))));
    }
}
