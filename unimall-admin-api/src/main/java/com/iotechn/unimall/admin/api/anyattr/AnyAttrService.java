package com.iotechn.unimall.admin.api.anyattr;


import com.iotechn.unimall.core.annotation.HttpMethod;
import com.iotechn.unimall.core.annotation.HttpOpenApi;
import com.iotechn.unimall.core.annotation.HttpParam;
import com.iotechn.unimall.core.annotation.HttpParamType;
import com.iotechn.unimall.core.annotation.param.NotNull;
import com.iotechn.unimall.core.exception.ServiceException;
import com.iotechn.unimall.data.domain.AnyattrDO;
import com.iotechn.unimall.data.dto.goods.AnyAttrDTO;

import java.util.List;

@HttpOpenApi(group = "admin.anyattr", description = "任意调用服务接口")
public interface AnyAttrService {

    @HttpMethod(description = "获取任意列表")
    public List<AnyattrDO> getList(
            @HttpParam(name = "id", type = HttpParamType.COMMON, description = "任意ID") String id,
            @HttpParam(name = "type", type = HttpParamType.COMMON, description = "任意数据type") String type,
            @HttpParam(name = "aid", type = HttpParamType.COMMON, description = "任意数据aid") String aid,
            @HttpParam(name = "attr16", type = HttpParamType.COMMON, description = "任意数据attr16") String attr16

            ) throws ServiceException;

    @HttpMethod(description = "根据ID删除数据")
    public String delById(
            @NotNull @HttpParam(name = "aid", type = HttpParamType.COMMON, description = "任意ID") String aid) throws ServiceException;

    @HttpMethod(description = "插入数据")
    public String insertObj(
            @NotNull @HttpParam(name = "anyAttrDTO", type = HttpParamType.COMMON, description = "任意JSON数据") AnyAttrDTO anyAttrDTO) throws ServiceException;

    @HttpMethod(description = "更新数据")
    public String updateObj(
            @NotNull @HttpParam(name = "anyAttrDTO", type = HttpParamType.COMMON, description = "任意JSON数据") AnyAttrDTO anyAttrDTO) throws ServiceException;

}

