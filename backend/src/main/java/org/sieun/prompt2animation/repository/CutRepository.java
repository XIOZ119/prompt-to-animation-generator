package org.sieun.prompt2animation.repository;

import org.sieun.prompt2animation.domain.Cut;
import org.sieun.prompt2animation.domain.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CutRepository extends JpaRepository<Cut, Long> {

    List<Cut> findBySceneOrderByCutOrderAsc(Scene scene);
}
