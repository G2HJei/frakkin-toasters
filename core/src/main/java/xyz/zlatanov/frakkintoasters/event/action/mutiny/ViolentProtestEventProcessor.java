package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

public class ViolentProtestEventProcessor extends MutinyCardEventProcessor<ViolentProtestEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.VIOLENT_PROTEST;
    }

    // SendToSickbayEvent leaves a President in the "Brig" where he is (rule A2.3.3.3).
    @Override
    protected Followup resolve() {
        return all(new DrawSkillCardsOfColorEvent(event.playerNumber(), POLITICS, 2),
                new SendToSickbayEvent(game.playerNumber(game.president())),
                thenDiscard());
    }
}
