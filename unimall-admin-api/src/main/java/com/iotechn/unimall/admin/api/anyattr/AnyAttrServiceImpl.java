package com.iotechn.unimall.admin.api.anyattr;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.iotechn.unimall.core.exception.ServiceException;
import com.iotechn.unimall.data.domain.AnyattrDO;
import com.iotechn.unimall.data.dto.goods.AnyAttrDTO;
import com.iotechn.unimall.data.mapper.AnyattrMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnyAttrServiceImpl implements  AnyAttrService {
    private static final Logger logger = LoggerFactory.getLogger(AnyAttrServiceImpl.class);
    @Autowired
    private AnyattrMapper anyattrMapper;
    @Override
    public List<AnyattrDO> getList(Long id, String type) throws ServiceException {
        List<AnyattrDO> categoryDOS = anyattrMapper.getList(id,type);
        return categoryDOS;
    }
    @Override
    public String delById(String aid) throws ServiceException {
        anyattrMapper.delete(
                new EntityWrapper<AnyattrDO>()
                        .eq("aid", aid));
        return "ok";
    }
    @Override
    public String updateObj(AnyAttrDTO anyAttrDTO) throws ServiceException {
//        List<ImgDO> imgDOList = imgList.stream().map(item -> {
//            ImgDO imgDO = new ImgDO();
//            imgDO.setBizType(BizType.GOODS.getCode());
//            imgDO.setBizId(bizId);
//            imgDO.setUrl(item);
//            imgDO.setGmtCreate(now);
//            imgDO.setGmtUpdate(now);
//            return imgDO;
//        }).collect(Collectors.toList());

        AnyattrDO anyDO = new AnyattrDO();
        BeanUtils.copyProperties(anyAttrDTO, anyDO);
        anyattrMapper.updAttr(anyDO);
        return"'ok'";
    }
    @Override
    public String insertObj(AnyAttrDTO anyAttrDTO) throws ServiceException {
//        List<ImgDO> imgDOList = imgList.stream().map(item -> {
//            ImgDO imgDO = new ImgDO();
//            imgDO.setBizType(BizType.GOODS.getCode());
//            imgDO.setBizId(bizId);
//            imgDO.setUrl(item);
//            imgDO.setGmtCreate(now);
//            imgDO.setGmtUpdate(now);
//            return imgDO;
//        }).collect(Collectors.toList());

        AnyattrDO anyDO = new AnyattrDO();
        BeanUtils.copyProperties(anyAttrDTO, anyDO);
        anyDO.setAid(UUID.randomUUID().toString());
        logger.info("[获得ID] 值："+anyDO.getAid());
        anyattrMapper.insertAttr(anyDO);
        return"'ok'";
    }
}
