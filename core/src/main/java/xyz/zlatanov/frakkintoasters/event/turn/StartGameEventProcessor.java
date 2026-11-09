package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.CreateLoyaltyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextCagEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;

public class StartGameEventProcessor extends EventProcessor<StartGameEvent> {

    // Rule A3.1, without the steps the Kobol objective doesn't use: Objective Set Up (A3.6) and Trauma and Allies
    // (A3.10). The game boards (A3.3) are set up when the game is built.
    @Override
    public Followup process() {
        ops.startGame(); // shuffles the decks (A3.5)
        ops.galactica().setUp(); // ships (A3.4)
        val firstPlayer = ops.chooseFirstPlayer(); // A3.7.1
        return all(
                single(new CreateDestinyDeckEvent()), // A3.5.1
                selectCharacters(firstPlayer),
                all(new DetermineNextPresidentEvent(), new DetermineNextAdmiralEvent(), new DetermineNextCagEvent()), // A3.7.3
                single(new CreateLoyaltyDeckEvent()), // A3.8
                single(new ReceiveStartingSkillsEvent()), // A3.9
                single(new StartTurnEvent()));
    }

    // Clockwise from the first player (rule A3.7.2).
    private Followup selectCharacters(int firstPlayer) {
        return all(playersFrom(game, firstPlayer).stream()
                .map(player -> new PlayerDecisionEvent<>(player.number(), SelectCharacterEvent.class))
                .toArray(Event[]::new));
    }
}
