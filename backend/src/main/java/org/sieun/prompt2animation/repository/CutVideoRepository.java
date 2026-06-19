package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.CutVideo;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CutVideoRepository extends JpaRepository<CutVideo, Long> {

    Optional<CutVideo> findFirstByCutAndStatusOrderByIdDesc(Cut cut, GenerationStatus status);

    Optional<CutVideo> findFirstByCutOrderByIdDesc(Cut cut);

    long countByCut_SceneAndStatus(Scene scene, GenerationStatus status);

    long countByCut_SceneAndStatusIn(Scene scene, List<GenerationStatus> statuses);

    @Query("SELECT cv FROM CutVideo cv WHERE cv.cut IN :cuts ORDER BY cv.id DESC")
    List<CutVideo> findByCutIn(@Param("cuts") List<Cut> cuts);

    @Query("SELECT cv FROM CutVideo cv WHERE cv.cut IN :cuts AND cv.status = :status ORDER BY cv.id DESC")
    List<CutVideo> findByCutInAndStatus(@Param("cuts") List<Cut> cuts, @Param("status") GenerationStatus status);
}
