package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.deck.ShuffleTreacheryIntoDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.BAIT_AND_SWITCH;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class BaitAndSwitchEventProcessorTest extends EventTestHarness<BaitAndSwitchEvent> {

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
        player(1).mutinyCards().addOnTop(BAIT_AND_SWITCH);
    }

    @Test
    void drawsTwoOfAnyTypeThenTwoTreacheryGoIntoTheDestinyDeck() {
        execute(new BaitAndSwitchEvent(1));

        assertFollowup(all(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_2_OF_ANY_COLOR),
                new ShuffleTreacheryIntoDestinyDeckEvent(2), new DiscardPlayedMutinyCardEvent(BAIT_AND_SWITCH)));
    }
}
