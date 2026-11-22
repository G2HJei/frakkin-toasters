package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.SecretEvent;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.skill.PlayedCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;

/**
 * Who may see what, shared by the projections.
 */
public final class Disclosure {

    /**
     * The decks whose discard pile lies face down, or that no card is ever discarded to.
     */
    public static final Set<DeckId> FACEDOWN_DISCARDS = EnumSet.of(CIVILIAN_SHIPS, GALACTICA_DAMAGE, PEGASUS_DAMAGE,
            BASESTAR_DAMAGE, DESTINATION, DESTINY, LOYALTY, LOYALTY_NOT_CYLON, MOTIVE);

    /**
     * The types that are a card, or hold one.
     */
    public static final Set<Class<?>> CARD_TYPES = Set.of(SkillCard.class, PlayedCard.class, PlayedInterruptCard.class,
            LoyaltyCard.class, MotiveCard.class, MutinyCard.class, QuorumCard.class, CrisisCard.class,
            SuperCrisisCard.class, DestinationCard.class, GalacticaDamage.class, PegasusDamage.class,
            BasestarDamage.class, CivilianShip.class, Event.class, Followup.class);

    /**
     * Every event component that holds a card, and whether everyone may see it. A card that ends up face up (played,
     * discarded, in play) is public. A card only its holder sees is private: it is in his hand, or he looked at it and
     * put it back. An event shows its fields only when all its card components are public, otherwise only its name and
     * its actor. A component missing here counts as private, and a test fails until it is added.
     */
    public static final Map<String, Boolean> CARD_COMPONENTS = Map.ofEntries(
            entry("AssumeCommandEvent.discards", true), // the Skill Card discard piles are face up
            entry("BuildNukeEvent.card", true), // a card played for its ability is played face up
            entry("CapricaPlaySuperCrisisCardEvent.superCrisisCard", true),
            entry("ChangeOfPlansEvent.changeOfPlansCard", true),
            entry("ChooseADifferentPathEvent.card", true),
            entry("ChooseCrisisSectionEvent.card", true),
            entry("ChooseCrisisTargetEvent.card", true),
            entry("ChooseDestinationEvent.chosen", true),
            entry("ChooseDestinationEvent.others", false), // they go to the bottom of the deck, only the chooser saw them
            entry("ChooseGalacticaDamageEvent.resolved", true),
            entry("ChooseGalacticaDamageEvent.returned", false),
            entry("ChooseGalacticaDamageTokensEvent.resolved", true),
            entry("ChooseGalacticaDamageTokensEvent.returned", false), // back in the pile, only the chooser saw them
            entry("ChooseSuperCrisisSectionEvent.card", true),
            entry("CombatVeteranEvent.card", true),
            entry("ConsolidatePowerEvent.card", true),
            entry("CriticalSituationEvent.card", true),
            entry("DeclareEmergencyEvent.declareEmergencyCard", true),
            entry("DetermineSkillCheckAbilitiesOrderEvent.cardsWithAbilities", true),
            entry("Discard1MutinyCardEvent.cardToDiscard", true),
            entry("DiscardDownTo1MutinyCardEvent.cardToKeep", false),
            entry("DiscardFromDestinyDeckEvent.cards", false),
            entry("DiscardHeldQuorumCardEvent.card", true), // the Quorum discard pile is face up
            entry("DiscardPlayedMutinyCardEvent.card", true), // the Mutiny discard pile is face up (rule A2.3.3)
            entry("DiscardPlayedQuorumCardEvent.card", true), // a played Quorum Card is face up
            entry("DiscardQuorumCardsEvent.cards", true),
            entry("DiscardSkillCardsEvent.cards", true),
            entry("DiscardSuperCrisisCardEvent.card", true), // the Super Crisis discard pile is face up
            entry("DogfightEvent.removedCard", true),
            entry("EngineRoomEvent.discardCard1", true),
            entry("EngineRoomEvent.discardCard2", true),
            entry("ExecutiveOrderEvent.card", true),
            entry("ForceTheirHandEvent.skillCard", true),
            entry("FullThrottleEvent.card", true),
            entry("GasCloudPlaceEvent.cards", false),
            entry("GlimpseTheFaceOfGodPlaceEvent.cards", false), // they go on the Destiny deck, only he knows them
            entry("HereticDiscardEvent.card", true), // the Super Crisis discard pile is face up
            entry("ImpeachmentEvent.discards", true),
            entry("LaunchReservesEvent.card", true),
            entry("LaunchScoutEvent.card", true),
            entry("MaximumFirepowerEvent.card", true),
            entry("NegotiationEvent.card", true),
            entry("MoralCompassEvent.card", true),
            entry("MoveEvent.discardCard", true),
            entry("PlaceCrisisCardOnBottomEvent.card", false),
            entry("PlaceCrisisCardOnTopEvent.card", false),
            entry("PlaceDestinationCardAtBottomEvent.card", false),
            entry("PlaceDestinationCardOnTopEvent.card", false),
            entry("PlayCardsBeforeSkillCheckEvent.skillCards", false), // face down until they are revealed
            entry("PlayCardsIntoSkillCheckEvent.skillCards", false), // nobody may know who played which card
            entry("PlayInterruptCardEvent.card", false),
            entry("UseRollAbilityEvent.card", false),
            entry("PoliticallyAdroitEvent.card", false), // only the two players know it
            entry("PoliticalProwessEvent.politicalProwessCard", true),
            entry("PopularInfluenceEvent.card", true),
            entry("PopularInfluenceChoiceEvent.given", false), // only the player and the President learn it
            entry("PopularInfluenceChoiceEvent.other", false), // shown once it is played or discarded
            entry("PreventivePolicyEvent.card", true),
            entry("QuickThinkingEvent.cardToRemove", true),
            entry("RaptorSpecialistEvent.card", true),
            entry("RepairEvent.card", true),
            entry("ReligiousVisionsEvent.crisisCardToPlay", true),
            entry("ReligiousVisionsEvent.crisisCardToPutAtBottom", false), // only Laura Roslin saw it
            entry("RemoveMutinyCardFromGameEvent.card", true),
            entry("ResignationEvent.discards", true), // the Quorum discard pile is face up
            entry("ResolveCapricaCrisisCardEvent.crisisCardToPlay", true),
            entry("ResolveFinalFiveCardEvent.card", true),
            entry("ResolveFinalFiveCardsEvent.cards", true),
            entry("RevealCylonEvent.card", true), // revealed
            entry("RevealCylonOnExecutionEvent.card", true),
            entry("RevealMotiveCardEvent.card", true), // revealed
            entry("RevealPersonalGoalEvent.card", true), // revealed
            entry("RevealPersonalGoalEvent.discards", true),
            entry("ResolveCapricaCrisisCardEvent.crisisCardToPutAtBottom", false),
            entry("ResolveCrisisCardEvent.card", true),
            entry("ResolveSuperCrisisCardEvent.card", true),
            entry("ResolveCrisisSectionEvent.card", true),
            entry("ResolveCrisisRollEvent.card", true),
            entry("ResolveCrisisSectionThenEvent.card", true),
            entry("ResolveDestinationCardEvent.card", true),
            entry("SecretDestinyEvent.card", true),
            entry("ScoutForFuelEvent.card", true),
            entry("SetTheAgendaChoiceEvent.cards", false), // only the player and the President know them
            entry("StateOfEmergencyEvent.card", true),
            entry("TestTheLimitsEvent.card", true),
            entry("TiebreakCardsBeforeSkillCheckEvent.keptCards", true), // the cards were revealed
            entry("TiebreakInterruptCardsEvent.keptCards", true),
            entry("UnorthodoxPlanEvent.card", true), // the window's cards show when it closes
            entry("AbusePowerChoiceEvent.chosen", false), // only he saw the cards he drew; the card shows when he plays it
            entry("AbusePowerChoiceEvent.others", false), // to the bottom of the deck, only he saw them
            entry("ReorderTopQuorumCardsEvent.order", false), // only she looked at the top of the deck
            entry("SkilledPoliticianChoiceEvent.chosen", false), // the card shows when she plays it
            entry("SkilledPoliticianChoiceEvent.others", false), // to the bottom of the deck, only she saw them
            entry("StarPlayerEvent.discards", true), // the Skill Card discard piles are face up
            entry("TreatmentGiveEvent.cards", false), // only the two players know them
            entry("LogicBoundEvent.card", true), // it goes in face up
            entry("FastLearnerIntoCheckEvent.cards", false), // only Dee saw them, they go in face down
            entry("QuickFixEvent.card", true), // a revealed card of the check
            entry("HumanDelusionEvent.cards", true), // they go in face up
            entry("DutifulEvent.card", true), // discarded face up
            entry("NecessaryStepsEvent.given", false), // only Zarek and the player who gets it know it
            entry("NecessaryStepsEvent.bottom", false), // only Zarek saw it
            // the setups of a sandbox, whose one viewer sees everything
            entry("GiveLoyaltyCardToPlayerEvent.card", true),
            entry("GiveMutinyCardToPlayerEvent.card", true),
            entry("GiveSuperCrisisCardToPlayerEvent.card", true),
            entry("GiveQuorumCardToPresidentEvent.card", true),
            entry("GiveSkillCardToPlayerEvent.card", true),
            entry("PutCrisisCardOnTopEvent.card", true),
            entry("PutDestinationCardOnTopEvent.card", true),
            entry("PutDestinyCardOnTopEvent.card", true),
            entry("PutSkillCardOnTopEvent.card", true));

