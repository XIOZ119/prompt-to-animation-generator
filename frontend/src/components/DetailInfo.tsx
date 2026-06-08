import { useState } from 'react'
import type { GenerationResultResponse } from '../types/generation'

interface DetailInfoProps {
  result?: GenerationResultResponse
  onOpenVideo: (videoUrl: string) => void
}

type DetailTab = 'scene' | 'cuts'

function DetailInfo({ result, onOpenVideo }: DetailInfoProps) {
  const [activeTab, setActiveTab] = useState<DetailTab>('scene')

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
        <div className="table-wrap">
          {result?.cuts.length ? (
            <table>
              <thead>
                <tr>
                  <th>컷</th>
                  <th>지속 시간</th>
                  <th>이미지 프롬프트</th>
                  <th>이미지</th>
                  <th>비디오</th>
                </tr>
              </thead>
              <tbody>
                {result.cuts.map((cut) => (
                  <tr key={cut.cutOrder}>
                    <td>Cut {cut.cutOrder}</td>
                    <td>{cut.durationSec ?? 5}초</td>
                    <td>{cut.imagePrompt}</td>
                    <td>
                      {cut.imageUrl ? (
                        <img
                          alt={`Cut ${cut.cutOrder} 이미지`}
                          className="table-thumb"
                          src={cut.imageUrl}
                        />
                      ) : (
                        '-'
                      )}
                    </td>
                    <td>
                      {cut.videoUrl ? (
                        <button
                          className="play-link"
                          onClick={() => onOpenVideo(cut.videoUrl!)}
                          type="button"
                        >
                          ▶
                        </button>
                      ) : (
                        '-'
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="empty-text">완료 후 컷 정보가 표시됩니다.</p>
          )}
        </div>
      )}
    </section>
  )
}

export default DetailInfo
