import type { Cut } from '../types/generation'

interface CutResultListProps {
  cuts: Cut[]
  selectedCutOrder: number | null
  onSelectCut: (cutOrder: number) => void
  onOpenVideo: (videoUrl: string) => void
}

function CutResultList({
  cuts,
  selectedCutOrder,
  onSelectCut,
  onOpenVideo,
}: CutResultListProps) {
  if (cuts.length === 0) {
    return null
  }

  return (
    <div className="cut-strip-wrap">
      <div className="cut-strip-heading">
        <strong>컷별 생성 결과 (총 {cuts.length}개)</strong>
      </div>
      <div className="cut-strip">
        {cuts.map((cut) => (
          <article
            className={`cut-card ${
              selectedCutOrder === cut.cutOrder ? 'is-selected' : ''
            }`}
            key={cut.cutOrder}
          >
            <span className="cut-order">{String(cut.cutOrder).padStart(2, '0')}</span>
            <button
              className="cut-preview-button"
              onClick={() => onSelectCut(cut.cutOrder)}
              type="button"
            >
              {cut.imageUrl ? (
                <img alt={`Cut ${cut.cutOrder}`} src={cut.imageUrl} />
              ) : (
                <span className="media-placeholder">이미지 대기</span>
              )}
            </button>
            <span className="cut-meta">
              <b>{cut.durationSec ?? 5}초</b>
              <b>Cut {cut.cutOrder}</b>
            </span>
            {cut.videoUrl && (
              <button
                className="cut-video-button"
                onClick={() => onOpenVideo(cut.videoUrl!)}
                type="button"
              >
                ▶
              </button>
            )}
          </article>
        ))}
        {cuts.length > 5 && (
          <button className="round-next" type="button" aria-label="다음 컷 보기">
            ›
          </button>
        )}
      </div>
    </div>
  )
}

export default CutResultList
