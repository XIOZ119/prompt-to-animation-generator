package org.sieun.prompt2animation.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sieun.prompt2animation.config.JpaConfig;
import org.sieun.prompt2animation.domain.*;
import org.sieun.prompt2animation.fixture.TestFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class CutImageRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired CutImageRepository cutImageRepository;

    private Generation generation;
    private Scene scene;
    private Cut cut1;
    private Cut cut2;

    @BeforeEach
    void setUp() {
        generation = em.persist(TestFixtures.pendingGeneration());
        scene = em.persist(TestFixtures.sceneFor(generation));
        cut1 = em.persist(TestFixtures.cutFor(scene, 1, 5));
        cut2 = em.persist(TestFixtures.cutFor(scene, 2, 5));
        em.flush();
    }

    @Test
    void findThumbnailsByGenerationId_returnsOnlyCompletedImageUrls() {
        // JPQL 내 enum 리터럴 하드코딩 검증
        em.persist(TestFixtures.completedCutImage(cut1, "http://img1.jpg"));
        em.persist(TestFixtures.failedCutImage(cut1));
        em.flush();

        List<String> thumbnails = cutImageRepository.findThumbnailsByGenerationId(
                generation.getId(), PageRequest.of(0, 10));

        assertThat(thumbnails).containsExactly("http://img1.jpg");
    }

    @Test
    void findThumbnailsByGenerationId_noCompletedImages_returnsEmpty() {
        em.persist(TestFixtures.failedCutImage(cut1));
        em.flush();

        List<String> thumbnails = cutImageRepository.findThumbnailsByGenerationId(
                generation.getId(), PageRequest.of(0, 10));

        assertThat(thumbnails).isEmpty();
    }

    @Test
    void countByCut_SceneAndStatusIn_sumsAcrossMultipleStatuses() {
        em.persist(TestFixtures.completedCutImage(cut1, "http://img1.jpg"));
        em.persist(TestFixtures.completedCutImage(cut2, "http://img2.jpg"));
        em.persist(TestFixtures.failedCutImage(cut1));
        em.persist(TestFixtures.processingCutImage(cut2));
        em.flush();

        long count = cutImageRepository.countByCut_SceneAndStatusIn(
                scene, List.of(GenerationStatus.COMPLETED, GenerationStatus.FAILED));

        assertThat(count).isEqualTo(3); // COMPLETED 2 + FAILED 1
    }

    @Test
    void findFirstByCutAndStatusOrderByIdDesc_returnsLatestInserted() {
        CutImage older = em.persist(TestFixtures.completedCutImage(cut1, "http://old.jpg"));
        CutImage newer = em.persist(TestFixtures.completedCutImage(cut1, "http://new.jpg"));
        em.flush();

        Optional<CutImage> result = cutImageRepository.findFirstByCutAndStatusOrderByIdDesc(
                cut1, GenerationStatus.COMPLETED);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
        assertThat(result.get().getImageUrl()).isEqualTo("http://new.jpg");
    }

    @Test
    void findFirstByCutOrderByIdDesc_returnsLatestRegardlessOfStatus() {
        em.persist(TestFixtures.completedCutImage(cut1, "http://completed.jpg"));
        CutImage latest = em.persist(TestFixtures.failedCutImage(cut1));
        em.flush();

        Optional<CutImage> result = cutImageRepository.findFirstByCutOrderByIdDesc(cut1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(latest.getId());
        assertThat(result.get().getStatus()).isEqualTo(GenerationStatus.FAILED);
    }
}
