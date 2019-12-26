package com.iotechn.unimall.launcher.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.iotechn.unimall.core.Const;
import com.iotechn.unimall.core.annotation.HttpMethod;
import com.iotechn.unimall.core.annotation.HttpParam;
import com.iotechn.unimall.core.annotation.HttpParamType;
import com.iotechn.unimall.core.annotation.ResultType;
import com.iotechn.unimall.core.annotation.param.NotNull;
import com.iotechn.unimall.core.annotation.param.Range;
import com.iotechn.unimall.core.annotation.param.TextFormat;
import com.iotechn.unimall.core.exception.AdminServiceException;
import com.iotechn.unimall.core.exception.ExceptionDefinition;
import com.iotechn.unimall.core.exception.ServiceException;
import com.iotechn.unimall.core.util.MD5Util;
import com.iotechn.unimall.data.domain.AdminDO;
import com.iotechn.unimall.data.domain.RoleDO;
import com.iotechn.unimall.data.domain.RolePermissionDO;
import com.iotechn.unimall.data.dto.AdminDTO;
import com.iotechn.unimall.data.dto.UserDTO;
import com.iotechn.unimall.data.enums.RoleStatusType;
import com.iotechn.unimall.data.mapper.AdminMapper;
import com.iotechn.unimall.data.mapper.RoleMapper;
import com.iotechn.unimall.data.mapper.RolePermissionMapper;
import com.iotechn.unimall.data.util.SessionUtil;
import com.iotechn.unimall.launcher.exception.LauncherExceptionDefinition;
import com.iotechn.unimall.launcher.exception.LauncherServiceException;
import com.iotechn.unimall.launcher.manager.ApiManager;
import com.iotechn.unimall.launcher.model.GatewayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2018-08-08
 * Time: 下午11:00
 */
