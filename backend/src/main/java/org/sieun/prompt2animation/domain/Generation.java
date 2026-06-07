package org.sieun.prompt2animation.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Generation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String userPrompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenerationStatus status;

    @Column(columnDefinition = "TEXT")
    private String resultUrl;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime completedAt;

    public static Generation create(String userPrompt) {
        Generation generation = new Generation();
        generation.userPrompt = userPrompt;
        generation.status = GenerationStatus.PENDING;
        return generation;
    }

    public void markProcessing() {
        this.status = GenerationStatus.PROCESSING;
    }

    public void markCompleted(String resultUrl) {
        this.status = GenerationStatus.COMPLETED;
        this.resultUrl = resultUrl;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = GenerationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void markTimeout() {
        this.status = GenerationStatus.TIMEOUT;
        this.completedAt = LocalDateTime.now();
    }
}
