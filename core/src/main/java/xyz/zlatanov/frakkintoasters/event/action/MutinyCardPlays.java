package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.*;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;

/**
 * Which Mutiny Cards a player can play now. A card is offered only when it can be played, as a Skill Card is
 * ({@link SkillCardPlays}), so a decision to play one always has an answer.
 */
@NoArgsConstructor(access = PRIVATE)
public final class MutinyCardPlays {

    /**
     * @return whether the player could play the card now, if he held it. Each card's own rules are in its processor
     */
    public static boolean canPlay(GameView game, PlayerView player, MutinyCard card) {
        // Revealed Cylons never play them, Cylon Leaders only while Infiltrating (rules 5.2.3.4 and 5.2.3.5).
        if (!player.isHuman()) {
            return false;
        }
        return switch (card) {
            case ARMED_RESISTANCE, BAIT_AND_SWITCH, BETRAYAL_OF_TRUST, CONTROVERSIAL_SPEECH, FEED_THE_PEOPLE, NECESSARY_RISK,
                 PANIC, PEACEFUL_RESISTANCE, RUINED_REPUTATION, SCAVENGING_FOR_PARTS, SELFISH_ACT, SEND_A_MESSAGE,
                 THE_STRONG_SURVIVE, VIOLENT_PROTEST -> true;
            case ASSUME_COMMAND -> AssumeCommandEventProcessor.canPlay(game, player);
            case BLACKMAIL -> BlackmailEventProcessor.canPlay(game, player);
            case CLIPPED_WINGS -> ClippedWingsEventProcessor.canPlay(game);
            case IMPEACHMENT -> ImpeachmentEventProcessor.canPlay(game, player);
            case MAKE_A_DEAL -> MakeADealEventProcessor.canPlay(game);
            case SET_THE_AGENDA -> SetTheAgendaEventProcessor.canPlay(game, player);
            case UNAUTHORIZED_USAGE -> UnauthorizedUsageEventProcessor.canPlay(game);
            case WEAPONS_ARMED -> WeaponsArmedEventProcessor.canPlay(game);
        };
    }

    /**
     * @return the event that plays the card
     */
    public static Class<? extends MutinyCardEvent> eventClass(MutinyCard card) {
        return switch (card) {
            case ARMED_RESISTANCE -> ArmedResistanceEvent.class;
            case ASSUME_COMMAND -> AssumeCommandEvent.class;
            case BAIT_AND_SWITCH -> BaitAndSwitchEvent.class;
            case BETRAYAL_OF_TRUST -> BetrayalOfTrustEvent.class;
            case BLACKMAIL -> BlackmailEvent.class;
            case CLIPPED_WINGS -> ClippedWingsEvent.class;
            case CONTROVERSIAL_SPEECH -> ControversialSpeechEvent.class;
            case FEED_THE_PEOPLE -> FeedThePeopleEvent.class;
            case IMPEACHMENT -> ImpeachmentEvent.class;
            case MAKE_A_DEAL -> MakeADealEvent.class;
            case NECESSARY_RISK -> NecessaryRiskEvent.class;
            case PANIC -> PanicEvent.class;
            case PEACEFUL_RESISTANCE -> PeacefulResistanceEvent.class;
            case RUINED_REPUTATION -> RuinedReputationEvent.class;
            case SCAVENGING_FOR_PARTS -> ScavengingForPartsEvent.class;
            case SELFISH_ACT -> SelfishActEvent.class;
            case SEND_A_MESSAGE -> SendAMessageEvent.class;
            case SET_THE_AGENDA -> SetTheAgendaEvent.class;
            case THE_STRONG_SURVIVE -> TheStrongSurviveEvent.class;
            case UNAUTHORIZED_USAGE -> UnauthorizedUsageEvent.class;
            case VIOLENT_PROTEST -> ViolentProtestEvent.class;
            case WEAPONS_ARMED -> WeaponsArmedEvent.class;
        };
    }

    /**
     * @return whether the player holds a Mutiny Card he can play now
     */
    public static boolean canPlayAny(GameView game, PlayerView player) {
        return player.mutinyCards().cards().stream().distinct().anyMatch(card -> canPlay(game, player, card));
    }
}
