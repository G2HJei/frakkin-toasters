package xyz.zlatanov.frakkintoasters.event.character;

import java.util.Optional;
import xyz.zlatanov.frakkintoasters.event.action.character.EndRemoteActionEvent;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommunicationsEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DUTIFUL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class DutifulEventProcessorTest extends EventTestHarness<DutifulEvent> {

    private static final SkillCard CARD = new SkillCard(1, CONSOLIDATE_POWER);

    // On his turn, after "Communications": a card for another activation.
    @Test
    void hoshiActivatesTheLocationAgain() {
        player(1).character(LOUIS_HOSHI);
        player(1).gainSkillCards(CARD);
        moveTo(COMMUNICATIONS, LOUIS_HOSHI);
        assertEquals(single(new OfferDutifulEvent(1, COMMUNICATIONS)),
                DutifulEventProcessor.after(game, player(1), COMMUNICATIONS));
        assertEquals(one(new DutifulEvent(1, COMMUNICATIONS, CARD), new NoOpEvent(1)),
                DutifulEventProcessor.offer(game, player(1), COMMUNICATIONS));

        execute(new DutifulEvent(1, COMMUNICATIONS, CARD));

        assertTrue(game.turn().hasUsed(1, DUTIFUL));
        assertFollowup(all(new DiscardSkillCardsEvent(1, List.of(CARD)),
                new PlayerDecisionEvent<>(1, CommunicationsEvent.class)));
        assertEquals(NONE, DutifulEventProcessor.after(game, player(1), COMMUNICATIONS)); // once a turn
    }

    @Test
    void notWithoutACard() {
        player(1).character(LOUIS_HOSHI);
        moveTo(COMMUNICATIONS, LOUIS_HOSHI);

        assertEquals(NONE, DutifulEventProcessor.offer(game, player(1), COMMUNICATIONS));
        assertInvalid(new DutifulEvent(1, COMMUNICATIONS, CARD));
    }

    @Test
    void notAnotherLocation() {
        player(1).character(LOUIS_HOSHI);
        player(1).gainSkillCards(CARD);
        moveTo(RESEARCH_LAB, LOUIS_HOSHI);

        assertEquals(NONE, DutifulEventProcessor.offer(game, player(1), RESEARCH_LAB));
        assertInvalid(new DutifulEvent(1, RESEARCH_LAB, CARD));
    }

    @Test
    void notOnAnotherPlayersTurn() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(LOUIS_HOSHI);
        player(2).gainSkillCards(CARD);
        moveTo(COMMUNICATIONS, LOUIS_HOSHI);

        assertEquals(NONE, DutifulEventProcessor.after(game, player(2), COMMUNICATIONS));
        assertInvalid(new DutifulEvent(2, COMMUNICATIONS, CARD));
    }

    // Activated from elsewhere, e.g. with an "Unorthodox Plan": he takes the action there again, as a remote action.
    @Test
    void hoshiActivatesTheLocationAgainFromElsewhere() {
        player(1).character(LOUIS_HOSHI);
        player(1).gainSkillCards(CARD);
        moveTo(RESEARCH_LAB, LOUIS_HOSHI);
        assertEquals(one(new DutifulEvent(1, COMMUNICATIONS, CARD), new NoOpEvent(1)),
                DutifulEventProcessor.offer(game, player(1), COMMUNICATIONS));

        execute(new DutifulEvent(1, COMMUNICATIONS, CARD));

        assertEquals(Optional.of(COMMUNICATIONS), game.turn().remoteActionLocation(1));
        assertFollowup(allOf(single(new DiscardSkillCardsEvent(1, List.of(CARD))),
                all(new PlayerDecisionEvent<>(1, LocationEvent.class), new EndRemoteActionEvent(1))));
    }

    // Not inside "Organized", which never takes the same location twice.
    @Test
    void notDuringOrganized() {
        player(1).character(LOUIS_HOSHI);
        player(1).gainSkillCards(CARD);
        moveTo(RESEARCH_LAB, LOUIS_HOSHI);
        ops.allowRemoteAction(1, COMMUNICATIONS);

        assertEquals(NONE, DutifulEventProcessor.offer(game, player(1), COMMUNICATIONS));
        assertInvalid(new DutifulEvent(1, COMMUNICATIONS, CARD));
    }
}
