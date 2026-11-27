import { useRef } from 'react';
import { usePlaceNear } from '../art/nearPointer';
import { useTable } from '../table/tableContext';
import { Groups } from './AnswerPanel';
import { moves, narrow, type Answer, type Move } from './answers';
import { useSelection } from './selection';

export function AnswerPopover({ onMove }: { onMove(move: Move, event: Answer | null): void }) {
  const table = useTable();
  const picked = useSelection((state) => state.picked);
  const anchor = useSelection((state) => state.anchor);
  const picking = useSelection((state) => state.picking);
  const left = narrow(moves(table.game?.input), picked);
  const shown = picked.length > 0 && anchor !== null && picking === null && left.length > 1;
  const ref = useRef<HTMLDivElement>(null);
  usePlaceNear(ref, shown ? anchor : null);
  if (!shown) {
    return null;
  }
  const send = (move: Move) => {
    useSelection.getState().clear();
    onMove(move, move.event);
  };
  return (
    <div ref={ref} className="answer-pop" role="dialog" aria-label="answers here">
      <div className="selection-bar">
        <span className="muted">{left.length} answers</span>
        <button type="button" className="link" onClick={() => useSelection.getState().back()}>
          Back
        </button>
        <button type="button" className="link" onClick={() => useSelection.getState().clear()}>
          Clear
        </button>
      </div>
      {/* a group opens here, next to the click */}
      <Groups moves={left} onMove={send} onOpen={(kind) => useSelection.getState().pick({ type: 'kind', kind }, anchor)} />
    </div>
  );
}
