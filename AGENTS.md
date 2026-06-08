# Frontend Agent Guide

## Project

Prompt-to-Animation Generator frontend.

The frontend receives a user prompt, creates a generation request, polls the backend for progress, and displays the final generated scene, cuts, images, videos, and merged result video.

## Stack

- React
- TypeScript
- Vite
- TanStack Query
- axios

## Environment

- Use VITE_API_BASE_URL for the backend base URL.
- Do not hardcode http://localhost:8080 inside API functions.

Example:

    VITE_API_BASE_URL=http://localhost:8080

## Folder Structure

text src/ ├─ api/ │ └─ generationApi.ts ├─ types/ │ └─ generation.ts ├─ pages/ │ └─ GenerationPage.tsx ├─ components/ │ ├─ PromptForm.tsx │ ├─ ProgressPanel.tsx │ ├─ SceneResult.tsx │ └─ CutResultList.tsx ├─ App.tsx ├─ main.tsx └─ App.css

## Folder Rules

- API request logic goes in src/api.
- Shared TypeScript types go in src/types.
- Page-level components go in src/pages.
- Reusable UI components go in src/components.
- Keep components simple for MVP.
- Avoid unnecessary global state.
- Prefer props over context unless state sharing becomes complex.

## API Response Format

All backend responses use this wrapper:

ts export interface ApiResponse<T> { success: boolean; message: string; errorCode: string | null; data: T; }

## API Flow

### 1. Create Generation

http POST /api/generations

Request Body

json { "userPrompt": "..." }

Expected Response Data

ts { generationId: number; status: "PENDING"; }

### 2. Poll Generation Status

http GET /api/generations/{generationId}

Rules:

- Poll every 3 seconds.
- Stop polling when status is COMPLETED or FAILED.

Expected Status Values

ts type GenerationStatus = | "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";

Expected Current Step Values

ts type CurrentStep = | "SCENE_GENERATION" | "CUT_IMAGE_GENERATION" | "CUT_VIDEO_GENERATION" | "VIDEO_MERGE";

### 3. Fetch Result

http GET /api/generations/{generationId}/result

Rules:

- Call only when status becomes COMPLETED.
- Display scene title, scenario, cuts, image URLs, video URLs, and final result URL.

## React Query Rules

### Create Generation

Use useMutation.

### Poll Status

Use useQuery.

Rules:

- refetchInterval: 3000
- Poll only while status is not COMPLETED and not FAILED.
- Use enabled when generationId exists.

### Fetch Result

Use useQuery.

Rules:

- Fetch only when status is COMPLETED.
- Use enabled to prevent unnecessary requests.

## Type Definitions

ts export interface ApiResponse<T> { success: boolean; message: string; errorCode: string | null; data: T; } export interface CreateGenerationResponse { generationId: number; status: string; } export interface GenerationStatusResponse { generationId: number; status: string; currentStep: string; progress: number; errorMessage?: string; } export interface Scene { title: string; scenario: string; } export interface Cut { cutOrder: number; imagePrompt: string; videoPrompt: string; imageUrl?: string; videoUrl?: string; } export interface GenerationResultResponse { resultUrl: string; scene: Scene; cuts: Cut[]; }

## UI Priority

1. Prompt input
2. Generate button
3. Loading / status display
4. Progress percentage
5. Current step display
6. Error message display
7. Scene title and scenario
8. Cut image display
9. Cut video display
10. Final merged result video display

## Prompt Input Rules

- Prompt is required.
- Prompt maximum length is 500 characters.
- Show current character count.
- Disable submit button when:
  - prompt is empty
  - prompt length exceeds 500
  - request is in progress

## Styling Rules

- Keep CSS simple.
- Prioritize functionality over visual polish.
- Match the provided reference image layout when available.
- Do not over-engineer responsive design for MVP.
- Use clear sections and readable spacing.

## Error Handling

Display backend error messages when available.

Common Errors:

- PROMPT_REQUIRED
- PROMPT_TOO_LONG
- INVALID_GENERATION_ID
- GENERATION_NOT_FOUND
- GENERATION_NOT_COMPLETED
- TOO_MANY_REQUESTS
- GENERATION_CREATE_FAILED
- GENERATION_STATUS_FETCH_FAILED
- GENERATION_RESULT_FETCH_FAILED

## Implementation Priority

1. Complete API integration.
2. Implement polling correctly.
3. Display generation progress.
4. Display final result.
5. Match the reference UI.
6. Refactor components if needed.

## Do Not

- Do not hardcode mock API responses.
- Do not ignore the ApiResponse wrapper.
- Do not call result API before status is COMPLETED.
- Do not add Redux, Zustand, Recoil, or other state libraries.
- Do not add unnecessary dependencies.
- Do not over-engineer the MVP.
