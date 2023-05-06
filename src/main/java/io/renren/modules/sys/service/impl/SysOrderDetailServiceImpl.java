/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.renren.common.constants.StatisticsObject;
import io.renren.common.exception.RRException;
import io.renren.common.utils.AssertUtils;
import io.renren.common.utils.OrderUtils;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.app.dao.OrderDetailDao;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.app.utils.DateUtil;
import io.renren.modules.sys.entity.SysUserEntity;
import io.renren.modules.sys.entity.SysUserRoleEntity;
import io.renren.modules.sys.listener.OrderImportListener;
import io.renren.modules.sys.service.SysOrderDetailService;
import io.renren.modules.sys.service.SysUserRoleService;
import io.renren.modules.sys.service.SysUserService;
import io.renren.modules.sys.vo.CountTempVo;
import io.renren.modules.sys.vo.OrderImportVo;
import io.renren.modules.sys.vo.StatisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.shiro.SecurityUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service("sysOrderDetailService")
public class SysOrderDetailServiceImpl extends ServiceImpl<OrderDetailDao, OrderDetailEntity> implements SysOrderDetailService {

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
//		1,客户管理员
//		2,代理商
//		3,客户
		//date start,end
		IPage<OrderDetailEntity> page = this.page(
				new Query<OrderDetailEntity>().getPage(params),
				new QueryWrapper<OrderDetailEntity>()
						.like(StringUtils.isNotBlank(customerName), "customer_name", customerName)
						.like(StringUtils.isNotBlank(customerManager), "customer_manager", customerManager)
						.eq(status != null && status > 0, "status", status)

		);

