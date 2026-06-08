import type {
  ApiError,
  GenerationHistoryPageResponse,
  GenerationHistoryResponse,
  GenerationHistorySort,
  GenerationHistoryStatusFilter,
} from '../types/generation'

interface HistoryDrawerProps {
  history?: GenerationHistoryPageResponse
  isLoading: boolean
  error?: ApiError | null
  isOpen: boolean
  page: number
  selectedGenerationId?: number | null
  sort: GenerationHistorySort
  statusFilter: GenerationHistoryStatusFilter
  onClose: () => void
  onPageChange: (page: number) => void
  onSelect: (history: GenerationHistoryResponse) => void
  onSortChange: (sort: GenerationHistorySort) => void
  onStatusFilterChange: (status: GenerationHistoryStatusFilter) => void
}

const statusTabs: Array<{
  label: string
  value: GenerationHistoryStatusFilter
}> = [
  { label: '전체', value: 'ALL' },
  { label: '완료', value: 'COMPLETED' },
  { label: '실패', value: 'FAILED' },
]

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

const getStatusLabel = (status: string) => {
  if (status === 'COMPLETED') {
    return '완료'
  }

  if (status === 'TIMEOUT') {
    return '시간 초과'
  }

  if (status === 'FAILED') {
    return '실패'
  }

  if (status === 'PROCESSING') {
    return '진행 중'
  }

  return '대기'
}

const truncateText = (text: string, maxLength = 15) =>
  text.length > maxLength ? `${text.slice(0, maxLength)}...` : text

function HistoryDrawer({
  history,
  isLoading,
  error,
  isOpen,
  page,
  selectedGenerationId,
  sort,
  statusFilter,
  onClose,
  onPageChange,
  onSelect,
  onSortChange,
  onStatusFilterChange,
}: HistoryDrawerProps) {
  if (!isOpen) {
    return null
  }

  const histories = history?.content ?? []
  const totalPages = history?.totalPages ?? 0
  const pageNumbers = Array.from({ length: totalPages }, (_, index) => index).slice(
    0,
    8,
  )

  return (
    <aside className="history-drawer" aria-label="생성 기록">
      <div className="drawer-header">
        <h2>생성 기록</h2>
        <button aria-label="생성 기록 닫기" onClick={onClose} type="button">
          ×
        </button>
      </div>

      <div className="history-tabs">
        {statusTabs.map((tab) => (
          <button
            className={statusFilter === tab.value ? 'is-active' : ''}
            key={tab.value}
            onClick={() => onStatusFilterChange(tab.value)}
            type="button"
          >
            {tab.label}
          </button>
        ))}
        <select
          aria-label="정렬"
          onChange={(event) =>
            onSortChange(event.target.value as GenerationHistorySort)
          }
          value={sort}
        >
          <option value="latest">최신순</option>
          <option value="oldest">오래된순</option>
        </select>
      </div>

      <div className="history-list">
        {isLoading ? (
          <div className="history-empty">생성 기록을 불러오는 중입니다.</div>
        ) : error ? (
          <div className="history-empty">
            {error.errorCode
              ? `${error.message} (${error.errorCode})`
              : error.message}
          </div>
        ) : histories.length ? (
          histories.map((item) => {
            const isFailed = item.status === 'FAILED' || item.status === 'TIMEOUT'

            return (
              <button
                className={`history-item ${
                  selectedGenerationId === item.generationId ? 'is-selected' : ''
                }`}
                key={item.generationId}
                onClick={() => onSelect(item)}
                type="button"
              >
                <div className="history-thumb">
                  {item.thumbnailUrl ? (
                    <img alt="" src={item.thumbnailUrl} />
                  ) : (
                    <span>이미지 없음</span>
                  )}
                </div>
                <div>
                  <strong className={item.title ? '' : 'is-empty-title'}>
                    {item.title ?? '제목이 없습니다.'}
                  </strong>
                  <span>
                    {item.durationSec ?? 0}초 <i aria-hidden="true">|</i>{' '}
                    {formatCreatedAt(item.createdAt)}
                  </span>
                  {isFailed && (
                    <small>
                      {truncateText(
                        item.errorMessage || getStatusLabel(item.status),
                      )}
                    </small>
                  )}
                </div>
                <em className={isFailed ? 'is-failed' : ''}>
                  {getStatusLabel(item.status)}
                </em>
                <span className="chevron" aria-hidden="true">
                  ›
                </span>
              </button>
            )
          })
        ) : (
          <div className="history-empty">생성 기록이 없습니다.</div>
        )}
      </div>

      {totalPages > 1 && (
        <div className="history-pagination">
          <button
            disabled={!history?.hasPrevious}
            onClick={() => onPageChange(Math.max(page - 1, 0))}
            type="button"
          >
            ‹
          </button>
          {pageNumbers.map((pageNumber) => (
            <button
              className={page === pageNumber ? 'is-active' : ''}
              key={pageNumber}
              onClick={() => onPageChange(pageNumber)}
              type="button"
            >
              {pageNumber + 1}
            </button>
          ))}
          <button
            disabled={!history?.hasNext}
            onClick={() => onPageChange(page + 1)}
            type="button"
          >
            ›
          </button>
        </div>
      )}
    </aside>
  )
}

export default HistoryDrawer
