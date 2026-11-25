import { useState } from 'react';
import { RESOURCE_NAMES } from '../../labels/results';
import type { TurnResources } from '../../wire/types';

type Resource = keyof typeof RESOURCE_NAMES;

// in the order of the categorical slots, validated on the table's surface (dataviz validator, dark mode)
const SERIES: Resource[] = ['fuel', 'food', 'morale', 'population'];

const WIDTH = 640;
const HEIGHT = 220;
const PAD = { top: 12, right: 92, bottom: 26, left: 28 };
const LABEL_GAP = 13;

/**
 * The fleet's resources at the start of every turn and at the end, as lines on one axis (they share a scale). A legend
 * names the lines and their ends carry their names too; the crosshair shows every value of the turn under the pointer,
 * and the table under the chart holds the same numbers.
 */
export function ResourcesChart({ resources }: { resources: TurnResources[] }) {
  const [hover, setHover] = useState<number | null>(null);
  if (resources.length < 2) {
    return null;
  }
  const max = Math.max(15, ...resources.flatMap((point) => SERIES.map((series) => point[series])));
  const innerWidth = WIDTH - PAD.left - PAD.right;
  const innerHeight = HEIGHT - PAD.top - PAD.bottom;
  const x = (index: number) => PAD.left + (index / (resources.length - 1)) * innerWidth;
  const y = (value: number) => PAD.top + innerHeight - (value / max) * innerHeight;
  const ticks = [0, 5, 10, 15].filter((tick) => tick <= max);
  const every = Math.max(1, Math.ceil(resources.length / 12));
  const turnName = (point: TurnResources) => (point.end ? 'End' : `${point.turn}`);
  const last = resources[resources.length - 1];
  const ends = endLabels(SERIES.map((series) => ({ series, y: y(last[series]) })));

  return (
    <figure className="resources-chart">
      <figcaption>
        <span className="chart-title">Resources at the start of each turn</span>
        <span className="legend">
          {SERIES.map((series) => (
            <span key={series} className="legend-item">
              <span className={`swatch ${series}`} aria-hidden="true" />
              {RESOURCE_NAMES[series]}
            </span>
          ))}
        </span>
      </figcaption>
      <div className="chart-frame">
        <svg
          viewBox={`0 0 ${WIDTH} ${HEIGHT}`}
          role="img"
          aria-label="The resources over the turns; the table below has the numbers"
          onPointerMove={(event) => {
            const box = event.currentTarget.getBoundingClientRect();
            const at = ((event.clientX - box.left) / box.width) * WIDTH;
            const index = Math.round(((at - PAD.left) / innerWidth) * (resources.length - 1));
            setHover(Math.max(0, Math.min(resources.length - 1, index)));
          }}
          onPointerLeave={() => setHover(null)}
        >
          {ticks.map((tick) => (
            <g key={tick} className="grid">
              <line x1={PAD.left} x2={PAD.left + innerWidth} y1={y(tick)} y2={y(tick)} />
              <text x={PAD.left - 6} y={y(tick) + 4} textAnchor="end">
                {tick}
              </text>
            </g>
          ))}
          {resources.map((point, index) =>
            index % every === 0 || index === resources.length - 1 ? (
              <text key={index} className="axis" x={x(index)} y={HEIGHT - 8} textAnchor="middle">
                {turnName(point)}
              </text>
            ) : null,
          )}
          {SERIES.map((series) => (
            <polyline key={series} className={`line ${series}`} points={resources.map((point, index) => `${x(index)},${y(point[series])}`).join(' ')} />
          ))}
          {ends.map(({ series, labelY }) => (
            <text key={series} className="end-label" x={PAD.left + innerWidth + 8} y={labelY + 4}>
              {RESOURCE_NAMES[series]} {last[series]}
            </text>
          ))}
          {hover !== null && (
            <g className="crosshair">
              <line x1={x(hover)} x2={x(hover)} y1={PAD.top} y2={PAD.top + innerHeight} />
              {SERIES.map((series) => (
                <circle key={series} className={`marker ${series}`} cx={x(hover)} cy={y(resources[hover][series])} r={4} />
              ))}
            </g>
          )}
        </svg>
        {hover !== null && (
          <div className="chart-tooltip" style={{ left: `${(x(hover) / WIDTH) * 100}%` }} role="status">
            <strong>{resources[hover].end ? 'At the end' : `Turn ${resources[hover].turn}`}</strong>
            {SERIES.map((series) => (
              <span key={series} className="tooltip-row">
                <span className={`swatch ${series}`} aria-hidden="true" />
                {RESOURCE_NAMES[series]} <b>{resources[hover][series]}</b>
              </span>
            ))}
          </div>
        )}
      </div>
      <details className="chart-table">
        <summary>As a table</summary>
        <table>
          <thead>
            <tr>
              <th scope="col">Turn</th>
              {SERIES.map((series) => (
                <th key={series} scope="col">
                  {RESOURCE_NAMES[series]}
                </th>
              ))}
              <th scope="col">Distance</th>
            </tr>
          </thead>
          <tbody>
            {resources.map((point, index) => (
              <tr key={index}>
                <th scope="row">{turnName(point)}</th>
                {SERIES.map((series) => (
                  <td key={series}>{point[series]}</td>
                ))}
                <td>{point.distance}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </details>
    </figure>
  );
}

// The names at the lines' ends, moved apart where the lines end close together.
function endLabels(ends: { series: Resource; y: number }[]): { series: Resource; labelY: number }[] {
  const sorted = [...ends].sort((a, b) => a.y - b.y);
  const placed: { series: Resource; labelY: number }[] = [];
  for (const end of sorted) {
    const previous = placed[placed.length - 1];
    placed.push({ series: end.series, labelY: previous && end.y - previous.labelY < LABEL_GAP ? previous.labelY + LABEL_GAP : end.y });
  }
  return placed;
}
