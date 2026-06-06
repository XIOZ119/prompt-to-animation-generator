package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.CutImage;
import org.sieun.prompt2animation.domain.GenerationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CutImageRepository extends JpaRepository<CutImage, Long> {

    Optional<CutImage> findFirstByCutAndStatusOrderByIdDesc(Cut cut, GenerationStatus status);
}
