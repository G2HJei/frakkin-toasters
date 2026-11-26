import type { ReactNode } from 'react';
import { Link } from 'react-router';
import { useMe } from '../api/queries';
import { characterSheetArt, deckBackArt, dieArt, tokenArt, type ArtRef } from '../art/images';
import boardUrl from '../table/board/board.jpg';
import { REPOSITORY_URL } from './Site';

// board.jpg's size, and the parts of it the page shows: x, y, width and height in its pixels
const BOARD_SIZE = [2003, 2317] as const;
const BOARD_PARTS = {
  galactica: [139, 1494, 1726, 823],
  colonialOne: [46, 724, 916, 394],
  pegasus: [967, 730, 985, 481],
  cylonLocations: [185, 388, 973, 290],
  basestar: [1228, 203, 695, 353],
  jumpTrack: [151, 1286, 625, 139],
} as const;

type BoardPart = keyof typeof BOARD_PARTS;

/** A part of the board, cut out of board.jpg. */
function BoardCrop({ part, label, className }: { part: BoardPart; label?: string; className?: string }) {
  const [x, y, width, height] = BOARD_PARTS[part];
  return (
    <svg
      className={`board-crop ${className ?? ''}`}
      viewBox={`${x} ${y} ${width} ${height}`}
      preserveAspectRatio="xMidYMid slice"
      style={{ aspectRatio: `${width} / ${height}` }}
      role={label ? 'img' : undefined}
      aria-label={label}
      aria-hidden={label ? undefined : true}
    >
      <image href={boardUrl} width={BOARD_SIZE[0]} height={BOARD_SIZE[1]} />
    </svg>
  );
}

const SHOWCASE: { part: BoardPart; label: string; kicker: string; title: string; text: string }[] = [
  {
    part: 'galactica',
    label: 'Galactica on the board',
    kicker: 'Battlestar Galactica',
    title: 'Every station aboard',
    text: 'Move between Command, Weapons Control, the Hangar Deck and the rest of the ship, take their actions, and keep her flying while the crises pile up.',
  },
  {
    part: 'colonialOne',
    label: 'Colonial One on the board',
    kicker: 'Colonial One',
    title: 'Politics of survival',
    text: "The President's Quorum Cards, the Press Room, Administration and the Quorum Chamber: the fleet is governed as much as it is commanded.",
  },
  {
    part: 'pegasus',
    label: 'Pegasus on the board',
    kicker: 'Battlestar Pegasus',
    title: 'A second battlestar',
    text: 'Pegasus joins the fleet with her CIC, Main Batteries, Engine Room and Airlock, until the Cylons tear her apart.',
  },
  {
    part: 'cylonLocations',
    label: 'The Cylon locations on the board',
    kicker: 'The Cylon locations',
    title: 'Revealed, but not done',
    text: 'A revealed Cylon plays on from Caprica, the Cylon Fleet, the Human Fleet, the Resurrection Ship and the Basestar Bridge. Cylon Leaders chase their own agendas.',
  },
  {
    part: 'jumpTrack',
    label: 'The jump preparation track on the board',
    kicker: 'The jump',
    title: 'Find Kobol',
    text: 'Prepare the FTL, choose the Destination Cards and cover the distance to Kobol, with the fleet, the morale and the population intact.',
  },
];

const FEATURES: { art: ArtRef | null; title: string; text: ReactNode }[] = [
  {
    art: deckBackArt('LOYALTY'),
    title: 'Hidden loyalties',
    text: "Nobody sees another player's hand or Loyalty Cards: the server sends each viewer only what they may know.",
  },
  {
    art: dieArt(8),
    title: 'Rules by the engine',
    text: 'Only legal moves are offered. Skill checks, crises, die rolls and the abilities of every character resolve by the rules.',
  },
  {
    art: tokenArt('CURRENT_PLAYER'),
    title: 'A live table',
    text: 'The board, the log and the chat update as the others play, and the tab tells you when it is your move.',
  },
  {
    art: tokenArt('CENTURION'),
    title: 'Games that last',
    text: 'Leave and come back: a game picks up where it stopped. Players vote out an absent seat and hand it to an observer.',
  },
  {
    art: deckBackArt('CRISIS'),
    title: 'Sandbox',
    text: 'Play every seat yourself, undo, set up a scenario, and export the game to share it.',
  },
  {
    art: tokenArt('NUKE'),
    title: 'Free and open source',
    text: (
      <>
        No ads, no tracking, no fees. The code is on{' '}
        <a href={REPOSITORY_URL} target="_blank" rel="noreferrer">
          GitHub
        </a>
        .
      </>
    ),
  },
];

