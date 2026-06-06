package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Generation;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SceneRepository extends JpaRepository<Scene, Long> {

    Optional<Scene> findByGeneration(Generation generation);
}
