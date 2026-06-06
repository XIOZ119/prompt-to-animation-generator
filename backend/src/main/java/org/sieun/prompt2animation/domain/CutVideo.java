package org.sieun.prompt2animation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CutVideo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cut_id", nullable = false)
    private Cut cut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cut_image_id", nullable = false)
    private CutImage cutImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    private String videoUrl;

    private String errorMessage;

    public static CutVideo create(Cut cut, CutImage cutImage) {
        CutVideo cutVideo = new CutVideo();
        cutVideo.cut = cut;
        cutVideo.cutImage = cutImage;
        cutVideo.status = GenerationStatus.PENDING;
        return cutVideo;
    }

    public void markProcessing() {
        this.status = GenerationStatus.PROCESSING;
    }

    public void markCompleted(String videoUrl) {
        this.status = GenerationStatus.COMPLETED;
        this.videoUrl = videoUrl;
    }

    public void markFailed(String errorMessage) {
        this.status = GenerationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
