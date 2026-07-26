import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from '../App.jsx';

// Owner: M6. Placeholder smoke test so `npm test -- --run` has something to run on Day 1.
describe('App shell', () => {
  it('renders the Kartyavya heading', () => {
    render(<App />);
    expect(screen.getByText(/Kartyavya/i)).toBeInTheDocument();
  });
});
