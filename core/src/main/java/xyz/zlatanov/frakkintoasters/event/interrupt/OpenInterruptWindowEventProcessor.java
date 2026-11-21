package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Objects;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEventProcessor.holdsCardFor;
import static xyz.zlatanov.frakkintoasters.event.interrupt.RollAbilities.hasAbilityFor;

public class OpenInterruptWindowEventProcessor extends EventProcessor<OpenInterruptWindowEvent> {

    @Override
    protected boolean isValid() {
        return event.window() != null && game.turn().interruptWindow().isEmpty();
    }

    @Override
    public Followup process() {
        val window = event.window();
        if (window.startsDieRoll()) {
            ops.startDieRoll();
        } else if (window.followsDieRoll() && game.turn().dieRoll().fixed()) {
            return NONE; // "Longshot": nothing modifies or rerolls the result he chose
        }
        // A player is asked for a card when he holds one for the window, then for an ability when he has one for it.
        val decisions = playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .flatMap(player -> Stream.of(
                                holdsCardFor(game, player, window) ? PlayInterruptCardEvent.class : null,
                                hasAbilityFor(game, player, window) ? UseRollAbilityEvent.class : null)
                        .filter(Objects::nonNull)
                        .map(answer -> (Event) new PlayerDecisionEvent<>(player.number(), answer)))
                .toList();
        if (decisions.isEmpty()) {
            return NONE; // no pause, so a pause shows only that someone holds a card or an ability
        }
        ops.interrupts().openWindow(window);
        return all(Stream.concat(decisions.stream(), Stream.of(new CloseInterruptWindowEvent())).toArray(Event[]::new));
    }
}
