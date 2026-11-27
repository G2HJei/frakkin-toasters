package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.GiveSkillCardEvent;
import xyz.zlatanov.frakkintoasters.engine.ScriptEvent;
import xyz.zlatanov.frakkintoasters.engine.TestEngines;
import xyz.zlatanov.frakkintoasters.engine.journal.FactRecorder;
import xyz.zlatanov.frakkintoasters.engine.journal.Journal;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.*;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent;
import xyz.zlatanov.frakkintoasters.event.deck.CreateDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.jump.ChooseDestinationEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsIntoSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.ShuffleAndDivideCardsEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.KeepTabsOnVisitorFailEvent;
import xyz.zlatanov.frakkintoasters.operation.GameOps;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection.*;
import xyz.zlatanov.frakkintoasters.state.character.CharacterAbility;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.GameStep;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.Randomness;
import xyz.zlatanov.frakkintoasters.state.Team;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.PlayedInterruptCard;
import xyz.zlatanov.frakkintoasters.state.interrupt.UsedRollAbility;
import xyz.zlatanov.frakkintoasters.state.ship.ShipZone;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckResult;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.FINAL_FIVE_DAMAGE_GALACTICA_TWICE;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class LiveUpdateProjectionTest {

    private static final Viewer HOLDER   = Viewer.player(1);
    private static final Viewer OTHER    = Viewer.player(2);
    private static final Viewer OBSERVER = Viewer.observer();

    private static final SkillCard CONSOLIDATE = new SkillCard(1, CONSOLIDATE_POWER);
    private static final SkillCard EXECUTIVE   = new SkillCard(2, EXECUTIVE_ORDER);

    // Who sees a fact: everyone, only player 1 (whom the sample names), only the President (player 1), or nobody.
    private enum Rule {PUBLIC, PLAYER_1, PRESIDENT, NOBODY}

    private record Sample(Fact fact, Rule rule) {
    }

    // One sample per fact, and two where the deck or the pile decides.
    private static final List<Sample> SAMPLES = List.of(
            new Sample(new BoardFact.ResourceDecreased(Resource.FUEL, 1), Rule.PUBLIC),
            new Sample(new BoardFact.ResourceIncreased(Resource.FOOD, 1), Rule.PUBLIC),
            new Sample(new BoardFact.JumpPreparationIncreased(), Rule.PUBLIC),
            new Sample(new BoardFact.JumpPreparationDecreased(), Rule.PUBLIC),
            new Sample(new BoardFact.JumpPreparationReset(), Rule.PUBLIC),
            new Sample(new BoardFact.LocationDamaged(ARMORY), Rule.PUBLIC),
            new Sample(new BoardFact.LocationRepaired(ARMORY), Rule.PUBLIC),
            new Sample(new BoardFact.BoardingPartyAdvanced(), Rule.PUBLIC),
            new Sample(new BoardFact.PursuitAdvanced(), Rule.PUBLIC),
            new Sample(new BoardFact.PursuitReset(), Rule.PUBLIC),
            new Sample(new BoardFact.EngineRoomActivated(), Rule.PUBLIC),
            new Sample(new BoardFact.PegasusDestroyed(), Rule.PUBLIC),
            new Sample(new DeckFact.DeckShuffled(DeckId.CRISIS, List.of(CrisisCard.DETENTE)), Rule.NOBODY),
            new Sample(new DeckFact.CardsDrawn(DeckId.POLITICS, List.of(CONSOLIDATE)), Rule.NOBODY),
            new Sample(new DeckFact.CardsAddedOnTop(DeckId.CRISIS, List.of(CrisisCard.DETENTE)), Rule.NOBODY),
            new Sample(new DeckFact.CardsAddedOnTop(DeckId.GALACTICA_DAMAGE, List.of(GalacticaDamage.values()[0])), Rule.PUBLIC),
            new Sample(new DeckFact.CardsAddedToBottom(DeckId.DESTINATION, List.of(DestinationCard.DEEP_SPACE)), Rule.NOBODY),
            new Sample(new DeckFact.CardsDiscarded(DeckId.POLITICS, List.of(CONSOLIDATE)), Rule.PUBLIC),
            new Sample(new DeckFact.CardsDiscarded(DeckId.LOYALTY, List.of(NOT_CYLON)), Rule.NOBODY),
            new Sample(new DeckFact.CardsRemovedFromGame(DeckId.QUORUM, List.of(QuorumCard.FOOD_RATIONING)), Rule.PUBLIC),
            new Sample(new DeckFact.CardsPulled(DeckId.CRISIS, List.of(CrisisCard.DETENTE)), Rule.NOBODY),
            new Sample(new GameFact.GameStarted(3, ObjectiveCard.KOBOL), Rule.PUBLIC),
            new Sample(new GameFact.DieRollStarted(), Rule.PUBLIC),
            new Sample(new GameFact.DieRolled(4), Rule.PUBLIC),
            new Sample(new GameFact.DieRollCardPlayed(STRATEGIC_PLANNING, 2), Rule.PUBLIC),
            new Sample(new GameFact.DieRollAbilityUsed(CharacterAbility.ECO_OFFICER), Rule.PUBLIC),
            new Sample(new GameFact.DieRollReplaced(6, true), Rule.PUBLIC),
            new Sample(new GameFact.StepChanged(GameStep.ACTION), Rule.PUBLIC),
            new Sample(new GameFact.TurnStarted(2), Rule.PUBLIC),
            new Sample(new GameFact.TurnEndedEarly(), Rule.PUBLIC),
            new Sample(new GameFact.CurrentPlayerChanged(2), Rule.PUBLIC),
            new Sample(new GameFact.ActingPlayerChanged(3), Rule.PUBLIC),
            new Sample(new GameFact.CrisisCardPlayed(CrisisCard.DETENTE), Rule.PUBLIC),
            new Sample(new GameFact.CrisisCardKeptInPlay(SkillCheckType.DETECTOR_SABOTAGE), Rule.PUBLIC),
            new Sample(new GameFact.ReturnToCrisisStepChanged(true), Rule.PUBLIC),
            new Sample(new GameFact.NukeDiscarded(), Rule.PUBLIC),
            new Sample(new GameFact.NukeGained(), Rule.PUBLIC),
            new Sample(new GameFact.MoraleLostToExecution(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardsShown(1, List.of(new SkillCard(3, EXECUTIVE_ORDER))), Rule.PUBLIC),
            new Sample(new GameFact.CylonShipsHeld(), Rule.PUBLIC),
            new Sample(new GameFact.TurnLimitUsed(1, xyz.zlatanov.frakkintoasters.state.TurnLimit.CAG_PILOT_ACTION), Rule.PUBLIC),
            new Sample(new GameFact.RemoteActionAllowed(1, xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB), Rule.PUBLIC),
            new Sample(new GameFact.RemoteActionEnded(), Rule.PUBLIC),
            new Sample(new GameFact.StartLocationNoted(xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK), Rule.PUBLIC),
            new Sample(new GameFact.SentToBrigAtEndOfTurn(1), Rule.PUBLIC),
            new Sample(new GameFact.ActionCostPaid(1), Rule.PUBLIC),
            new Sample(new GameFact.TitleBorrowed(xyz.zlatanov.frakkintoasters.state.Title.ADMIRAL, ELLEN_TIGH), Rule.PUBLIC),
            new Sample(new GameFact.LossReductionAdded(Resource.FOOD), Rule.PUBLIC),
            new Sample(new GameFact.LossReduced(Resource.FOOD), Rule.PUBLIC),
            new Sample(new GameFact.SkillCardPlayed(1, new SkillCard(6, BUILD_NUKE)), Rule.PUBLIC),
            new Sample(new GameFact.CrisisCardsCleared(List.of(CrisisCard.DETENTE)), Rule.PUBLIC),
            new Sample(new GameFact.DestinationReached(DestinationCard.DEEP_SPACE), Rule.PUBLIC),
            new Sample(new InterruptFact.InterruptWindowOpened(new BeforeDieRoll()), Rule.PUBLIC),
            new Sample(new InterruptFact.InterruptCardPlayed(new PlayedInterruptCard(1, new SkillCard(3, STRATEGIC_PLANNING), null)), Rule.PLAYER_1),
            new Sample(new InterruptFact.InterruptCardReturned(new PlayedInterruptCard(1, new SkillCard(3, STRATEGIC_PLANNING), null)), Rule.PUBLIC),
            new Sample(new InterruptFact.InterruptCardResolved(new PlayedInterruptCard(1, new SkillCard(3, STRATEGIC_PLANNING), null)), Rule.PUBLIC),
            new Sample(new InterruptFact.InterruptWindowClosed(), Rule.PUBLIC),
            new Sample(new InterruptFact.RaidersWillMiss(GALACTICA_SPACE_3_OCLOCK, 2), Rule.PUBLIC),
            new Sample(new InterruptFact.RaiderMissed(GALACTICA_SPACE_3_OCLOCK), Rule.PUBLIC),
            new Sample(new InterruptFact.RollAbilityUsed(new UsedRollAbility(1, CharacterAbility.LONGSHOT, null, 6)), Rule.PLAYER_1),
            new Sample(new InterruptFact.RollAbilityWithdrawn(new UsedRollAbility(1, CharacterAbility.ECO_OFFICER, null, null)), Rule.PUBLIC),
            new Sample(new InterruptFact.RollAbilityResolved(new UsedRollAbility(1, CharacterAbility.ECO_OFFICER, null, null)), Rule.PUBLIC),
            new Sample(new PlayerFact.CharacterSelected(2, WILLIAM_ADAMA), Rule.PUBLIC),
            new Sample(new PlayerFact.CharacterMoved(LAURA_ROSLIN, COMMAND), Rule.PUBLIC),
            new Sample(new PlayerFact.CharacterRemovedFromBoards(LAURA_ROSLIN), Rule.PUBLIC),
            new Sample(new PlayerFact.CharacterRemoved(1, LAURA_ROSLIN), Rule.PUBLIC),
            new Sample(new PlayerFact.CardsGained(1, PlayerPile.SKILL, List.of(CONSOLIDATE)), Rule.PLAYER_1),
            new Sample(new PlayerFact.CardsLost(1, PlayerPile.LOYALTY, List.of(NOT_CYLON)), Rule.PLAYER_1),
            new Sample(new PlayerFact.MiracleTokenGained(1), Rule.PUBLIC),
            new Sample(new PlayerFact.MiracleTokenLost(1), Rule.PUBLIC),
            new Sample(new PlayerFact.AbilityUsed(1, CharacterAbility.CYLON_DETECTOR), Rule.PUBLIC),
            new Sample(new PlayerFact.AbilityTriggered(1, CharacterAbility.MEMENTO), Rule.PUBLIC),
            new Sample(new PlayerFact.AbilityTriggerCleared(1, CharacterAbility.MEMENTO), Rule.PUBLIC),
            new Sample(new PlayerFact.LoyaltyCardRevealed(1, NOT_CYLON), Rule.PUBLIC),
            new Sample(new PlayerFact.MotiveCardRevealed(1, MotiveCard.values()[0]), Rule.PUBLIC),
            new Sample(new PlayerFact.InfiltrationStarted(1), Rule.PUBLIC),
            new Sample(new PlayerFact.InfiltrationEnded(1), Rule.PUBLIC),
            new Sample(new PlayerFact.CivilianShipRevealed(1, 2001), Rule.PUBLIC),
            new Sample(new PlayerFact.CivilianShipForgotten(1, 2001), Rule.PUBLIC),
            new Sample(new PlayerFact.OwnTurnStarted(1), Rule.PUBLIC),
            new Sample(new PlayerFact.LoyaltyCardSeen(1, 2, NOT_CYLON), Rule.PLAYER_1),
            new Sample(new PlayerFact.FinalFiveCardShown(2, FINAL_FIVE_DAMAGE_GALACTICA_TWICE), Rule.PUBLIC),
            new Sample(new PlayerFact.CardsSeen(1, DeckId.DESTINATION, List.of(DestinationCard.DEEP_SPACE)), Rule.PLAYER_1),
            new Sample(new ShipFact.ShipMoved(1, ShipZone.RESERVES, ShipZone.GALACTICA_SPACE, GALACTICA_SPACE_5_OCLOCK), Rule.PUBLIC),
            new Sample(new ShipFact.PilotChanged(1, LAURA_ROSLIN), Rule.PUBLIC),
            new Sample(new ShipFact.BasestarDamageAttached(1000, BasestarDamage.CRITICAL_HIT), Rule.PUBLIC),
            new Sample(new ShipFact.BasestarDamageCleared(1000), Rule.PUBLIC),
            new Sample(new SkillCheckFact.SkillCheckStarted(SkillCheckType.ADMIRALS_QUARTERS), Rule.PUBLIC),
            new Sample(new SkillCheckFact.TargetChosen(2), Rule.PUBLIC),
            new Sample(new SkillCheckFact.PlayedBy(2), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardsPlayedBefore(1, List.of(CONSOLIDATE)), Rule.PLAYER_1),
            new Sample(new SkillCheckFact.CardsLookedAt(1, 2, List.of(CONSOLIDATE)), Rule.PLAYER_1),
            new Sample(new SkillCheckFact.DifficultyAdjusted(3), Rule.PUBLIC),
            new Sample(new SkillCheckFact.PoliticsCountPositive(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.StrengthZeroed(xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardsDivided(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.PlayedBeforeRevealed(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardsPlaced(SkillCheckPile.CARDS, List.of(CONSOLIDATE), true), Rule.NOBODY),
            new Sample(new SkillCheckFact.CardsPlaced(SkillCheckPile.MATCHING, List.of(CONSOLIDATE), false), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardsDrawn(SkillCheckPile.CARDS, List.of(CONSOLIDATE)), Rule.NOBODY),
            new Sample(new SkillCheckFact.CardsDrawn(SkillCheckPile.NON_MATCHING, List.of(CONSOLIDATE)), Rule.PUBLIC),
            new Sample(new SkillCheckFact.CardRemoved(CONSOLIDATE, 1), Rule.PUBLIC),
            new Sample(new SkillCheckFact.AbilityResolved(IRON_WILL), Rule.PUBLIC),
            new Sample(new SkillCheckFact.EmergencyDeclared(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.TotalStrengthDetermined(5), Rule.PUBLIC),
            new Sample(new SkillCheckFact.ResultDetermined(SkillCheckResult.PASS), Rule.PUBLIC),
            new Sample(new SkillCheckFact.PassResultReplaced(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.FailResultPrevented(), Rule.PUBLIC),
            new Sample(new SkillCheckFact.SkillCheckEnded(), Rule.PUBLIC),
            new Sample(new TitleFact.PresidentAssigned(WILLIAM_ADAMA), Rule.PUBLIC),
            new Sample(new TitleFact.AdmiralAssigned(WILLIAM_ADAMA), Rule.PUBLIC),
            new Sample(new TitleFact.CagAssigned(WILLIAM_ADAMA), Rule.PUBLIC),
            new Sample(new TitleFact.QuorumCardPutInPlay(QuorumCard.PROBATION, 2), Rule.PUBLIC),
            new Sample(new TitleFact.QuorumCardLeftPlay(QuorumCard.PROBATION), Rule.PUBLIC),
            new Sample(new TitleFact.PresidentQuorumCardGained(QuorumCard.values()[0]), Rule.PRESIDENT),
            new Sample(new TitleFact.PresidentQuorumCardsLost(List.of(QuorumCard.values()[0])), Rule.PRESIDENT));

    @Test
    void everyFactHasASample() {
        val sampled = SAMPLES.stream().map(sample -> sample.fact().getClass()).collect(Collectors.toSet());
        val missing = Arrays.stream(Fact.class.getPermittedSubclasses())
                .flatMap(area -> Arrays.stream(area.getPermittedSubclasses()))
                .filter(type -> !sampled.contains(type) && type != GameFact.GameEnded.class) // the end shows everything
                .map(Class::getSimpleName)
                .toList();
        assertEquals(List.of(), missing);
    }

    @TestFactory
    Stream<DynamicTest> eachFactShowsToWhoMayKnowIt() {
        return SAMPLES.stream().map(sample -> DynamicTest.dynamicTest(sample.fact().toString(), () -> {
            // Laura Roslin, player 1, is the President
            val journal = List.<JournalEntry>of(
                    new ScopeOpened(1, 0, new NoOpEvent(1), 1),
                    new FactRecorded(1, new PlayerFact.CharacterSelected(1, LAURA_ROSLIN)),
                    new FactRecorded(1, new TitleFact.PresidentAssigned(LAURA_ROSLIN)),
                    new FactRecorded(1, sample.fact()),
                    new ScopeClosed(1, Followup.NONE));
            val seenBy = switch (sample.rule()) {
                case PUBLIC -> Set.of(HOLDER, OTHER, OBSERVER);
                case PLAYER_1, PRESIDENT -> Set.of(HOLDER);
                case NOBODY -> Set.<Viewer>of();
            };
            for (val viewer : List.of(HOLDER, OTHER, OBSERVER)) {
                val projected = lastFact(LiveUpdateProjection.of(journal, viewer));
                if (seenBy.contains(viewer)) {
                    assertEquals(sample.fact(), assertInstanceOf(Shown.class, projected, viewer.toString()).fact(), viewer.toString());
                } else {
                    val hidden = assertInstanceOf(Hidden.class, projected, viewer.toString());
                    assertEquals(sample.fact().getClass(), hidden.fact());
                }
            }
        }));
    }

    // A face down Skill Card doesn't show its color, in a hand or in a skill check.
    @Test
    void hiddenSkillCardsShowOnlyTheirCount() {
        val journal = List.<JournalEntry>of(
                new ScopeOpened(1, 0, new NoOpEvent(1), 1),
                new FactRecorded(1, new PlayerFact.CardsGained(1, PlayerPile.SKILL, List.of(CONSOLIDATE, EXECUTIVE, EXECUTIVE))),
                new FactRecorded(1, new SkillCheckFact.CardsPlaced(SkillCheckPile.CARDS, List.of(CONSOLIDATE, EXECUTIVE), true)),
                new ScopeClosed(1, Followup.NONE));

        val facts = facts(LiveUpdateProjection.of(journal, OBSERVER));
        assertEquals(new Hidden(PlayerFact.CardsGained.class, 1, null, null, PlayerPile.SKILL, null, 3),
                facts.get(facts.size() - 2));
        assertEquals(new Hidden(SkillCheckFact.CardsPlaced.class, null, null, null, null, SkillCheckPile.CARDS, 2),
                facts.getLast());
    }

    @Test
    void onceTheGameHasEndedEverythingShows() {
        val drawn = new DeckFact.CardsDrawn(DeckId.POLITICS, List.of(CONSOLIDATE));
        val journal = List.<JournalEntry>of(
                new ScopeOpened(1, 0, new NoOpEvent(1), 1),
                new FactRecorded(1, drawn),
                new FactRecorded(1, new GameFact.GameEnded(Team.CYLONS, List.of())),
                new ScopeClosed(1, Followup.NONE));

        assertEquals(new Shown(drawn), LiveUpdateProjection.of(journal, OBSERVER).entries().stream()
                .map(ScopeLog.class::cast).findFirst().orElseThrow().facts().getFirst());
    }

    @Test
    void anEventShowsItsFieldsOnlyWhenTheyArePublic() {
        val section = new ChooseCrisisSectionEvent(2, CrisisCard.DETENTE, ChooseCrisisSectionEvent.Section.TOP);
        val destination = new ChooseDestinationEvent(2, DestinationCard.DEEP_SPACE, List.of(DestinationCard.values()[0]));
        val journal = List.<JournalEntry>of(
                new ScopeOpened(1, 0, section, 2),
                new ScopeClosed(1, Followup.NONE),
                new ScopeOpened(2, 0, destination, 2),
                new ScopeClosed(2, Followup.NONE),
                new JournalEntry.ChoiceMade(2, 2, single(destination)),
                new JournalEntry.ChoiceMade(2, 2, single(section)));

        val observed = LiveUpdateProjection.of(journal, OBSERVER).entries();

        assertEquals(new ScopeLog(1, 0, 2, ChooseCrisisSectionEvent.class, section, List.of(), false), observed.get(0));
        assertEquals(new ScopeLog(2, 0, 2, ChooseDestinationEvent.class, null, List.of(), false), observed.get(1));
        assertEquals(new ChoiceLog(2, 2, null), observed.get(2)); // a choice shows who chose
        assertEquals(new ChoiceLog(2, 2, single(section)), observed.get(3));
        assertEquals(new ChoiceLog(2, 2, single(destination)), LiveUpdateProjection.of(journal, OTHER).entries().get(2));
    }

    // That the engine answered for a player shows only to him: that it was his only answer can tell what he holds.
    @Test
    void anAnswerTheEngineTookShowsAsSuchOnlyToItsPlayer() {
        val journal = List.<JournalEntry>of(
                new ScopeOpened(1, 0, new NoOpEvent(1), 1),
                new ScopeClosed(1, single(new PlayerDecisionEvent<>(1, NoOpEvent.class))),
                new JournalEntry.OnlyAnswerTaken(1),
                new ScopeOpened(2, 1, new NoOpEvent(1), 1),
                new ScopeClosed(2, Followup.NONE));

        assertEquals(List.of(false, true), automatic(journal, HOLDER));
        assertEquals(List.of(false, true), automatic(journal, Viewer.sandbox()));
        assertEquals(List.of(false, false), automatic(journal, OTHER));
        assertEquals(List.of(false, false), automatic(journal, OBSERVER));
    }

    private static List<Boolean> automatic(List<JournalEntry> journal, Viewer viewer) {
        return LiveUpdateProjection.of(journal, viewer).entries().stream()
                .map(entry -> ((ScopeLog) entry).automatic())
                .toList();
    }

    // Every event component that holds a card is either public or private, so none can leak by default.
    @Test
    void everyCardOfAnEventIsMarkedPublicOrPrivate() {
        val cardComponents = EventCatalog.types().stream()
                .filter(Class::isRecord)
                .filter(type -> !type.getProtectionDomain().getCodeSource().getLocation().toString().contains("ixtures"))
                .flatMap(type -> Arrays.stream(type.getRecordComponents()))
                .filter(Disclosure::holdsCard)
                .map(Disclosure::name)
                .filter(name -> !name.startsWith("PlayerDecisionEvent.")) // names its action, not a card
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(cardComponents, new TreeSet<>(Disclosure.CARD_COMPONENTS.keySet()));
    }

    // "During an interrupt window the others get an empty update, and the played card when it closes."
    @Test
    void theOthersSeeNothingOfAnOpenInterruptWindowAndThePlayedCardWhenItCloses() {
        val strategicPlanning = new SkillCard(3, STRATEGIC_PLANNING);
        val engine = TestEngines.start(3, Randomness.seeded(42), new ScriptEvent(all(
                all(new SelectCharacterEvent(1, WILLIAM_ADAMA), new SelectCharacterEvent(2, LAURA_ROSLIN),
                        new SelectCharacterEvent(3, KARA_STARBUCK_THRACE), new CreateDestinyDeckEvent()),
                all(new GiveSkillCardEvent(2, strategicPlanning), new GiveSkillCardEvent(3, new SkillCard(4, STRATEGIC_PLANNING))),
                single(new KeepTabsOnVisitorFailEvent()),
                single(new CylonsWinEvent()))));

        // player 2 plays his card, and the window waits for player 3
        var before = engine.journal().size();
        engine.submit(2, PlayInterruptCardEvent.play(2, strategicPlanning));
        val entries = engine.journal().entries();
        assertTrue(LiveUpdateProjection.of(entries, before, Viewer.player(1)).isEmpty());
        assertTrue(LiveUpdateProjection.of(entries, before, Viewer.player(3)).isEmpty());
        assertTrue(LiveUpdateProjection.of(entries, before, Viewer.observer()).isEmpty());
        assertFalse(LiveUpdateProjection.of(entries, before, Viewer.player(2)).isEmpty());

        // player 3 passes, the window closes and everyone sees the card that took effect
        before = engine.journal().size();
        engine.submit(3, PlayInterruptCardEvent.pass(3));
        val resolved = facts(LiveUpdateProjection.of(engine.journal().entries(), before, Viewer.player(1)));
        assertTrue(resolved.contains(new Shown(new InterruptFact.InterruptCardResolved(
                new PlayedInterruptCard(2, strategicPlanning, null)))), resolved.toString());
    }

    // "A skill check: nobody sees who played which card."
    @Test
    void nobodyElseSeesWhoPlayedWhichCardIntoASkillCheck() {
        val journal = new Journal();
        val game = Game.builder().randomness(Randomness.seeded(1)).build();
        val ops = new GameOps(game, new FactRecorder(game, journal));
        ops.startGame();
        ops.player(1).selectCharacter(WILLIAM_ADAMA);
        ops.player(2).selectCharacter(LAURA_ROSLIN);
        ops.skillCheck().start(SkillCheckType.ADMIRALS_QUARTERS);
        ops.skillCheck().createDestinyDeck();
        ops.skillCheck().playFromDestinyDeck(2); // face down, before the players play theirs
        ops.player(1).receiveSkillCard(EXECUTIVE);
        ops.player(2).receiveSkillCard(CONSOLIDATE);
        playInto(journal, ops, 1, EXECUTIVE);
        playInto(journal, ops, 2, CONSOLIDATE);
        val divide = journal.openScope(new ShuffleAndDivideCardsEvent(), 1, Journal.ROOT_SCOPE);
        ops.skillCheck().shuffleAndDivideCards();
        journal.closeScope(Followup.NONE);

        val observed = LiveUpdateProjection.of(journal.entries(), OBSERVER).entries().stream()
                .filter(ScopeLog.class::isInstance)
                .map(ScopeLog.class::cast)
                .filter(scope -> scope.event() == PlayCardsIntoSkillCheckEvent.class)
                .toList();
        assertEquals(2, observed.size());
        for (val scope : observed) {
            assertNull(scope.details());
            assertTrue(scope.facts().stream().allMatch(Hidden.class::isInstance), scope.toString());
        }
        // the divided piles show, sorted, without who played them
        val divided = LiveUpdateProjection.of(journal.entries(), OBSERVER).entries().stream()
                .filter(entry -> entry instanceof ScopeLog scope && scope.scopeId() == divide)
                .map(ScopeLog.class::cast)
                .findFirst()
                .orElseThrow();
        assertTrue(divided.facts().stream().anyMatch(fact -> fact instanceof Shown(SkillCheckFact.CardsPlaced placed, var ignored)
                && placed.pile() != SkillCheckPile.CARDS), divided.toString());
        // the player himself sees his own cards
        val own = LiveUpdateProjection.of(journal.entries(), HOLDER).entries().stream()
                .filter(entry -> entry instanceof ScopeLog scope && scope.event() == PlayCardsIntoSkillCheckEvent.class && scope.actor() == 1)
                .map(ScopeLog.class::cast)
                .findFirst()
                .orElseThrow();
        assertTrue(own.facts().contains(new Shown(new PlayerFact.CardsLost(1, PlayerPile.SKILL, List.of(EXECUTIVE)))));
    }

    // The cards played before a skill check lie face down until the reveal, which names them to everyone by who played
    // them; the next check starts with none.
    @Test
    void theRevealNamesTheCardsPlayedBeforeTheCheck() {
        val revealed = new SkillCheckFact.PlayedBeforeRevealed();
        val journal = List.<JournalEntry>of(
                new ScopeOpened(1, 0, new NoOpEvent(1), 1),
                new FactRecorded(1, new SkillCheckFact.SkillCheckStarted(SkillCheckType.ADMIRALS_QUARTERS)),
                new FactRecorded(1, new SkillCheckFact.CardsPlayedBefore(2, List.of(EXECUTIVE))),
                new FactRecorded(1, new SkillCheckFact.CardsPlayedBefore(1, List.of(CONSOLIDATE))),
                new FactRecorded(1, revealed),
                new FactRecorded(1, new SkillCheckFact.SkillCheckEnded()),
                new FactRecorded(1, new SkillCheckFact.SkillCheckStarted(SkillCheckType.ADMIRALS_QUARTERS)),
                new FactRecorded(1, revealed),
                new ScopeClosed(1, Followup.NONE));
        for (val viewer : List.of(HOLDER, OTHER, OBSERVER, Viewer.sandbox())) {
            val shown = facts(LiveUpdateProjection.of(journal, viewer)).stream()
                    .filter(fact -> fact instanceof Shown shownFact && shownFact.fact() instanceof SkillCheckFact.PlayedBeforeRevealed)
                    .map(fact -> ((Shown) fact).revealed())
                    .toList();
            assertEquals(List.of(
                    List.of(new GameProjection.ShownCards(1, List.of(CONSOLIDATE)), new GameProjection.ShownCards(2, List.of(EXECUTIVE))),
                    List.of()), shown, viewer.toString());
        }
        // before the reveal, the observer sees only how many
        assertInstanceOf(Hidden.class, facts(LiveUpdateProjection.of(journal, OBSERVER)).get(1));
    }

    private static void playInto(Journal journal, GameOps ops, int player, SkillCard card) {
        journal.openScope(new PlayCardsIntoSkillCheckEvent(player, List.of(card)), player, Journal.ROOT_SCOPE);
        ops.skillCheck().playCards(player, card);
        journal.closeScope(Followup.NONE);
    }

    private static FactProjection lastFact(LiveUpdateProjection projection) {
        val scope = (ScopeLog) projection.entries().getLast();
        return scope.facts().getLast();
    }

    private static List<FactProjection> facts(LiveUpdateProjection projection) {
        return projection.entries().stream()
                .filter(ScopeLog.class::isInstance)
                .flatMap(entry -> ((ScopeLog) entry).facts().stream())
                .toList();
    }

}
