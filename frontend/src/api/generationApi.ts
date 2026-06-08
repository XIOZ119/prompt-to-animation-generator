import axios, { AxiosError } from 'axios'
import type {
  ApiError,
  ApiResponse,
  CreateGenerationResponse,
  GenerationHistoryPageResponse,
  GenerationHistoryQueryParams,
  GenerationResultResponse,
  GenerationStatusResponse,
} from '../types/generation'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

const unwrap = <T>(response: ApiResponse<T>): T => {
  if (!response.success) {
    throw {
      message: response.message || '요청 처리 중 오류가 발생했습니다.',
      errorCode: response.errorCode,
    } satisfies ApiError
  }

  return response.data
}

const toApiError = (error: unknown): ApiError => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResponse<unknown>>
    const body = axiosError.response?.data

    return {
      message:
        body?.message ||
        axiosError.message ||
        '서버와 통신하는 중 오류가 발생했습니다.',
      errorCode: body?.errorCode ?? null,
    }
  }

  if (typeof error === 'object' && error !== null && 'message' in error) {
    return error as ApiError
  }

  return {
    message: '알 수 없는 오류가 발생했습니다.',
    errorCode: null,
  }
}

export const createGeneration = async (
  userPrompt: string,
): Promise<CreateGenerationResponse> => {
  try {
    const response = await apiClient.post<ApiResponse<CreateGenerationResponse>>(
      '/api/generations',
      { userPrompt },
    )

    return unwrap(response.data)
  } catch (error) {
    throw toApiError(error)
  }
}

export const fetchGenerationStatus = async (
  generationId: number,
): Promise<GenerationStatusResponse> => {
  try {
    const response = await apiClient.get<ApiResponse<GenerationStatusResponse>>(
      `/api/generations/${generationId}`,
    )

    return unwrap(response.data)
  } catch (error) {
    throw toApiError(error)
  }
}

export const fetchGenerationResult = async (
  generationId: number,
): Promise<GenerationResultResponse> => {
  try {
    const response = await apiClient.get<ApiResponse<GenerationResultResponse>>(
      `/api/generations/${generationId}/result`,
    )

    return unwrap(response.data)
  } catch (error) {
    throw toApiError(error)
  }
}

export const fetchGenerationHistory = async ({
  status = 'ALL',
  page = 0,
  size = 6,
  sort = 'latest',
}: GenerationHistoryQueryParams = {}): Promise<GenerationHistoryPageResponse> => {
  try {
    const response = await apiClient.get<ApiResponse<GenerationHistoryPageResponse>>(
      '/api/generations',
      {
        params: {
          status,
          page,
          size,
          sort,
        },
      },
    )

    return unwrap(response.data)
  } catch (error) {
    throw toApiError(error)
  }
}
