package com.iotechn.unimall.app.api.appraise;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.iotechn.unimall.biz.service.appriaise.AppraiseBizService;
import com.iotechn.unimall.core.exception.ExceptionDefinition;
import com.iotechn.unimall.core.exception.AppServiceException;
import com.iotechn.unimall.core.exception.ServiceException;
import com.iotechn.unimall.data.component.CacheComponent;
import com.iotechn.unimall.data.domain.AppraiseDO;
import com.iotechn.unimall.data.domain.ImgDO;
import com.iotechn.unimall.data.domain.OrderDO;
import com.iotechn.unimall.data.domain.OrderSkuDO;
import com.iotechn.unimall.data.dto.appraise.AppraiseRequestItemDTO;
import com.iotechn.unimall.data.dto.appraise.AppraiseRequestDTO;
import com.iotechn.unimall.data.dto.appraise.AppraiseResponseDTO;
import com.iotechn.unimall.data.enums.BizType;
import com.iotechn.unimall.data.enums.OrderStatusType;
import com.iotechn.unimall.data.mapper.AppraiseMapper;
import com.iotechn.unimall.data.mapper.ImgMapper;
import com.iotechn.unimall.data.mapper.OrderMapper;
import com.iotechn.unimall.data.mapper.OrderSkuMapper;
import com.iotechn.unimall.data.model.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/*
@author kbq
@date  2019/7/6 - 11:08
*/
@Service
public class AppraiseServiceImpl implements AppraiseService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ImgMapper imgMapper;
    @Autowired
    private AppraiseMapper appraiseMapper;
    @Autowired
    private OrderSkuMapper orderSkuMapper;
    @Autowired
    private CacheComponent cacheComponent;
    @Autowired
    private AppraiseBizService appraiseBizService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addAppraise(AppraiseRequestDTO appraiseRequestDTO, Long userId) throws ServiceException {
        if(appraiseRequestDTO.getOrderId() == null){
            throw new AppServiceException(ExceptionDefinition.APPRAISE_PARAM_CHECK_FAILED);
        }
        //校验是否有对应等待评价的订单
        Integer integer = orderMapper.selectCount(new EntityWrapper<OrderDO>()
                .eq("status", OrderStatusType.WAIT_APPRAISE.getCode())
                .eq("id", appraiseRequestDTO.getOrderId())
                .eq("user_id", userId));
        if(integer == 0){
            throw new AppServiceException(ExceptionDefinition.APPRAISE_ORDER_CHECK_FAILED);
        }

        //如果传入评价list中没有数据，就直接转变订单状态发出
        Date now = new Date();
        if(appraiseRequestDTO.getAppraiseDTOList() == null || appraiseRequestDTO.getAppraiseDTOList().size() == 0){
            OrderDO orderDO =  new OrderDO();
            orderDO.setStatus(OrderStatusType.COMPLETE.getCode());
            orderDO.setId(appraiseRequestDTO.getOrderId());
            orderDO.setGmtUpdate(now);
            orderMapper.updateById(orderDO);
        }

        //循环读取订单评价中所有商品的评价
        for (AppraiseRequestItemDTO appraiseDTO : appraiseRequestDTO.getAppraiseDTOList()){
            List<OrderSkuDO> orderSkuDOList = orderSkuMapper.selectList(new EntityWrapper<OrderSkuDO>()
                    .eq("sku_id",appraiseDTO.getSkuId())
                    .eq("order_id",appraiseRequestDTO.getOrderId()));
            //从order_sku表中 验证是否有对应的表单和商品
            if(CollectionUtils.isEmpty(orderSkuDOList)){
                throw new AppServiceException(ExceptionDefinition.APPRAISE_PARAM_CHECK_FAILED);
            }

            AppraiseDO appraiseDO = new AppraiseDO();
            BeanUtils.copyProperties(appraiseDTO,appraiseDO);
            appraiseDO.setSpuId(orderSkuDOList.get(0).getSpuId());
            appraiseDO.setId(null); //防止传入id,导致插入数据库出错
            appraiseDO.setOrderId(appraiseRequestDTO.getOrderId()); //从传入数据取出，不使用DTO中的冗余数据
            appraiseDO.setUserId(userId);
            appraiseDO.setGmtCreate(now);
            appraiseDO.setGmtUpdate(appraiseDO.getGmtCreate());
            appraiseMapper.insert(appraiseDO);  //插入该订单该商品评价
            cacheComponent.delPrefixKey(AppraiseBizService.CA_APPRAISE_KEY + appraiseDO.getSpuId()); //删除商品评论缓存
            if(appraiseDTO.getImgUrl() == null || appraiseDTO.getImgUrl().equals("")){
                continue;
            }
            String imgUrlS = appraiseDTO.getImgUrl();
            String[] imgUrlList = imgUrlS.split(",");   //传入图片
            for (String imgurl : imgUrlList){
                ImgDO imgDO = new ImgDO();
                imgDO.setBizType(BizType.COMMENT.getCode());
                imgDO.setBizId(appraiseDO.getId());
                imgDO.setUrl(imgurl);
                imgDO.setGmtCreate(now);
                imgDO.setGmtUpdate(imgDO.getGmtCreate());
                imgMapper.insert(imgDO);
            }
        }

        //改变订单状态
        OrderDO orderDO =  new OrderDO();
        orderDO.setStatus(OrderStatusType.COMPLETE.getCode());
        orderDO.setId(appraiseRequestDTO.getOrderId());
        orderDO.setGmtUpdate(now);
        orderMapper.updateById(orderDO);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteAppraiseById(Long appraiseId, Long userId) throws ServiceException {
        Integer delete = appraiseMapper.delete(new EntityWrapper<AppraiseDO>()
                .eq("id", appraiseId)
                .eq("user_id", userId)); //根据用户Id,评价Id
        if(delete > 0){
            return true;
        }else{
            throw new AppServiceException(ExceptionDefinition.APPRAISE_PARAM_CHECK_FAILED);
        }
    }

    //TODO 去除冗余代码
    @Override
    public Page<AppraiseResponseDTO> getUserAllAppraise(Long userId, Integer pageNo, Integer pageSize) throws ServiceException {
        Integer count = appraiseMapper.selectCount(new EntityWrapper<AppraiseDO>().eq("user_id",userId));
        Integer totalPage = 1;
        if(pageSize <= 0 || pageNo <= 0){
            throw new AppServiceException(ExceptionDefinition.APPRAISE_PARAM_CHECK_FAILED);
        }
        if(count % pageSize == 0 && count != 0){
            totalPage = count / pageSize;
        }else {
            totalPage = count / pageSize + 1;
        }
        if(pageNo >= totalPage){
            pageNo = totalPage;
        }
        Integer offset = pageSize * (pageNo-1);
        List<AppraiseResponseDTO> appraiseResponseDTOS = appraiseMapper.selectUserAllAppraise(userId,offset,pageSize);
        for (AppraiseResponseDTO appraiseResponseDTO : appraiseResponseDTOS){
            appraiseResponseDTO.setImgList(imgMapper.getImgs(BizType.COMMENT.getCode(), appraiseResponseDTO.getId()));
        }
        Page<AppraiseResponseDTO> pageination = new Page<>(appraiseResponseDTOS,pageNo,pageSize,count);
        return pageination;
    }



    @Override
    public Page<AppraiseResponseDTO> getSpuAllAppraise(Long spuId, Integer pageNo, Integer pageSize) throws ServiceException {
        return appraiseBizService.getSpuAllAppraise(spuId, pageNo, pageSize);
    }

    @Override
    public AppraiseResponseDTO getOneById(Long userId, Long appraiseId) throws ServiceException {


        AppraiseResponseDTO appraiseResponseDTO = appraiseMapper.selectOneById(appraiseId);
        if(appraiseResponseDTO == null){
            throw new AppServiceException(ExceptionDefinition.APPRAISE_PARAM_CHECK_FAILED);
        }
        appraiseResponseDTO.setImgList(imgMapper.getImgs(BizType.COMMENT.getCode(), appraiseResponseDTO.getId()));

        return appraiseResponseDTO;
    }


}
