import type { Cut } from '../types/generation'

interface CutResultListProps {
  cuts: Cut[]
  selectedCutOrder: number | null
  onSelectCut: (cutOrder: number) => void
}

function CutResultList({
  cuts,
  selectedCutOrder,
  onSelectCut,
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
          <button
            className={`cut-card ${
              selectedCutOrder === cut.cutOrder ? 'is-selected' : ''
            }`}
            key={cut.cutOrder}
            onClick={() => onSelectCut(cut.cutOrder)}
            type="button"
          >
            <span className="cut-order">{String(cut.cutOrder).padStart(2, '0')}</span>
            {cut.imageUrl ? (
              <img alt={`Cut ${cut.cutOrder}`} src={cut.imageUrl} />
            ) : (
              <span className="media-placeholder">이미지 대기</span>
            )}
            <span className="cut-meta">
              <b>{cut.durationSec ?? 5}초</b>
              <b>Cut {cut.cutOrder}</b>
            </span>
          </button>
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
