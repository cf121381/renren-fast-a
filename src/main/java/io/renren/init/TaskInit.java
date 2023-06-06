package io.renren.init;

import io.renren.common.utils.BeanFindUtils;
import io.renren.modules.job.entity.ScheduleJobEntity;
import io.renren.modules.job.service.ScheduleJobService;
import io.renren.modules.job.task.ITask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * 该类通过扫描所有实现了 ITask 接口的类，并根据数据库中已经存在的定时任务，
 * 将需要新增的任务添加到数据库中，实现了自动加载和初始化定时任务的功能。
 */
@Component
@Slf4j
public class TaskInit implements ApplicationRunner {

    @Autowired
    private ScheduleJobService jobService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.debug("Task initialization begins...");

        List<String> jobBeanName = new ArrayList<>();
        //从Springboot项目中查询出所有实现了ITask接口的实现类
        Set<Class<?>> impl = BeanFindUtils.findInterface("io.renren.modules.job.task.impl", ITask.class);
        for (Class<?> aClass : impl) {
            Component annotation = aClass.getAnnotation(Component.class);
            jobBeanName.add(annotation.value());
        }

        //从数据库中查询出所有的定时任务
        List<ScheduleJobEntity> list = jobService.list();
        for (ScheduleJobEntity entity : list){
            @NotBlank(message = "bean名称不能为空")
            String beanName = entity.getBeanName();
            //移除已经存在的beanName，剩下的 beanName 就是需要新增的任务
            jobBeanName.remove(beanName);
        }

        //如果有新增的定时任务，则添加进数据库
        if (!jobBeanName.isEmpty()){
            for (String beanName : jobBeanName){
                ScheduleJobEntity entity = new ScheduleJobEntity();
                entity.setBeanName(beanName);
                //1，暂停执行，需从管理系统手动恢复
                entity.setStatus(1);
                entity.setCronExpression("0 00 04 * * ?");
                entity.setCreateTime(new Date());

                jobService.saveJob(entity);
            }
        }

        log.debug("Task addition " + jobBeanName.size());
        log.debug("Task initialization is complete...");
    }

}
