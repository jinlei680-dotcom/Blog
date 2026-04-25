export function estimateReadingTime(text) {
  if (!text) return 1
  const cleaned = text
    .replace(/```[\s\S]*?```/g, '')
    .replace(/`[^`]*`/g, '')
    .replace(/^\s*#{1,6}\s+/gm, '')
    .replace(/[*_\[\]>]/g, '')
  const chinese = (cleaned.match(/[一-龥぀-ヿ]/g) || []).length
  const english = (cleaned.replace(/[一-龥぀-ヿ]/g, '').match(/\b\w+\b/g) || []).length
  const minutes = chinese / 250 + english / 200
  return Math.max(1, Math.round(minutes))
}

export function countWords(text) {
  if (!text) return 0
  const chinese = (text.match(/[一-龥]/g) || []).length
  const english = (text.replace(/[一-龥]/g, '').match(/\b\w+\b/g) || []).length
  return chinese + english
}

export function countParagraphs(text) {
  if (!text) return 0
  return text.split(/\n\n+/).filter(p => p.trim().length > 0).length
}
