package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsIntoSkillCheckEventProcessor;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.PASS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class SkillCheckAbilityOffersTest extends TestHarness {

    private final SkillCard leadership = new SkillCard(3, EXECUTIVE_ORDER);
    private final SkillCard tactics    = new SkillCard(2, LAUNCH_SCOUT);

    private void revealedCheck() {
        player(2).character(LAURA_ROSLIN);
        player(2).gainSkillCards(leadership, tactics);
        ops.skillCheck().createDestinyDeck().start(WATER_SABOTAGED);
        ops.skillCheck().playCards(2, leadership, tactics);
        ops.skillCheck().shuffleAndDivideCards();
    }

    @Test
    void fastLearnerOffersEveryDeckWithCardsLeft() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        ops.skillCheck().start(WATER_SABOTAGED);

        val offer = FastLearnerEventProcessor.offer(game);

        assertNotEquals(NONE, offer);
        assertTrue(offer.toString().contains(new FastLearnerEvent(1, TREACHERY).toString()));
    }

    @Test
    void humanDelusionIsADecision() {
        player(1).character(CAPRICA_SIX);
        player(1).gainSkillCards(leadership);
        ops.skillCheck().start(WATER_SABOTAGED);

        assertEquals(one(new PlayerDecisionEvent<>(1, HumanDelusionEvent.class), new NoOpEvent(1)),
                HumanDelusionEventProcessor.offer(game));
    }

    @Test
    void quickFixOffersEveryRevealedCardOnHerTurn() {
        player(1).character(CALLANDRA_CALLY_TYROL);
        ops.changeCurrentPlayer(1);
        revealedCheck();

        assertEquals(one(new QuickFixEvent(1, leadership), new QuickFixEvent(1, tactics), new NoOpEvent(1)),
                QuickFixEventProcessor.offer(game));
    }

    @Test
    void commandAuthorityOnceTheCheckIsResolved() {
        player(1).character(WILLIAM_ADAMA);
        revealedCheck();
        assertEquals(NONE, CommandAuthorityEventProcessor.offer(game));
        assertTrue(CommandAuthorityEventProcessor.mayBeOffered(game));

        ops.skillCheck().totalStrength(5);
        ops.skillCheck().determineResult(PASS);

        assertEquals(one(new CommandAuthorityEvent(1), new NoOpEvent(1)), CommandAuthorityEventProcessor.offer(game));
    }

    @Test
    void nobodyIsOfferedAnAbilityHeDoesntHave() {
        player(1).character(LEE_ADAMA);
        ops.changeCurrentPlayer(1);
        revealedCheck();

        assertEquals(NONE, FastLearnerEventProcessor.offer(game));
        assertEquals(NONE, BlindDevotionEventProcessor.offer(game));
        assertEquals(NONE, QuickFixEventProcessor.offer(game));
        assertEquals(NONE, HumanDelusionEventProcessor.offer(game));
        assertFalse(CommandAuthorityEventProcessor.mayBeOffered(game));
    }

    // "Calculating": Simon O'Neill puts in 2, or 3 Infiltrating, instead of 1 or 2, but not from the "Brig".
    @Test
    void calculating() {
        player(1).character(SIMON_ONEIL);
        moveTo(CYLON_FLEET, SIMON_ONEIL);
        ops.skillCheck().start(WATER_SABOTAGED);
        assertEquals(2, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));

        player(1).infiltrateGalactica();
        moveTo(ADMINISTRATION, SIMON_ONEIL);
        assertEquals(3, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));

        moveTo(BRIG, SIMON_ONEIL);
        assertEquals(1, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));
    }

    // "Vanity": Aaron Doral puts nothing into a check in another player's Action step.
    @Test
    void vanity() {
        player(1).character(AARON_DORAL).infiltrateGalactica();
        moveTo(ADMINISTRATION, AARON_DORAL);
        ops.skillCheck().start(WATER_SABOTAGED);
        ops.changeStep(GameStep.ACTION);
        ops.changeCurrentPlayer(2);
        assertEquals(0, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));

        ops.changeCurrentPlayer(1);
        assertEquals(2, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));

        ops.changeCurrentPlayer(2);
        ops.changeStep(GameStep.CRISIS);
        assertEquals(2, PlayCardsIntoSkillCheckEventProcessor.maxCards(game, player(1)));
    }
}
