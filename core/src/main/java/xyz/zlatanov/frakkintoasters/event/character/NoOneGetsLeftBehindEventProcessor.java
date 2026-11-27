package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.lose;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.POPULATION;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.NO_ONE_GETS_LEFT_BEHIND;

public class NoOneGetsLeftBehindEventProcessor extends EventProcessor<NoOneGetsLeftBehindEvent> {

    public static Followup offer(GameView game) {
        return game.players().stream()
                .filter(player -> CharacterAbilities.canUseMiracle(game, player, NO_ONE_GETS_LEFT_BEHIND))
                .findFirst()
                .map(helo -> one(new NoOneGetsLeftBehindEvent(helo.number()), new NoOpEvent(helo.number())))
                .orElse(NONE);
    }

    @Override
    protected boolean isValid() {
        return CharacterAbilities.canUseMiracle(game, player, NO_ONE_GETS_LEFT_BEHIND);
    }

    @Override
    public Followup process() {
        ops.player(player.number()).useAbility(NO_ONE_GETS_LEFT_BEHIND);
        return all(lose(FUEL, 1), gain(POPULATION, 2));
    }
}
