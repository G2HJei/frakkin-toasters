package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MORAL_COMPASS;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.ABANDON_GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;

class MoralCompassEventProcessorTest extends EventTestHarness<MoralCompassEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
        player(2).character(KARL_HELO_AGATHON);
        ops.playCrisisCard(RIOTS_NO_JUMP);
    }

    // The other section resolves: the bottom of "Riots" costs 1 population and 1 fuel.
    @Test
    void theOtherSectionResolves() {
        execute(new MoralCompassEvent(2, RIOTS_NO_JUMP, BOTTOM));

        assertEquals(0, player(2).miracleTokens());
        assertTrue(game.turn().hasUsed(2, MORAL_COMPASS));
        assertFollowup(all(lose(POPULATION, 1), lose(FUEL, 1)));
    }

    // He changes another player's choice, to a section that can be chosen.
    @Test
    void whoMayChange() {
        assertEquals(Optional.of(2), MoralCompassEventProcessor.changer(game, 1, RIOTS_NO_JUMP, BOTTOM));
        assertEquals(Optional.empty(), MoralCompassEventProcessor.changer(game, 2, RIOTS_NO_JUMP, BOTTOM));
        while (game.nukes() > 0) {
            ops.discardNuke();
        }
        // "Abandon Galactica"'s top needs a nuke token
        assertEquals(Optional.empty(), MoralCompassEventProcessor.changer(game, 1, ABANDON_GALACTICA, TOP));
        assertInvalid(new MoralCompassEvent(1, RIOTS_NO_JUMP, BOTTOM)); // Saul Tigh hasn't the ability
    }

    @Test
    void onlyOnTheCardBeingResolved() {
        assertInvalid(new MoralCompassEvent(2, ABANDON_GALACTICA, BOTTOM));
    }

    @Test
    void onlyWithAMiracleToken() {
        ops.player(2).loseMiracleTokens();

        assertInvalid(new MoralCompassEvent(2, RIOTS_NO_JUMP, BOTTOM));
    }
}
