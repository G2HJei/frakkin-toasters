package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.character.DelusionalIntuitionEvent;
import xyz.zlatanov.frakkintoasters.event.character.ReligiousVisionsEvent;
import xyz.zlatanov.frakkintoasters.event.character.SecretDestinyEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ResolveCrisisCardEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.GameStep.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RAIDERS_INBOUND;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

class CrisisStepEventProcessorTest extends EventTestHarness<CrisisStepEvent> {

    @Test
    void theCurrentPlayerDrawsACrisisCardThatStaysWithTheTurn() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        crisisDeck.nextCard(RAIDERS_INBOUND);

        execute(new CrisisStepEvent());

        assertEquals(CRISIS, game.turn().step());
        assertEquals(List.of(RAIDERS_INBOUND), game.turn().crisisCards());
        assertEquals(0, crisisDeck.discardSize()); // until the end of the turn (rule 8.8)
        assertFollowup(new ResolveCrisisCardEvent(RAIDERS_INBOUND));
    }

    // "After the Activate Cylon Ships step, return to the Resolve Crisis step": the new card brings its own step.
    @Test
    void aCardThatReturnedToTheStepActivatesTheCylonShipsAfterIt() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        crisisDeck.nextCard(DETENTE);

        execute(new CrisisStepEvent(true));

        assertFollowup(all(new ResolveCrisisCardEvent(DETENTE), new ActivateCylonShipsStepEvent()));
    }

    // "Religious Visions": Laura Roslin draws 2 and picks the one to resolve, only she sees them.
    @Test
    void lauraRoslinDrawsTwoCardsAndPicksOne() {
        player(1).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        crisisDeck.nextCard(RAIDERS_INBOUND, DETENTE);

        execute(new CrisisStepEvent());

        assertEquals(List.of(), game.turn().crisisCards()); // until she picks one
        assertFollowup(one(
                new ReligiousVisionsEvent(1, RAIDERS_INBOUND, DETENTE),
                new ReligiousVisionsEvent(1, DETENTE, RAIDERS_INBOUND)));
    }

    // "Secret Destiny": once the card is revealed, Kara "Starbuck" Thrace may replace it, or let it resolve.
    @Test
    void starbuckMayReplaceTheRevealedCard() {
        player(1).character(LOUANNE_KAT_KATRAINE);
        moveTo(HANGAR_DECK, LOUANNE_KAT_KATRAINE);
        player(2).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        crisisDeck.nextCard(RAIDERS_INBOUND);

        execute(new CrisisStepEvent());

        assertFollowup(one(new SecretDestinyEvent(2, RAIDERS_INBOUND), new ResolveCrisisCardEvent(RAIDERS_INBOUND)));
    }

    // "Delusional Intuition": after drawing the card, Gaius Baltar draws a Skill Card.
    @Test
    void gaiusBaltarDrawsASkillCardAfterTheCrisisCard() {
        player(1).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
        crisisDeck.nextCard(RAIDERS_INBOUND);

        execute(new CrisisStepEvent());

        assertFollowup(all(new DelusionalIntuitionEvent(1), new ResolveCrisisCardEvent(RAIDERS_INBOUND)));
    }

    // "Exceptions: Revealed Cylons and characters in the Brig do not perform this step." (rule 8.5)
    @Test
    void aRevealedCylonHasNoCrisisStep() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(CAPRICA, KARA_STARBUCK_THRACE);
        revealCylon();

        assertNoCrisisStep();
    }

    @Test
    void aCharacterInTheBrigHasNoCrisisStep() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        assertNoCrisisStep();
    }

    // A Cylon Leader counts as revealed (rule 6.2.1).
    @Test
    void aCylonLeaderHasNoCrisisStep() {
        setUpGame(4);
        player(1).character(CAVIL);
        moveTo(CYLON_FLEET, CAVIL);

        assertNoCrisisStep();
    }

    // "An Infiltrating Cylon Leader draws a Crisis Card at the end of his turn." (rule 6.2.3.4)
    @Test
    void anInfiltratingCylonLeaderDrawsACrisisCard() {
        setUpGame(4);
        player(1).character(SHARON_ATHENA_AGATHON).infiltrateGalactica();
        moveTo(HANGAR_DECK, SHARON_ATHENA_AGATHON);
        crisisDeck.nextCard(RAIDERS_INBOUND);

        execute(new CrisisStepEvent());

        assertEquals(1, game.turn().crisisCards().size());
        assertFollowup(new ResolveCrisisCardEvent(RAIDERS_INBOUND));
    }

    @Test
    void strandedHeloDrawsACrisisCard() {
        player(1).character(KARL_HELO_AGATHON);
        crisisDeck.nextCard(RAIDERS_INBOUND);

        execute(new CrisisStepEvent());

        assertEquals(1, game.turn().crisisCards().size());
        assertFollowup(new ResolveCrisisCardEvent(RAIDERS_INBOUND));
    }

    private void assertNoCrisisStep() {
        val deck = crisisDeck.cards();

        execute(new CrisisStepEvent());

        assertEquals(SETUP, game.turn().step());
        assertEquals(deck, crisisDeck.cards());
        assertEquals(0, crisisDeck.discardSize());
        assertEquals(List.of(), game.turn().crisisCards());
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new CrisisStepEvent());

        assertEquals(SETUP, game.turn().step());
    }
}
