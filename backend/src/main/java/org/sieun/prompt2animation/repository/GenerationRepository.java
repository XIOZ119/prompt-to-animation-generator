package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Generation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationRepository extends JpaRepository<Generation, Long> {
}
