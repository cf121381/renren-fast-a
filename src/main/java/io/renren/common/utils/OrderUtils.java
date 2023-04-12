package io.renren.common.utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import io.renren.common.constants.OrderStatus;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.sys.vo.OrderVo;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author changbindong
 * @date 4/12/23
 * @description
 */
public class OrderUtils {

	public static OrderVo adapt2Vo(OrderDetailEntity orderDetail) {
		if (Objects.isNull(orderDetail)) {
			return null;
		}
		OrderVo orderVo = JSONUtil.toBean(JSONUtil.toJsonStr(orderDetail), OrderVo.class);
		orderVo.setStatusStr(OrderStatus.of(orderVo.getStatus()).getName());
		if (Objects.nonNull(orderVo.getBookTime())) {
			orderVo.setBookDateStr(DateUtils.format(orderVo.getBookTime()));
		}
		if (Objects.nonNull(orderVo.getAmount())) {
			orderVo.setAmountStr(String.valueOf(orderVo.getAmount() / 100D));
		}
		return orderVo;
	}

	public static PageUtils adapt2VoPage(IPage<OrderDetailEntity> page){
		if(Objects.isNull(page)){
			return new PageUtils(Lists.newArrayList(), 0, 10, 1);
		}
		Page<OrderVo> voIPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
		if(CollectionUtils.isEmpty(page.getRecords())){
			new PageUtils(voIPage);
		}
		List<OrderVo> orderVoList = page.getRecords().stream().map(OrderUtils::adapt2Vo).filter(Objects::nonNull).collect(Collectors.toList());
		voIPage.setRecords(orderVoList);
		return new PageUtils(voIPage);
	}
}
