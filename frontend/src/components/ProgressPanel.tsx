import type { GenerationStatusResponse } from '../types/generation'

interface ProgressPanelProps {
  status?: GenerationStatusResponse
  isCreating: boolean
  errorMessage?: string | null
}

const stepLabels = [
  { key: 'SCENE_GENERATION', label: 'Scene\n생성' },
  { key: 'CUT_IMAGE_GENERATION', label: 'Cut Image\n생성' },
  { key: 'CUT_VIDEO_GENERATION', label: 'Cut Video\n생성' },
  { key: 'VIDEO_MERGE', label: '최종 영상\n병합' },
]

const stepIndexByKey = new Map(stepLabels.map((step, index) => [step.key, index]))

const isWarningStatus = (status?: string | null) =>
  status === 'FAILED' || status === 'TIMEOUT'

function ProgressPanel({ status, isCreating, errorMessage }: ProgressPanelProps) {
  const progress = status?.progress ?? (isCreating ? 5 : 0)
  const completedStepCount = status?.completedStepCount ?? 0
  const totalStepCount = status?.totalStepCount ?? 14
  const isCompleted = status?.status === 'COMPLETED'
  const isFailed = status?.status === 'FAILED' || status?.status === 'TIMEOUT'
  const hasImageWarning = status?.cuts?.some((cut) =>
    isWarningStatus(cut.imageStatus),
  )
  const hasVideoWarning = status?.cuts?.some((cut) =>
    isWarningStatus(cut.videoStatus),
  )
  const hasCutWarning = hasImageWarning || hasVideoWarning
  const activeStepIndex = isCompleted
    ? stepLabels.length
    : stepIndexByKey.get(status?.currentStep ?? '') ?? -1

  return (
    <section className="panel status-panel">
      <h2>2. 생성 상태</h2>

      <div
        className={`status-card ${isFailed ? 'is-failed' : ''} ${
          hasCutWarning ? 'is-warning' : ''
        }`}
      >
        {status || isCreating ? (
          <>
            <div className="status-heading">
              <span className="status-mark" aria-hidden="true">
                {hasCutWarning
                  ? '!'
                  : isFailed
                    ? '!'
                    : isCompleted
                      ? '✓'
                      : activeStepIndex + 1 || '...'}
              </span>
              <strong>
                {isFailed
                  ? status?.status === 'TIMEOUT'
                    ? '시간 초과'
                    : '실패'
                  : isCompleted
                    ? '완료'
                    : status?.status || '요청 중'}
              </strong>
              {!isCompleted && !isFailed && (
                <span className="status-pill">PROCESSING</span>
              )}
            </div>

            {isCompleted && (
              <p className="status-message">
                애니메이션 생성이 성공적으로 완료되었습니다!
              </p>
            )}

            {!isCompleted && (
              <ol className="stepper" aria-label="생성 단계">
                {stepLabels.map((step, index) => {
                  const hasStepWarning =
                    (step.key === 'CUT_IMAGE_GENERATION' && hasImageWarning) ||
                    (step.key === 'CUT_VIDEO_GENERATION' && hasVideoWarning)
                  const state = hasStepWarning
                    ? 'is-warning'
                    : index < activeStepIndex
                      ? 'is-done'
                      : index === activeStepIndex
                        ? 'is-active'
                        : ''

                  return (
                    <li className={state} key={step.key}>
                      <span>
                        {hasStepWarning
                          ? '!'
                          : index < activeStepIndex
                            ? '✓'
                            : index + 1}
                      </span>
                      <b>{step.label}</b>
                    </li>
                  )
                })}
              </ol>
            )}

            <div className="progress-summary">
              <div>
                <span>{isCompleted ? '전체 진행률' : '현재 진행 중'}</span>
                <strong>
                  {isCompleted
                    ? `${completedStepCount || totalStepCount} / ${totalStepCount} 단계`
                    : status?.currentStepMessage || `${completedStepCount} / ${totalStepCount} 단계`}
                </strong>
              </div>
              <b>{progress}%</b>
            </div>
            <div className="progress-bar" aria-label={`진행률 ${progress}%`}>
              <span style={{ width: `${Math.min(progress, 100)}%` }} />
            </div>
          </>
        ) : (
          <p className="empty-text">프롬프트를 입력하고 생성을 시작해주세요.</p>
        )}

        {(errorMessage || status?.errorMessage) && (
          <p className="error-text">{errorMessage || status?.errorMessage}</p>
        )}
      </div>

      <div className="guide-box">
        <strong>생성 단계 안내</strong>
        <ol>
          <li>Scene 생성 (GPT-5.4 mini)</li>
          <li>Cut Image 생성 (Nano Banana)</li>
          <li>Cut Video 생성 (Kling-2.6)</li>
          <li>최종 영상 병합 (FFmpeg)</li>
        </ol>
      </div>
    </section>
  )
}

export default ProgressPanel
