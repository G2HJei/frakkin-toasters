package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.state.character.CharacterType;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FOOD;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.FEED_THE_PEOPLE;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class FeedThePeopleEventProcessorTest extends EventTestHarness<FeedThePeopleEvent> {

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
        player(1).mutinyCards().addOnTop(FEED_THE_PEOPLE);
    }

    @Test
    void foodForTheJumpTrackAndTheCardLeavesTheGame() {
        execute(new FeedThePeopleEvent(1));

        assertFollowup(all(gain(FOOD, 1), new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent(),
                new RemoveMutinyCardFromGameEvent(FEED_THE_PEOPLE)));
    }

    // An Infiltrating Cylon Leader plays Mutiny Cards like a human (rule 5.2.3.5).
    @Test
    void byAnInfiltratingCylonLeader() {
        player(3).mutinyCards().addOnTop(FEED_THE_PEOPLE);
        player(3).removeCharacter();
        player(3).character(CAVIL);
        player(3).infiltrateGalactica();

        execute(new FeedThePeopleEvent(3));

        assertFollowup(all(gain(FOOD, 1), new DecreaseJumpPreparationEvent(), new DecreaseJumpPreparationEvent(),
                new RemoveMutinyCardFromGameEvent(FEED_THE_PEOPLE)));
    }

    @Test
    void notByACylonLeaderWhoIsNotInfiltrating() {
        player(3).mutinyCards().addOnTop(FEED_THE_PEOPLE);
        player(3).removeCharacter();
        player(3).character(CAVIL);

        assertInvalid(new FeedThePeopleEvent(3));
    }
}
