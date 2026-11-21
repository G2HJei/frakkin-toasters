package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.BETRAYAL_OF_TRUST;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class BetrayalOfTrustEventProcessorTest extends EventTestHarness<BetrayalOfTrustEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(BETRAYAL_OF_TRUST);
    }

    @Test
    void twoTreacheryThenALookAtTheTopDestinationCard() {
        execute(new BetrayalOfTrustEvent(1));

        assertFollowup(all(treachery(1, 2), new LookAtTopCardEvent(1, DESTINATION),
                new DiscardPlayedMutinyCardEvent(BETRAYAL_OF_TRUST)));
    }
}
