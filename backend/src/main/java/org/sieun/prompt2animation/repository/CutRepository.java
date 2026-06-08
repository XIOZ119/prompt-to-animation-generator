package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CutRepository extends JpaRepository<Cut, Long> {

    List<Cut> findBySceneOrderByCutOrderAsc(Scene scene);

    long countByScene(Scene scene);

    @Query("SELECT COALESCE(SUM(c.durationSec), 0) FROM Cut c WHERE c.scene.generation.id = :generationId")
    Integer sumDurationSecByGenerationId(@Param("generationId") Long generationId);
}
