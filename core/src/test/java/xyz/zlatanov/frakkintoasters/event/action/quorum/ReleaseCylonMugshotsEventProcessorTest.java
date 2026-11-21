package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.LookAtLoyaltyCardEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.RELEASE_CYLON_MUGSHOTS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ReleaseCylonMugshotsEventProcessorTest extends EventTestHarness<ReleaseCylonMugshotsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(2).loyaltyCards().addOnTop(NOT_CYLON);
        player(3).character(KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(RELEASE_CYLON_MUGSHOTS);
    }

    @Test
    void thePresidentLooksThenRolls() {
        execute(new ReleaseCylonMugshotsEvent(1, 2));

        assertFollowup(all(single(new LookAtLoyaltyCardEvent(1, 2)), rollDie(new ReleaseCylonMugshotsResultEvent())));
    }

    @Test
    void notAPlayerWithoutLoyaltyCards() {
        assertInvalid(new ReleaseCylonMugshotsEvent(1, 3));
    }

    @Test
    void notHisOwnCards() {
        player(1).loyaltyCards().addOnTop(NOT_CYLON);

        assertInvalid(new ReleaseCylonMugshotsEvent(1, 1));
    }
}
