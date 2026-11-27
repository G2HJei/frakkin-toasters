import {useEffect} from 'react';
import {Link, useLocation} from 'react-router';
import {REPOSITORY_URL} from './Site';

const FANTASY_FLIGHT_URL = 'https://www.fantasyflightgames.com';
const UPDATED = '26 November 2026';

export function AboutPage() {
    const {hash} = useLocation();
    // the router doesn't scroll to an anchor by itself: the login page links to both
    useEffect(() => {
        if (hash) {
            document.getElementById(hash.slice(1))?.scrollIntoView();
        }
    }, [hash]);

    return (
        <main className="page about">
            <header className="page-head">
                <h1 className="label">About</h1>
                <p className="flavour">So say we all.</p>
            </header>

            <section className="panel cf prose" aria-labelledby="about-site">
                <h2 id="about-site">Frakkin Toasters</h2>
                <p>
                    A free, non-commercial fan project: the Battlestar Galactica board game in the browser, so the
                    people who own it can play it
                    together when they can't sit at one table. The code is open source, on{' '}
                    <a href={REPOSITORY_URL} target="_blank" rel="noreferrer">
                        GitHub
                    </a>
                    .
                </p>
                <p>
                    <strong>You need your own copy of the game and its expansions.</strong> The board game was published
                    by{' '}
                    <a href={FANTASY_FLIGHT_URL} target="_blank" rel="noreferrer">
                        Fantasy Flight Games
                    </a>
                    . Its names, rules, images and artwork belong to their owners, and this site is not affiliated with
                    or endorsed by Fantasy Flight
                    Games or the owners of Battlestar Galactica.
                </p>
            </section>

            <section id="terms" className="panel cf prose" aria-labelledby="terms-title">
                <h2 id="terms-title">Terms of Service</h2>
                <p className="muted small">Last updated {UPDATED}.</p>
                <ol>
                    <li>
                        <strong>Registering.</strong> Your first login registers you. By registering you accept these
                        terms and confirm that you own a
                        physical copy of the Battlestar Galactica board game and its expansions. If you don't, don't
                        register.
                    </li>
                    <li>
                        <strong>The site.</strong> A free fan project for playing the board game online. The game's
                        material is shown only so that its
                        owners can play it here.
                    </li>
                    <li>
                        <strong>Your account.</strong> You log in through Google or Discord, and you are responsible for
                        what is done with your account.
                        Choose a name that is not offensive and does not impersonate anyone.
                    </li>
                    <li>
                        <strong>Conduct.</strong> No unlawful content in names or chat. Don't disrupt games on purpose,
                        exploit bugs, or flood the site with automated requests. Hosts may remove or ban users from
                        their games, and we may remove content, games or accounts that break these terms.
                    </li>
                    <li>
                        <strong>No warranty.</strong> The site is free and provided as is, without any promise that it
                        works, keeps your games or stays online. We may change it or shut it down at any time.
                    </li>
                    <li>
                        <strong>Liability.</strong> As far as the law allows, we are not liable for any loss or damage
                        from using the site.
                    </li>
                    <li>
                        <strong>Changes.</strong> We may change these terms, and the date above shows when we last did.
                        Using the site after a change means you accept the new terms.
                    </li>
                </ol>
            </section>

            <section id="privacy" className="panel cf prose" aria-labelledby="privacy-title">
                <h2 id="privacy-title">Privacy Policy</h2>
                <p className="muted small">Last updated {UPDATED}.</p>
                <dl>
                    <dt>What we store</dt>
                    <dd>
                        Your account: which provider you log in with (Google or Discord) and its id for your account,
                        your display name and your settings. Nothing else of your Google or Discord profile, neither
                        your email address nor your picture. Your games: the games you host, join or watch, every move
                        in them, your seats and votes, your chat messages and the problems you report, with their times.
                        Your login session.
                    </dd>
                    <dt>Why</dt>
                    <dd>
                        Only to run your account and your games. There are no ads, no analytics and no tracking, and we
                        neither sell nor share your data.
                    </dd>
                    <dt>Who sees it</dt>
                    <dd>
                        Logged in users see your display name in the user list, the lobbies and the games. The players
                        and observers of a game see its moves and chat; the rules decide what each of them sees of the
                        hidden cards.
                    </dd>
                    <dt>Cookies and browser storage</dt>
                    <dd>
                        Only what the site needs to work: a session cookie that keeps you logged in, a security cookie
                        against forged requests, and, in your browser's storage, where you last read each game's log and
                        chat. Notifications of your move come from your own browser, and only if you allow them.
                    </dd>
                    <dt>Login providers</dt>
                    <dd>Google and Discord handle your login under their own privacy policies.</dd>
                    <dt>How long</dt>
                    <dd>As long as your account exists. A game stays with its moves and chat, so that its players can
                        look back on it.
                    </dd>
                    <dt>Your rights</dt>
                    <dd>
                        You can change your name in your <Link to="/profile">profile</Link>. To get a copy of your data,
                        have it corrected or deleted, use the <a href="#">contact</a> link. You may also complain to
                        your data protection authority.
                    </dd>
                </dl>
            </section>
        </main>
    );
}
