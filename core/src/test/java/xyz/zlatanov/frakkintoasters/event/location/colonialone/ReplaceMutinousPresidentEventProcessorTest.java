package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ReplaceMutinousPresidentEventProcessorTest extends EventTestHarness<ReplaceMutinousPresidentEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(TOM_ZAREK);
        player(2).character(LAURA_ROSLIN);
        player(3).character(SAUL_TIGH);
        president(LAURA_ROSLIN);
    }

    // "If the President has any Mutiny Cards, choose a player to gain the President title."
    @Test
    void shouldLetThePlayerChooseANewPresident() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);

        execute(new ReplaceMutinousPresidentEvent(1));

        assertFollowup(new PlayerDecisionEvent<>(1, MakePresidentEvent.class));
    }

    // While the Vice President isn't President, nobody else can be chosen: there is no choice.
    @Test
    void theVicePresidentBecomesPresident() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        ops.titles().putInPlay(QuorumCard.ASSIGN_VICE_PRESIDENT, 3);

        execute(new ReplaceMutinousPresidentEvent(1));

        assertFollowup(new MakePresidentEvent(SAUL_TIGH));
    }

    @Test
    void aVicePresidentWhoIsPresidentChangesNothing() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        ops.titles().putInPlay(QuorumCard.ASSIGN_VICE_PRESIDENT, 2);

        execute(new ReplaceMutinousPresidentEvent(1));

        assertFollowup(new PlayerDecisionEvent<>(1, MakePresidentEvent.class));
    }

    // "Accept Prophecy" lets the President keep the title: he is asked first.
    @Test
    void thePresidentMayDefendThePresidencyWithAcceptProphecy() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        ops.titles().putInPlay(QuorumCard.ACCEPT_PROPHECY, null);

        execute(new ReplaceMutinousPresidentEvent(1));

        assertFollowup(one(new DefendPresidencyEvent(2, true, 1), new DefendPresidencyEvent(2, false, 1)));
    }

    @Test
    void shouldKeepAPresidentWithoutMutinyCards() {
        execute(new ReplaceMutinousPresidentEvent(1));
    }

    // The decision needs an answer: revealed Cylons hold no titles.
    @Test
    void shouldKeepThePresidentWhenNobodyElseCanTakeTheTitle() {
        player(2).mutinyCards().addOnTop(WEAPONS_ARMED);
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);
        player(3).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);

        execute(new ReplaceMutinousPresidentEvent(1));
    }
}
