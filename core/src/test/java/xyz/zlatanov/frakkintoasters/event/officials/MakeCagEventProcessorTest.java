package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class MakeCagEventProcessorTest extends EventTestHarness<MakeCagEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(2).character(LEE_APOLLO_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    @Test
    void shouldChangeTheCag() {
        execute(new MakeCagEvent(KARA_STARBUCK_THRACE));

        assertEquals(KARA_STARBUCK_THRACE, game.cag());
    }

    @Test
    void notTheCagAgain() {
        assertInvalid(new MakeCagEvent(LEE_APOLLO_ADAMA));
    }

    // The CAG loses the title in the "Brig" (rule 4.4.3.2).
    @Test
    void notACharacterInTheBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        assertInvalid(new MakeCagEvent(KARA_STARBUCK_THRACE));
    }
}
