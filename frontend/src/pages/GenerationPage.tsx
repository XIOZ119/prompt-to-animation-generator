import { useMemo, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import {
  createGeneration,
  fetchGenerationHistory,
  fetchGenerationResult,
  fetchGenerationStatus,
} from '../api/generationApi'
import DetailInfo from '../components/DetailInfo'
import HistoryDrawer from '../components/HistoryDrawer'
import ProgressPanel from '../components/ProgressPanel'
import PromptForm from '../components/PromptForm'
import SceneResult from '../components/SceneResult'
import type {
  ApiError,
  GenerationHistoryResponse,
  GenerationHistorySort,
  GenerationHistoryStatusFilter,
  GenerationResultResponse,
  GenerationStatusResponse,
} from '../types/generation'

const DEFAULT_PROMPT =
  '숲 속에서 곰이 꿀을 발견하고\n맛있게 먹는 30초 애니메이션을 만들어줘.'

const isTerminalStatus = (status?: string) =>
  status === 'COMPLETED' || status === 'FAILED' || status === 'TIMEOUT'

function GenerationPage() {
  const [prompt, setPrompt] = useState(DEFAULT_PROMPT)
  const [generationId, setGenerationId] = useState<number | null>(null)
  const [resultLookupId, setResultLookupId] = useState('')
  const [historyOpen, setHistoryOpen] = useState(false)
  const [historyPage, setHistoryPage] = useState(0)
  const [historySort, setHistorySort] = useState<GenerationHistorySort>('latest')
  const [historyStatusFilter, setHistoryStatusFilter] =
    useState<GenerationHistoryStatusFilter>('ALL')
  const [videoModalUrl, setVideoModalUrl] = useState<string | null>(null)
  const [selectedHistoryResult, setSelectedHistoryResult] =
    useState<GenerationResultResponse>()
  const [selectedHistoryStatus, setSelectedHistoryStatus] =
    useState<GenerationStatusResponse>()

  const createMutation = useMutation({
    mutationFn: createGeneration,
    onSuccess: (data) => {
      setGenerationId(data.generationId)
      setSelectedHistoryResult(undefined)
      setSelectedHistoryStatus(undefined)
    },
  })

  const statusQuery = useQuery({
    queryKey: ['generation-status', generationId],
    queryFn: () => fetchGenerationStatus(generationId!),
    enabled: generationId !== null,
    refetchInterval: (query) => {
      const status = query.state.data?.status

      return isTerminalStatus(status) ? false : 3000
    },
  })

  const shouldFetchResult = isTerminalStatus(statusQuery.data?.status)

  const resultQuery = useQuery({
    queryKey: ['generation-result', generationId],
    queryFn: () => fetchGenerationResult(generationId!),
    enabled: generationId !== null && shouldFetchResult,
  })

  const historyQuery = useQuery({
    queryKey: [
      'generation-history',
      historyStatusFilter,
      historyPage,
      historySort,
    ],
    queryFn: () =>
      fetchGenerationHistory({
        status: historyStatusFilter,
        page: historyPage,
        size: 6,
        sort: historySort,
      }),
    enabled: historyOpen,
  })

  const previousResultMutation = useMutation({
    mutationFn: fetchGenerationResult,
    onSuccess: (data, requestedGenerationId) => {
      setGenerationId(requestedGenerationId)
      setSelectedHistoryResult(data)
      setSelectedHistoryStatus(undefined)
    },
  })

  const activeResult = selectedHistoryResult || resultQuery.data
  const activeStatus = selectedHistoryStatus || statusQuery.data
  const apiError =
    (createMutation.error as ApiError | null)?.message ||
    (statusQuery.error as ApiError | null)?.message ||
    (resultQuery.error as ApiError | null)?.message ||
    (previousResultMutation.error as ApiError | null)?.message ||
    null

  const displayError = useMemo(() => {
    const errors = [
      createMutation.error as ApiError | null,
      statusQuery.error as ApiError | null,
      resultQuery.error as ApiError | null,
      previousResultMutation.error as ApiError | null,
    ].filter(Boolean) as ApiError[]

    if (!errors.length) {
      return null
    }

    return errors.map((error) =>
      error.errorCode ? `${error.message} (${error.errorCode})` : error.message,
    )[0]
  }, [
    createMutation.error,
    previousResultMutation.error,
    resultQuery.error,
    statusQuery.error,
  ])

  const handleCreate = () => {
    createMutation.mutate(prompt.trim())
  }

  const handleSelectHistoryRecord = (history: GenerationHistoryResponse) => {
    setGenerationId(history.generationId)
    setSelectedHistoryStatus(undefined)
    setSelectedHistoryResult(undefined)
    previousResultMutation.mutate(history.generationId)
  }

  const handleHistoryStatusFilterChange = (
    status: GenerationHistoryStatusFilter,
  ) => {
    setHistoryStatusFilter(status)
    setHistoryPage(0)
  }

  const handleHistorySortChange = (sort: GenerationHistorySort) => {
    setHistorySort(sort)
    setHistoryPage(0)
  }

  const handleFetchResultById = () => {
    const targetGenerationId = Number(resultLookupId)

    if (Number.isInteger(targetGenerationId) && targetGenerationId > 0) {
      previousResultMutation.mutate(targetGenerationId)
    }
  }

  return (
    <main className="app-shell">
      <header className="app-header">
        <h1>AI 애니메이션 생성기</h1>
        <div className="header-actions">
          <label className="sr-only" htmlFor="result-lookup-id">
            결과 조회 generationId
          </label>
          <input
            className="result-lookup-input"
            id="result-lookup-id"
            inputMode="numeric"
            min="1"
            onChange={(event) => setResultLookupId(event.target.value)}
            placeholder="generationId"
            type="number"
            value={resultLookupId}
          />
          <button
            className="history-button"
            disabled={
              previousResultMutation.isPending ||
              !Number.isInteger(Number(resultLookupId)) ||
              Number(resultLookupId) <= 0
            }
            onClick={handleFetchResultById}
            type="button"
          >
            결과 조회
          </button>
          <button
            className="history-button"
            onClick={() => setHistoryOpen(true)}
            type="button"
          >
            <span aria-hidden="true">↻</span>
            생성 기록
          </button>
        </div>
      </header>

      <div className="app-grid">
        <div className="left-column">
          <PromptForm
            isSubmitting={
              createMutation.isPending ||
              (!!activeStatus && !isTerminalStatus(activeStatus.status))
            }
            onPromptChange={setPrompt}
            onSubmit={handleCreate}
            prompt={prompt}
          />
          <ProgressPanel
            errorMessage={displayError}
            isCreating={createMutation.isPending}
            result={activeResult}
            status={activeStatus}
          />
        </div>

        <div className="right-column">
          <SceneResult
            result={activeResult}
          />
          <DetailInfo
            onOpenVideo={setVideoModalUrl}
            result={activeResult}
          />
        </div>
      </div>

      {apiError && <div className="toast-error">{apiError}</div>}

      <HistoryDrawer
        error={(historyQuery.error as ApiError | null) ?? null}
        history={historyQuery.data}
        isLoading={historyQuery.isLoading || historyQuery.isFetching}
        isOpen={historyOpen}
        onClose={() => setHistoryOpen(false)}
        onPageChange={setHistoryPage}
        onSelect={handleSelectHistoryRecord}
        onSortChange={handleHistorySortChange}
        onStatusFilterChange={handleHistoryStatusFilterChange}
        page={historyPage}
        selectedGenerationId={generationId}
        sort={historySort}
        statusFilter={historyStatusFilter}
      />

      {videoModalUrl && (
        <div
          className="video-modal-backdrop"
          onClick={() => setVideoModalUrl(null)}
          role="presentation"
        >
          <div
            className="video-modal"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-label="컷 비디오 재생"
          >
            <button
              aria-label="비디오 닫기"
              className="video-modal-close"
              onClick={() => setVideoModalUrl(null)}
              type="button"
            >
              ×
            </button>
            <video controls src={videoModalUrl} />
          </div>
        </div>
      )}
    </main>
  )
}

export default GenerationPage
