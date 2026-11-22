package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommandEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.CommunicationsEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.WeaponsControlEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DUTIFUL;

public class DutifulEventProcessor extends EventProcessor<DutifulEvent> {

    /**
     * @return the decision on the location's action, for the 3 locations "Dutiful" takes again
     */
    static Optional<Class<? extends LocationEvent>> locationAction(Location location) {
        return switch (location) {
            case COMMAND -> Optional.of(CommandEvent.class);
            case COMMUNICATIONS -> Optional.of(CommunicationsEvent.class);
            case WEAPONS_CONTROL -> Optional.of(WeaponsControlEvent.class);
            case null, default -> Optional.empty();
        };
    }

    // Once during his turn, with a card to discard and the location able to act again, wherever he activated it from.
    // Not inside "Organized", his only action at another location, which never takes a location twice.
    public static boolean canUse(GameView game, PlayerView player, Location location) {
        return locationAction(location).isPresent()
                && player.number() == game.turn().currentPlayerNumber()
                && CharacterAbilities.canUseOncePerTurn(game, player, DUTIFUL)
                && !player.skillCards().isEmpty()
                && game.turn().remoteActionLocation(player.number()).isEmpty()
                && LocationEvent.canAct(game, player, location);
    }

    /**
     * @return after the player activated the location, the offer of "Dutiful", queued only when he has it on his turn
     * and hasn't used it yet. Whether he can use it is known once the action is done
     */
    public static Followup after(GameView game, PlayerView player, Location location) {
        return player.hasAbility(DUTIFUL)
                && player.number() == game.turn().currentPlayerNumber()
                && !game.turn().hasUsed(player.number(), DUTIFUL)
                ? single(new OfferDutifulEvent(player.number(), location))
                : NONE;
    }

    /**
     * @return the choice of the card to discard for another activation, or none
     */
    public static Followup offer(GameView game, PlayerView player, Location location) {
        if (!canUse(game, player, location)) {
            return NONE;
        }
        val options = new ArrayList<Event>();
        player.skillCards().cards().stream().distinct()
                .forEach(card -> options.add(new DutifulEvent(player.number(), location, card)));
        options.add(new NoOpEvent(player.number()));
        return one(options.toArray(Event[]::new));
    }

    @Override
    protected boolean isValid() {
        return event.card() != null
                && canUse(game, player, event.location())
                && holdsSkillCards(List.of(event.card()));
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(DUTIFUL);
        // What the discard brings may stop him, so the action is offered once it is done (F-70a615).
        return all(new DiscardSkillCardsEvent(player.number(), List.of(event.card())),
                new DutifulAgainEvent(player.number(), event.location()));
    }
}
