package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_BOOMER_VALERII;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.SLEEPER_AGENT;

class SleeperAgentEventProcessorTest extends EventTestHarness<SleeperAgentEvent> {

    @Test
    void boomerGoesToTheBrig() {
        player(1).character(SHARON_BOOMER_VALERII);
        moveTo(ARMORY, SHARON_BOOMER_VALERII);

        execute(new SleeperAgentEvent(1));

        assertFollowup(new SendToBrigEvent(1));
        assertTrue(game.turn().hasUsed(1, SLEEPER_AGENT));
    }

    // Her cards may have put her there already, e.g. a second Mutiny Card.
    @Test
    void boomerAlreadyInTheBrigStays() {
        player(1).character(SHARON_BOOMER_VALERII);
        moveTo(BRIG, SHARON_BOOMER_VALERII);

        execute(new SleeperAgentEvent(1));
    }

    @Test
    void onlyBoomerHasIt() {
        player(1).character(WILLIAM_ADAMA);

        assertInvalid(new SleeperAgentEvent(1));
    }

    // A revealed Cylon's abilities are ignored (rule 6.1.2.2).
    @Test
    void aRevealedBoomerDoesntHaveIt() {
        player(1).character(SHARON_BOOMER_VALERII);
        revealCylon();

        assertInvalid(new SleeperAgentEvent(1));
    }
}
