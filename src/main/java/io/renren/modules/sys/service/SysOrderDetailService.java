package io.renren.modules.sys.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import io.renren.common.utils.PageUtils;
import io.renren.modules.app.entity.OrderDetailEntity;
import io.renren.modules.app.form.OrderQueryForm;

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
}
