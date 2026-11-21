package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.SkillCardEventProcessor;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.POPULAR_INFLUENCE;

public class PopularInfluenceEventProcessor extends SkillCardEventProcessor<PopularInfluenceEvent> {

    private static final int CARDS = 2;

    public static boolean canPlay(GameView game) {
        return game.president() != null && game.decks().quorumCardsLeft() > 0;
    }

    @Override
    public SkillCardType type() {
        return POPULAR_INFLUENCE;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game);
    }

    // With only 1 card left, it goes to the President and there is nothing to play.
    @Override
    protected Followup resolve() {
        val drawn = ops.titles().drawQuorumCards(Math.min(CARDS, game.decks().quorumCardsLeft()));
        ops.player(player.number()).see(DeckId.QUORUM, drawn);
        if (drawn.size() == 1) {
            ops.titles().giveQuorumCardToPresident(drawn.getFirst());
            return NONE;
        }
        // Two cards of one type make the same option twice, which the choice collapses.
        return one(new PopularInfluenceChoiceEvent(player.number(), drawn.get(0), drawn.get(1)),
                new PopularInfluenceChoiceEvent(player.number(), drawn.get(1), drawn.get(0)));
    }
}
