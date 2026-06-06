package com.example.edu_project.config.db;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.edu_project.entity.SysUser;
import com.example.edu_project.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.default-password:Admin123}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) {
        initAdminAccount();
    }

    private void initAdminAccount() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, "admin");
        long count = sysUserMapper.selectCount(wrapper);

        if (count == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setNickname("管理员");
            admin.setRole("admin");
            admin.setStatus(1);
            admin.setLoginFailCount(0);
            sysUserMapper.insert(admin);
            log.info("默认管理员账号初始化成功: admin");
        } else {
            log.info("管理员账号已存在，跳过初始化");
        }
    }
}
