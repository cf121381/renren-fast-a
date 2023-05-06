package io.renren.modules.sys.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.app.form.OrderQueryForm;
import io.renren.modules.sys.vo.StatisticsVo;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户
 *
 * @author Mark sunlightcs@gmail.com
 */
public interface SysOrderDetailService extends IService<OrderDetailEntity> {

	PageUtils queryPage(@RequestParam Map<String, Object> params);

	String batchImport(MultipartFile file);

	/**
	 * 订单统计
	 * @param statisticsObject 统计对象
	 * @return
	 */
	StatisticsVo statisticsOrder(String statisticsObject);
}
