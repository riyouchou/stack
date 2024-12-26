package org.yx.hoststack.edge.queue.consumers;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.queue.message.HostHeartMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class HostHbConsumer implements Runnable {

    private final MessageQueues messageQueues;

    @Override
    public void run() {
        while (true) {
            try {
                List<HostHeartMessage> batchHostHb = Lists.newArrayList();
                messageQueues.getHostHbQueue().drainTo(batchHostHb, 50);
                if (!batchHostHb.isEmpty()) {
                    EdgeClientConnector.getInstance().hostHb(batchHostHb);
                } else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WorkQueueConsumer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.WorkQueueConsumer_ConsumerHostHb)
                        .e(e);
            }
        }
    }
}