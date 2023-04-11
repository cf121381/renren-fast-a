package io.renren.modules.sys.controller;

import java.util.Map;

import javax.annotation.Resource;

import io.renren.common.utils.PageUtils;
import io.renren.common.utils.R;
import io.renren.modules.sys.service.SysOrderDetailService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author changbindong
 * @date 4/6/23
 * @description
 */
@RestController
@RequestMapping("/order/")
@Api("订单接口")
public class OrderController {

	@Resource
	private SysOrderDetailService sysOrderDetailService;


	@GetMapping("/list")
	public R getWechatCredential(@RequestParam Map<String, Object> params) {
		PageUtils page = sysOrderDetailService.queryPage(params);
		return R.ok().put("page", page);
	}

	@PostMapping("/batch_import")
	public R batchImport(@RequestBody MultipartFile file) {
		String s = sysOrderDetailService.batchImport(file);
		if (StringUtils.isEmpty(s)) {
			return R.ok();
		}
		else {
			return R.error(s);
		}
	}
}
