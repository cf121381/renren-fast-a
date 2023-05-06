package io.renren.modules.sys.controller;

import javax.annotation.Resource;

import io.renren.common.utils.R;
import io.renren.modules.sys.service.SysLogService;
import io.renren.modules.sys.service.SysOrderDetailService;
import io.swagger.annotations.Api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author changbindong
 * @date 5/3/23
 * @description
 */
@RestController
@RequestMapping("/statistics/")
@Api("订单接口")
public class StatisticsController {

	@Resource
	private SysLogService sysLogService;

	@Resource
	private SysOrderDetailService sysOrderDetailService;

	@GetMapping("/access")
	public R accessStatistics() {
		return R.ok().put("data", sysLogService.accessStatistics());
	}

	@GetMapping("/order")
	public R orderStatistics(@RequestParam("so") String statisticsObject) {
		return R.ok().put("data", sysOrderDetailService.statisticsOrder(statisticsObject));
	}

}
