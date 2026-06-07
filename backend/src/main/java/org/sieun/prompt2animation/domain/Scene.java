package org.sieun.prompt2animation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_id", nullable = false, unique = true)
    private Generation generation;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String scenario;

    public static Scene create(Generation generation, String title, String scenario) {
        Scene scene = new Scene();
        scene.generation = generation;
        scene.title = title;
        scene.scenario = scenario;
        return scene;
    }
}
