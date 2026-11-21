package xyz.zlatanov.frakkintoasters.event.loyalty;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.discard;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_SICKBAY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class CylonPowerSendToSickbayEventProcessorTest extends EventTestHarness<CylonPowerSendToSickbayEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_SICKBAY).reveal(CYLON_SEND_TO_SICKBAY);
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
    }

    // The character discards 5 Skill Cards, then goes to "Sickbay".
    @Test
    void shouldDiscardFiveCardsAndGoToSickbay() {
        player(2).gainSkillCards(new SkillCard(3, LAUNCH_SCOUT));

        execute(new CylonPowerSendToSickbayEvent(1, 2));

        assertFollowup(all(discard(2, 5), new SendToSickbayEvent(2)));
    }

    @Test
    void shouldNotAskForADiscardWithoutCards() {
        execute(new CylonPowerSendToSickbayEvent(1, 2));

        assertFollowup(new SendToSickbayEvent(2));
    }

    @Test
    void shouldRejectACharacterOnColonialOne() {
        assertInvalid(new CylonPowerSendToSickbayEvent(1, 3));
    }
}
