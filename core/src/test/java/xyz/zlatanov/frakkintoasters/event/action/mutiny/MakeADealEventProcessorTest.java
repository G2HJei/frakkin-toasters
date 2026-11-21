package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.MAKE_A_DEAL;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class MakeADealEventProcessorTest extends EventTestHarness<MakeADealEvent> {

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
        player(1).mutinyCards().addOnTop(MAKE_A_DEAL);
    }

    @Test
    void aCharacterLeavesTheBrigThenAChosenPlayerDrawsAMutinyCard() {
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new MakeADealEvent(1, 2, COMMAND, 3));

        assertFollowup(all(new MoveFromBrigEvent(2, COMMAND), new DiscardPlayedMutinyCardEvent(MAKE_A_DEAL),
                new DrawMutinyCardEvent(3)));
    }

    @Test
    void notWithNobodyInTheBrig() {
        assertInvalid(new MakeADealEvent(1, 2, COMMAND, 3));
    }

    @Test
    void toAnyGalacticaLocationSickbayIncluded() {
        moveTo(BRIG, WILLIAM_ADAMA);

        execute(new MakeADealEvent(1, 2, SICKBAY, 3));

        assertFollowup(all(new MoveFromBrigEvent(2, SICKBAY), new DiscardPlayedMutinyCardEvent(MAKE_A_DEAL),
                new DrawMutinyCardEvent(3)));
    }

    @Test
    void notBackToTheBrig() {
        moveTo(BRIG, WILLIAM_ADAMA);

        assertInvalid(new MakeADealEvent(1, 2, BRIG, 3));
    }

    @Test
    void notOffGalactica() {
        moveTo(BRIG, WILLIAM_ADAMA);

        assertInvalid(new MakeADealEvent(1, 2, PRESS_ROOM, 3));
    }

    @Test
    void notARevealedCylonToDraw() {
        moveTo(BRIG, WILLIAM_ADAMA);
        player(3).loyaltyCards().addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE)
                .reveal(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_REDUCE_MORALE);

        assertInvalid(new MakeADealEvent(1, 2, COMMAND, 3));
    }
}
