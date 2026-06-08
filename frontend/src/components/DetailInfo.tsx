import { useState } from 'react'
import type { GenerationResultResponse } from '../types/generation'

interface DetailInfoProps {
  result?: GenerationResultResponse
  onOpenVideo: (videoUrl: string) => void
}

type DetailTab = 'scene' | 'cuts'

function DetailInfo({ result, onOpenVideo }: DetailInfoProps) {
  const [activeTab, setActiveTab] = useState<DetailTab>('scene')
  const [selectedCutOrder, setSelectedCutOrder] = useState<number | null>(null)
  const selectedCut =
    result?.cuts.find(
      (cut) => cut.cutOrder === (selectedCutOrder ?? result.cuts[0]?.cutOrder),
    ) ?? null

  return (
    <section className="panel detail-panel">
      <h2>4. 상세 정보</h2>

      <div className="tabs" role="tablist" aria-label="상세 정보">
        <button
          className={activeTab === 'scene' ? 'is-active' : ''}
          onClick={() => setActiveTab('scene')}
          role="tab"
          type="button"
        >
          Scene
        </button>
        <button
          className={activeTab === 'cuts' ? 'is-active' : ''}
          onClick={() => setActiveTab('cuts')}
          role="tab"
          type="button"
        >
          Cuts
        </button>
      </div>

      {activeTab === 'scene' && (
        <div className="detail-box">
          {result?.scene ? (
            <dl className="scene-info">
              <dt>제목</dt>
              <dd>{result.scene.title}</dd>
              <dt>시나리오</dt>
              <dd>{result.scene.scenario}</dd>
            </dl>
          ) : (
            <p className="empty-text">완료 후 Scene 정보가 표시됩니다.</p>
          )}
        </div>
      )}

      {activeTab === 'cuts' && (
        <div className="detail-box cut-detail-box">
          {result?.cuts.length ? (
            <>
              <div className="cut-button-list">
                {result.cuts.map((cut) => (
                  <button
                    className={
                      selectedCut?.cutOrder === cut.cutOrder ? 'is-active' : ''
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
            <p className="empty-text">완료 후 컷 정보가 표시됩니다.</p>
          )}
        </div>
      )}
    </section>
  )
}

export default DetailInfo
