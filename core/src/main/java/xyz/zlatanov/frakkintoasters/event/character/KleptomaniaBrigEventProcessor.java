package xyz.zlatanov.frakkintoasters.event.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.KLEPTOMANIA;

public class KleptomaniaBrigEventProcessor extends EventProcessor<KleptomaniaBrigEvent> {

    // The turn may have changed things since: he is in the "Brig" already, revealed, or executed and playing another
    // character. Then nothing happens.
    @Override
    public Followup process() {
        return player.hasAbility(KLEPTOMANIA) && canBeSentToBrig(game, player.number())
                ? single(new SendToBrigEvent(player.number()))
                : NONE;
    }
}