@Controller
@RequestMapping("/m.api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RoleMapper roleMapper;

    /**
     * rolePermissionMapper
     */
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private StringRedisTemplate userRedisTemplate;

    @Value("${com.iotechn.unimall.env}")
    private String ENV;

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String invoke(HttpServletRequest req, HttpServletResponse res) {
        long invokeTime = System.currentTimeMillis();
        try {
            logger.info("[用户请求] request=" + JSONObject.toJSONString(req.getParameterMap()));
            Object obj = process(req, res, invokeTime);
            if(Const.IGNORE_PARAM_LIST.contains(obj.getClass())){
                return obj.toString();
            }
            String result = JSONObject.toJSONString(obj);
            long during = System.currentTimeMillis() - invokeTime;
//            if (during > 1000) {
//                logger.info("[用户请求] 慢接口");
//            }
            logger.info("[用户请求] 用时 " + during + "ms, response=" + JSONObject.toJSONString(result));
            return result;
        } catch (ServiceException e) {
            GatewayResponse gatewayResponse = new GatewayResponse();
            gatewayResponse.setTimestamp(invokeTime);
            gatewayResponse.setErrno(e.getCode());
            gatewayResponse.setErrmsg(e.getMessage());
            String result = JSONObject.toJSONString(gatewayResponse);
            long during = System.currentTimeMillis() - invokeTime;
            logger.info("[用户请求] 用时 " + during + "ms, response=" + JSONObject.toJSONString(result));
            return result;
        }
    }


    private Object process(HttpServletRequest request, HttpServletResponse response, long invokeTime) throws ServiceException {
        try {
            ApiManager apiManager = applicationContext.getBean(ApiManager.class);
            Map<String, String[]> parameterMap = request.getParameterMap();
            String[] gps = parameterMap.get("_gp");
            String[] mts = parameterMap.get("_mt");
            if(gps == null  || mts == null || gps.length == 0 || mts.length == 0){
                throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_API_NOT_EXISTS);
            }
            String _gp = gps[0];
            String _mt = mts[0];
            String[] _types = parameterMap.get("_type");
            String _type = null;
            if (_types != null && _types.length > 0) {
                _type = _types[0];
            }
            Method method = apiManager.getMethod(_gp, _mt);
            String[] accessTokenGb = parameterMap.get("admintoken");
            if(accessTokenGb == null  || accessTokenGb.length == 0 ){
                accessTokenGb=new String[1] ;
            }else{

                String admin = userRedisTemplate.opsForValue().get(Const.ADMIN_REDIS_PREFIX + accessTokenGb);
                if (StringUtils.isEmpty(admin)) {
                    List<AdminDO> adminDOS = adminMapper.selectList(
                            new EntityWrapper<AdminDO>()
                                    .eq("username", "guest"));
                    if (CollectionUtils.isEmpty(adminDOS)) {
                        throw new AdminServiceException(ExceptionDefinition.ADMIN_NOT_EXIST);
                    }
                    AdminDO adminDO = adminDOS.get(0);

                    List<Long> ids = JSONObject.parseArray(adminDO.getRoleIds(), Long.class);
                    if (CollectionUtils.isEmpty(ids)) {
                        throw new AdminServiceException(ExceptionDefinition.ADMIN_ROLE_IS_EMPTY);
                    }
                    List<RoleDO> roleDOList = roleMapper.selectList(
                            new EntityWrapper<RoleDO>()
                                    .in("id", ids)
                                    .eq("status", RoleStatusType.ACTIVE.getCode()));
                    List<String> roleNames = new LinkedList<>();
                    roleDOList.forEach(item -> {
                        roleNames.add(item.getName());
                    });
                    AdminDTO adminDTO = new AdminDTO();
                    adminDTO.setRoles(roleNames);
                    BeanUtils.copyProperties(adminDO, adminDTO);
                    adminDTO.setRoleIds(JSONObject.parseArray(adminDO.getRoleIds(), Long.class));
                    adminDTO.setPassword(null);
                    List<RolePermissionDO> rolePermissionDOList = rolePermissionMapper.selectList(
                            new EntityWrapper<RolePermissionDO>()
                                    .in("role_id", ids)
                                    .eq("deleted", 0));
                    List<String> permissionNames = new LinkedList<>();
                    rolePermissionDOList.forEach(item -> {
                        permissionNames.add(item.getPermission());
                    });
                    adminDTO.setPerms(permissionNames);

                    userRedisTemplate.opsForValue().set(Const.ADMIN_REDIS_PREFIX + accessTokenGb[0], JSONObject.toJSONString(adminDTO), 10, TimeUnit.MINUTES);
                }
            }
            if (method == null) {
                throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_API_NOT_EXISTS);
            }
            HttpMethod httpMethod = method.getAnnotation(HttpMethod.class);
            if (httpMethod == null) {
                //只起标记作用防止调到封闭方法了
                throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_API_NOT_EXISTS);
            }





            String permission = httpMethod.permission();
            //                    String permission = httpMethod.permission();  在方法头里声明 如果此方法需要权限则需要判断  并在此方法体内做逻辑处理

            //
            if (!StringUtils.isEmpty(permission)) {
                //若需要权限，则校验当前用户是否具有权限
                String accessToken = request.getHeader(Const.ADMIN_ACCESS_TOKEN)!=null? request.getHeader(Const.ADMIN_ACCESS_TOKEN) : accessTokenGb[0];
                String admin = userRedisTemplate.opsForValue().get(Const.ADMIN_REDIS_PREFIX + accessToken);
                // 暂时注释

                // 针对此菜单  不做登陆检验

                if(_gp== "goods" &&  _mt=="getGoodsPage") {

                }else{
                    if (StringUtils.isEmpty(admin)) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_ADMIN_NOT_LOGIN);
                    }




                    AdminDTO adminDTO = JSONObject.parseObject(admin, AdminDTO.class);
                    SessionUtil.setAdmin(adminDTO);
                    // 防止提交频繁
                    String key = request.getRequestURI();
                    key += "_" + adminDTO.getUsername();
                    String count = userRedisTemplate.opsForValue().get(Const.ACCESS_LIMIT_REACHED + key);
                    if(count  == null) {
                        userRedisTemplate.opsForValue().set(Const.ACCESS_LIMIT_REACHED + key, "1", 5, TimeUnit.SECONDS);
                    }else if(Integer.parseInt(count) < 5) {
                        Integer c=Integer.parseInt(count);
                        userRedisTemplate.opsForValue().set(Const.ACCESS_LIMIT_REACHED + key, ++c+"", 5, TimeUnit.SECONDS);
                    }else {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_ACCESS_LIMIT_REACHED);
                    }
                    if (!SessionUtil.hasPerm(permission)) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_ADMIN_PERMISSION_DENY);
                    }

                }

            }


            Object serviceBean = applicationContext.getBean(method.getDeclaringClass());
            Parameter[] methodParameters = method.getParameters();
            Object[] args = new Object[methodParameters.length];
            for (int i = 0; i < methodParameters.length; i++) {
                Parameter methodParam = methodParameters[i];
                HttpParam httpParam = methodParam.getAnnotation(HttpParam.class);
                if (httpParam == null) {
                    throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_API_NOT_EXISTS);
                }
                if (httpParam.type() == HttpParamType.COMMON) {
                    String[] paramArray = parameterMap.get(httpParam.name());
                    if (paramArray != null && paramArray.length > 0 && !StringUtils.isEmpty(paramArray[0])) {
                        Class<?> type = methodParam.getType();
                        //参数校验
                        checkParam(type, methodParam, paramArray[0]);
                        if (String.class == type) {
                            args[i] = paramArray[0];
                        } else if (Const.IGNORE_PARAM_LIST.contains(type)) {
                            Constructor<?> constructor = type.getConstructor(String.class);
                            args[i] = constructor.newInstance(paramArray[0]);
                        } else if (type.isArray()) {
                            //若是数组
                            Class<?> itemType = type.getComponentType();
                            Object realType[] = (Object[]) Array.newInstance(itemType,paramArray.length);
                            if(paramArray.length > 0){
                                for(int j = 0; j < paramArray.length; j++) {
                                    if(Const.IGNORE_PARAM_LIST.contains(itemType)){
                                        Constructor<?> constructor = itemType.getConstructor(String.class);
                                        realType[j] = constructor.newInstance(paramArray[j]);
                                    }else {
                                        realType[j] = JSONObject.parseObject(paramArray[j], itemType);
                                    }
                                }
                            }
                            args[i] = realType;
                        } else {
                            //Json解析
                            args[i] = JSONObject.parseObject(paramArray[0], type);
                        }
                    } else {
                        if (!StringUtils.isEmpty(httpParam.valueDef())) {
                            //若有默认值
                            Class<?> type = methodParam.getType();
                            Constructor<?> constructor = type.getConstructor(String.class);
                            args[i] = constructor.newInstance(httpParam.valueDef());
                        } else {
                            if (methodParam.getAnnotation(NotNull.class) != null) {
                                logger.error("missing :" + httpParam.name());
                                throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                            }
                            args[i] = null;
                        }
                    }
                } else if (httpParam.type() == HttpParamType.USER_ID) {
//                    String accessToken = request.getHeader(Const.USER_ACCESS_TOKEN);
                    String accessToken = request.getHeader(Const.USER_ACCESS_TOKEN) !=null? request.getHeader(Const.USER_ACCESS_TOKEN) : accessTokenGb[0];
                    if (!StringUtils.isEmpty(accessToken)) {
                        String userJson = userRedisTemplate.opsForValue().get(Const.USER_REDIS_PREFIX + accessToken);
                        if (!StringUtils.isEmpty(userJson)) {
                            UserDTO userDTO = JSONObject.parseObject(userJson, UserDTO.class);
                            SessionUtil.setUser(userDTO);
                            args[i] = userDTO.getId();
                            userRedisTemplate.expire(Const.USER_REDIS_PREFIX + accessToken, 30, TimeUnit.MINUTES);
                            continue;
                        }
                    }
                    if (args[i] == null && methodParam.getAnnotation(NotNull.class) != null) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_USER_NOT_LOGIN);
                    }
                } else if (httpParam.type() == HttpParamType.ADMIN_ID) {
                    // 暂时注释
//                    String accessToken = request.getHeader(Const.ADMIN_ACCESS_TOKEN);
                    String accessToken = request.getHeader(Const.ADMIN_ACCESS_TOKEN) !=null? request.getHeader(Const.ADMIN_ACCESS_TOKEN) : accessTokenGb[0];
                    if (!StringUtils.isEmpty(accessToken)) {
                        String userJson = userRedisTemplate.opsForValue().get(Const.ADMIN_REDIS_PREFIX + accessToken);
                        if (!StringUtils.isEmpty(userJson)) {
                            AdminDTO adminDTO = JSONObject.parseObject(userJson, AdminDTO.class);
                            SessionUtil.setAdmin(adminDTO);
                            args[i] = adminDTO.getId();
                            userRedisTemplate.expire(Const.ADMIN_REDIS_PREFIX + accessToken, 30, TimeUnit.MINUTES);
                            continue;
                        }
                    }
                    if (args[i] == null && methodParam.getAnnotation(NotNull.class) != null) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_ADMIN_NOT_LOGIN);
                    }
                } else if (httpParam.type() == HttpParamType.IP) {
                    //这里根据实际情况来定。 若使用了负载均衡，Ip将会被代理服务器设置到某个Header里面
                    if (ENV.equals("1")) {
                        //若是开发环境
                        args[i] = "27.10.60.71";
                    } else {
                        args[i] = request.getHeader("X-Forwarded-For");
                    }
                } else if (httpParam.type() == HttpParamType.HEADER) {
                    String header = request.getHeader(httpParam.name());
                    args[i] = header;
                    if (header == null && methodParam.getAnnotation(NotNull.class) != null) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                    }
                }
            }

            Object invokeObj = method.invoke(serviceBean, args);
            ResultType resultType = httpMethod.type();
            if (!StringUtils.isEmpty(_type) && "raw".equals(_type)) {
                //如果是不用包装的直接返回
                return invokeObj;
            }
            //下面是需要包装返回的
            if (resultType == ResultType.COOKIE) {
                //加入Cookie时处理
                if (StringUtils.isEmpty(httpMethod.retName())) {
                    throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_API_NOT_EXISTS);
                } else {
                    //setCookie
                    Cookie cookie = new Cookie(httpMethod.retName(), (String) invokeObj);
                    cookie.setPath("/");
                    if (httpMethod.maxAge() != -1) {
                        cookie.setMaxAge(httpMethod.maxAge());
                    }
                    response.addCookie(cookie);
                }
            }
            GatewayResponse gatewayResponse = new GatewayResponse();
            gatewayResponse.setErrno(200);
            gatewayResponse.setErrmsg("成功");
            gatewayResponse.setTimestamp(invokeTime);
            gatewayResponse.setData(invokeObj);
            return gatewayResponse;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException proxy = (InvocationTargetException) e;
                Throwable targetException = proxy.getTargetException();
                if (targetException instanceof ServiceException) {
                    throw (ServiceException) targetException;
                }
            }
            logger.error("[网关] 系统未知异常", e);
            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_UNKNOWN_EXCEPTION);
        }
    }


    private void checkParam(Class<?> type, Parameter methodParam, String target) throws ServiceException {
        if (type == String.class) {
            TextFormat textFormat = methodParam.getAnnotation(TextFormat.class);
            if (textFormat != null) {
                String regex = textFormat.regex();
                if (!StringUtils.isEmpty(regex)) {
                    //如果正则生效，则直接使用正则校验
                    if (!target.matches(regex)) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                    }
                } else {
                    boolean notChinese = textFormat.notChinese();
                    if (notChinese) {
                        if (target.matches("[\\u4e00-\\u9fa5]+")) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }

                    String[] contains = textFormat.contains();
                    for (int j = 0; j < contains.length; j++) {
                        if (!target.contains(contains[j])) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }

                    String[] notContains = textFormat.notContains();
                    for (int j = 0; j < notContains.length; j++) {
                        if (target.contains(notContains[j])) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }

                    String startWith = textFormat.startWith();
                    if (!StringUtils.isEmpty(startWith)) {
                        if (!target.startsWith(startWith)) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }

                    String endsWith = textFormat.endsWith();
                    if (!StringUtils.isEmpty(target)) {
                        if (!target.endsWith(endsWith)) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }
                    int targetLength = target.length();
                    int length = textFormat.length();
                    if (length != -1) {
                        if (targetLength != length) {
                            throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                        }
                    }

                    if (targetLength < textFormat.lengthMin()) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                    }

                    if (targetLength > textFormat.lengthMax()) {
                        throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                    }
                }
            }
        } else if (type == Integer.class) {
            Range range = methodParam.getAnnotation(Range.class);
            Integer integer = new Integer(target);
            if (range != null) {
                if (integer > range.max() || integer < range.min()) {
                    throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                }
            }
        } else if (type == Long.class) {
            Range range = methodParam.getAnnotation(Range.class);
            if (range != null) {
                Long integer = new Long(target);
                if (integer > range.max() || integer < range.min()) {
                    throw new LauncherServiceException(LauncherExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
                }
            }
        }
    }
}
