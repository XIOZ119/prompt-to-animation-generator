export type GenerationStatus =
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "TIMEOUT";

export type CurrentStep =
  | "SCENE_GENERATION"
  | "CUT_IMAGE_GENERATION"
  | "CUT_VIDEO_GENERATION"
  | "VIDEO_MERGE"
  | "COMPLETED"
  | "FAILED";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  errorCode: string | null;
  data: T;
}

export interface ApiError {
  message: string;
  errorCode: string | null;
}

export interface CreateGenerationResponse {
  generationId: number;
  status: GenerationStatus;
  createdAt?: string;
}

export interface GenerationStatusResponse {
  generationId: number;
  status: GenerationStatus;
  currentStep: CurrentStep | string;
  currentStepMessage?: string;
  progress: number;
  completedStepCount?: number;
  totalStepCount?: number;
  errorMessage?: string | null;
  cuts?: GenerationStatusCut[];
}

export interface GenerationStatusCut {
  cutOrder: number;
  imageStatus: GenerationStatus | null;
  videoStatus: GenerationStatus | null;
}

export interface Scene {
  title: string;
  scenario: string;
}

export interface Cut {
  cutOrder: number;
  durationSec?: number;
  imagePrompt: string;
  videoPrompt: string;
  imageUrl?: string;
  videoUrl?: string;
}

export interface GenerationResultResponse {
  generationId: number;
  resultUrl: string;
  scene: Scene;
  cuts: Cut[];
}

export type GenerationHistoryStatusFilter =
  | "ALL"
  | "PENDING"
  | "PROCESSING"
  | "COMPLETED"
  | "FAILED"
  | "TIMEOUT";

export type GenerationHistorySort = "latest" | "oldest";

export interface GenerationHistoryResponse {
  generationId: number;
  title: string | null;
  thumbnailUrl: string | null;
  status: GenerationStatus;
  durationSec: number | null;
  createdAt: string;
  errorMessage: string | null;
}

export interface GenerationHistoryPageResponse {
  content: GenerationHistoryResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface GenerationHistoryQueryParams {
  status?: GenerationHistoryStatusFilter;
  page?: number;
  size?: number;
  sort?: GenerationHistorySort;
}

export interface GenerationHistoryItem {
  generationId: number;
  prompt: string;
  status: GenerationStatus;
  createdAt?: string;
  result?: GenerationResultResponse;
  statusDetail?: GenerationStatusResponse;
}