const CHARACTERS = ['LAURA_ROSLIN', 'KARA_STARBUCK_THRACE', 'WILLIAM_ADAMA', 'GAIUS_BALTAR', 'SHARON_BOOMER_VALERII'];

/** The landing page: a line from the series, the way in, and what the game and the site offer. */
export function HomePage() {
  const me = useMe();
  const characters = CHARACTERS.map(characterSheetArt).filter((art): art is ArtRef => art !== null);

  return (
    <main className="home">
      <section className="hero">
        <BoardCrop part="basestar" className="hero-basestar" />
        <div className="hero-content">
          <img className="hero-logo" src="/logo.svg" alt="" />
          <h1 className="hero-brand">Frakkin Toasters</h1>
          <p className="hero-tagline">The Battlestar Galactica board game, at one table in the browser.</p>
          <figure className="quote">
            <blockquote>All of this has happened before, and all of this will happen again.</blockquote>
            <figcaption>The Book of Pythia</figcaption>
          </figure>
        </div>
      </section>

      <section className="cta cf" aria-label="get started">
        {me.data ? (
          <>
            <div>
              <p className="cta-kicker">God has a plan for you, {me.data.name}</p>
              <p className="cta-text">The fleet is waiting: host a game, join one, or try a sandbox.</p>
            </div>
            <Link className="button primary cta-play" to="/play">
              Play
            </Link>
          </>
        ) : (
          <>
            <div>
              <p className="cta-kicker">Report for duty</p>
              <p className="cta-text">
                Log in with your account to play; the first login registers you. Registering confirms you own a physical copy of the game and its expansions.
              </p>
            </div>
            <div className="cta-buttons">
              <Link className="button primary" to="/login">
                Register
              </Link>
              <Link className="button" to="/login">
                Log in
              </Link>
            </div>
          </>
        )}
      </section>

      <section className="showcase" aria-label="the game">
        {SHOWCASE.map((item) => (
          <article key={item.part} className={`showcase-row ${item.part}`}>
            <div className="showcase-art cf">
              <BoardCrop part={item.part} label={item.label} />
            </div>
            <div className="showcase-text">
              <p className="kicker">{item.kicker}</p>
              <h2>{item.title}</h2>
              <p>{item.text}</p>
            </div>
          </article>
        ))}
      </section>

      <section className="features" aria-label="the site">
        <h2 className="section-title">
          <span>The whole game, every rule</span>
        </h2>
        <p className="section-lead muted">
          Every Crisis, Super Crisis, Destination, Skill, Quorum, Mutiny, Loyalty and Motive Card, the 99 character abilities, the Cylon
          Leaders and Pegasus, played to the Kobol objective.
        </p>
        <ul className="feature-grid">
          {FEATURES.map((feature) => (
            <li key={feature.title} className="feature cf">
              {feature.art && <img className="feature-art" src={feature.art.url} alt="" loading="lazy" />}
              <h3>{feature.title}</h3>
              <p>{feature.text}</p>
            </li>
          ))}
        </ul>
      </section>

      <section className="crew" aria-label="the characters">
        <h2 className="section-title">
          <span>Choose your character</span>
        </h2>
        <div className="crew-fan">
          {characters.map((art) => (
            <img key={art.key} src={art.url} alt={art.label} title={art.label} loading="lazy" />
          ))}
        </div>
      </section>
    </main>
  );
}
