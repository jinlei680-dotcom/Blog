import DOMPurify from 'dompurify'

/**
 * Sanitize HTML output from Markdown rendering to prevent XSS attacks.
 * Removes script tags, on* event attributes, and javascript: protocol URLs.
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    FORBID_TAGS: ['script', 'style'],
    FORBID_ATTR: ['onerror', 'onload', 'onclick', 'onmouseover', 'onfocus', 'onblur', 'onchange', 'oninput', 'onsubmit', 'onkeydown', 'onkeyup', 'onkeypress'],
    ALLOW_DATA_ATTR: false
  })
}
