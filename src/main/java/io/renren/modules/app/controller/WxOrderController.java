package io.renren.modules.app.controller;

import java.util.Map;

import javax.annotation.Resource;

import io.renren.common.annotation.WxSysLog;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.app.service.OrderDetailService;
import io.renren.modules.sys.vo.OrderVo;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author changbindong
 * @date 4/6/23
 * @description
 */
@RestController
@RequestMapping("/weixin/order/")
@Api("订单接口")
public class WxOrderController {

	@Resource
	private OrderDetailService orderDetailService;

	@WxSysLog("query_order_list")
	@GetMapping("/list")
	public R orderList(@RequestParam Map<String, Object> params) {
		PageUtils page = orderDetailService.queryPage(params);
		return R.ok().put("page", page);
	}

	@WxSysLog("query_order_detail")
	@GetMapping("/detail")
	public R orderDetail(@RequestParam("orderId") Long orderId, @RequestParam("openId") String openId) {
		OrderVo orderVo = orderDetailService.orderDetail(openId, orderId);
		return R.ok().put("data", orderVo);
	}

}
