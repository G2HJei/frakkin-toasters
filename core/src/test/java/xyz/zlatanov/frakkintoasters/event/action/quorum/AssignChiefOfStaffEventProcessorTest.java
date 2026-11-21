package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_CHIEF_OF_STAFF;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class AssignChiefOfStaffEventProcessorTest extends EventTestHarness<AssignChiefOfStaffEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ASSIGN_CHIEF_OF_STAFF);
    }

    @Test
    void anotherPlayerHoldsTheCard() {
        execute(new AssignChiefOfStaffEvent(1, 2));

        assertEquals(Optional.of(2), game.quorumCardHolder(ASSIGN_CHIEF_OF_STAFF));
        assertFollowup(new DrawSkillCardsOfColorEvent(1, POLITICS, 2));
    }

    @Test
    void notThePresidentHimself() {
        assertInvalid(new AssignChiefOfStaffEvent(1, 1));
    }
}
