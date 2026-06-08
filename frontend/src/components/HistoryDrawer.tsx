import { useState } from 'react'
import type {
  ApiError,
  GenerationHistoryPageResponse,
  GenerationHistoryResponse,
  GenerationHistorySort,
  GenerationHistoryStatusFilter,
  GenerationResultResponse,
} from '../types/generation'

interface HistoryDrawerProps {
  history?: GenerationHistoryPageResponse
  detailRecord?: GenerationHistoryResponse | null
  detailResult?: GenerationResultResponse
  isLoading: boolean
  isDetailLoading: boolean
  error?: ApiError | null
  detailError?: ApiError | null
  isOpen: boolean
  page: number
  selectedGenerationId?: number | null
  sort: GenerationHistorySort
  statusFilter: GenerationHistoryStatusFilter
  onClose: () => void
  onDetailBack: () => void
  onOpenVideo: (videoUrl: string) => void
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
  detailRecord,
  detailResult,
  isLoading,
  isDetailLoading,
  error,
  detailError,
  isOpen,
  page,
  selectedGenerationId,
  sort,
  statusFilter,
  onClose,
  onDetailBack,
  onOpenVideo,
  onPageChange,
  onSelect,
  onSortChange,
  onStatusFilterChange,
}: HistoryDrawerProps) {
  const [detailTab, setDetailTab] = useState<'scene' | 'cuts'>('scene')
  const [selectedCutOrder, setSelectedCutOrder] = useState<number | null>(null)

  if (!isOpen) {
    return null
  }

  const histories = history?.content ?? []
  const totalPages = history?.totalPages ?? 0
  const pageNumbers = Array.from({ length: totalPages }, (_, index) => index).slice(
    0,
    8,
  )
  const selectedCut =
    detailResult?.cuts.find(
      (cut) => cut.cutOrder === (selectedCutOrder ?? detailResult.cuts[0]?.cutOrder),
    ) ?? null

  if (detailRecord) {
    return (
      <aside className="history-drawer" aria-label="생성 기록 상세">
        <div className="drawer-header">
          <h2>생성 기록 상세</h2>
          <button aria-label="생성 기록 닫기" onClick={onClose} type="button">
            ×
          </button>
        </div>

        <div className="history-detail-content">
          <div className="history-detail-summary">
            <div className="history-detail-thumb">
              {detailRecord.thumbnailUrl ? (
                <img alt="" src={detailRecord.thumbnailUrl} />
              ) : (
                <span>이미지 없음</span>
              )}
            </div>
            <div className="history-detail-meta">
              <strong className={detailRecord.title ? '' : 'is-empty-title'}>
                {detailRecord.title ?? '제목이 없습니다.'}
              </strong>
              <span>
                {detailRecord.durationSec ?? 0}초 <i aria-hidden="true">|</i>{' '}
                {formatCreatedAt(detailRecord.createdAt)}
              </span>
              <span>생성 ID: gen_{detailRecord.generationId}</span>
            </div>
            <em
              className={
                detailRecord.status === 'FAILED' ||
                detailRecord.status === 'TIMEOUT'
                  ? 'is-failed'
                  : ''
              }
            >
              {getStatusLabel(detailRecord.status)}
            </em>
          </div>

          {isDetailLoading ? (
            <div className="history-empty">생성 기록 상세를 불러오는 중입니다.</div>
          ) : detailError ? (
            <div className="history-empty">
              {detailError.errorCode
                ? `${detailError.message} (${detailError.errorCode})`
                : detailError.message}
            </div>
          ) : (
            <>
              <section className="history-detail-section">
                <h3>최종 결과</h3>
                {detailResult?.resultUrl ? (
                  <video controls src={detailResult.resultUrl} />
                ) : (
                  <div className="history-detail-empty">최종 영상 없음</div>
                )}
              </section>

              <section className="history-detail-section">
                <h3>상세 정보</h3>
                <div className="history-detail-tabs" role="tablist">
                  <button
                    className={detailTab === 'scene' ? 'is-active' : ''}
                    onClick={() => setDetailTab('scene')}
                    type="button"
                  >
                    Scene
                  </button>
                  <button
                    className={detailTab === 'cuts' ? 'is-active' : ''}
                    onClick={() => setDetailTab('cuts')}
                    type="button"
                  >
                    Cuts
                  </button>
                </div>
                {detailTab === 'scene' && (
                  <div className="history-detail-info">
                    <dl>
                      <dt>제목</dt>
                      <dd>{detailResult?.scene.title ?? '제목이 없습니다.'}</dd>
                      <dt>줄거리</dt>
                      <dd>{detailResult?.scene.scenario ?? '-'}</dd>
                    </dl>
                  </div>
                )}
                {detailTab === 'cuts' && (
                  <div className="history-detail-cuts">
                    {detailResult?.cuts.length ? (
                      <>
                        <div className="cut-button-list">
                          {detailResult.cuts.map((cut) => (
                            <button
                              className={
                                selectedCut?.cutOrder === cut.cutOrder
                                  ? 'is-active'
                                  : ''
                              }
                              key={cut.cutOrder}
                              onClick={() => setSelectedCutOrder(cut.cutOrder)}
                              type="button"
                            >
                              Cut {cut.cutOrder}
                            </button>
                          ))}
                        </div>

                        {selectedCut && (
                          <div className="selected-cut-detail">
                            <div className="selected-cut-media">
                              {selectedCut.imageUrl ? (
                                <img
                                  alt={`Cut ${selectedCut.cutOrder} 이미지`}
                                  src={selectedCut.imageUrl}
                                />
                              ) : (
                                <span>이미지 없음</span>
                              )}
                              {selectedCut.videoUrl && (
                                <button
                                  className="selected-cut-play"
                                  onClick={() => onOpenVideo(selectedCut.videoUrl!)}
                                  type="button"
                                >
                                  ▶ 비디오 재생
                                </button>
                              )}
                            </div>
                            <dl className="selected-cut-info">
                              <dt>컷</dt>
                              <dd>Cut {selectedCut.cutOrder}</dd>
                              <dt>지속 시간</dt>
                              <dd>{selectedCut.durationSec ?? 5}초</dd>
                              <dt>이미지 프롬프트</dt>
                              <dd>{selectedCut.imagePrompt}</dd>
                              <dt>비디오 프롬프트</dt>
                              <dd>{selectedCut.videoPrompt}</dd>
                            </dl>
                          </div>
                        )}
                      </>
                    ) : (
                      <p className="empty-text">컷 정보가 없습니다.</p>
                    )}
                  </div>
                )}
              </section>
            </>
          )}
        </div>

        <div className="history-detail-footer">
          <button onClick={onDetailBack} type="button">
            목록으로 돌아가기
          </button>
        </div>
      </aside>
    )
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
                onClick={() => {
                  setDetailTab('scene')
                  setSelectedCutOrder(null)
                  onSelect(item)
                }}
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
