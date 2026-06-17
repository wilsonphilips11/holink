import { describe, it, expect } from 'vitest';
import {
  isFreeOfHtml,
  isSafeHttpUrl,
  normalizeUsername,
  validateUsernameInput,
} from '@/utils/validation';

describe('validation utils', () => {
  describe('isSafeHttpUrl', () => {
    it('allows http and https URLs', () => {
      expect(isSafeHttpUrl('https://example.com')).toBe(true);
      expect(isSafeHttpUrl('http://example.com/path')).toBe(true);
    });

    it('rejects dangerous and invalid URLs', () => {
      expect(isSafeHttpUrl('javascript:alert(1)')).toBe(false);
      expect(isSafeHttpUrl('data:text/html,<script>')).toBe(false);
      expect(isSafeHttpUrl('google.com')).toBe(false);
      expect(isSafeHttpUrl('ftp://example.com')).toBe(false);
      expect(isSafeHttpUrl('https://')).toBe(false);
      expect(isSafeHttpUrl('https://exa mple.com')).toBe(false);
    });
  });

  describe('isFreeOfHtml', () => {
    it('rejects script tags', () => {
      expect(isFreeOfHtml('<script>alert(1)</script>')).toBe(false);
    });

    it('allows plain text', () => {
      expect(isFreeOfHtml('Hello World')).toBe(true);
    });
  });

  describe('username validation', () => {
    it('normalizes username', () => {
      expect(normalizeUsername('John Doe')).toBe('johndoe');
    });

    it('rejects uppercase', () => {
      expect(validateUsernameInput('UPPERCASE')).toBe('Username must be lowercase');
    });

    it('rejects spaces', () => {
      expect(validateUsernameInput('john doe')).toBe('Username cannot contain spaces');
    });

    it('rejects @ symbol', () => {
      expect(validateUsernameInput('@john')).toBe('Username cannot contain special characters');
    });

    it('allows mixed-case names with spaces for normalization', () => {
      expect(validateUsernameInput('John Doe')).toBeNull();
    });
  });
});
