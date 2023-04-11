/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.service.impl;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.renren.common.constants.OrderStatus;
import io.renren.common.exception.RRException;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.app.dao.OrderDetailDao;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.service.OrderDetailService;
import io.renren.modules.app.service.UserService;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.sys.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Service;


@Slf4j
@Service("orderDetailService")
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailDao, OrderDetailEntity> implements OrderDetailService {
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	@Resource
	private UserService userService;

	@Resource
	private SysUserService sysUserService;

	@Resource
	private SysUserRoleService sysUserRoleService;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
//		String username = (String)params.get("username");
//		Long createUserId = (Long)params.get("createUserId");
		String customerName = (String) params.get("customerName");
		Integer status = Objects.nonNull(params.get("status")) ? Integer.parseInt((String) params.get("status")) : null;
		String customerManager = (String) params.get("customerManager");
		String orderNo = (String) params.get("orderNo");
		String openId = (String) params.get("openId");
		if (StringUtils.isBlank(openId)) {
			throw new RRException("用户未登录");
		}
		UserEntity user = userService.queryByOpenId(openId);
		if (Objects.isNull(user) || StringUtils.isBlank(user.getMobile())) {
			return new PageUtils(Lists.newArrayList(), 0, 10, 1);
		}
		String phone = user.getMobile();
		List<Long> roleList = getRoleIdListByPhone(phone);
		if (CollectionUtils.isEmpty(roleList)) {
			return new PageUtils(Lists.newArrayList(), 0, 10, 1);
		}
//		1,客户管理员
//		2,代理商
//		3,客户
		//date start,end
		IPage<OrderDetailEntity> page = this.page(
				new Query<OrderDetailEntity>().getPage(params),
				new QueryWrapper<OrderDetailEntity>()
						.like(StringUtils.isNotBlank(customerName), "customer_name", customerName)
						.like(StringUtils.isNotBlank(customerManager), "customer_manager", customerManager)
						.like(StringUtils.isNotBlank(orderNo), "order_no", orderNo)
						.eq(status != null && status > 0, "status", status)
						//客户管理员
						.eq(roleList.contains(1L), "customer_manager_phone", phone)
						.eq(roleList.contains(2L), "agent_phone", phone)
						.eq(roleList.contains(3L), "book_user_phone", phone)

		);

		List<OrderVo> orderVoList = JSON.parseArray(JSONUtil.toJsonStr(page.getRecords()), OrderVo.class);
		Page<OrderVo> voIPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		voIPage.setRecords(orderVoList);
		orderVoList.forEach(orderVo -> {
			orderVo.setStatusStr(OrderStatus.of(orderVo.getStatus()).getName());
			if (Objects.nonNull(orderVo.getBookTime())) {
				orderVo.setBookDateStr(formatter.format(orderVo.getBookTime()));
			}
			if (Objects.nonNull(orderVo.getAmount())) {
				orderVo.setAmountStr(String.valueOf(orderVo.getAmount() / 100D));
			}
		});
		return new PageUtils(voIPage);
	}

	public OrderVo orderDetail(String openId, Long orderId) {
		UserEntity user = userService.queryByOpenId(openId);
		if (Objects.isNull(user) || StringUtils.isBlank(user.getMobile())) {
			return null;
		}
		OrderDetailEntity orderDetail = this.getById(orderId);
		OrderVo orderVo = JSONUtil.toBean(JSONUtil.toJsonStr(orderDetail), OrderVo.class);
		orderVo.setStatusStr(OrderStatus.of(orderVo.getStatus()).getName());
		if (Objects.nonNull(orderVo.getBookTime())) {
			orderVo.setBookDateStr(formatter.format(orderVo.getBookTime()));
		}
		if (Objects.nonNull(orderVo.getAmount())) {
			orderVo.setAmountStr(String.valueOf(orderVo.getAmount() / 100D));
		}
		return orderVo;
	}

	private List<Long> getRoleIdListByPhone(String phone) {
		SysUserEntity sysUserEntity = sysUserService.queryByMobile(phone);
		if (Objects.isNull(sysUserEntity)) {
			return Lists.newArrayList();
		}
		return sysUserRoleService.queryRoleIdList(sysUserEntity.getUserId());
	}
}
