package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.module.UpgradeModuleCmdData;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;

@Service("module")
public class ModuleJob extends BaseJob implements IJob {
    public ModuleJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                     CenterService centerService, ServerCacheInfoServiceBiz serverCacheInfoServiceBiz,
                     TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, centerService, serverCacheInfoServiceBiz, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case UPGRADE -> upgrade(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            default -> "";
        };
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        return null;
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeModuleCmdData upgradeModuleCmdData = (UpgradeModuleCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeModuleData", JSON.toJSONString(upgradeModuleCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ModuleTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : upgradeModuleCmdData.getHostIds()) {
            targetList.add(JobParams.ModuleTarget.newBuilder()
                    .setHostId(hostId)
                    .setJobDetailId(jobId + StringPool.DASH + hostId)
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", hostId)
                    .fluentPut("moduleName", upgradeModuleCmdData.getModuleName())
                    .fluentPut("version", upgradeModuleCmdData.getVersion())
                    .fluentPut("downloadUrl", upgradeModuleCmdData.getDownloadUrl())
                    .fluentPut("md5", upgradeModuleCmdData.getMd5());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JobParams.ModuleUpgrade jobParams = JobParams.ModuleUpgrade.newBuilder()
                .setModuleName(upgradeModuleCmdData.getModuleName())
                .setVersion(upgradeModuleCmdData.getVersion())
                .setDownloadUrl(upgradeModuleCmdData.getDownloadUrl())
                .setMd5(upgradeModuleCmdData.getMd5())
                .addAllTarget(targetList)
                .build();
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, null, "", jobDetailList);
                kvLogger.i();
                sendJobToAgent(jobCmd, upgradeModuleCmdData.getHostIds(), jobParams.getDownloadUrlBytes());
                return jobId;
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                        .e(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, null, "", jobDetailList);
            kvLogger.i();
            sendJobToAgent(jobCmd, upgradeModuleCmdData.getHostIds(), jobParams.toByteString());
            return jobId;
        }
    }
}
