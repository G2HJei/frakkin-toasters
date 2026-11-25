import { useEffect, useRef, useState, type ReactNode } from 'react';
import { ArtImg } from '../../art/Art';
import { characterArt } from '../../art/images';
import { useChatStore } from '../../live/chatStore';
import type { ChatLine } from '../../wire/types';
import { useFollowBottom } from '../log/useFollowBottom';
import { useTable } from '../tableContext';

export const MAX_LENGTH = 500;

export interface ChatPanelProps {
  // the switch between the log and the chat, over the panel
  tabs: ReactNode;
  // whether the viewer writes: a player or an observer of the game; whoever only watches a finished game reads
  writes: boolean;
  // the last message the viewer had seen when he opened the chat: the red line goes under it
  seenBefore: number | null;
  send(text: string): boolean;
}

/**
 * The public chat, in place of the log: one for everyone at the table. A message shows its writer's portrait (his
 * seat's character) or "observer", his name and the time; a red line marks where the viewer left off when he opened the
 * chat. Enter sends, Shift+Enter breaks the line.
 */
export function ChatPanel({ tabs, writes, seenBefore, send }: ChatPanelProps) {
  const table = useTable();
  const messages = useChatStore((state) => state.messages);
  const rejected = useChatStore((state) => state.rejected);
  const [draft, setDraft] = useState('');
  const { scrollRef, contentRef, following, unseen, onScroll, toBottom } = useFollowBottom(messages);
  const input = useRef<HTMLTextAreaElement>(null);
  const firstUnseen = seenBefore === null ? null : (messages.find((line) => line.id > seenBefore)?.id ?? null);

  useEffect(() => {
    input.current?.focus();
  }, []);

  const submit = () => {
    const text = draft.trim();
    if (!text || !writes) {
      return;
    }
    useChatStore.getState().clearRejected();
    if (send(text)) {
      setDraft('');
    }
  };

  return (
    <section className="panel cf log chat" aria-label="chat">
      {tabs}
      <div className="log-body chat-body" ref={scrollRef} onScroll={onScroll}>
        <div ref={contentRef}>
          {messages.length === 0 ? (
            <p className="muted chat-empty">Nothing said yet.</p>
          ) : (
            <ol className="chat-lines">
              {messages.map((line) => (
                <Line
                  key={line.id}
                  line={line}
                  unseenFromHere={line.id === firstUnseen}
                  character={line.playerNumber !== null ? (table.game?.players?.find((player) => player.number === line.playerNumber)?.character ?? null) : null}
                />
              ))}
            </ol>
          )}
        </div>
      </div>
      {!following && (
        <button type="button" className={unseen ? 'log-to-bottom cf unseen' : 'log-to-bottom cf'} aria-label="Go to bottom" title={unseen ? 'New messages below' : 'Go to bottom'} onClick={toBottom}>
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 5v14M6 13l6 6 6-6" />
          </svg>
        </button>
      )}
      {writes ? (
        <form
          className="chat-compose"
          onSubmit={(event) => {
            event.preventDefault();
            submit();
          }}
        >
          <textarea
            ref={input}
            value={draft}
            maxLength={MAX_LENGTH}
            rows={2}
            aria-label="message"
            placeholder="Say something at the table"
            onChange={(event) => setDraft(event.target.value)}
            onKeyDown={(event) => {
              // the reference's R and the other keys of the table stay out of the field
              event.stopPropagation();
              if (event.key === 'Enter' && !event.shiftKey) {
                event.preventDefault();
                submit();
              }
            }}
          />
          <button type="submit" className="primary" disabled={!draft.trim()}>
            Send
          </button>
          {draft.length > MAX_LENGTH - 50 && <small className="muted">{MAX_LENGTH - draft.length} left</small>}
        </form>
      ) : (
        <p className="muted chat-readonly">Only the players and observers of the game write here.</p>
      )}
      {rejected && (
        <p className="rejected" role="alert">
          {rejected}
        </p>
      )}
    </section>
  );
}

function Line({ line, character, unseenFromHere }: { line: ChatLine; character: string | null; unseenFromHere: boolean }) {
  const table = useTable();
  const portrait = character ? characterArt(character) : null;
  const time = line.at ? new Date(line.at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '';
  return (
    <>
      {unseenFromHere && (
        <li className="last-seen" aria-label="new since you last looked">
          <span>new</span>
        </li>
      )}
      <li className="chat-line">
        <span className="line-token">{portrait ? <ArtImg art={portrait} /> : <span className="initial">{(line.name ?? '?').slice(0, 1)}</span>}</span>
        <div className="line-body">
          <span className="chat-head">
            <span className="actor">{line.name}</span>
            <span className="muted"> {line.playerNumber !== null ? table.playerName(line.playerNumber) : 'observer'}</span>
            <time className="muted" dateTime={line.at ?? undefined}>
              {' '}
              {time}
            </time>
          </span>
          <span className="chat-text">{line.text}</span>
        </div>
      </li>
    </>
  );
}
