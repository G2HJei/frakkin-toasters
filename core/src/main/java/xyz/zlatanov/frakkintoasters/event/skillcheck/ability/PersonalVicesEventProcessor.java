package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

public class PersonalVicesEventProcessor extends EventProcessor<PersonalVicesEvent> {

    @Override
    public Followup process() {
        val current = game.turn().currentPlayerNumber();
        // Each human player draws 1, clockwise from the current player, while Treachery Cards are left (rule 8.2.2.5).
        playersFrom(game, current).stream()
                .filter(PlayerView::isHuman)
                .limit(game.decks().skillCardsLeft(TREACHERY))
                .forEach(p -> ops.player(p.number()).drawSkillCards(TREACHERY));
        // A revealed Cylon current player draws no Mutiny Card, DrawMutinyCardEventProcessor checks that.
        return single(new DrawMutinyCardEvent(current));
    }
}
