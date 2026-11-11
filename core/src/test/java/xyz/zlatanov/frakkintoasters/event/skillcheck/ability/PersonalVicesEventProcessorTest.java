package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class PersonalVicesEventProcessorTest extends EventTestHarness<PersonalVicesEvent> {

    private final SkillCard treachery1 = new SkillCard(1, DRADIS_CONTACT);
    private final SkillCard treachery2 = new SkillCard(2, BAIT);
    private final SkillCard treachery3 = new SkillCard(3, A_BETTER_MACHINE);

    @Test
    void shouldDrawTreachery() {
        seat(LOUANNE_KAT_KATRAINE, KARA_STARBUCK_THRACE, LEE_ADAMA);
        treacheryDeck.nextCard(treachery1, treachery2, treachery3);

        execute(new PersonalVicesEvent());

        assertSkillCards(1, treachery1);
        assertSkillCards(2, treachery2);
        assertSkillCards(3, treachery3);
        assertFollowup(new DrawMutinyCardEvent(1));
    }

    // "Each human player draws 1 Treachery Card."
    @Test
    void aRevealedCylonDrawsNoTreachery() {
        seat(LOUANNE_KAT_KATRAINE, KARA_STARBUCK_THRACE, LEE_ADAMA);
        player(2).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);
        treacheryDeck.nextCard(treachery1, treachery3);

        execute(new PersonalVicesEvent());

        assertSkillCards(1, treachery1);
        assertNoSkillCards(2);
        assertSkillCards(3, treachery3);
        assertFollowup(new DrawMutinyCardEvent(1));
    }

    @Test
    void aRevealedCylonCurrentPlayerDrawsNoTreachery() {
        seat(LOUANNE_KAT_KATRAINE, KARA_STARBUCK_THRACE, LEE_ADAMA);
        revealCylon();
        treacheryDeck.nextCard(treachery2, treachery3);

        execute(new PersonalVicesEvent());

        assertNoSkillCards(1);
        assertSkillCards(2, treachery2);
        assertSkillCards(3, treachery3);
        assertFollowup(new DrawMutinyCardEvent(1)); // which draws nothing for a revealed Cylon
    }

    // A Cylon Leader is a human player only while Infiltrating (rule 6.2.3.1).
    @Test
    void aCylonLeaderDrawsTreacheryOnlyWhileInfiltrating() {
        seat(LOUANNE_KAT_KATRAINE, CAPRICA_SIX, CAVIL);
        player(3).infiltrateGalactica();
        treacheryDeck.nextCard(treachery1, treachery3);

        execute(new PersonalVicesEvent());

        assertSkillCards(1, treachery1);
        assertNoSkillCards(2);
        assertSkillCards(3, treachery3);
        assertFollowup(new DrawMutinyCardEvent(1));
    }

    // "Players will be unable to draw cards of that type until some of that type of cards have been discarded."
    // (rule 8.2.2.5)
    @Test
    void theLastTreacheryCardsGoClockwiseFromTheCurrentPlayer() {
        seat(LOUANNE_KAT_KATRAINE, KARA_STARBUCK_THRACE, LEE_ADAMA);
        game.turn().currentPlayerNumber(3);
        treacheryDeck.clear();
        game.decks().discard(treachery1);
        game.decks().discard(treachery2);

        execute(new PersonalVicesEvent());

        // the reshuffle picks who gets which
        assertEquals(1, player(3).skillCards().size());
        assertEquals(1, player(1).skillCards().size());
        assertNoSkillCards(2);
        assertFollowup(new DrawMutinyCardEvent(3));
    }

    private void seat(Character... characters) {
        for (int i = 0; i < characters.length; i++) {
            player(i + 1).character(characters[i]);
        }
    }
}
