package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.treachery;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_DRAW_TREACHERY_CARDS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class CylonPowerDrawTreacheryEventProcessorTest extends EventTestHarness<CylonPowerDrawTreacheryEvent> {

    // Each human, from the Cylon's left, trades a random Skill Card for a Treachery card, then the Cylon draws 2.
    @Test
    void shouldMakeEachHumanTradeACardForTreachery() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        player(2).loyaltyCards().addOnTop(CYLON_DRAW_TREACHERY_CARDS).reveal(CYLON_DRAW_TREACHERY_CARDS);

        execute(new CylonPowerDrawTreacheryEvent(2));

        assertFollowup(all(
                all(new DiscardRandomSkillCardsEvent(3, 1), treachery(3, 1)),
                all(new DiscardRandomSkillCardsEvent(1, 1), treachery(1, 1)),
                single(treachery(2, 2))));
    }
}
