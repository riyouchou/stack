package org.yx.hoststack.protocol.ws.agent.jobs.volume;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnmountVolumeJob {
    private String volumeId;
    private String cid;
}