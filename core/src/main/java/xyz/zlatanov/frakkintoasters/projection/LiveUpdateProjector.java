package xyz.zlatanov.frakkintoasters.projection;

import lombok.val;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ChoiceMade;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.EventInjected;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.FactRecorded;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.OnlyAnswerTaken;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeClosed;
import xyz.zlatanov.frakkintoasters.engine.journal.JournalEntry.ScopeOpened;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.*;
import xyz.zlatanov.frakkintoasters.event.SecretEvent;
import xyz.zlatanov.frakkintoasters.projection.LiveUpdateProjection.*;
import xyz.zlatanov.frakkintoasters.state.PlayerPile;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckPile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.engine.journal.Journal.ROOT_SCOPE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.BASESTAR_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.GALACTICA_DAMAGE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.PEGASUS_DAMAGE;

/**
 * Builds a {@link LiveUpdateProjection}. Most facts are public and pass as they are. A fact with cards follows the rules
 * of {@link GameProjector}: a player's cards show to him, the others see how many (a draw from a Skill Card deck names
 * its color by the deck). A
 * shuffle never shows its order, and a draw only a count, because the fact that says where the cards went decides who
 * sees them. An event shows its name and its actor, and its fields when they are public ({@link Disclosure}). The scope
 * of a secret event shows only to its actor, and so does that the engine took an answer itself. Followups never show. Once the game has ended, everything shows.
 */
final class LiveUpdateProjector {

    private final List<JournalEntry> journal;
    private final Viewer             viewer;
    private final boolean            seesEverything;

    // what the rules need to know of the game so far
    private final Map<Character, Integer> playerOf = new HashMap<>();
    private       Character               president;

    private final List<LogEntry>       entries = new ArrayList<>();
    private       ScopeOpened          openScope;
    private       boolean              openScopeHidden;
    private       boolean              openScopeAutomatic;
    private       boolean              nextScopeAutomatic;
    private       List<FactProjection> facts;

    LiveUpdateProjector(List<JournalEntry> journal, Viewer viewer) {
        this.journal = journal;
        this.viewer = viewer;
        this.seesEverything = journal.stream()
                .anyMatch(entry -> entry instanceof FactRecorded(int ignored, GameFact.GameEnded ignoredFact))
                || viewer instanceof Viewer.Sandbox;
    }

    LiveUpdateProjection project(int from) {
        for (int i = 0; i < journal.size(); i++) {
            val entry = journal.get(i);
            if (i >= from) {
                project(entry);
            }
            learn(entry);
        }
        if (openScope != null && !openScopeHidden) {
            entries.add(scopeLog()); // the entries end inside a scope
        }
        return new LiveUpdateProjection(List.copyOf(entries));
    }

    private void project(JournalEntry entry) {
        switch (entry) {
            case ScopeOpened opened -> {
                openScope = opened;
                openScopeHidden = opened.event() instanceof SecretEvent && !sees(opened.actor());
                openScopeAutomatic = nextScopeAutomatic && sees(opened.actor());
                nextScopeAutomatic = false;
                facts = new ArrayList<>();
            }
            case FactRecorded(int scopeId, Fact fact) -> {
                if (openScope == null || openScope.scopeId() != scopeId) {
                    // a fact outside a scope of these entries
                    openScope = new ScopeOpened(scopeId, ROOT_SCOPE, null, ROOT_SCOPE);
                    openScopeHidden = false;
                    openScopeAutomatic = false;
                    facts = new ArrayList<>();
                }
                if (!openScopeHidden) {
                    facts.add(fact(fact));
                }
            }
            case ScopeClosed ignored -> {
                if (openScope != null && !openScopeHidden) {
                    entries.add(scopeLog());
                }
                openScope = null;
            }
            // the scope of the injected event follows, and shows it
            case EventInjected ignored -> {
            }
            // the scope of the answer follows
            case OnlyAnswerTaken ignored -> nextScopeAutomatic = true;
            case ChoiceMade(int scopeId, int chooser, var option) ->
                    entries.add(new ChoiceLog(scopeId, chooser,
                            seesEverything || viewer.is(chooser) || Disclosure.isPublic(option) ? option : null));
        }
    }

    private ScopeLog scopeLog() {
        val event = openScope.event();
        return new ScopeLog(openScope.scopeId(), openScope.parentId(), openScope.actor(),
                event == null ? null : event.getClass(),
                event != null && (seesEverything || Disclosure.isPublic(event)) ? event : null,
                List.copyOf(facts), openScopeAutomatic);
    }

