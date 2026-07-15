/**
 * 用户对外展示名：优先昵称，无昵称时使用用户名。
 * @param {object|string|null|undefined} source 用户对象，或含 nickname/username 的字段
 */
export function displayUserName(source) {
  if (source == null) {
    return '用户'
  }
  if (typeof source === 'string') {
    const t = source.trim()
    return t || '用户'
  }
  const nick = (source.nickname ?? source.publisherNickname ?? '').trim()
  if (nick) {
    return nick
  }
  const uname = (source.username ?? source.publisherUsername ?? '').trim()
  if (uname) {
    return uname
  }
  const id = source.id ?? source.userId ?? source.publisherId
  return id != null ? `用户${id}` : '用户'
}

/** 是否已设置有效昵称 */
export function hasNickname(source) {
  if (!source || typeof source !== 'object') {
    return false
  }
  const nick = (source.nickname ?? source.publisherNickname ?? '').trim()
  return nick.length > 0
}
