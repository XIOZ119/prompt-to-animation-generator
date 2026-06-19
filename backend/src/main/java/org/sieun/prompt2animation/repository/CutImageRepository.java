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

    @Query("SELECT ci.cut.scene.generation.id, ci.imageUrl FROM CutImage ci WHERE ci.cut.scene.generation.id IN :generationIds AND ci.status = org.sieun.prompt2animation.domain.GenerationStatus.COMPLETED ORDER BY ci.cut.cutOrder ASC, ci.id DESC")
    List<Object[]> findThumbnailsByGenerationIds(@Param("generationIds") List<Long> generationIds);

    @Query("SELECT ci FROM CutImage ci WHERE ci.cut IN :cuts ORDER BY ci.id DESC")
    List<CutImage> findByCutIn(@Param("cuts") List<Cut> cuts);

    @Query("SELECT ci FROM CutImage ci WHERE ci.cut IN :cuts AND ci.status = :status ORDER BY ci.id DESC")
    List<CutImage> findByCutInAndStatus(@Param("cuts") List<Cut> cuts, @Param("status") GenerationStatus status);
}