    private Disclosure() {
    }

    /**
     * @return whether everyone may see the event's fields: it isn't secret, and every card it holds is public
     */
    public static boolean isPublic(Event event) {
        if (event instanceof SecretEvent) {
            return false;
        }
        return Arrays.stream(event.getClass().getRecordComponents())
                .filter(Disclosure::holdsCard)
                .allMatch(component -> CARD_COMPONENTS.getOrDefault(name(component), false));
    }

    /**
     * @return whether everyone may see the events of the option
     */
    public static boolean isPublic(Followup option) {
        return switch (option) {
            case Followup.None ignored -> true;
            case Followup.Single(Event event) -> isPublic(event);
            case Followup.AllOf all -> all.followups().stream().allMatch(Disclosure::isPublic);
            case Followup.OneOf oneOf -> oneOf.options().stream().allMatch(Disclosure::isPublic);
        };
    }

    public static boolean holdsCard(RecordComponent component) {
        return holdsCard(component.getGenericType());
    }

    public static String name(RecordComponent component) {
        return component.getDeclaringRecord().getSimpleName() + "." + component.getName();
    }

    private static boolean holdsCard(Type type) {
        return switch (type) {
            case Class<?> cls -> CARD_TYPES.stream().anyMatch(card -> card.isAssignableFrom(cls));
            case ParameterizedType parameterized -> Arrays.stream(parameterized.getActualTypeArguments())
                    .anyMatch(Disclosure::holdsCard);
            case WildcardType wildcard -> {
                // List<?>: the cards of a deck or a pile
                val bound = wildcard.getUpperBounds()[0];
                yield bound == Object.class || holdsCard(bound);
            }
            default -> false;
        };
    }
}
