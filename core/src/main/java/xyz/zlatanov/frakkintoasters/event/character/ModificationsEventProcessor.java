package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import java.util.Optional;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.crisis.CrisisCardFollowups;
import xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import java.util.ArrayList;
import java.util.Arrays;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.MODIFICATIONS;

public class ModificationsEventProcessor extends EventProcessor<ModificationsEvent> {

    /**
     * @return the card's activation, or the activation the player who can use the ability picks instead. The card's
     * names nobody, so the choice is his
     */
    public static Followup offer(GameView game, Event cardActivation) {
        return simon(game)
                .map(simon -> options(simon, cardActivation))
                .orElse(single(cardActivation));
    }

    /**
     * @return the offer at the start of a step without a Crisis Card, e.g. when the current player is in the "Brig":
     * nothing activates unless "Modifications" picks an activation
     */
    public static Followup offerWithoutCard(GameView game) {
        return simon(game)
                .map(simon -> options(simon, new NoOpEvent(simon.number())))
                .orElse(NONE);
    }

    private static Optional<? extends PlayerView> simon(GameView game) {
        return game.players().stream()
                .filter(player -> CharacterAbilities.canUseMiracle(game, player, MODIFICATIONS))
                .findFirst();
    }

    private static Followup options(PlayerView simon, Event withoutModifications) {
        val options = new ArrayList<Event>();
        options.add(withoutModifications);
        Arrays.stream(CylonActivation.values())
                .map(activation -> new ModificationsEvent(simon.number(), activation))
                .forEach(options::add);
        return one(options.toArray(Event[]::new));
    }

    @Override
    protected boolean isValid() {
        return CharacterAbilities.canUseMiracle(game, player, MODIFICATIONS)
                && event.activation() != null
                && game.turn().step() == ACTIVATE_CYLON_SHIPS;
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(MODIFICATIONS);
        return single(CrisisCardFollowups.activation(event.activation()));
    }
}
