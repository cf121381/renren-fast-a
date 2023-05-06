package io.renren.modules.sys.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 *
 * @author changbindong
 * @date 5/3/23
 * @description
 */
@Data
public class StatisticsVo implements Serializable {

	private static final long serialVersionUID = -4542823536031217861L;

	private List<String> yCoordinates;

	private List<String> xCoordinates;

}
