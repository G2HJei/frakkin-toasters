import { fireEvent, render, screen } from '@testing-library/react';
import { StrictMode } from 'react';
import { describe, expect, it } from 'vitest';
import { ArtImg, ArtPreview, previewHandlers, usePreview } from './Art';
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

  it('keeps a preview that shows the same image again, e.g. a standee with his mat, also in StrictMode', () => {
    const art = characterArt('LAURA_ROSLIN')!;
    render(
      <StrictMode>
        <span data-testid="standee" {...previewHandlers(art, <ArtImg art={art} />)} />
        <ArtPreview />
      </StrictMode>,
    );

    fireEvent.mouseEnter(screen.getByTestId('standee'));
    expect(usePreview.getState().art?.key).toBe(art.key);
    expect(document.querySelector('.art-preview-extra img')?.getAttribute('data-art')).toBe(art.key);
  });
});
