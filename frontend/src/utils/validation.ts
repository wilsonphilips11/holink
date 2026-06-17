const HTML_PATTERN = /<[^>]*>|javascript:|on\w+\s*=/i;
const SCRIPT_PATTERN = /<script[^>]*>.*?<\/script>/is;
const BLOCKED_SCHEMES = ['javascript', 'data', 'vbscript', 'file'];

export function isFreeOfHtml(value: string): boolean {
  if (!value) return true;
  if (SCRIPT_PATTERN.test(value)) return false;
  return !HTML_PATTERN.test(value);
}

export function isSafeHttpUrl(url: string): boolean {
  if (!url?.trim()) return false;
  const trimmed = url.trim();
  try {
    const parsed = new URL(trimmed);
    const scheme = parsed.protocol.replace(':', '').toLowerCase();
    return (scheme === 'http' || scheme === 'https') && !BLOCKED_SCHEMES.includes(scheme);
  } catch {
    return false;
  }
}

export function normalizeUsername(raw: string): string {
  return raw.trim().toLowerCase().replace(/[^a-z0-9]/g, '');
}

function isAllUppercaseLetters(value: string): boolean {
  let hasLetter = false;
  for (const char of value) {
    if (/[a-zA-Z]/.test(char)) {
      hasLetter = true;
      if (char === char.toLowerCase()) {
        return false;
      }
    }
  }
  return hasLetter;
}

export function validateUsernameInput(raw: string): string | null {
  if (!raw?.trim()) return 'Username is required';
  const trimmed = raw.trim();
  if (trimmed.includes('@')) return 'Username cannot contain special characters';
  if (isAllUppercaseLetters(trimmed)) return 'Username must be lowercase';
  if (trimmed.includes(' ') && trimmed === trimmed.toLowerCase()) {
    return 'Username cannot contain spaces';
  }
  const normalized = normalizeUsername(raw);
  if (normalized.length > 30) return 'Username must be at most 30 characters';
  if (!/^[a-z0-9]+$/.test(normalized)) return 'Invalid username format';
  return null;
}

export function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;',
  };
  return text.replace(/[&<>"']/g, (char) => map[char] || char);
}
