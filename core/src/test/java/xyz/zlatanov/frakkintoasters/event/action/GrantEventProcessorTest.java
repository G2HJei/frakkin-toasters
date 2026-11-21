package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.GameFact.ActingPlayerChanged;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_4_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;

class GrantEventProcessorTest extends EventTestHarness<GrantEvent> {

    // The choice is made as the game stands when the grant runs: the pilot the Hangar Deck just launched.
    @Test
    void aGrantedActionIsOfferedWhenItRuns() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.player(1).pilot(viperAt(GALACTICA_SPACE_4_OCLOCK).id());
        player(1).gainSkillCards(new SkillCard(6, BUILD_NUKE));

        execute(new GrantEvent(1, ACTION, GrantEvent.GrantedBy.HANGAR_DECK));

        assertFollowup(one(new PlayerDecisionEvent<>(1, ActivateViperEvent.class),
                new PlayerDecisionEvent<>(1, ActionEvent.class),
                new NoOpEvent(1)));
        assertEquals(List.of(), actingPlayerChanges()); // he was the acting player already
    }

    @Test
    void anotherPlayerBecomesTheActingPlayer() {
        player(2).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);

        execute(new GrantEvent(2, ACTION, GrantEvent.GrantedBy.HANGAR_DECK));

        assertFollowup(one(new PlayerDecisionEvent<>(2, ActionEvent.class), new NoOpEvent(2)));
        assertEquals(2, game.turn().actingPlayerNumber());
        assertEquals(List.of(new ActingPlayerChanged(2)), actingPlayerChanges());
    }

    @Test
    void theGrantNamesAPlayer() {
        assertInvalid(new GrantEvent(9, ACTION, GrantEvent.GrantedBy.HANGAR_DECK));
    }

    private List<ActingPlayerChanged> actingPlayerChanges() {
        return journal.facts().stream()
                .filter(ActingPlayerChanged.class::isInstance)
                .map(ActingPlayerChanged.class::cast)
                .toList();
    }
}
