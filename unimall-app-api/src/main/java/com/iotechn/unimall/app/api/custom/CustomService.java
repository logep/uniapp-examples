package com.iotechn.unimall.app.api.custom;

import com.iotechn.unimall.core.annotation.HttpMethod;
import com.iotechn.unimall.core.annotation.HttpOpenApi;
import com.iotechn.unimall.core.annotation.HttpParam;
import com.iotechn.unimall.core.annotation.HttpParamType;
import com.iotechn.unimall.core.annotation.param.NotNull;
import com.iotechn.unimall.core.exception.ServiceException;
import com.iotechn.unimall.data.domain.OrderDO;
import com.iotechn.unimall.data.model.Page;

@HttpOpenApi(group = "custom", description = "自测服务")
public interface CustomService {
//    permission = "operation:order:list",  加上权限 就是需要登陆才能访问  不加权限可以限制一天访问次数

//    @NotNull @HttpParam(name = "adminId", type = HttpParamType.ADMIN_ID, description = "管理员Id") Long adminId  加上这个必须是admin管理登陆


    @HttpMethod(description = "测试自测接口")
    public String   say() throws ServiceException;
}
