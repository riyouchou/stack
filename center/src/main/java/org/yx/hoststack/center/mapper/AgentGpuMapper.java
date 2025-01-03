package org.yx.hoststack.center.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.yx.hoststack.center.entity.AgentGpu;
import org.apache.ibatis.annotations.Mapper;
/**
 * HOST/CONTAINER-AGENT GPU信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 *
 */
@Mapper
public interface AgentGpuMapper extends BaseMapper<AgentGpu> {

}