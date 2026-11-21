package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.ActionCosts;
import xyz.zlatanov.frakkintoasters.event.character.CharacterBans;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

/**
 * Plays a Skill Card for its ability. The card has to be of the processor's {@link #type()} and in the player's hand,
 * and the player human. Subclasses check the details of the event in {@link #isValidPlay()}, and {@link #resolve()}
 * the ability once the card is discarded. A player whose ability puts a cost on the card ({@link ActionCosts}) pays it
 * first: the discard, then the same event again, which plays the card.
 */
public abstract class SkillCardEventProcessor<T extends SkillCardEvent> extends EventProcessor<T> {

    /**
     * @return the type of the cards the event plays
     */
    public abstract SkillCardType type();

    @Override
    protected final boolean isValid() {
        // The cost is paid and the play follows. process() checks again whether he can still play it.
        return game.turn().actionCostPaid(player.number()) || canPlay();
    }

    protected boolean isValidPlay() {
        return true;
    }

    @Override
    public final Followup process() {
        val playerNumber = player.number();
        if (game.turn().actionCostPaid(playerNumber)) {
            val canStillPlay = canPlay();
            ops.actionCostSettled();
            if (!canStillPlay) {
                return NONE; // e.g. a Treachery Card he discarded drew him a Mutiny Card that changed what he can do
            }
        } else if (ActionCosts.skillCardCost(player, type()) > 0) {
            ops.payActionCost(playerNumber);
            return allOf(ActionCosts.skillCardDiscard(player, event.card()), single(event));
        }
        ops.player(playerNumber).play(event.card());
        return resolve();
    }

    protected abstract Followup resolve();

    private boolean canPlay() {
        val card = event.card();
        // Revealed Cylons, and Cylon Leaders who aren't Infiltrating, can't use the abilities of Skill Cards (rules
        // 6.1.2.3, 6.2.1). Infiltrating ones can (rule 6.2.3.2). "Specialized" forbids some cards, "Reluctant" costs
        // another card.
        return card != null
                && card.type() == type()
                && player.isHuman()
                && holdsSkillCards(List.of(card))
                && CharacterBans.canUseSkillCardAction(player, type())
                && ActionCosts.canPaySkillCardCost(game, player, type())
                && isValidPlay();
    }
}
