package com.iotechn.unimall.app.api.custom;

import com.iotechn.unimall.core.annotation.HttpMethod;
import com.iotechn.unimall.core.annotation.HttpOpenApi;
import com.iotechn.unimall.core.exception.ServiceException;

@HttpOpenApi(group = "custom", description = "自测服务")
public interface CustomService {


    @HttpMethod(description = "测试自测接口")
    public String   say() throws ServiceException;
}
