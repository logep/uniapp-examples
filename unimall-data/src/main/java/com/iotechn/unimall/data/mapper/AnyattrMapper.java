package com.iotechn.unimall.data.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.iotechn.unimall.data.domain.AnyattrDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnyattrMapper extends BaseMapper<AnyattrDO > {

    public List<AnyattrDO> getList(@Param("id") String id, @Param("type")String type, @Param("aid")String aid, @Param("attr16")String attr16);
    public Integer updAttr(AnyattrDO anyattrDOS);
    public Integer insertAttr(AnyattrDO anyattrDOS);
}
