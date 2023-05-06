/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.app.service;


import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.sys.vo.OrderVo;

import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface OrderDetailService extends IService<OrderDetailEntity> {

	PageUtils queryPage(@RequestParam Map<String, Object> params);

	OrderVo orderDetail(String openId, Long orderId);
}
