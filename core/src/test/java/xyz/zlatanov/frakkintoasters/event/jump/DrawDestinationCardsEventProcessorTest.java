package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.CYLON_AMBUSH;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DEEP_SPACE;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class DrawDestinationCardsEventProcessorTest extends EventTestHarness<DrawDestinationCardsEvent> {

    @BeforeEach
    void anAdmiral() {
        player(2).character(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
    }

    // "The Admiral draws two cards from the Destination deck, chooses one, and places the other on the bottom of the deck."
    @Test
    void theAdmiralChoosesOneOfTwoCards() {
        destinationDeck.nextCard(ICY_MOON, DEEP_SPACE);

        execute(new DrawDestinationCardsEvent());

        assertFollowup(one(new ChooseDestinationEvent(2, ICY_MOON, List.of(DEEP_SPACE)),
                new ChooseDestinationEvent(2, DEEP_SPACE, List.of(ICY_MOON))));
    }

    // The Mission Specialist chooses instead of the Admiral, out of 3 cards, and the card leaves play.
    @Test
    void theMissionSpecialistChoosesOneOfThreeCards() {
        player(3).character(KARA_STARBUCK_THRACE);
        ops.titles().putInPlay(QuorumCard.ASSIGN_MISSION_SPECIALIST, 3);
        destinationDeck.nextCard(ICY_MOON, DEEP_SPACE, CYLON_AMBUSH);

        execute(new DrawDestinationCardsEvent());

        assertFollowup(one(new ChooseDestinationEvent(3, ICY_MOON, List.of(DEEP_SPACE, CYLON_AMBUSH)),
                new ChooseDestinationEvent(3, DEEP_SPACE, List.of(ICY_MOON, CYLON_AMBUSH)),
                new ChooseDestinationEvent(3, CYLON_AMBUSH, List.of(ICY_MOON, DEEP_SPACE))));
        assertEquals(List.of(), game.quorumCardsInPlay());
    }

    // "Blind Jump": the Admiral draws 1 card, so there is nothing to choose.
    @Test
    void afterABlindJumpTheAdmiralDrawsOneCard() {
        destinationDeck.nextCard(xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON);

        execute(new DrawDestinationCardsEvent(true));

        assertFollowup(new ResolveDestinationCardEvent(xyz.zlatanov.frakkintoasters.state.card.DestinationCard.ICY_MOON));
    }
}
