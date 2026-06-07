package org.sieun.prompt2animation.client.kie.dto;

public record TaskCreateResponse(
        Integer code,
        String msg,
        TaskData data
) {
    public record TaskData(String taskId) {}
}
