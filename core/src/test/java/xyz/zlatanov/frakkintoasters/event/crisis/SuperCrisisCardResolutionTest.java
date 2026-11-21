package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.location.cylon.CapricaPlaySuperCrisisCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.SkillCheckResultFollowups;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult.*;

/**
 * Plays every Super Crisis Card at "Caprica" and resolves it through the real processors, the way the engine will: both
 * sections of a card with a chooser, and every printed result of a card with a skill check. So no section or result
 * ends in an event without a processor. Felix Gaeta is the revealed Cylon who plays them.
 */
class SuperCrisisCardResolutionTest extends TestHarness {

    private static final int CYLON     = 1;
    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(CYLON).character(FELIX_GAETA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        player(CYLON).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE).reveal(CYLON_REDUCE_MORALE);
        moveTo(CAPRICA, FELIX_GAETA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
        loyaltyNotCylonDeck.nextCard(NOT_CYLON); // for an execution
        ops.startTurn(); // the Cylon's, who plays the card at "Caprica"
        for (int player = PRESIDENT; player <= CAG; player++) {
            player(player).gainSkillCards(new SkillCard(1, REPAIR), new SkillCard(2, REPAIR), new SkillCard(3, REPAIR));
        }
    }

    static Stream<SuperCrisisCard> cardsWithAChooser() {
        return Arrays.stream(SuperCrisisCard.values()).filter(card -> card.chooser() != null);
    }

    static Stream<SuperCrisisCard> cardsWithASkillCheck() {
        return Arrays.stream(SuperCrisisCard.values()).filter(card -> card.skillCheck() != null);
    }

