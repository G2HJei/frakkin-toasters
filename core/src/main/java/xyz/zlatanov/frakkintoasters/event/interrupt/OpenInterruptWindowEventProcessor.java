package xyz.zlatanov.frakkintoasters.event.interrupt;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEventProcessor.holdsCardFor;

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
        }
        val holders = playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .filter(player -> holdsCardFor(game, player, window))
                .toList();
        if (holders.isEmpty()) {
            return NONE; // no pause, so a pause shows only that someone holds a card
        }
        ops.interrupts().openWindow(window);
        return all(Stream.concat(
                        holders.stream().map(player -> new PlayerDecisionEvent<>(player.number(), PlayInterruptCardEvent.class)),
                        Stream.of(new CloseInterruptWindowEvent()))
                .toArray(Event[]::new));
    }
}
