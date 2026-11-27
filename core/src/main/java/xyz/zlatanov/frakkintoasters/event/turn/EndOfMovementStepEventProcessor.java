package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.character.ForcedDiscards;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.character.PoliticallyAdroitEvent;
import xyz.zlatanov.frakkintoasters.event.character.PoliticallyAdroitEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.KLEPTOMANIA;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.POLITICALLY_ADROIT;

public class EndOfMovementStepEventProcessor extends EventProcessor<EndOfMovementStepEvent> {

    static final int KLEPTOMANIA_DISCARDS = 2;

    public static boolean actsThen(PlayerView player) {
        return player.hasAbility(POLITICALLY_ADROIT) || player.hasAbility(KLEPTOMANIA);
    }

    @Override
    public Followup process() {
        if (game.turn().endedEarly()) {
            return NONE;
        }
        val current = game.currentPlayer();
        val followups = new ArrayList<Followup>();
        if (PoliticallyAdroitEventProcessor.canGiveToAnyone(game, current)) {
            // "may": a card for 2 is offered, never forced
            followups.add(one(
                    new PlayerDecisionEvent<>(current.number(), PoliticallyAdroitEvent.class),
                    new NoOpEvent(current.number())));
        }
        if (current.hasAbility(KLEPTOMANIA) && withAnotherPlayer(game, current)) {
            ops.player(current.number()).useAbility(KLEPTOMANIA);
            if (current.skillCards().size() >= KLEPTOMANIA_DISCARDS) {
                // a forced discard: "For Love" may make it smaller
                followups.add(single(ForcedDiscards.discard(game, current.number(), KLEPTOMANIA_DISCARDS)));
            } else {
                ops.sendToBrigAtEndOfTurn(current.number()); // he can't discard 2
            }
        }
        return all(followups.toArray(Followup[]::new));
    }

    private static boolean withAnotherPlayer(GameView game, PlayerView player) {
        val location = game.locate(player.character());
        return location.isPresent() && game.players().stream()
                .filter(other -> other.number() != player.number() && other.character() != null)
                .anyMatch(other -> game.locate(other.character()).equals(location));
    }
}
