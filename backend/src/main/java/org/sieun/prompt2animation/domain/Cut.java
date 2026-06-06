package org.sieun.prompt2animation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @Column(nullable = false)
    private Integer cutOrder;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imagePrompt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String videoPrompt;

    @Column(nullable = false)
    private Integer durationSec;

    public static Cut create(Scene scene, Integer cutOrder, String imagePrompt, String videoPrompt, Integer durationSec) {
        Cut cut = new Cut();
        cut.scene = scene;
        cut.cutOrder = cutOrder;
        cut.imagePrompt = imagePrompt;
        cut.videoPrompt = videoPrompt;
        cut.durationSec = durationSec;
        return cut;
    }
}
