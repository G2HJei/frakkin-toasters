package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ship.ShuffleCivilianShipIntoDeckEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.SCAVENGING_FOR_PARTS;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class ScavengingForPartsEventProcessorTest extends EventTestHarness<ScavengingForPartsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(KARA_STARBUCK_THRACE);
        player(1).mutinyCards().addOnTop(SCAVENGING_FOR_PARTS);
    }

    @Test
    void damageGalacticaThenTheCivilianShipGoesBack() {
        val ship = civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        execute(new ScavengingForPartsEvent(1, ship.id()));

        assertFollowup(all(damageGalactica(), single(new ShuffleCivilianShipIntoDeckEvent(ship.id())),
                single(new DiscardPlayedMutinyCardEvent(SCAVENGING_FOR_PARTS))));
    }

    @Test
    void withoutACivilianShipInSpaceOnlyTheDamage() {
        execute(new ScavengingForPartsEvent(1, null));

        assertFollowup(all(damageGalactica(), single(new DiscardPlayedMutinyCardEvent(SCAVENGING_FOR_PARTS))));
    }

    @Test
    void aCivilianShipInSpaceHasToBeChosen() {
        civilianShipAt(GALACTICA_SPACE_3_OCLOCK);

        assertInvalid(new ScavengingForPartsEvent(1, null));
    }
}