		return OrderUtils.adapt2VoPage(page);
	}

	@Transactional(rollbackFor = Throwable.class)
	@Override
	public String batchImport(MultipartFile file) {
		List<OrderImportVo> orderImportList = Lists.newArrayList();
		try {
			EasyExcel.read(file.getInputStream(), OrderImportVo.class, new OrderImportListener(orderImportList))
					.sheet().headRowNumber(1).doRead();
			log.info("容器导入，解析完成，共{}条", orderImportList.size());
		}
		catch (IOException e) {
			log.error("容器导入异常", e);
			throw new RRException(e.getMessage());
		}

		AssertUtils.isTrue(CollectionUtils.isNotEmpty(orderImportList), "文件内容为空");
		AssertUtils.isTrue(orderImportList.size() <= 2000, "一次导入不能超过2000条");

		List<OrderImportVo> errorList = orderImportList.stream().filter(containerImport -> !containerImport.getFlag()).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(errorList)) {
			return errorList.stream().map(e -> "第" + e.getRowNum() + "行:" + e.getErrorMsg()).collect(Collectors.joining(","));
		}

		List<OrderImportVo> needUpdateList = orderImportList.stream().filter(OrderImportVo::isUpdate).collect(Collectors.toList());
		List<OrderImportVo> needSaveList = orderImportList.stream().filter(x -> !x.isUpdate()).collect(Collectors.toList());

		//根据导入的内容，自动创建对应角色的用户
		//代理商
		Map<String, String> agents = orderImportList.stream()
				.filter(o -> StringUtils.isNotBlank(o.getAgentPhone()))
				.collect(Collectors.toMap(OrderImportVo::getAgentPhone, OrderImportVo::getAgent, (a, b) -> b));
		//客户(下单人)
		Map<String, String> customers = orderImportList.stream()
				.filter(o -> StringUtils.isNotBlank(o.getBookUserPhone()))
				.collect(Collectors.toMap(OrderImportVo::getBookUserPhone, OrderImportVo::getBookUser, (a, b) -> b));
		//客户管理员
		Map<String, String> customerManagers = orderImportList.stream()
				.filter(o -> StringUtils.isNotBlank(o.getCustomerManagerPhone()))
				.collect(Collectors.toMap(OrderImportVo::getCustomerManagerPhone, OrderImportVo::getCustomerManager, (a, b) -> b));

		List<String> allPhone = orderImportList.stream()
				.map(o -> Lists.newArrayList(o.getAgentPhone(), o.getBookUserPhone(), o.getCustomerManagerPhone()))
				.flatMap(List::stream).distinct().collect(Collectors.toList());

		List<SysUserEntity> allExistsUser = sysUserService.queryByMobileList(allPhone);

		Map<String, SysUserEntity> phoneUserMap = allExistsUser.stream().collect(Collectors.toMap(SysUserEntity::getMobile, Function.identity()));
		handleUpdateUser(agents, customers, customerManagers, allExistsUser, phoneUserMap);
		//处理需要新建的用户
		handleAddUsers(agents, customers, customerManagers, allPhone, phoneUserMap);
		saveOrderData(needUpdateList, needSaveList);
		return StringUtils.EMPTY;
	}

	private void handleUpdateUser(Map<String, String> agents, Map<String, String> customers, Map<String, String> customerManagers, List<SysUserEntity> allExistsUser, Map<String, SysUserEntity> phoneUserMap) {
		if (CollectionUtils.isNotEmpty(allExistsUser)) {
			//处理需要更新的
			List<SysUserRoleEntity> userRoleList = sysUserRoleService.queryRoleIdList(allExistsUser.stream().map(SysUserEntity::getUserId).distinct().collect(Collectors.toList()));
			Map<Long, SysUserEntity> userIdUserMap = allExistsUser.stream().collect(Collectors.toMap(SysUserEntity::getUserId, Function.identity()));
			Map<String, List<Long>> phoneRolesMap = userRoleList.stream().collect(Collectors.groupingBy(s -> userIdUserMap.get(s.getUserId()).getMobile(), Collectors.mapping(SysUserRoleEntity::getRoleId, Collectors.toList())));

			//处理用户 存在的用户角色是否满足上传数据要求；角色不足的，补足角色。补足角色这块可能有点复杂
			phoneRolesMap.forEach((phone, roleIds) -> {
				//1,客户管理员
				//2,代理商
				//3,客户
				if (customerManagers.containsKey(phone) && !roleIds.contains(1L)) {
					roleIds.add(1L);
				}
				if (agents.containsKey(phone) && !roleIds.contains(2L)) {
					roleIds.add(2L);
				}
				if (customers.containsKey(phone) && !roleIds.contains(3L)) {
					roleIds.add(3L);
				}
				phoneUserMap.get(phone).setUsername(findName(agents, customers, customerManagers, phone));
				phoneUserMap.get(phone).setRoleIdList(roleIds);
			});
			phoneUserMap.forEach((key, value) -> {
				sysUserService.update(value);
			});
		}
	}

	private void handleAddUsers(Map<String, String> agents, Map<String, String> customers, Map<String, String> customerManagers, List<String> allPhone, Map<String, SysUserEntity> phoneUserMap) {
		Map<String, List<Long>> phoneRolesMap = allPhone.stream().filter(p -> !phoneUserMap.containsKey(p)).map(phone -> {
			List<Long> roleIdList = Lists.newArrayList();
			//1,客户管理员
			//2,代理商
			//3,客户
			if (agents.containsKey(phone)) {
				roleIdList.add(2L);
			}
			if (customers.containsKey(phone)) {
				roleIdList.add(3L);
			}
			if (customerManagers.containsKey(phone)) {
				roleIdList.add(1L);
			}
			return new Pair<>(phone, roleIdList);
		}).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		Long operateUserId = ((SysUserEntity) SecurityUtils.getSubject().getPrincipal()).getUserId();
		List<SysUserEntity> addList = phoneRolesMap.entrySet().stream().map(entry -> {
			SysUserEntity entity = new SysUserEntity();
			entity.setMobile(entry.getKey());
			entity.setPassword(RandomStringUtils.randomAlphanumeric(10));
			entity.setUsername(findName(agents, customers, customerManagers, entry.getKey()));
			entity.setRoleIdList(entry.getValue());
			entity.setCreateUserId(operateUserId);
			return entity;
		}).collect(Collectors.toList());
		addList.forEach(user -> sysUserService.saveUser(user));
	}

	private String findName(Map<String, String> agents, Map<String, String> customers, Map<String, String> customerManagers, String phone) {
		String name = phone;
		if (customers.containsKey(phone)) {
			name = customers.get(phone);
		}
		if (agents.containsKey(phone)) {
			name = agents.get(phone);
		}
		if (customerManagers.containsKey(phone)) {
			name = customerManagers.get(phone);
		}
		return name;
	}

	private void saveOrderData(List<OrderImportVo> needUpdateList, List<OrderImportVo> needSaveList) {
		if (CollectionUtils.isNotEmpty(needUpdateList)) {
			List<String> orderNos = needUpdateList.stream().map(OrderImportVo::getOrderNo).distinct().collect(Collectors.toList());
			List<OrderDetailEntity> updateOrderList = this.list(new QueryWrapper<OrderDetailEntity>()
					.in("order_no", orderNos));
			Map<String, OrderDetailEntity> noEntityMap = updateOrderList.stream().collect(Collectors.toMap(OrderDetailEntity::getOrderNo, Function.identity()));
			needUpdateList.forEach(vo -> {
				OrderDetailEntity entity = noEntityMap.get(vo.getOrderNo());
				BeanUtils.copyProperties(vo, entity);
				entity.setBookTime(vo.getBookTime());
				entity.setStatus(vo.getOrderStatus().getValue());
				if (StringUtils.isNotBlank(vo.getAmount())) {
					entity.setAmount(new BigDecimal(vo.getAmount()));
				}
			});
			this.updateBatchById(updateOrderList);
		}
		if (CollectionUtils.isNotEmpty(needSaveList)) {
			List<OrderDetailEntity> saveOrderList = needSaveList.stream().map(vo -> {
				OrderDetailEntity entity = new OrderDetailEntity();
				BeanUtils.copyProperties(vo, entity);
				entity.setStatus(vo.getOrderStatus().getValue());
				if (StringUtils.isNotBlank(vo.getAmount())) {
					entity.setAmount(new BigDecimal(vo.getAmount()));
				}
				return entity;
			}).collect(Collectors.toList());
			this.saveBatch(saveOrderList);
		}
	}


	@Override
	public StatisticsVo statisticsOrder(String statisticsObject) {
		StatisticsObject so = StatisticsObject.of(statisticsObject);
		//查询指定日期内所有的单子
		List<OrderDetailEntity> orderList = this.list(new QueryWrapper<OrderDetailEntity>()
				.ge("book_time", DateUtil.getMonthStartTimeByDate(new Date()))
				.lt("book_time",DateUtil.getMonthEndTimeByDate(new Date()))
		);
		if (CollectionUtils.isEmpty(orderList)) {
			return new StatisticsVo();
		}

		//进行统计
		//1、聚合,按照厂商销售、按照佳杰销售、按照代理商销售分别聚合

		Map<String, List<OrderDetailEntity>> groupedOrder;
		switch (so) {
			case BOOK_USER:
				groupedOrder = orderList.stream().collect(Collectors.groupingBy(OrderDetailEntity::getBookUserPhone));

				break;
			case CUSTOMER_MANAGER:
				groupedOrder = orderList.stream().collect(Collectors.groupingBy(OrderDetailEntity::getCustomerManagerPhone));
				break;
			case AGENT:
				groupedOrder = orderList.stream().collect(Collectors.groupingBy(OrderDetailEntity::getAgentPhone));
				break;
			default:
				groupedOrder = Maps.newHashMap();
		}
		List<CountTempVo> countTempVoList = groupedOrder.entrySet().stream().map(entry -> {
			CountTempVo vo = new CountTempVo();
			vo.setOrderCount(entry.getValue().size());
			//获取用户信息，用来填充
			SysUserEntity sysUser = sysUserService.queryByMobile(entry.getKey());
			if (Objects.isNull(sysUser)) {
				return null;
			}
			vo.setSysUser(sysUser);
			List<OrderDetailEntity> orders = entry.getValue();
			vo.setOrderTotalAmount(orders.stream().map(OrderDetailEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
			vo.setOrderAvgAmount(orders.stream().map(OrderDetailEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(orders.size()), 2, RoundingMode.HALF_UP));
			return vo;
		}).filter(Objects::nonNull).sorted(Comparator.comparing(CountTempVo::getOrderTotalAmount).reversed()).collect(Collectors.toList());

		StatisticsVo statisticsVo = new StatisticsVo();
		statisticsVo.setXCoordinates(countTempVoList.stream().map(x -> x.getSysUser().getUsername()).collect(Collectors.toList()));
		statisticsVo.setYCoordinates(countTempVoList.stream().map(x -> x.getOrderTotalAmount().toString()).collect(Collectors.toList()));
		return statisticsVo;
	}
}
