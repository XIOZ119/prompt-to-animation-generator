package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.CutVideo;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CutVideoRepository extends JpaRepository<CutVideo, Long> {

    Optional<CutVideo> findFirstByCutAndStatusOrderByIdDesc(Cut cut, GenerationStatus status);

    long countByCut_SceneAndStatus(Scene scene, GenerationStatus status);
}
