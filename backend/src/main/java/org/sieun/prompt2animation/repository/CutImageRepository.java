package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.CutImage;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CutImageRepository extends JpaRepository<CutImage, Long> {

    Optional<CutImage> findFirstByCutAndStatusOrderByIdDesc(Cut cut, GenerationStatus status);

    Optional<CutImage> findFirstByCutOrderByIdDesc(Cut cut);

    long countByCut_SceneAndStatus(Scene scene, GenerationStatus status);

    long countByCut_SceneAndStatusIn(Scene scene, List<GenerationStatus> statuses);

    @Query("SELECT ci.imageUrl FROM CutImage ci WHERE ci.cut.scene.generation.id = :generationId AND ci.status = org.sieun.prompt2animation.domain.GenerationStatus.COMPLETED ORDER BY ci.cut.cutOrder ASC, ci.id DESC")
    List<String> findThumbnailsByGenerationId(@Param("generationId") Long generationId, Pageable pageable);
}
