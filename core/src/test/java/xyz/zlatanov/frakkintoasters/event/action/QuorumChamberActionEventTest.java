package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class QuorumChamberActionEventTest extends EventTest {

    @Test
    void shouldDrawQuorumCardAndFollowup() {
        player(1).selectCharacter(LAURA_ROSLIN);
        game.president(LAURA_ROSLIN);

        val followup = execute(new QuorumChamberActionEvent(1));

        assertEquals(1, presidentHand.cards().size());
        assertEquals(expectedFollowup(), followup);
    }

    private Followup expectedFollowup() {
        return one(
                new DrawQuorumCardEvent(),
                new PlayQuorumCardEvent());
    }
}