    // Who holds which character, and who is President, as the facts go.
    private void learn(JournalEntry entry) {
        if (!(entry instanceof FactRecorded(int ignored, Fact fact))) {
            return;
        }
        switch (fact) {
            case PlayerFact.CharacterSelected f -> playerOf.put(f.character(), f.playerNumber());
            case PlayerFact.CharacterRemoved f -> playerOf.remove(f.character());
            case TitleFact.PresidentAssigned f -> president = f.character();
            default -> {
            }
        }
    }

    private boolean sees(int player) {
        return seesEverything || viewer.is(player);
    }

    private boolean seesPresidentsHand() {
        return seesEverything || president != null && playerOf.containsKey(president) && viewer.is(playerOf.get(president));
    }

    /* the rule of each fact: exhaustive, so a new fact doesn't compile until it has one */

    private FactProjection fact(Fact fact) {
        return switch (fact) {
            case BoardFact f -> shown(f);
            case DeckFact f -> deckFact(f);
            case GameFact f -> shown(f);
            case InterruptFact f -> interruptFact(f);
            case PlayerFact f -> playerFact(f);
            case ShipFact f -> shown(f);
            case SkillCheckFact f -> skillCheckFact(f);
            case TitleFact f -> titleFact(f);
        };
    }

    private FactProjection deckFact(DeckFact fact) {
        if (seesEverything) {
            return shown(fact);
        }
        return switch (fact) {
            case DeckFact.DeckShuffled f -> hidden(f, null, null, f.deck(), null, null, f.drawOrder());
            case DeckFact.CardsPulled f -> hidden(f, null, null, f.deck(), null, null, f.cards());
            case DeckFact.CardsDrawn f -> hidden(f, null, null, f.deck(), null, null, f.cards());
            // a card put back on a deck is known to who looked at it, a damage token is known to everyone
            case DeckFact.CardsAddedOnTop f -> isDamage(f.deck()) ? shown(f) : hidden(f, null, null, f.deck(), null, null, f.cards());
            case DeckFact.CardsAddedToBottom f -> isDamage(f.deck()) ? shown(f) : hidden(f, null, null, f.deck(), null, null, f.cards());
            case DeckFact.CardsDiscarded f -> Disclosure.FACEDOWN_DISCARDS.contains(f.deck())
                    ? hidden(f, null, null, f.deck(), null, null, f.cards())
                    : shown(f);
            // a card that leaves the game was played face up
            case DeckFact.CardsRemovedFromGame f -> shown(f);
        };
    }

    private FactProjection interruptFact(InterruptFact fact) {
        return switch (fact) {
            // face down until the window closes, when InterruptCardResolved and InterruptCardReturned show it
            case InterruptFact.InterruptCardPlayed f -> sees(f.card().playerNumber())
                    ? shown(f)
                    : hidden(f, f.card().playerNumber(), null, null, null, null, List.of(f.card().card()));
            case InterruptFact.InterruptWindowOpened f -> shown(f);
            case InterruptFact.InterruptCardReturned f -> shown(f);
            case InterruptFact.InterruptCardResolved f -> shown(f);
            case InterruptFact.InterruptWindowClosed f -> shown(f);
            case InterruptFact.RaidersWillMiss f -> shown(f);
            case InterruptFact.RaiderMissed f -> shown(f);
            // secret until the window closes, when RollAbilityResolved or RollAbilityWithdrawn shows it
            case InterruptFact.RollAbilityUsed f -> sees(f.ability().playerNumber())
                    ? shown(f)
                    : hidden(f, f.ability().playerNumber(), null, null, null, null, List.of());
            case InterruptFact.RollAbilityWithdrawn f -> shown(f);
            case InterruptFact.RollAbilityResolved f -> shown(f);
        };
    }

