/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.renren.common.constants.LogFromType;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;
import io.renren.modules.app.utils.DateUtil;
import io.renren.modules.sys.dao.SysLogDao;
import io.renren.modules.sys.entity.SysLogEntity;
import io.renren.modules.sys.service.SysLogService;
import io.renren.modules.sys.vo.StatisticsVo;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("sysLogService")
public class SysLogServiceImpl extends ServiceImpl<SysLogDao, SysLogEntity> implements SysLogService {

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		String key = (String) params.get("key");

		IPage<SysLogEntity> page = this.page(
				new Query<SysLogEntity>().getPage(params),
				new QueryWrapper<SysLogEntity>().like(StringUtils.isNotBlank(key), "username", key)
		);

		return new PageUtils(page);
	}

	@Override
	public StatisticsVo accessStatistics() {
		Date now = new Date();
		List<SysLogEntity> data = this.list(new QueryWrapper<SysLogEntity>()
				.eq("from_type", LogFromType.WX)
				.between("create_date", DateUtil.getMonthStartTimeByDate(now), DateUtil.getMonthEndTimeByDate(now))
		);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Long> groupedData = data.stream()
				.collect(Collectors.groupingBy(d -> simpleDateFormat.format(d.getCreateDate()), Collectors.counting()));


		StatisticsVo vo = new StatisticsVo();
		vo.setXCoordinates(new ArrayList<>(groupedData.keySet()));
		vo.setYCoordinates(groupedData.values().stream().map(String::valueOf).collect(Collectors.toList()));
		return vo;
	}
}
