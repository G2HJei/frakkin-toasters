package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardType;

import java.util.List;

/**
 * Plays a Skill Card for its ability. The card has to be of the processor's {@link #type()} and in the player's hand,
 * and the player human. Subclasses check the details of the event in {@link #isValidPlay()}, and {@link #resolve()}
 * the ability once the card is discarded.
 */
public abstract class SkillCardEventProcessor<T extends SkillCardEvent> extends EventProcessor<T> {

    /**
     * @return the type of the cards the event plays
     */
    public abstract SkillCardType type();

    @Override
    protected final boolean isValid() {
        val card = event.card();
        // Revealed Cylons, and Cylon Leaders who aren't Infiltrating, can't use the abilities of Skill Cards (rules
        // 6.1.2.3, 6.2.1). Infiltrating ones can (rule 6.2.3.2).
        return card != null
                && card.type() == type()
                && player.isHuman()
                && holdsSkillCards(List.of(card))
                && isValidPlay();
    }

    protected boolean isValidPlay() {
        return true;
    }

    @Override
    public final Followup process() {
        ops.player(player.number()).play(event.card());
        return resolve();
    }

    protected abstract Followup resolve();
}
