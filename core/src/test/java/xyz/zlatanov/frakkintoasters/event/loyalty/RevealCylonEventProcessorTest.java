package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.Actions;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_3;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.*;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.WEAPONS_ARMED;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class RevealCylonEventProcessorTest extends EventTestHarness<RevealCylonEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(2).character(WILLIAM_ADAMA);
        moveTo(COMMAND, WILLIAM_ADAMA);
        player(3).character(LAURA_ROSLIN);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        player(1).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE, NOT_CYLON);
    }

    // The reveal is an action of a hidden Cylon (rule 6.1): step 1, then the card's power, then the other steps.
    @Test
    void shouldRevealTheCardAndUseItsPower() {
        assertTrue(RevealCylonEventProcessor.canReveal(game, player(1)));
        assertTrue(Actions.canTakeOtherAction(game, player(1)));
        assertFalse(RevealCylonEventProcessor.canReveal(game, player(2)));

        execute(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        assertTrue(player(1).isRevealedCylon());
        assertEquals(List.of(CYLON_REDUCE_MORALE), player(1).loyaltyCards().revealedCards());
        assertFollowup(all(
                one(new CylonPowerReduceMoraleEvent(1), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
    }

    // "He must also discard all of his Mutiny Cards and any remaining miracle tokens."
    @Test
    void shouldDiscardTheMutinyCardsAndTheMiracleToken() {
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);

        execute(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        assertTrue(player(1).mutinyCards().isEmpty());
        assertEquals(List.of(WEAPONS_ARMED), mutinyDeck.discardedCards());
        assertFalse(player(1).hasMiracleToken());
        assertFollowup(all(
                one(new CylonPowerReduceMoraleEvent(1), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
    }

    // "The player discards down to three Skill Cards", before the power.
    @Test
    void shouldDiscardDownToThreeSkillCardsFirst() {
        player(1).gainSkillCards(new SkillCard(1, EVASIVE_MANOEUVRES), new SkillCard(2, EVASIVE_MANOEUVRES),
                new SkillCard(3, LAUNCH_SCOUT), new SkillCard(4, LAUNCH_SCOUT));

        execute(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        assertFollowup(all(
                single(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_3)),
                one(new CylonPowerReduceMoraleEvent(1), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
    }

    // "If a Cylon reveals while in the "Brig", he may not use the reveal actions on his Loyalty Card."
    @Test
    void shouldNotUseThePowerInTheBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        execute(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        assertTrue(player(1).isRevealedCylon());
        assertFollowup(new FinishCylonRevealEvent(1, true));
    }

    @Test
    void shouldOfferTheCharactersOnGalacticaToSendToTheBrig() {
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG);

        execute(new RevealCylonEvent(1, CYLON_SEND_TO_BRIG));

        // Laura Roslin is on Colonial One, not on Galactica.
        assertFollowup(all(
                one(new CylonPowerSendToBrigEvent(1, 2), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
    }

    @Test
    void shouldAskNothingWhenNobodyCanBeSentToSickbay() {
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_SICKBAY);
        moveTo(ADMINISTRATION, WILLIAM_ADAMA);

        execute(new RevealCylonEvent(1, CYLON_SEND_TO_SICKBAY));

        assertFollowup(new FinishCylonRevealEvent(1, true));
    }

    // Unlike the others, the Treachery power isn't a "may".
    @Test
    void shouldAlwaysMakeThePlayersDrawTreachery() {
        player(1).loyaltyCards().addOnTop(CYLON_DRAW_TREACHERY_CARDS);

        execute(new RevealCylonEvent(1, CYLON_DRAW_TREACHERY_CARDS));

        assertFollowup(all(new CylonPowerDrawTreacheryEvent(1), new FinishCylonRevealEvent(1, true)));
    }

    @Test
    void shouldOfferTheOtherPowers() {
        player(1).loyaltyCards().addOnTop(CYLON_CAN_PLACE_CENTURION);

        execute(new RevealCylonEvent(1, CYLON_CAN_PLACE_CENTURION));

        assertFollowup(all(
                one(new CylonPowerPlaceCenturionEvent(1), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
        assertEquals(one(new CylonPowerDecreaseJumpTrackEvent(1), new NoOpEvent(1)),
                CylonRevealPowers.power(game, 1, CYLON_DECREASE_JUMP_TRACK));
        assertEquals(one(new CylonPowerDamageGalacticaEvent(1), new NoOpEvent(1)),
                CylonRevealPowers.power(game, 1, CYLON_DAMAGE_GALACTICA));
    }

    @Test
    void shouldRejectACardHeDoesntHold() {
        assertInvalid(new RevealCylonEvent(1, CYLON_SEND_TO_BRIG));
    }

    @Test
    void shouldRejectACardThatIsntACylonCard() {
        assertInvalid(new RevealCylonEvent(1, NOT_CYLON));
    }

    @Test
    void shouldRejectARevealedCylon() {
        player(1).loyaltyCards().addOnTop(CYLON_SEND_TO_BRIG).reveal(CYLON_SEND_TO_BRIG);

        assertInvalid(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));
    }

    // A Cylon Leader has no Loyalty Cards to reveal.
    @Test
    void shouldRejectACylonLeader() {
        setUpGame(4);
        player(4).character(SHARON_ATHENA_AGATHON);
        ops.player(4).infiltrateGalactica(HANGAR_DECK);
        player(4).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE);

        assertInvalid(new RevealCylonEvent(4, CYLON_REDUCE_MORALE));
    }

    // "Lies and Secrets": Cavil discards his hand when a player reveals a "You Are a Cylon" card.
    @Test
    void cavilDiscardsHisHandAtAReveal() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        player(2).character(CAVIL);
        player(2).gainSkillCards(new SkillCard(1, LAUNCH_SCOUT));
        player(1).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE);

        execute(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        assertFollowup(all(
                single(new DiscardAllSkillCardsEvent(2)),
                one(new CylonPowerReduceMoraleEvent(1), new NoOpEvent(1)),
                single(new FinishCylonRevealEvent(1, true))));
    }

    // "Spiritual Crisis": Gaius Baltar (alt.) can only reveal from the "Brig".
    @Test
    void baltarAltRevealsOnlyFromTheBrig() {
        setUpGame();
        player(1).character(GAIUS_BALTAR_ALT);
        moveTo(ADMIRALS_QUARTERS, GAIUS_BALTAR_ALT);
        player(1).loyaltyCards().addOnTop(CYLON_REDUCE_MORALE);

        assertInvalid(new RevealCylonEvent(1, CYLON_REDUCE_MORALE));

        moveTo(BRIG, GAIUS_BALTAR_ALT);
        assertTrue(RevealCylonEventProcessor.canReveal(game, player(1)));
    }
}
