import { useState } from 'react';
import { LOBBY_CHAT_MAX_LENGTH, useLobbyChat, useMe, useSayInLobby, type LobbyLine } from '../api/queries';
import { useFollowBottom } from '../table/log/useFollowBottom';

export function LobbyChat() {
  const chat = useLobbyChat();
  const say = useSayInLobby();
  const me = useMe().data;
  const [draft, setDraft] = useState('');
  const lines = chat.data ?? [];
  const { scrollRef, contentRef, onScroll } = useFollowBottom(lines);

  const submit = () => {
    const text = draft.trim();
    if (!text || say.isPending) {
      return;
    }
    say.mutate(text, { onSuccess: () => setDraft('') });
  };

  return (
    <section className="panel cf lobby-chat" aria-label="Chat">
      <div className="panel-head">
        <h2>Chat</h2>
      </div>
      <div className="lobby-chat-body" ref={scrollRef} onScroll={onScroll}>
        <div ref={contentRef}>
          {lines.length === 0 ? (
            <p className="muted empty">{chat.isPending ? 'Loading…' : 'Nothing said yet.'}</p>
          ) : (
            <ol className="lobby-chat-lines">
              {lines.map((line) => (
                <Line key={line.id} line={line} mine={line.userId === me?.id} />
              ))}
            </ol>
          )}
        </div>
      </div>
      <form
        className="lobby-chat-compose"
        onSubmit={(event) => {
          event.preventDefault();
          submit();
        }}
      >
        <textarea
          value={draft}
          maxLength={LOBBY_CHAT_MAX_LENGTH}
          rows={1}
          aria-label="message"
          placeholder="Say something to everyone here"
          onChange={(event) => setDraft(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && !event.shiftKey) {
              event.preventDefault();
              submit();
            }
          }}
        />
      </form>
      {say.error && (
        <p className="error" role="alert">
          {say.error.message}
        </p>
      )}
    </section>
  );
}

function Line({ line, mine }: { line: LobbyLine; mine: boolean }) {
  const time = new Date(line.at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  return (
    <li className={mine ? 'mine' : undefined}>
      <span className="lobby-chat-head">
        <span className="lobby-chat-name">{line.name}</span>
        <time className="muted" dateTime={line.at}>
          {time}
        </time>
      </span>
      <span className="lobby-chat-text">{line.text}</span>
    </li>
  );
}
