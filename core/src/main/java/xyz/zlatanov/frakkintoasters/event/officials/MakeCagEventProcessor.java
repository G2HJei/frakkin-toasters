package xyz.zlatanov.frakkintoasters.event.officials;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.view.GameView;

import static xyz.zlatanov.frakkintoasters.event.officials.MakeAdmiralEventProcessor.canHoldMilitaryTitle;

public class MakeCagEventProcessor extends EventProcessor<MakeCagEvent> {

    @Override
    protected boolean isValid() {
        return canBecomeCag(game, event.character());
    }

    @Override
    public Followup process() {
        ops.titles().assignCag(event.character());
        return Followup.NONE;
    }

    // The CAG loses the title in the "Brig" too (rule 4.4.3.2).
    public static boolean canBecomeCag(GameView game, Character character) {
        return character != game.cag() && canHoldMilitaryTitle(game, character);
    }
}
