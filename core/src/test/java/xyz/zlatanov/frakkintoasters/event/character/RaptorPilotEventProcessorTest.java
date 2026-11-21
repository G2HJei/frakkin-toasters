package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON_ALT;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.RAPTOR_PILOT;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.RIOTS_NO_JUMP;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.WATER_SABOTAGED;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;

class RaptorPilotEventProcessorTest extends EventTestHarness<RaptorPilotEvent> {

    // The top 2: the one put back first, top or bottom, then the other.
    @Test
    void heloPutsEachOfTheTopTwoBack() {
        player(1).character(KARL_HELO_AGATHON_ALT);
        crisisDeck.nextCard(RIOTS_NO_JUMP, WATER_SABOTAGED);

        execute(new RaptorPilotEvent(1, CRISIS));

        assertTrue(game.turn().hasUsed(1, RAPTOR_PILOT));
        var placeWater = one(new PlaceCrisisCardOnTopEvent(1, WATER_SABOTAGED), new PlaceCrisisCardOnBottomEvent(1, WATER_SABOTAGED));
        var placeRiots = one(new PlaceCrisisCardOnTopEvent(1, RIOTS_NO_JUMP), new PlaceCrisisCardOnBottomEvent(1, RIOTS_NO_JUMP));
        assertFollowup(one(
                all(single(new PlaceCrisisCardOnTopEvent(1, RIOTS_NO_JUMP)), placeWater),
                all(single(new PlaceCrisisCardOnBottomEvent(1, RIOTS_NO_JUMP)), placeWater),
                all(single(new PlaceCrisisCardOnTopEvent(1, WATER_SABOTAGED)), placeRiots),
                all(single(new PlaceCrisisCardOnBottomEvent(1, WATER_SABOTAGED)), placeRiots)));
    }

    @Test
    void onlyHelo() {
        player(1).character(LAURA_ROSLIN);

        assertInvalid(new RaptorPilotEvent(1, CRISIS));
    }
}
