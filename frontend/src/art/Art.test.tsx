import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { ArtImg, usePreview } from './Art';
import { characterArt } from './images';

describe('an image', () => {
  it('takes its preview with it when it goes away under the pointer', () => {
    const art = characterArt('LAURA_ROSLIN')!;
    const view = render(<ArtImg art={art} />);

    fireEvent.mouseEnter(screen.getByRole('img'));
    expect(usePreview.getState().art?.key).toBe(art.key);
    // a re-render with a new ref of the same image keeps it
    view.rerender(<ArtImg art={{ ...art }} />);
    expect(usePreview.getState().art?.key).toBe(art.key);
    view.unmount();
    expect(usePreview.getState().art).toBeNull();
  });
});
