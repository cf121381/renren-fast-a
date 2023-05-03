/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.common.aspect;

import java.lang.reflect.Method;
import java.util.Date;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import io.renren.common.annotation.SysLog;
import io.renren.common.annotation.WxSysLog;
import io.renren.common.constants.LogFromType;
import io.renren.common.utils.HttpContextUtils;
import io.renren.common.utils.IPUtils;
import io.renren.modules.app.wx.MiniprogramHelper;
import io.renren.modules.app.wx.UserVo;
import io.renren.modules.sys.entity.SysLogEntity;
import io.renren.modules.sys.service.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;


/**
 * 系统日志，切面处理类
 *
 * @author Mark sunlightcs@gmail.com
 */
@Aspect
@Component
public class WxSysLogAspect {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Resource
	private SysLogService sysLogService;

	@Resource
	private MiniprogramHelper miniprogramHelper;

	@Pointcut("@annotation(io.renren.common.annotation.WxSysLog)")
	public void logPointCut() {

	}

	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		long beginTime = System.currentTimeMillis();
		//执行方法
		Object result = point.proceed();
		//执行时长(毫秒)
		long time = System.currentTimeMillis() - beginTime;

		//保存日志
		saveSysLog(point, time);

		return result;
	}

	private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		SysLogEntity sysLog = new SysLogEntity();
		WxSysLog wxSysLog = method.getAnnotation(WxSysLog.class);
		if (wxSysLog != null) {
			//注解上的描述
			sysLog.setOperation(wxSysLog.value());
		}
		sysLog.setFromType(LogFromType.WX);

		//请求的方法名
		String className = joinPoint.getTarget().getClass().getName();
		String methodName = signature.getName();
		sysLog.setMethod(className + "." + methodName + "()");

		//请求的参数
		Object[] args = joinPoint.getArgs();
		try {
			String params = new Gson().toJson(args);
			sysLog.setParams(params);
		}
		catch (Exception e) {

		}

		//获取request
		HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
		//设置IP地址
		sysLog.setIp(IPUtils.getIpAddr(request));

		String openId = request.getParameter("openId");
		logger.info("openId:{}", openId);
		UserVo userVo = miniprogramHelper.getUserInfo(openId);
		logger.info("user vo:{}", JSONUtil.toJsonStr(userVo));

		//用户名
		sysLog.setUsername(userVo.getPhone());

		sysLog.setTime(time);
		sysLog.setCreateDate(new Date());
		//保存系统日志
		sysLogService.save(sysLog);
	}
}
