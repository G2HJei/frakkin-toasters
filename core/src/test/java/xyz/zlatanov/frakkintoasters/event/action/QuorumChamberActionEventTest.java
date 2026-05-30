package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayQuorumCardEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class QuorumChamberActionEventTest extends EventTest {

    @Test
    void shouldDrawQuorumCardAndFollowup() {
        selectCharacter(1, LAURA_ROSLIN);
        game.president(LAURA_ROSLIN);

        executeAndAssertFollowup(new QuorumChamberActionEvent(1),
                one(
                        new DrawQuorumCardEvent(),
                        new PlayQuorumCardEvent()));

        assertEquals(1, presidentHand.cards().size());
    }
    
}
