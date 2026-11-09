package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.drawExactly;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterType.CYLON_LEADER;

public class ReceiveStartingSkillsEventProcessor extends EventProcessor<ReceiveStartingSkillsEvent> {

    @Override
    public Followup process() {
        return all(playersFrom(game, game.turn().currentPlayerNumber()).stream()
                .skip(1)     // The current player is the first player.
                .map(player -> new PlayerDecisionEvent<>(player.number(), DrawSkillCardsEvent.class,
                        drawExactly(startingCards(player))))
                .toArray(Event[]::new));
    }

    private static int startingCards(PlayerView player) {
        // A Cylon Leader draws 2, unless Infiltrating like Sharon "Athena" Agathon (rule 6.2.1.1).
        return player.character().type() == CYLON_LEADER && !player.isInfiltrating() ? 2 : 3;
    }
}
