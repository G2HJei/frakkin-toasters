package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;

public class ImpeachmentEventProcessor extends MutinyCardEventProcessor<ImpeachmentEvent> {

    static final int DISCARDS = 5;

    // Unlike "Assume Command", the card allows the "Brig": the President keeps his title there (rule 4.4.1.2).
    public static boolean canPlay(GameView game, PlayerView player) {
        return player.skillCards().size() >= DISCARDS && canBecomePresident(game, player.character());
    }

    @Override
    public MutinyCard card() {
        return MutinyCard.IMPEACHMENT;
    }

    @Override
    protected boolean isValidPlay() {
        return canPlay(game, player)
                && event.discards() != null
                && event.discards().size() == DISCARDS
                && holdsSkillCards(event.discards());
    }

    @Override
    protected Followup resolve() {
        return all(new DiscardSkillCardsEvent(event.playerNumber(), event.discards()),
                new MakePresidentEvent(player.character()),
                thenDiscard());
    }
}
