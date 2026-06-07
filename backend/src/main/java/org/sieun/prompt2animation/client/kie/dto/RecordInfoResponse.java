package org.sieun.prompt2animation.client.kie.dto;

public record RecordInfoResponse(
        Integer code,
        String msg,
        RecordData data
) {
    public record RecordData(
            String taskId,
            String state,
            String resultJson,
            String failMsg
    ) {}
}