    @Test
    void everyCardHasASkillCheckOrAChooser() {
        Arrays.stream(SuperCrisisCard.values())
                .forEach(card -> assertTrue(card.skillCheck() == null ^ card.chooser() == null, card.name()));
        cardsWithASkillCheck().forEach(card -> assertTrue(card.skillCheck().isSuperCrisis(), card.name()));
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldResolveTheTopSectionOfEveryCardWithAChooser(SuperCrisisCard card) {
        resolve(card, TOP);
        assertEquals(List.of(card), superCrisisDeck.discardedCards());
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldResolveTheBottomSectionOfEveryCardWithAChooser(SuperCrisisCard card) {
        resolve(card, BOTTOM);
        assertEquals(List.of(card), superCrisisDeck.discardedCards());
    }

    @ParameterizedTest
    @MethodSource("cardsWithASkillCheck")
    void shouldResolveThePassOfEveryCard(SuperCrisisCard card) {
        resolve(card, PASS);
    }

    @ParameterizedTest
    @MethodSource("cardsWithASkillCheck")
    void shouldResolveThePartialPassOfEveryCardThatHasOne(SuperCrisisCard card) {
        if (card.skillCheck().partialPass() != null) {
            resolve(card, PARTIAL_PASS);
        }
    }

    @ParameterizedTest
    @MethodSource("cardsWithASkillCheck")
    void shouldResolveTheFailOfEveryCard(SuperCrisisCard card) {
        resolve(card, FAIL);
    }

    /* the cards */

    @Test
    void shouldPlaceCenturions() {
        resolve(FIGHTING_BLIND, TOP);
        assertEquals(2, galacticaBoard.boardingPartyTrack().size());
    }

    @Test
    void shouldExecuteTheCag() {
        resolve(FIGHTING_BLIND, BOTTOM);
        assertTrue(game.removedComponents().items().contains(LEE_APOLLO_ADAMA));
    }

    @Test
    void shouldMakeTheHumansTradeCardsForTreachery() {
        val morale = galacticaBoard.morale();
        resolve(PSYCHOLOGICAL_WARFARE, TOP);
        assertEquals(morale - 1, galacticaBoard.morale());
        for (int player = PRESIDENT; player <= CAG; player++) {
            assertEquals(3, player(player).skillCards().size());
            assertEquals(2, player(player).skillCards().cards().stream().filter(c -> c.type().color() == TREACHERY).count());
        }
        // A revealed Cylon ignores the discard, the negative half (rule 6.1.2.1), and still draws the Treachery.
        assertEquals(2, player(CYLON).skillCards().size());
        assertTrue(player(CYLON).skillCards().cards().stream().allMatch(c -> c.type().color() == TREACHERY));
    }

    // As many Treachery cards as are left after the Cylon drew his, up to 6.
    @Test
    void shouldRebuildTheDestinyDeckFromTreachery() {
        val left = game.decks().skillCardsLeft(TREACHERY) - 2;
        resolve(PSYCHOLOGICAL_WARFARE, BOTTOM);
        assertEquals(2, player(CYLON).skillCards().size());
        assertEquals(Math.min(SuperCrisisCardFollowups.PSYCHOLOGICAL_WARFARE_TREACHERY, left), game.decks().destiny().size());
        assertTrue(game.decks().destiny().cards().stream().allMatch(c -> c.type().color() == TREACHERY));
    }

    @Test
    void shouldDamageGalacticaTwice() {
        galacticaDamage.nextCard(GalacticaDamage.ARMORY, GalacticaDamage.COMMAND);
        val morale = galacticaBoard.morale();
        resolve(DEMAND_PEACE_MANIFESTO, TOP);
        assertEquals(morale - 1, galacticaBoard.morale());
        assertEquals(2, damagedGalacticaLocations());
    }

    @Test
    void shouldMakeThePresidentAndTheAdmiralDiscardTheirHands() {
        resolve(DEMAND_PEACE_MANIFESTO, BOTTOM);
        assertNoSkillCards(PRESIDENT);
        assertNoSkillCards(ADMIRAL);
        assertEquals(3, player(CAG).skillCards().size());
    }

    @Test
    void shouldLetTheCylonTakeAHumansHand() {
        resolve(HUMAN_PRISONER, FAIL);
        assertEquals(3, player(CYLON).skillCards().size());
        assertNoSkillCards(PRESIDENT); // the first human to his left
        assertEquals(SICKBAY, locate(LAURA_ROSLIN));
    }

    @Test
    void shouldKeepTheBombInPlayAndCloseColonialOne() {
        resolve(BOMB_ON_COLONIAL_1, FAIL);
        assertEquals(SICKBAY, locate(LAURA_ROSLIN));
        assertTrue(game.colonialOneDestroyed());
        assertTrue(superCrisisDeck.discardedCards().isEmpty());
    }

    @Test
    void shouldDiscardTheBombWhenItIsDefused() {
        resolve(BOMB_ON_COLONIAL_1, PASS);
        assertFalse(game.colonialOneDestroyed());
        assertEquals(List.of(BOMB_ON_COLONIAL_1), superCrisisDeck.discardedCards());
    }

    @Test
    void shouldKeepTheFarmInPlayOnAFail() {
        resolve(THE_FARM, FAIL);
        assertTrue(game.crisisCardsInPlay().contains(THE_FARM.skillCheck()));
        assertTrue(superCrisisDeck.discardedCards().isEmpty());
    }

    /* resolving */

    private long damagedGalacticaLocations() {
        return Arrays.stream(xyz.zlatanov.frakkintoasters.state.board.Location.values())
                .filter(galacticaBoard::isDamaged)
                .count();
    }

    // Plays the card at "Caprica" and picks the section.
    private void resolve(SuperCrisisCard card, Section section) {
        val resolveAndDiscard = play(card);
        val choice = execute(((Followup.Single) resolveAndDiscard.followups().getFirst()).event());
        val option = ((Followup.OneOf) choice).options().stream()
                .filter(o -> ((Followup.Single) o).event() instanceof ChooseSuperCrisisSectionEvent e && e.section() == section)
                .findFirst()
                .orElseThrow();
        run(option);
        run(resolveAndDiscard.followups().getLast());
    }

    // Plays the card at "Caprica", starts its skill check and resolves the result, as if the check had come to it.
    private void resolve(SuperCrisisCard card, SkillCheckResult result) {
        val resolveAndDiscard = play(card);
        execute(((Followup.Single) resolveAndDiscard.followups().getFirst()).event());
        assertEquals(card.skillCheck(), game.activeSkillCheck().type());
        assertEquals(CYLON, game.activeSkillCheck().playedBy());
        ops.skillCheck().determineResult(result);
        run(new SkillCheckResultFollowups(game).followup());
        ops.skillCheck().end();
        run(resolveAndDiscard.followups().getLast());
    }

    private Followup.AllOf play(SuperCrisisCard card) {
        player(CYLON).superCrisisCards().addOnTop(card);
        return (Followup.AllOf) execute(new CapricaPlaySuperCrisisCardEvent(CYLON, card));
    }

    private void run(Followup followup) {
        switch (followup) {
            case Followup.None ignored -> {
            }
            case Followup.Single single -> run(single.event() instanceof PlayerDecisionEvent<?> decision
                    ? execute(answer(decision))
                    : execute(single.event()));
            case Followup.AllOf all -> all.followups().forEach(this::run);
            // The optional ones ("may") have a NoOpEvent to decline with, otherwise the first option.
            case Followup.OneOf oneOf -> run(oneOf.options().stream()
                    .filter(o -> ((Followup.Single) o).event() instanceof NoOpEvent)
                    .findFirst()
                    .orElse(oneOf.options().iterator().next()));
        }
    }

    // The first legal answer.
    private Event answer(PlayerDecisionEvent<?> decision) {
        val action = decision.action();
        if (action == DiscardSkillCardsEvent.class) {
            val limit = decision.eventConstraints().getFirst();
            val count = Integer.parseInt(limit.name().substring(limit.name().lastIndexOf('_') + 1));
            val hand = player(decision.playerNumber()).skillCards().cards();
            return new DiscardSkillCardsEvent(decision.playerNumber(), hand.subList(0, Math.min(count, hand.size())), limit);
        } else if (action == PlaceCivilianShipEvent.class) {
            val free = GALACTICA_SPACE.locations().stream()
                    .filter(location -> galacticaBoard.shipsIn(location, CivilianShip.class).isEmpty())
                    .findFirst()
                    .orElseThrow();
            return new PlaceCivilianShipEvent(decision.playerNumber(), free);
        } else if (action == SelectCharacterEvent.class) {
            return new SelectCharacterEvent(decision.playerNumber(), SAUL_TIGH); // after an execution
        }
        throw new AssertionError("A card asks for " + decision);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <E extends Event> Followup execute(E event) {
        val processorClass = Class.forName(event.getClass().getName() + "Processor");
        val processor = (EventProcessor<E>) processorClass.getDeclaredConstructor().newInstance();
        try {
            return processor.prepare(game, event).run(ops);
        } catch (RuntimeException e) {
            throw new AssertionError(event + " failed", e);
        }
    }
}
