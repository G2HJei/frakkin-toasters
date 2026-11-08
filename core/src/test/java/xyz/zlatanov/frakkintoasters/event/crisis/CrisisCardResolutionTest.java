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
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

/**
 * Resolves Crisis Cards from {@link ResolveCrisisCardEvent} to the last followup through the real processors, the way
 * the engine will, so a section can't end in an event that has no processor.
 */
class CrisisCardResolutionTest extends TestHarness {

    private static final int CURRENT   = 1;
    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(CURRENT).character(FELIX_GAETA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, FELIX_GAETA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    static Stream<CrisisCard> cardsWithAChooser() {
        return Arrays.stream(CrisisCard.values()).filter(card -> card.chooser() != null);
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldResolveTheTopSectionOfEveryCardWithAChooser(CrisisCard card) {
        resolve(card, TOP);
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldResolveTheBottomSectionOfEveryCardWithAChooser(CrisisCard card) {
        resolve(card, BOTTOM);
    }

    @Test
    void shouldLoseAndGainResources() {
        resolve(RESCUE_CAPRICA_SURVIVORS, TOP);

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(7, galacticaBoard.food());
        assertEquals(13, galacticaBoard.population());
    }

    @Test
    void shouldDamageGalacticaTwice() {
        galacticaDamage.nextCard(GalacticaDamage.ARMORY, GalacticaDamage.WEAPONS_CONTROL);

        resolve(CONTROVERSIAL_MANUSCRIPT, BOTTOM);

        assertEquals(11, galacticaBoard.morale());
        assertTrue(galacticaBoard.damagedLocations().containsAll(List.of(ARMORY, WEAPONS_CONTROL)));
    }

    @Test
    void shouldMoveTheFleetTokenBack() {
        galacticaBoard.advanceJumpPreparation();

        resolve(LABOR_DISPUTE, BOTTOM);

        assertEquals(7, galacticaBoard.fuel());
        assertEquals(START, galacticaBoard.jumpPreparation());
    }

    @Test
    void shouldDestroyARaptorAndSendTheCurrentPlayerToSickbay() {
        resolve(HIDDEN_EXPLOSIVES, TOP);

        assertEquals(3, raptorsInReserves());
        assertEquals(SICKBAY, locate(FELIX_GAETA));
    }

    @Test
    void shouldLandTheCurrentPlayerWhenSentToSickbay() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        ops.player(CURRENT).pilot(viper.id());

        resolve(RESCUE_MISSION_RAIDER, TOP);

        assertEquals(9, galacticaBoard.morale());
        assertEquals(SICKBAY, locate(FELIX_GAETA));
        assertTrue(galacticaBoard.reserves().contains(viper));
    }

    @Test
    void shouldDiscardTheWholeHandOfThePresident() {
        draw(PRESIDENT, 4);

        resolve(AMBUSHED_BY_THE_PRESS, BOTTOM);

        assertNoSkillCards(PRESIDENT);
    }

    @Test
    void shouldDiscardFromThePresidentThenTheCurrentPlayer() {
        draw(PRESIDENT, 3);
        draw(CURRENT, 4);

        resolve(FOOD_SHORTAGE, BOTTOM);

        assertEquals(7, galacticaBoard.food());
        assertEquals(1, player(PRESIDENT).skillCards().size());
        assertEquals(1, player(CURRENT).skillCards().size());
    }

    @Test
    void shouldDiscard5WhenThePresidentIsTheCurrentPlayer() {
        president(FELIX_GAETA);
        draw(CURRENT, 6);

        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);

        assertEquals(1, player(CURRENT).skillCards().size());
    }

    @Test
    void shouldDiscard6WhenTheCagIsTheAdmiral() {
        cag(WILLIAM_ADAMA);
        draw(ADMIRAL, 7);

        resolve(RAIDERS_INBOUND, BOTTOM);

        assertEquals(1, player(ADMIRAL).skillCards().size());
    }

    @Test
    void shouldAllowTheSectionWhenItCantBeFulfilled() {
        draw(PRESIDENT, 1);

        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);

        assertNoSkillCards(PRESIDENT);
        assertNoSkillCards(CURRENT);
    }

    @Test
    void shouldLeaveARevealedCylonWhereTheyAre() {
        moveTo(CAPRICA, FELIX_GAETA);
        draw(CURRENT, 3);
        revealCylon();

        resolve(HIDDEN_EXPLOSIVES, TOP);
        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);

        assertEquals(3, raptorsInReserves());
        assertEquals(CAPRICA, locate(FELIX_GAETA));
        assertEquals(3, player(CURRENT).skillCards().size());
    }

    private void draw(int playerNumber, int count) {
        ops.player(playerNumber).drawSkillCards(Collections.<SkillCardColor>nCopies(count, POLITICS));
    }

    private long raptorsInReserves() {
        return galacticaBoard.reserves().stream().filter(Raptor.class::isInstance).count();
    }

    /* a minimal engine */

    private void resolve(CrisisCard card, Section section) {
        val choice = (Followup.OneOf) execute(new ResolveCrisisCardEvent(card));
        run(choice.options().stream()
                .filter(option -> ((Followup.Single) option).event() instanceof ChooseCrisisSectionEvent e
                        && e.section() == section)
                .findFirst()
                .orElseThrow());
    }

    private void run(Followup followup) {
        switch (followup) {
            case Followup.None ignored -> {
            }
            case Followup.Single single -> run(single.event() instanceof PlayerDecisionEvent<?> decision
                    ? execute(discardFirstCards(decision))
                    : execute(single.event()));
            case Followup.AllOf all -> all.followups().forEach(this::run);
            case Followup.OneOf ignored -> fail("A section has no further choices");
        }
    }

    // A player with fewer cards than the limit discards all of them.
    private Event discardFirstCards(PlayerDecisionEvent<?> decision) {
        assertEquals(DiscardSkillCardsEvent.class, decision.action());
        val limit = decision.eventConstraints().getFirst();
        val count = Integer.parseInt(limit.name().substring(limit.name().lastIndexOf('_') + 1));
        val hand = player(decision.playerNumber()).skillCards().cards();
        val cards = List.copyOf(hand.subList(0, Math.min(count, hand.size())));
        return new DiscardSkillCardsEvent(decision.playerNumber(), cards, limit);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <E extends Event> Followup execute(E event) {
        val processorClass = Class.forName(event.getClass().getName() + "Processor");
        val processor = (EventProcessor<E>) processorClass.getDeclaredConstructor().newInstance();
        return processor.prepare(game, event).run(ops);
    }
}