    private FactProjection playerFact(PlayerFact fact) {
        return switch (fact) {
            case PlayerFact.CardsGained f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), null, null, f.pile(), null, f.cards());
            case PlayerFact.CardsLost f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), null, null, f.pile(), null, f.cards());
            case PlayerFact.LoyaltyCardSeen f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), f.targetPlayerNumber(), null, PlayerPile.LOYALTY, null, List.of(f.card()));
            case PlayerFact.FinalFiveCardShown f -> shown(f);
            case PlayerFact.CardsSeen f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), null, f.deck(), null, null, f.cards());
            case PlayerFact.CharacterSelected f -> shown(f);
            case PlayerFact.CharacterMoved f -> shown(f);
            case PlayerFact.CharacterRemovedFromBoards f -> shown(f);
            case PlayerFact.CharacterRemoved f -> shown(f);
            case PlayerFact.MiracleTokenGained f -> shown(f);
            case PlayerFact.MiracleTokenLost f -> shown(f);
            case PlayerFact.AbilityUsed f -> shown(f);
            case PlayerFact.AbilityTriggered f -> shown(f);
            case PlayerFact.AbilityTriggerCleared f -> shown(f);
            case PlayerFact.LoyaltyCardRevealed f -> shown(f);
            case PlayerFact.MotiveCardRevealed f -> shown(f);
            case PlayerFact.InfiltrationStarted f -> shown(f);
            case PlayerFact.InfiltrationEnded f -> shown(f);
            // that he looked at the ship is public, its cargo isn't in the fact
            case PlayerFact.CivilianShipRevealed f -> shown(f);
            case PlayerFact.CivilianShipForgotten f -> shown(f);
            case PlayerFact.OwnTurnStarted f -> shown(f);
        };
    }

    private FactProjection skillCheckFact(SkillCheckFact fact) {
        if (seesEverything) {
            return shown(fact);
        }
        return switch (fact) {
            case SkillCheckFact.CardsPlayedBefore f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), null, null, null, null, f.cards());
            // the face down pile: its order would tell who played which card
            case SkillCheckFact.CardsPlaced f -> f.pile() == SkillCheckPile.CARDS
                    ? hidden(f, null, null, null, null, f.pile(), f.cards())
                    : shown(f);
            case SkillCheckFact.CardsDrawn f -> f.pile() == SkillCheckPile.CARDS
                    ? hidden(f, null, null, null, null, f.pile(), f.cards())
                    : shown(f);
            case SkillCheckFact.SkillCheckStarted f -> shown(f);
            case SkillCheckFact.TargetChosen f -> shown(f);
            case SkillCheckFact.PlayedBy f -> shown(f);
            case SkillCheckFact.PlayedBeforeRevealed f -> shown(f);
            case SkillCheckFact.CardsShown f -> shown(f);
            case SkillCheckFact.CardsLookedAt f -> sees(f.playerNumber())
                    ? shown(f)
                    : hidden(f, f.playerNumber(), null, null, null, null, f.cards());
            case SkillCheckFact.DifficultyAdjusted f -> shown(f);
            case SkillCheckFact.PoliticsCountPositive f -> shown(f);
            case SkillCheckFact.StrengthZeroed f -> shown(f);
            case SkillCheckFact.CardsDivided f -> shown(f);
            case SkillCheckFact.CardRemoved f -> shown(f);
            case SkillCheckFact.AbilityResolved f -> shown(f);
            case SkillCheckFact.EmergencyDeclared f -> shown(f);
            case SkillCheckFact.TotalStrengthDetermined f -> shown(f);
            case SkillCheckFact.ResultDetermined f -> shown(f);
            case SkillCheckFact.PassResultReplaced f -> shown(f);
            case SkillCheckFact.FailResultPrevented f -> shown(f);
            case SkillCheckFact.SkillCheckEnded f -> shown(f);
        };
    }

    private FactProjection titleFact(TitleFact fact) {
        return switch (fact) {
            case TitleFact.PresidentQuorumCardGained f -> seesPresidentsHand()
                    ? shown(f)
                    : hidden(f, president(), null, null, null, null, List.of(f.card()));
            case TitleFact.PresidentQuorumCardsLost f -> seesPresidentsHand()
                    ? shown(f)
                    : hidden(f, president(), null, null, null, null, f.cards());
            case TitleFact.PresidentAssigned f -> shown(f);
            case TitleFact.AdmiralAssigned f -> shown(f);
            case TitleFact.CagAssigned f -> shown(f);
            // the cards in play lie face up
            case TitleFact.QuorumCardPutInPlay f -> shown(f);
            case TitleFact.QuorumCardLeftPlay f -> shown(f);
        };
    }

    private Integer president() {
        return president == null ? null : playerOf.get(president);
    }

    private static boolean isDamage(DeckId deck) {
        return deck == GALACTICA_DAMAGE || deck == PEGASUS_DAMAGE || deck == BASESTAR_DAMAGE;
    }

    private static Shown shown(Fact fact) {
        return new Shown(fact);
    }

    private static Hidden hidden(Fact fact, Integer player, Integer otherPlayer, DeckId deck, PlayerPile pile,
                                 SkillCheckPile skillCheckPile, List<?> cards) {
        return new Hidden(fact.getClass(), player, otherPlayer, deck, pile, skillCheckPile, cards.size());
    }

}
