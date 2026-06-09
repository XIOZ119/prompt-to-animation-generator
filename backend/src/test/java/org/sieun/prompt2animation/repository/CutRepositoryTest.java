package org.sieun.prompt2animation.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sieun.prompt2animation.config.JpaConfig;
import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.Scene;
import org.sieun.prompt2animation.fixture.TestFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class CutRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired CutRepository cutRepository;

    private Generation generation;
    private Scene scene;

    @BeforeEach
    void setUp() {
        generation = em.persist(TestFixtures.pendingGeneration());
        scene = em.persist(TestFixtures.sceneFor(generation));
        em.flush();
    }

    @Test
    void sumDurationSecByGenerationId_sumsAllCutDurations() {
        em.persist(TestFixtures.cutFor(scene, 1, 5));
        em.persist(TestFixtures.cutFor(scene, 2, 5));
        em.persist(TestFixtures.cutFor(scene, 3, 10));
        em.flush();

        Integer total = cutRepository.sumDurationSecByGenerationId(generation.getId());

        assertThat(total).isEqualTo(20);
    }

    @Test
    void sumDurationSecByGenerationId_noCuts_returnsZeroNotNull() {
        // COALESCE(..., 0) 검증 — 컷이 없을 때 null 대신 0 반환
        Integer total = cutRepository.sumDurationSecByGenerationId(generation.getId());

        assertThat(total).isNotNull();
        assertThat(total).isEqualTo(0);
    }

    @Test
    void findBySceneOrderByCutOrderAsc_returnsInCutOrderAscending() {
        em.persist(TestFixtures.cutFor(scene, 3, 5));
        em.persist(TestFixtures.cutFor(scene, 1, 5));
        em.persist(TestFixtures.cutFor(scene, 2, 5));
        em.flush();

        List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);

        assertThat(cuts).extracting(Cut::getCutOrder).containsExactly(1, 2, 3);
    }

    @Test
    void findBySceneOrderByCutOrderAsc_doesNotReturnOtherSceneCuts() {
        Generation otherGeneration = em.persist(TestFixtures.pendingGeneration());
        Scene otherScene = em.persist(TestFixtures.sceneFor(otherGeneration));

        em.persist(TestFixtures.cutFor(scene, 1, 5));
        em.persist(TestFixtures.cutFor(scene, 2, 5));
        em.persist(TestFixtures.cutFor(otherScene, 1, 5));
        em.flush();

        List<Cut> cuts = cutRepository.findBySceneOrderByCutOrderAsc(scene);

        assertThat(cuts).hasSize(2);
        assertThat(cuts).allMatch(c -> c.getScene().getId().equals(scene.getId()));
    }
}
