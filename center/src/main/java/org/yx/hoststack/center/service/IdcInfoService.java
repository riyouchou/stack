package org.yx.hoststack.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.yx.hoststack.center.common.req.idc.IdcCreateReq;
import org.yx.hoststack.center.common.req.idc.IdcListReq;
import org.yx.hoststack.center.common.req.idc.IdcUpdateReq;
import org.yx.hoststack.center.common.resp.idc.CreateIdcInfoResp;
import org.yx.hoststack.center.common.resp.idc.IdcListResp;
import org.yx.hoststack.center.entity.IdcInfo;

import java.util.List;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
public interface IdcInfoService extends IService<IdcInfo> {

    List<IdcListResp> list(IdcListReq idcListReq);

    CreateIdcInfoResp create(IdcCreateReq idcCreateReq);

    boolean update(IdcUpdateReq idcUpdateReq);


}