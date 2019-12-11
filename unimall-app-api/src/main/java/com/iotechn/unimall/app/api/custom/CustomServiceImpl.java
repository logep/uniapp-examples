package com.iotechn.unimall.app.api.custom;

import com.iotechn.unimall.core.exception.ServiceException;
import org.springframework.stereotype.Service;

@Service
public class CustomServiceImpl  implements  CustomService{
    @Override
    public String say() throws ServiceException {
        return "hello world";
    }
}
