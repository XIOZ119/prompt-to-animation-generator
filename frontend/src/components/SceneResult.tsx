import type { GenerationResultResponse } from '../types/generation'
import CutResultList from './CutResultList'

interface SceneResultProps {
  result?: GenerationResultResponse
  selectedCutOrder: number | null
  onSelectCut: (cutOrder: number) => void
  onOpenVideo: (videoUrl: string) => void
}

function SceneResult({
  result,
  selectedCutOrder,
  onSelectCut,
  onOpenVideo,
}: SceneResultProps) {
  return (
    <section className="panel result-panel">
      <h2>3. 결과</h2>
      <h3>최종 애니메이션 (30초)</h3>

      {result?.resultUrl ? (
        <>
          <video className="result-video" controls src={result.resultUrl} />
          <a className="download-button" href={result.resultUrl} download>
            <span aria-hidden="true">⇩</span>
            최종 영상 다운로드
          </a>
        </>
      ) : (
        <div className="result-empty">
          <strong>생성된 최종 영상이 여기에 표시됩니다.</strong>
          <span>완료되면 최종 영상과 컷 결과가 표시됩니다.</span>
        </div>
      )}

      <CutResultList
        cuts={result?.cuts ?? []}
        onOpenVideo={onOpenVideo}
        onSelectCut={onSelectCut}
        selectedCutOrder={selectedCutOrder}
      />
    </section>
  )
}

export default SceneResult
