package org.sieun.prompt2animation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CutImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cut_id", nullable = false)
    private Cut cut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    private String imageUrl;

    private String errorMessage;

    public static CutImage create(Cut cut) {
        CutImage cutImage = new CutImage();
        cutImage.cut = cut;
        cutImage.status = GenerationStatus.PENDING;
        return cutImage;
    }

    public void markProcessing() {
        this.status = GenerationStatus.PROCESSING;
    }

    public void markCompleted(String imageUrl) {
        this.status = GenerationStatus.COMPLETED;
        this.imageUrl = imageUrl;
    }

    public void markFailed(String errorMessage) {
        this.status = GenerationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
