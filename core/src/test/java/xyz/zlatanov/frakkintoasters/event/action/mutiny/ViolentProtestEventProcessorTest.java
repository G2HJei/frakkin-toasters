package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.VIOLENT_PROTEST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ViolentProtestEventProcessorTest extends EventTestHarness<ViolentProtestEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(WILLIAM_ADAMA);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(VIOLENT_PROTEST);
    }

    @Test
    void twoPoliticsAndThePresidentGoesToSickbay() {
        execute(new ViolentProtestEvent(1));

        assertFollowup(all(new DrawSkillCardsOfColorEvent(1, POLITICS, 2), new SendToSickbayEvent(2),
                new DiscardPlayedMutinyCardEvent(VIOLENT_PROTEST)));
    }
}
