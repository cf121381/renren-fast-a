/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.renren.common.constants.OrderStatus;
import io.renren.common.exception.RRException;
import io.renren.common.utils.AssertUtils;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.app.dao.OrderDetailDao;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.sys.listener.OrderImportListener;
import io.renren.modules.sys.service.SysOrderDetailService;
import io.renren.modules.sys.vo.OrderImportVo;
import io.renren.modules.sys.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service("sysOrderDetailService")
public class SysOrderDetailServiceImpl extends ServiceImpl<OrderDetailDao, OrderDetailEntity> implements SysOrderDetailService {
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

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

		if (CollectionUtils.isNotEmpty(needUpdateList)) {
			List<String> orderNos = needUpdateList.stream().map(OrderImportVo::getOrderNo).distinct().collect(Collectors.toList());
			List<OrderDetailEntity> updateOrderList = this.list(new QueryWrapper<OrderDetailEntity>()
					.in("order_no", orderNos));
			Map<String, OrderDetailEntity> noEntityMap = updateOrderList.stream().collect(Collectors.toMap(OrderDetailEntity::getOrderNo, Function.identity()));
			Map<String, OrderImportVo> newInfoMap = needUpdateList.stream().collect(Collectors.toMap(OrderImportVo::getOrderNo, Function.identity()));
			needUpdateList.forEach(vo -> {
				OrderDetailEntity entity = noEntityMap.get(vo.getOrderNo());
				BeanUtils.copyProperties(vo, entity);
				entity.setBookTime(vo.getBookTime());
				entity.setStatus(vo.getOrderStatus().getValue());
				if (StringUtils.isNotBlank(vo.getAmount())) {
					entity.setAmount((int) (Double.parseDouble(vo.getAmount()) * 100));
				}
			});
			this.updateBatchById(updateOrderList);
		}
		if (CollectionUtils.isNotEmpty(needSaveList)) {
			List<OrderDetailEntity> saveOrderList = needSaveList.stream().map(vo -> {
				OrderDetailEntity entity = new OrderDetailEntity();
				BeanUtils.copyProperties(vo, entity);
				entity.setStatus(vo.getOrderStatus().getValue());
				return entity;
			}).collect(Collectors.toList());
			this.saveBatch(saveOrderList);
		}
		return StringUtils.EMPTY;
	}
}
