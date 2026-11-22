package xyz.zlatanov.frakkintoasters.event.character;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

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
        // One offer at a time: it runs next and clears its trigger, and the check after it queues the next one. Queuing
        // them all would queue the ones left again after the first.
        return game.turn().triggeredAbilities().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(ability -> offer(entry.getKey(), ability)))
                .findFirst()
                .<Followup>map(Followup::single)
                .orElse(NONE);
    }

    private static Event offer(int playerNumber, CharacterAbility ability) {
        return switch (ability) {
            case MEMENTO -> new OfferMementoEvent(playerNumber);
            case EMOTIONALLY_FRAGILE -> new EmotionallyFragileEvent(playerNumber);
            default -> throw new FrakCallTheAdmiralException("Nothing triggers " + ability);
        };
    }
}
