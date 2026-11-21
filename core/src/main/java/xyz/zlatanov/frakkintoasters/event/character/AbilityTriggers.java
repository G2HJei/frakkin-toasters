package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;

/**
 * The abilities that answer something "immediately after" it happened, wherever it happened, e.g. "Memento" after any
 * population loss. The ops note the trigger ({@code AbilityTriggered}), and the engine asks for the offers after every
 * event, before what the event queued, as it asks {@code WinChecks}.
 */
@NoArgsConstructor(access = PRIVATE)
public final class AbilityTriggers {

    public static Followup check(GameView game) {
        if (game.outcome().isPresent()) {
            return NONE;
        }
        val offers = new ArrayList<Event>();
        game.turn().triggeredAbilities()
                .forEach((player, abilities) -> abilities.forEach(ability -> offers.add(offer(player, ability))));
        return all(offers.toArray(Event[]::new));
    }

    private static Event offer(int playerNumber, CharacterAbility ability) {
        return switch (ability) {
            case MEMENTO -> new OfferMementoEvent(playerNumber);
            case EMOTIONALLY_FRAGILE -> new EmotionallyFragileEvent(playerNumber);
            default -> throw new FrakCallTheAdmiralException("Nothing triggers " + ability);
        };
    }
}
