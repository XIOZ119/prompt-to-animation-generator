package org.sieun.prompt2animation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoMergeService {

    @Value("${storage.upload-dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    public String merge(Long generationId, List<String> videoUrls) {
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("merge_" + generationId + "_");
        } catch (IOException e) {
            throw new RuntimeException("임시 디렉토리 생성 실패: " + e.getMessage(), e);
        }

        try {
            List<Path> tempFiles = downloadAll(videoUrls, tempDir);
            Path listFile = writeConcatList(tempDir, tempFiles);

            Path outputFile = Path.of(uploadDir, generationId + ".mp4");
            Files.createDirectories(outputFile.getParent());

            runFFmpeg(listFile, outputFile);

            return baseUrl + "/results/" + generationId + ".mp4";
        } catch (Exception e) {
            throw new RuntimeException("영상 병합 실패: " + e.getMessage(), e);
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private List<Path> downloadAll(List<String> videoUrls, Path tempDir) throws IOException {
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < videoUrls.size(); i++) {
            Path dest = tempDir.resolve("video_" + i + ".mp4");
            try (InputStream in = URI.create(videoUrls.get(i)).toURL().openStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }
            files.add(dest);
        }
        return files;
    }

    private Path writeConcatList(Path tempDir, List<Path> files) throws IOException {
        String content = files.stream()
                .map(p -> "file '" + p.toAbsolutePath() + "'")
                .collect(Collectors.joining("\n"));
        Path listFile = tempDir.resolve("list.txt");
        Files.writeString(listFile, content);
        return listFile;
    }

    private void runFFmpeg(Path listFile, Path outputFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", listFile.toAbsolutePath().toString(),
                "-c", "copy",
                outputFile.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 실패 (exitCode=" + exitCode + "): " + output);
        }
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}
