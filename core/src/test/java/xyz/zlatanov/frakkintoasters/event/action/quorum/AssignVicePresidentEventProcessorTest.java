package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;

import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_VICE_PRESIDENT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class AssignVicePresidentEventProcessorTest extends EventTestHarness<AssignVicePresidentEvent> {

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(4).character(SHARON_ATHENA_AGATHON);
        ops.player(4).infiltrateGalactica(HANGAR_DECK);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ASSIGN_VICE_PRESIDENT);
    }

    @Test
    void anotherPlayerBecomesVicePresident() {
        execute(new AssignVicePresidentEvent(1, 2));

        assertFollowup(new DrawSkillCardsOfColorEvent(1, POLITICS, 2));
    }

    // An Infiltrating Cylon Leader may hold the other cards, not this one (rule 6.2.3.3).
    @Test
    void notAnInfiltratingCylonLeader() {
        assertInvalid(new AssignVicePresidentEvent(1, 4));
    }
}
