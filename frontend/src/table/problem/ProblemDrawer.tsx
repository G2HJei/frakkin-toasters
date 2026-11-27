import { useEffect, useRef, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { create } from 'zustand';
import { api } from '../../api/http';

export const MAX_LENGTH = 1000;

interface ProblemState {
  open: boolean;
  show(): void;
  hide(): void;
}

export const useProblem = create<ProblemState>((set) => ({
  open: false,
  show: () => set({ open: true }),
  hide: () => set({ open: false }),
}));

export function ProblemLink() {
  return (
    <button type="button" className="link problem-open" onClick={() => useProblem.getState().show()}>
      Report a problem
    </button>
  );
}

export function ProblemDrawer({ gameId }: { gameId: string }) {
  const open = useProblem((state) => state.open);
  const [text, setText] = useState('');
  const field = useRef<HTMLTextAreaElement>(null);
  const send = useMutation({ mutationFn: (problem: string) => api<{ id: number }>('POST', `/api/games/${gameId}/problems`, { text: problem }) });

  useEffect(() => {
    if (open) {
      send.reset();
      field.current?.focus();
    }
    // reset is stable
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  if (!open) {
    return null;
  }
  const close = () => useProblem.getState().hide();
  return (
    <aside
      className="problem panel cf"
      role="dialog"
      aria-label="report a problem"
      onKeyDown={(event) => {
        // the table's keys stay out of the box
        event.stopPropagation();
        if (event.key === 'Escape') {
          close();
        }
      }}
    >
      <h2>Report a problem</h2>
      {send.isSuccess ? (
        <p role="status">Thank you: the report is stored with this game and where it stands now.</p>
      ) : (
        <form
          onSubmit={(event) => {
            event.preventDefault();
            if (text.trim()) {
              send.mutate(text.trim(), { onSuccess: () => setText('') });
            }
          }}
        >
          <label className="problem-field">
            <span className="muted">What went wrong, and what did you expect?</span>
            <textarea ref={field} value={text} maxLength={MAX_LENGTH} rows={6} aria-label="problem" onChange={(event) => setText(event.target.value)} />
          </label>
          <div className="toolbar">
            <button type="submit" className="primary" disabled={!text.trim() || send.isPending}>
              Send
            </button>
            <small className="muted">{MAX_LENGTH - text.length} characters left</small>
          </div>
          {send.error && <p className="error">{send.error.message}</p>}
        </form>
      )}
      <button type="button" className="link problem-close" onClick={close}>
        Close
      </button>
    </aside>
  );
}
