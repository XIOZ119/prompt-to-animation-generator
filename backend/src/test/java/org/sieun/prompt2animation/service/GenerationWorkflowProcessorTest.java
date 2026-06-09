package org.sieun.prompt2animation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sieun.prompt2animation.client.kie.KieClient;
import org.sieun.prompt2animation.client.openai.OpenAiClient;
import org.sieun.prompt2animation.client.openai.dto.CutGenerationResult;
import org.sieun.prompt2animation.client.openai.dto.SceneGenerationResult;
import org.sieun.prompt2animation.domain.*;
import org.sieun.prompt2animation.fixture.TestFixtures;
import org.sieun.prompt2animation.repository.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerationWorkflowProcessorTest {

    @Mock GenerationRepository generationRepository;
    @Mock SceneRepository sceneRepository;
    @Mock CutRepository cutRepository;
    @Mock CutImageRepository cutImageRepository;
    @Mock CutVideoRepository cutVideoRepository;
    @Mock OpenAiClient openAiClient;
    @Mock KieClient kieClient;
    @Mock VideoMergeService videoMergeService;

    @InjectMocks GenerationWorkflowProcessor processor;

    private Generation generation;

    @BeforeEach
    void setUp() {
        generation = TestFixtures.processingGeneration();
        ReflectionTestUtils.setField(generation, "id", 1L);
        when(generationRepository.findById(1L)).thenReturn(Optional.of(generation));
    }

    @Nested
    class GenerateScene {

        @Test
        void durationSec5_staysAt5() {
            SceneGenerationResult sceneResult = new SceneGenerationResult(
                    "제목", "시나리오", "anime",
                    List.of(new CutGenerationResult(1, "img", "vid", 5))
            );
            when(openAiClient.generateScene(any())).thenReturn(sceneResult);
            when(sceneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Cut> cutCaptor = ArgumentCaptor.forClass(Cut.class);
            when(cutRepository.save(cutCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            processor.generateScene(1L);

            assertThat(cutCaptor.getValue().getDurationSec()).isEqualTo(5);
        }

        @Test
        void durationSec7_clampedTo5() {
            SceneGenerationResult sceneResult = new SceneGenerationResult(
                    "제목", "시나리오", "anime",
                    List.of(new CutGenerationResult(1, "img", "vid", 7))
            );
            when(openAiClient.generateScene(any())).thenReturn(sceneResult);
            when(sceneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Cut> cutCaptor = ArgumentCaptor.forClass(Cut.class);
            when(cutRepository.save(cutCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            processor.generateScene(1L);

            assertThat(cutCaptor.getValue().getDurationSec()).isEqualTo(5);
        }

        @Test
        void durationSec8_clampedTo10() {
            SceneGenerationResult sceneResult = new SceneGenerationResult(
                    "제목", "시나리오", "anime",
                    List.of(new CutGenerationResult(1, "img", "vid", 8))
            );
            when(openAiClient.generateScene(any())).thenReturn(sceneResult);
            when(sceneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Cut> cutCaptor = ArgumentCaptor.forClass(Cut.class);
            when(cutRepository.save(cutCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            processor.generateScene(1L);

            assertThat(cutCaptor.getValue().getDurationSec()).isEqualTo(10);
        }

        @Test
        void durationSec10_staysAt10() {
            SceneGenerationResult sceneResult = new SceneGenerationResult(
                    "제목", "시나리오", "anime",
                    List.of(new CutGenerationResult(1, "img", "vid", 10))
            );
            when(openAiClient.generateScene(any())).thenReturn(sceneResult);
            when(sceneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Cut> cutCaptor = ArgumentCaptor.forClass(Cut.class);
            when(cutRepository.save(cutCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            processor.generateScene(1L);

            assertThat(cutCaptor.getValue().getDurationSec()).isEqualTo(10);
        }

        @Test
        void styleIsPrependedToImageAndVideoPrompts() {
            SceneGenerationResult sceneResult = new SceneGenerationResult(
                    "제목", "시나리오", "anime",
                    List.of(new CutGenerationResult(1, "cat", "cat running", 5))
            );
            when(openAiClient.generateScene(any())).thenReturn(sceneResult);
            when(sceneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Cut> cutCaptor = ArgumentCaptor.forClass(Cut.class);
            when(cutRepository.save(cutCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

            processor.generateScene(1L);

            Cut savedCut = cutCaptor.getValue();
            assertThat(savedCut.getImagePrompt()).isEqualTo("anime, cat");
            assertThat(savedCut.getVideoPrompt()).isEqualTo("anime, cat running");
        }
    }

    @Nested
    class GenerateCutImages {

        private Scene scene;
        private Cut cut1;
        private Cut cut2;

        @BeforeEach
        void setUp() {
            scene = TestFixtures.sceneFor(generation);
            cut1 = TestFixtures.cutFor(scene, 1, 5);
            cut2 = TestFixtures.cutFor(scene, 2, 5);
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
        }

        @Test
        void allSucceed_createCompletedCutImages() throws Exception {
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1));
            when(kieClient.generateImage(anyString())).thenReturn("http://image.url");

            processor.generateCutImages(1L);

            ArgumentCaptor<CutImage> captor = ArgumentCaptor.forClass(CutImage.class);
            verify(cutImageRepository, times(2)).save(captor.capture());

            CutImage finalState = captor.getValue(); // 마지막으로 저장된 상태
            assertThat(finalState.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
            assertThat(finalState.getImageUrl()).isEqualTo("http://image.url");
        }

        @Test
        void kieClientThrows_createsCutImageWithFailedStatus() {
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1));
            when(kieClient.generateImage(anyString())).thenThrow(new RuntimeException("API 오류"));

            processor.generateCutImages(1L);

            ArgumentCaptor<CutImage> captor = ArgumentCaptor.forClass(CutImage.class);
            verify(cutImageRepository, times(2)).save(captor.capture());

            CutImage finalState = captor.getValue();
            assertThat(finalState.getStatus()).isEqualTo(GenerationStatus.FAILED);
            assertThat(finalState.getErrorMessage()).isEqualTo("API 오류");
        }

        @Test
        void kieClientThrows_continuesProcessingRemainingCuts() {
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1, cut2));
            when(kieClient.generateImage(cut1.getImagePrompt())).thenThrow(new RuntimeException("첫 번째 실패"));
            when(kieClient.generateImage(cut2.getImagePrompt())).thenReturn("http://image2.url");

            processor.generateCutImages(1L);

            // cut1: 2번 저장 (PROCESSING → FAILED), cut2: 2번 저장 (PROCESSING → COMPLETED)
            verify(cutImageRepository, times(4)).save(any(CutImage.class));
        }
    }

    @Nested
    class GenerateCutVideos {

        private Scene scene;
        private Cut cut1;
        private Cut cut2;

        @BeforeEach
        void setUp() {
            scene = TestFixtures.sceneFor(generation);
            cut1 = TestFixtures.cutFor(scene, 1, 5);
            cut2 = TestFixtures.cutFor(scene, 2, 5);
            when(sceneRepository.findByGeneration(generation)).thenReturn(Optional.of(scene));
        }

        @Test
        void allCutsHaveCompletedImages_createsCompletedCutVideos() {
            CutImage img1 = TestFixtures.completedCutImage(cut1, "http://img1.jpg");
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut1, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(img1));
            when(kieClient.generateVideo(anyString(), anyString(), anyInt())).thenReturn("http://video.mp4");

            processor.generateCutVideos(1L);

            ArgumentCaptor<CutVideo> captor = ArgumentCaptor.forClass(CutVideo.class);
            verify(cutVideoRepository, times(2)).save(captor.capture());

            CutVideo finalState = captor.getValue();
            assertThat(finalState.getStatus()).isEqualTo(GenerationStatus.COMPLETED);
            assertThat(finalState.getVideoUrl()).isEqualTo("http://video.mp4");
        }

        @Test
        void cutHasNoCompletedImage_createsFailedCutVideoAndContinues() {
            CutImage img2 = TestFixtures.completedCutImage(cut2, "http://img2.jpg");
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1, cut2));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut1, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.empty()); // cut1 이미지 없음
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut2, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(img2));
            when(kieClient.generateVideo(anyString(), anyString(), anyInt())).thenReturn("http://video2.mp4");

            processor.generateCutVideos(1L);

            // cut1: FAILED 1번 저장, cut2: PROCESSING + COMPLETED 2번 저장
            verify(cutVideoRepository, times(3)).save(any(CutVideo.class));
            // cut1 때문에 kieClient는 1번만 호출됨
            verify(kieClient, times(1)).generateVideo(anyString(), anyString(), anyInt());
        }

        @Test
        void kieClientThrows_createsCutVideoWithFailedStatus() {
            CutImage img1 = TestFixtures.completedCutImage(cut1, "http://img1.jpg");
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cut1));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cut1, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(img1));
            when(kieClient.generateVideo(anyString(), anyString(), anyInt()))
                    .thenThrow(new RuntimeException("비디오 API 오류"));

            processor.generateCutVideos(1L);

            ArgumentCaptor<CutVideo> captor = ArgumentCaptor.forClass(CutVideo.class);
            verify(cutVideoRepository, times(2)).save(captor.capture());

            CutVideo finalState = captor.getValue();
            assertThat(finalState.getStatus()).isEqualTo(GenerationStatus.FAILED);
        }

        @Test
        void passesCorrectDurationSecToKieClient() {
            Cut cutWith10Sec = TestFixtures.cutFor(scene, 1, 10);
            CutImage img = TestFixtures.completedCutImage(cutWith10Sec, "http://img.jpg");
            when(cutRepository.findBySceneOrderByCutOrderAsc(scene)).thenReturn(List.of(cutWith10Sec));
            when(cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(cutWith10Sec, GenerationStatus.COMPLETED))
                    .thenReturn(Optional.of(img));
            when(kieClient.generateVideo(anyString(), anyString(), eq(10))).thenReturn("http://video.mp4");

            processor.generateCutVideos(1L);

            verify(kieClient).generateVideo(anyString(), anyString(), eq(10));
        }
    }
}
