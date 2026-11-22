package xyz.zlatanov.frakkintoasters.event.action.character;

import java.util.List;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.CharacterActionEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class InfluentialEventProcessor extends CharacterActionEventProcessor<InfluentialEvent> {

    @Override
    public CharacterAbility ability() {
        return CharacterAbility.INFLUENTIAL;
    }

    static final int CARDS = 5;

    // The draw pile only: she looks at the deck as it is.
    public static boolean canUse(GameView game, PlayerView player) {
        return !game.decks().deck(DeckId.QUORUM).isEmpty();
    }

    @Override
    protected Followup resolve() {
        ops.player(event.playerNumber()).see(DeckId.QUORUM, top(game));
        return single(new PlayerDecisionEvent<>(event.playerNumber(), ReorderTopQuorumCardsEvent.class));
    }

    /**
     * @return the cards she reorders, the first on top
     */
    public static List<QuorumCard> top(GameView game) {
        List<QuorumCard> cards = game.decks().<QuorumCard>deck(DeckId.QUORUM).cards();
        return List.copyOf(cards.subList(0, Math.min(CARDS, cards.size())));
    }
}
