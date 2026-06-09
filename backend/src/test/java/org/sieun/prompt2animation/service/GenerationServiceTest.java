package org.sieun.prompt2animation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sieun.prompt2animation.domain.*;
import org.sieun.prompt2animation.event.GenerationCreatedEvent;
import org.sieun.prompt2animation.dto.response.*;
import org.sieun.prompt2animation.exception.CustomException;
import org.sieun.prompt2animation.exception.ErrorCode;
import org.sieun.prompt2animation.fixture.TestFixtures;
import org.sieun.prompt2animation.repository.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationServiceTest {

    @Mock GenerationRepository generationRepository;
    @Mock SceneRepository sceneRepository;
    @Mock CutRepository cutRepository;
    @Mock CutImageRepository cutImageRepository;
    @Mock CutVideoRepository cutVideoRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks GenerationService generationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generationService, "maxActiveGenerations", 3);
    }

    @Nested
    class CreateGeneration {

        @Test
        void belowRateLimit_savesAndPublishesEvent() {
            when(generationRepository.countByStatusIn(anyList())).thenReturn(2L);
            when(generationRepository.save(any(Generation.class))).thenAnswer(inv -> inv.getArgument(0));

            GenerationResponse response = generationService.createGeneration(TestFixtures.validRequest());

            verify(generationRepository).save(any(Generation.class));
            verify(eventPublisher).publishEvent(any(GenerationCreatedEvent.class));
            assertThat(response.status()).isEqualTo(GenerationStatus.PENDING);
        }

        @Test
        void atExactlyMaxActiveCount_throwsTooManyRequests() {
            when(generationRepository.countByStatusIn(anyList())).thenReturn(3L);

            assertThatThrownBy(() -> generationService.createGeneration(TestFixtures.validRequest()))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.TOO_MANY_REQUESTS);

            verify(generationRepository, never()).save(any());
        }

        @Test
        void repositoryThrows_wrapsAsGenerationCreateFailed() {
            when(generationRepository.countByStatusIn(anyList())).thenReturn(0L);
            when(generationRepository.save(any(Generation.class))).thenThrow(new RuntimeException("DB 오류"));

            assertThatThrownBy(() -> generationService.createGeneration(TestFixtures.validRequest()))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GENERATION_CREATE_FAILED);
        }
    }

    @Nested
    class GetGenerationHistory {

        @Test
        void negativePageNumber_throwsInvalidPageRequest() {
            assertThatThrownBy(() -> generationService.getGenerationHistory("ALL", -1, 10, "latest"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_PAGE_REQUEST);
        }

        @Test
        void zeroSize_throwsInvalidSizeRequest() {
            assertThatThrownBy(() -> generationService.getGenerationHistory("ALL", 0, 0, "latest"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_SIZE_REQUEST);
        }

        @Test
        void unknownStatus_throwsInvalidStatusFilter() {
            assertThatThrownBy(() -> generationService.getGenerationHistory("UNKNOWN", 0, 10, "latest"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_STATUS_FILTER);
        }

        @Test
        void unknownSort_throwsInvalidSortOption() {
            assertThatThrownBy(() -> generationService.getGenerationHistory("ALL", 0, 10, "wrong"))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_SORT_OPTION);
        }

        @Test
        void statusAll_callsFindAll() {
            when(generationRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

            generationService.getGenerationHistory("ALL", 0, 10, "latest");

            verify(generationRepository).findAll(any(Pageable.class));
            verify(generationRepository, never()).findByStatus(any(), any());
        }

        @Test
        void statusCompleted_callsFindByStatus() {
            when(generationRepository.findByStatus(eq(GenerationStatus.COMPLETED), any(Pageable.class)))
                    .thenReturn(Page.empty());

            generationService.getGenerationHistory("COMPLETED", 0, 10, "latest");

            verify(generationRepository).findByStatus(eq(GenerationStatus.COMPLETED), any(Pageable.class));
            verify(generationRepository, never()).findAll(any(Pageable.class));
        }

        @Test
        void oldestSort_usesAscendingSort() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(generationRepository.findAll(pageableCaptor.capture())).thenReturn(Page.empty());

            generationService.getGenerationHistory("ALL", 0, 10, "oldest");

            Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
        }

        @Test
        void latestSort_usesDescendingSort() {
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(generationRepository.findAll(pageableCaptor.capture())).thenReturn(Page.empty());

            generationService.getGenerationHistory("ALL", 0, 10, "latest");

            Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        }
    }

    @Nested
    class GetGenerationStatus {

        @Test
        void negativeId_throwsInvalidGenerationId() {
            assertThatThrownBy(() -> generationService.getGenerationStatus(-1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_GENERATION_ID);
        }

        @Test
        void zeroId_throwsInvalidGenerationId() {
            assertThatThrownBy(() -> generationService.getGenerationStatus(0L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_GENERATION_ID);
        }

        @Test
        void generationNotFound_throwsGenerationNotFound() {
            when(generationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> generationService.getGenerationStatus(99L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GENERATION_NOT_FOUND);
        }

        @Test
        void pendingStatus_returnsZeroProgressAndNullStep() {
            Generation generation = TestFixtures.pendingGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isNull();
            assertThat(response.completedStepCount()).isEqualTo(0);
            assertThat(response.totalStepCount()).isEqualTo(0);
            assertThat(response.progress()).isEqualTo(0);
        }

        @Test
        void processingNoScene_returnsSceneGenerationStep() {
            Generation generation = TestFixtures.processingGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.empty());

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.SCENE_GENERATION.name());
        }

        @Test
        void processingWithSceneNoImages_returnsCutImageGenerationStep() {
            Generation generation = TestFixtures.processingGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(
                    TestFixtures.cutFor(scene, 1, 5),
                    TestFixtures.cutFor(scene, 2, 5),
                    TestFixtures.cutFor(scene, 3, 5),
                    TestFixtures.cutFor(scene, 4, 5)
            );

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutImageRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(0L);
            when(cutVideoRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(0L);
            cuts.forEach(cut -> {
                when(cutImageRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
                when(cutVideoRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
            });

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.CUT_IMAGE_GENERATION.name());
            assertThat(response.currentStepMessage()).contains("컷 1");
        }

        @Test
        void processingImagesPartiallyDone_showsCurrentImageNumber() {
            Generation generation = TestFixtures.processingGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(
                    TestFixtures.cutFor(scene, 1, 5),
                    TestFixtures.cutFor(scene, 2, 5),
                    TestFixtures.cutFor(scene, 3, 5),
                    TestFixtures.cutFor(scene, 4, 5)
            );

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(2L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutImageRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(2L); // 2개 처리됨
            when(cutVideoRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(0L);
            cuts.forEach(cut -> {
                when(cutImageRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
                when(cutVideoRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
            });

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.CUT_IMAGE_GENERATION.name());
            assertThat(response.currentStepMessage()).contains("컷 3"); // processedImages(2) + 1 = 3
        }

        @Test
        void processingAllImagesDone_returnsCutVideoGenerationStep() {
            Generation generation = TestFixtures.processingGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(
                    TestFixtures.cutFor(scene, 1, 5),
                    TestFixtures.cutFor(scene, 2, 5),
                    TestFixtures.cutFor(scene, 3, 5),
                    TestFixtures.cutFor(scene, 4, 5)
            );

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(4L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutImageRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(4L); // 4개 전부 처리됨
            when(cutVideoRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(0L);
            cuts.forEach(cut -> {
                when(cutImageRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
                when(cutVideoRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
            });

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.CUT_VIDEO_GENERATION.name());
        }

        @Test
        void processingAllVideosDone_returnsVideoMergeStep() {
            Generation generation = TestFixtures.processingGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(
                    TestFixtures.cutFor(scene, 1, 5),
                    TestFixtures.cutFor(scene, 2, 5),
                    TestFixtures.cutFor(scene, 3, 5),
                    TestFixtures.cutFor(scene, 4, 5)
            );

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(4L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(4L);
            when(cutImageRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(4L);
            when(cutVideoRepository.countByCut_SceneAndStatusIn(eq(scene), anyList())).thenReturn(4L);
            cuts.forEach(cut -> {
                when(cutImageRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
                when(cutVideoRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
            });

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.VIDEO_MERGE.name());
        }

        @Test
        void completed_returnsCompletedStepAndFullProgress() {
            Generation generation = TestFixtures.completedGeneration("http://result.mp4");
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(
                    TestFixtures.cutFor(scene, 1, 5),
                    TestFixtures.cutFor(scene, 2, 5),
                    TestFixtures.cutFor(scene, 3, 5),
                    TestFixtures.cutFor(scene, 4, 5)
            );

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(4L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(4L);
            cuts.forEach(cut -> {
                when(cutImageRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
                when(cutVideoRepository.findFirstByCutOrderByIdDesc(cut)).thenReturn(Optional.empty());
            });

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.COMPLETED.name());
            // total = 1 + 4 + 4 + 1 = 10, completed = total = 10
            assertThat(response.completedStepCount()).isEqualTo(10);
            assertThat(response.totalStepCount()).isEqualTo(10);
            assertThat(response.progress()).isEqualTo(100);
        }

        @Test
        void failed_returnsFailedStep() {
            Generation generation = TestFixtures.failedGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(TestFixtures.cutFor(scene, 1, 5));

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutImageRepository.findFirstByCutOrderByIdDesc(cuts.get(0))).thenReturn(Optional.empty());
            when(cutVideoRepository.findFirstByCutOrderByIdDesc(cuts.get(0))).thenReturn(Optional.empty());

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.FAILED.name());
        }

        @Test
        void timeout_returnsFailedStepWithoutException() {
            Generation generation = TestFixtures.timeoutGeneration();
            Scene scene = TestFixtures.sceneFor(generation);
            List<Cut> cuts = List.of(TestFixtures.cutFor(scene, 1, 5));

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(cuts);
            when(cutImageRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED)).thenReturn(0L);
            when(cutImageRepository.findFirstByCutOrderByIdDesc(cuts.get(0))).thenReturn(Optional.empty());
            when(cutVideoRepository.findFirstByCutOrderByIdDesc(cuts.get(0))).thenReturn(Optional.empty());

            GenerationStatusResponse response = generationService.getGenerationStatus(1L);

            assertThat(response.currentStep()).isEqualTo(GenerationStep.FAILED.name());
            assertThat(response.status()).isEqualTo(GenerationStatus.TIMEOUT);
        }
    }

    @Nested
    class GetGenerationResult {

        @Test
        void negativeId_throwsInvalidGenerationId() {
            assertThatThrownBy(() -> generationService.getGenerationResult(-1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_GENERATION_ID);
        }

        @Test
        void generationNotFound_throwsGenerationNotFound() {
            when(generationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> generationService.getGenerationResult(99L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GENERATION_NOT_FOUND);
        }

        @Test
        void completedGeneration_returnsResult() {
            Generation generation = TestFixtures.completedGeneration("http://result.mp4");
            Scene scene = TestFixtures.sceneFor(generation);
            Cut cut = TestFixtures.cutFor(scene, 1, 5);
            CutImage img = TestFixtures.completedCutImage(cut, "http://img.jpg");
            CutVideo vid = TestFixtures.completedCutVideo(cut, img, "http://vid.mp4");

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(img));
            when(cutVideoRepository.findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(vid));

            GenerationResultResponse response = generationService.getGenerationResult(1L);

            assertThat(response.resultUrl()).isEqualTo("http://result.mp4");
            assertThat(response.cuts()).hasSize(1);
            assertThat(response.cuts().get(0).imageUrl()).isEqualTo("http://img.jpg");
            assertThat(response.cuts().get(0).videoUrl()).isEqualTo("http://vid.mp4");
        }

        @Test
        void failedGeneration_returnsResult() {
            Generation generation = TestFixtures.failedGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.empty());

            // FAILED도 예외 없이 결과 반환
            GenerationResultResponse response = generationService.getGenerationResult(1L);

            assertThat(response).isNotNull();
            assertThat(response.cuts()).isEmpty();
        }

        @Test
        void timeoutGeneration_returnsResult() {
            Generation generation = TestFixtures.timeoutGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.empty());

            // TIMEOUT도 종료 상태이므로 예외 없이 결과 반환
            GenerationResultResponse response = generationService.getGenerationResult(1L);

            assertThat(response).isNotNull();
            assertThat(response.cuts()).isEmpty();
        }

        @Test
        void pendingGeneration_throwsGenerationNotCompleted() {
            Generation generation = TestFixtures.pendingGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));

            assertThatThrownBy(() -> generationService.getGenerationResult(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GENERATION_NOT_COMPLETED);
        }

        @Test
        void processingGeneration_throwsGenerationNotCompleted() {
            Generation generation = TestFixtures.processingGeneration();
            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));

            assertThatThrownBy(() -> generationService.getGenerationResult(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.GENERATION_NOT_COMPLETED);
        }

        @Test
        void cutWithNoCompletedImage_returnsNullImageUrl() {
            Generation generation = TestFixtures.completedGeneration("http://result.mp4");
            Scene scene = TestFixtures.sceneFor(generation);
            Cut cut = TestFixtures.cutFor(scene, 1, 5);

            when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.empty());
            when(cutVideoRepository.findFirstByCutAndStatusOrderByIdDesc(cut, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.empty());

            GenerationResultResponse response = generationService.getGenerationResult(1L);

            assertThat(response.cuts().get(0).imageUrl()).isNull();
            assertThat(response.cuts().get(0).videoUrl()).isNull();
        }
    }
}
