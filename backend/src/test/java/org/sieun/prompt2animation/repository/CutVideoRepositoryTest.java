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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class CutVideoRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired CutVideoRepository cutVideoRepository;

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
    void findFirstByCutAndStatusOrderByIdDesc_returnsLatestCompleted() {
        CutImage img = em.persist(TestFixtures.completedCutImage(cut1, "http://img.jpg"));
        em.persist(TestFixtures.completedCutVideo(cut1, img, "http://old.mp4"));
        CutVideo newer = em.persist(TestFixtures.completedCutVideo(cut1, img, "http://new.mp4"));
        em.flush();

        Optional<CutVideo> result = cutVideoRepository.findFirstByCutAndStatusOrderByIdDesc(
                cut1, GenerationStatus.COMPLETED);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(newer.getId());
        assertThat(result.get().getVideoUrl()).isEqualTo("http://new.mp4");
    }

    @Test
    void findFirstByCutOrderByIdDesc_returnsLatestRegardlessOfStatus() {
        CutImage img = em.persist(TestFixtures.completedCutImage(cut1, "http://img.jpg"));
        em.persist(TestFixtures.completedCutVideo(cut1, img, "http://completed.mp4"));
        CutVideo latest = em.persist(TestFixtures.failedCutVideo(cut1));
        em.flush();

        Optional<CutVideo> result = cutVideoRepository.findFirstByCutOrderByIdDesc(cut1);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(latest.getId());
        assertThat(result.get().getStatus()).isEqualTo(GenerationStatus.FAILED);
    }

    @Test
    void countByCut_SceneAndStatus_countsCorrectly() {
        CutImage img1 = em.persist(TestFixtures.completedCutImage(cut1, "http://img1.jpg"));
        CutImage img2 = em.persist(TestFixtures.completedCutImage(cut2, "http://img2.jpg"));
        em.persist(TestFixtures.completedCutVideo(cut1, img1, "http://vid1.mp4"));
        em.persist(TestFixtures.completedCutVideo(cut2, img2, "http://vid2.mp4"));
        em.persist(TestFixtures.failedCutVideo(cut1));
        em.flush();

        long completedCount = cutVideoRepository.countByCut_SceneAndStatus(scene, GenerationStatus.COMPLETED);

        assertThat(completedCount).isEqualTo(2);
    }

    @Test
    void countByCut_SceneAndStatusIn_sumsMultipleStatuses() {
        CutImage img1 = em.persist(TestFixtures.completedCutImage(cut1, "http://img1.jpg"));
        CutImage img2 = em.persist(TestFixtures.completedCutImage(cut2, "http://img2.jpg"));
        em.persist(TestFixtures.completedCutVideo(cut1, img1, "http://vid1.mp4"));
        em.persist(TestFixtures.completedCutVideo(cut2, img2, "http://vid2.mp4"));
        em.persist(TestFixtures.failedCutVideo(cut1));
        em.flush();

        long count = cutVideoRepository.countByCut_SceneAndStatusIn(
                scene, List.of(GenerationStatus.COMPLETED, GenerationStatus.FAILED));

        assertThat(count).isEqualTo(3);
    }
}
