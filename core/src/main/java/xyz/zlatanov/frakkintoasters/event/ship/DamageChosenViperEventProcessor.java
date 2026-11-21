package xyz.zlatanov.frakkintoasters.event.ship;

import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.ship.AssaultRaptor;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class DamageChosenViperEventProcessor extends EventProcessor<DamageChosenViperEvent> {

    @Override
    protected boolean isValid() {
        return DamageVipersEventProcessor.candidates(game, DamageVipersEvent.Area.SPACE_OR_RESERVES).stream()
                .anyMatch(fighter -> fighter.id() == event.humanFighterId());
    }

    @Override
    public Followup process() {
        val fighter = DamageVipersEventProcessor.candidates(game, DamageVipersEvent.Area.SPACE_OR_RESERVES).stream()
                .filter(candidate -> candidate.id() == event.humanFighterId())
                .findFirst()
                .orElseThrow();
        // Assault raptors can't be damaged (rule 11.3.2.3), and Brendan "Hot Dog" Costanza's viper is destroyed instead
        // ("Forced to Eject").
        if (fighter instanceof AssaultRaptor || CharacterPenalties.destroyedInsteadOfDamaged(game, fighter.id())) {
            ops.ship(fighter.id()).destroy();
        } else {
            ops.ship(fighter.id()).damage();
        }
        return NONE;
    }
}
