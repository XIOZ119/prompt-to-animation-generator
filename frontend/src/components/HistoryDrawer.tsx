import type { GenerationHistoryItem } from '../types/generation'

interface HistoryDrawerProps {
  histories: GenerationHistoryItem[]
  isOpen: boolean
  selectedGenerationId?: number | null
  onClose: () => void
  onSelect: (history: GenerationHistoryItem) => void
}

const formatCreatedAt = (createdAt?: string) => {
  if (!createdAt) {
    return '-'
  }

  const date = new Date(createdAt)

  if (Number.isNaN(date.getTime())) {
    return createdAt
  }

  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function HistoryDrawer({
  histories,
  isOpen,
  selectedGenerationId,
  onClose,
  onSelect,
}: HistoryDrawerProps) {
  if (!isOpen) {
    return null
  }

  return (
    <aside className="history-drawer" aria-label="생성 기록">
      <div className="drawer-header">
        <h2>생성 기록</h2>
        <button aria-label="생성 기록 닫기" onClick={onClose} type="button">
          ×
        </button>
      </div>

      <div className="history-tabs">
        <button className="is-active" type="button">
          전체
        </button>
        <button type="button">완료</button>
        <button type="button">실패</button>
        <select aria-label="정렬">
          <option>최신순</option>
        </select>
      </div>

      <div className="history-list">
        {histories.length ? (
          histories.map((history) => (
            <button
              className={`history-item ${
                selectedGenerationId === history.generationId ? 'is-selected' : ''
              }`}
              key={history.generationId}
              onClick={() => onSelect(history)}
              type="button"
            >
              <div className="history-thumb">
                {history.result?.cuts[0]?.imageUrl ? (
                  <img alt="" src={history.result.cuts[0].imageUrl} />
                ) : (
                  <span>대기</span>
                )}
              </div>
              <div>
                <strong>{history.result?.scene.title || history.prompt}</strong>
                <span>
                  30초 <i aria-hidden="true">|</i> {formatCreatedAt(history.createdAt)}
                </span>
                {history.status === 'FAILED' && (
                  <small>{history.statusDetail?.errorMessage || '생성 실패'}</small>
                )}
              </div>
              <em className={history.status === 'FAILED' ? 'is-failed' : ''}>
                {history.status === 'FAILED' ? '실패' : '완료'}
              </em>
              <span className="chevron" aria-hidden="true">
                ›
              </span>
            </button>
          ))
        ) : (
          <div className="history-empty">아직 생성 기록이 없습니다.</div>
        )}
      </div>
    </aside>
  )
}

export default HistoryDrawer
