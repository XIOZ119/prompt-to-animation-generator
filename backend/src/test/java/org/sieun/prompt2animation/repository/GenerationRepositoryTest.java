package org.sieun.prompt2animation.repository;

import org.junit.jupiter.api.Test;
import org.sieun.prompt2animation.config.JpaConfig;
import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.fixture.TestFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class GenerationRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired GenerationRepository generationRepository;

    @Test
    void countByStatusIn_pendingAndProcessing_excludesCompleted() {
        em.persist(TestFixtures.pendingGeneration());
        em.persist(TestFixtures.processingGeneration());
        em.persist(TestFixtures.completedGeneration("http://result.mp4"));
        em.flush();

        long count = generationRepository.countByStatusIn(
                List.of(GenerationStatus.PENDING, GenerationStatus.PROCESSING));

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByStatusIn_noMatches_returnsZero() {
        em.persist(TestFixtures.completedGeneration("http://result.mp4"));
        em.flush();

        long count = generationRepository.countByStatusIn(
                List.of(GenerationStatus.PENDING, GenerationStatus.PROCESSING));

        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByStatus_returnsOnlyMatchingEntries() {
        em.persist(TestFixtures.pendingGeneration());
        em.persist(TestFixtures.pendingGeneration());
        em.persist(TestFixtures.completedGeneration("http://result.mp4"));
        em.flush();

        Page<Generation> result = generationRepository.findByStatus(
                GenerationStatus.PENDING, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).allMatch(g -> g.getStatus() == GenerationStatus.PENDING);
    }

    @Test
    void findByStatus_withPagination_respectsPageAndSize() {
        for (int i = 0; i < 5; i++) {
            em.persist(TestFixtures.pendingGeneration());
        }
        em.flush();

        Page<Generation> page1 = generationRepository.findByStatus(
                GenerationStatus.PENDING, PageRequest.of(0, 2, Sort.by("id").ascending()));
        Page<Generation> page2 = generationRepository.findByStatus(
                GenerationStatus.PENDING, PageRequest.of(1, 2, Sort.by("id").ascending()));

        assertThat(page1.getContent()).hasSize(2);
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(5);
    }
}
