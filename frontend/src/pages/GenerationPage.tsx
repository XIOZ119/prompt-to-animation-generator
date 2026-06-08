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

const DEFAULT_PROMPT = ''
const LATEST_GENERATION_ID_STORAGE_KEY = 'latestGenerationId'

const isTerminalStatus = (status?: string) =>
  status === 'COMPLETED' || status === 'FAILED' || status === 'TIMEOUT'

const getStoredGenerationId = () => {
  const storedGenerationId = window.localStorage.getItem(
    LATEST_GENERATION_ID_STORAGE_KEY,
  )
  const parsedGenerationId = Number(storedGenerationId)

  return Number.isInteger(parsedGenerationId) && parsedGenerationId > 0
    ? parsedGenerationId
    : null
}

const storeGenerationId = (generationId: number) => {
  window.localStorage.setItem(
    LATEST_GENERATION_ID_STORAGE_KEY,
    String(generationId),
  )
}

function GenerationPage() {
  const [prompt, setPrompt] = useState(DEFAULT_PROMPT)
  const [generationId, setGenerationId] = useState<number | null>(
    getStoredGenerationId,
  )
  const [resultLookupId, setResultLookupId] = useState('')
  const [historyOpen, setHistoryOpen] = useState(false)
  const [historyPage, setHistoryPage] = useState(0)
  const [historySort, setHistorySort] = useState<GenerationHistorySort>('latest')
  const [historyStatusFilter, setHistoryStatusFilter] =
    useState<GenerationHistoryStatusFilter>('ALL')
  const [selectedHistoryRecord, setSelectedHistoryRecord] =
    useState<GenerationHistoryResponse | null>(null)
  const [videoModalUrl, setVideoModalUrl] = useState<string | null>(null)
  const [selectedHistoryResult, setSelectedHistoryResult] =
    useState<GenerationResultResponse>()
  const [selectedHistoryStatus, setSelectedHistoryStatus] =
    useState<GenerationStatusResponse>()

  const createMutation = useMutation({
    mutationFn: createGeneration,
    onSuccess: (data) => {
      setGenerationId(data.generationId)
      storeGenerationId(data.generationId)
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

  const historyDetailQuery = useQuery({
    queryKey: ['generation-history-detail', selectedHistoryRecord?.generationId],
    queryFn: () => fetchGenerationResult(selectedHistoryRecord!.generationId),
    enabled: historyOpen && selectedHistoryRecord !== null,
  })

  const previousResultMutation = useMutation({
    mutationFn: fetchGenerationResult,
    onSuccess: (data, requestedGenerationId) => {
      setGenerationId(requestedGenerationId)
      storeGenerationId(requestedGenerationId)
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
    setSelectedHistoryRecord(history)
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
            onClick={() => {
              setSelectedHistoryRecord(null)
              setHistoryOpen(true)
            }}
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
        detailError={(historyDetailQuery.error as ApiError | null) ?? null}
        detailResult={historyDetailQuery.data}
        detailRecord={selectedHistoryRecord}
        history={historyQuery.data}
        isLoading={historyQuery.isLoading || historyQuery.isFetching}
        isDetailLoading={
          historyDetailQuery.isLoading || historyDetailQuery.isFetching
        }
        isOpen={historyOpen}
        onClose={() => setHistoryOpen(false)}
        onDetailBack={() => setSelectedHistoryRecord(null)}
        onOpenVideo={setVideoModalUrl}
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
