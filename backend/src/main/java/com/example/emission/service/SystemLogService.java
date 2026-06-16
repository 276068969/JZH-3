package com.example.emission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.emission.mapper.SystemLogMapper;
import com.example.emission.model.SystemLog;
import com.example.emission.model.UserAccount;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {

    private static final Logger log = LoggerFactory.getLogger(SystemLogService.class);

    private static final String CREATE_SYSTEM_LOG_TABLE_SQL =
        "CREATE TABLE IF NOT EXISTS system_logs ("
            + "id BIGINT PRIMARY KEY AUTO_INCREMENT, "
            + "operator VARCHAR(64) NOT NULL, "
            + "role VARCHAR(64) NOT NULL, "
            + "action VARCHAR(64) NOT NULL, "
            + "business_object VARCHAR(255) NOT NULL, "
            + "detail TEXT, "
            + "result VARCHAR(32) NOT NULL, "
            + "operate_time DATETIME NOT NULL, "
            + "ip VARCHAR(64), "
            + "INDEX idx_operator (operator), "
            + "INDEX idx_action (action), "
            + "INDEX idx_operate_time (operate_time)"
            + ")";

    private final SystemLogMapper systemLogMapper;
    private final JdbcTemplate jdbcTemplate;

    public SystemLogService(SystemLogMapper systemLogMapper, JdbcTemplate jdbcTemplate) {
        this.systemLogMapper = systemLogMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void ensureTableExists() {
        try {
            jdbcTemplate.execute(CREATE_SYSTEM_LOG_TABLE_SQL);
            log.info("系统日志表检查/创建完成");
        } catch (Exception e) {
            log.warn("系统日志表创建/检查失败：{}", e.getMessage());
        }
    }

    public void recordLog(String operator, String role, String action,
                          String businessObject, String detail, String result, String ip) {
        try {
            ensureTableExists();
            SystemLog systemLog = new SystemLog();
            systemLog.setOperator(operator);
            systemLog.setRole(role);
            systemLog.setAction(action);
            systemLog.setBusinessObject(businessObject);
            systemLog.setDetail(detail);
            systemLog.setResult(result);
            systemLog.setOperateTime(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            systemLog.setIp(ip);
            systemLogMapper.insert(systemLog);
        } catch (Exception e) {
            log.error("记录系统日志失败：{}", e.getMessage());
        }
    }

    public void recordLog(UserAccount user, String action,
                          String businessObject, String detail, String result, String ip) {
        String operator = user != null ? user.displayName() : "system";
        String role = user != null ? user.role() : "system";
        recordLog(operator, role, action, businessObject, detail, result, ip);
    }

    public Map<String, Object> getLogList(Integer page, Integer pageSize,
                                           String operator, String role,
                                           String action, String startTime, String endTime) {
        try {
            ensureTableExists();
            int current = page != null && page > 0 ? page : 1;
            int size = pageSize != null && pageSize > 0 ? pageSize : 20;

            LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();
            if (operator != null && !operator.isBlank()) {
                wrapper.like(SystemLog::getOperator, operator.trim());
            }
            if (role != null && !role.isBlank()) {
                wrapper.eq(SystemLog::getRole, role.trim());
            }
            if (action != null && !action.isBlank()) {
                wrapper.eq(SystemLog::getAction, action.trim());
            }
            if (startTime != null && !startTime.isBlank()) {
                wrapper.ge(SystemLog::getOperateTime, startTime.trim());
            }
            if (endTime != null && !endTime.isBlank()) {
                String end = endTime.trim() + " 23:59:59";
                wrapper.le(SystemLog::getOperateTime, end);
            }
            wrapper.orderByDesc(SystemLog::getOperateTime);

            IPage<SystemLog> pageResult = systemLogMapper.selectPage(new Page<>(current, size), wrapper);

            return Map.of(
                "total", pageResult.getTotal(),
                "page", current,
                "pageSize", size,
                "records", pageResult.getRecords()
            );
        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return Map.of("total", 0L, "page", page != null ? page : 1,
                "pageSize", pageSize != null ? pageSize : 20, "records", java.util.List.of());
        }
    }
}
