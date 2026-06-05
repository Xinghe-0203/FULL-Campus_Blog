package com.example.edu_project.config.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * 数据库配置类
 * 根据 DB_TYPE 环境变量动态切换 MySQL 或 SQLite 数据库
 * 仅在 DB_TYPE 环境变量存在时激活（测试环境不设置此变量，使用 Spring 默认 H2 数据源）
 */
@Configuration
@ConditionalOnProperty(name = "DB_TYPE")
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${DB_TYPE:mysql}")
    private String dbType;

    @Value("${DB_HOST:localhost}")
    private String dbHost;

    @Value("${DB_PORT:3306}")
    private String dbPort;

    @Value("${DB_NAME:campus_blog}")
    private String dbName;

    @Value("${DB_USERNAME:root}")
    private String dbUsername;

    @Value("${DB_PASSWORD:}")
    private String dbPassword;

    @Value("${DB_URL_SQLITE:jdbc:sqlite:campus_blog.db}")
    private String sqliteUrl;

    @Bean
    public DataSource dataSource() {
        if ("sqlite".equalsIgnoreCase(dbType)) {
            return createSqliteDataSource();
        }
        return createMysqlDataSource();
    }

    private HikariDataSource createSqliteDataSource() {
        log.info("========================================");
        log.info("   使用 SQLite 数据库");
        log.info("   数据库路径: {}", sqliteUrl);
        log.info("========================================");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(sqliteUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        config.setPoolName("CampusBlogSQLitePool");

        // SQLite 单文件数据库不适合高并发连接池
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);

        // SQLite PRAGMA 配置
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("foreign_keys", "ON");
        config.addDataSourceProperty("busy_timeout", "5000");

        HikariDataSource dataSource = new HikariDataSource(config);

        // 初始化 SQLite Schema
        initSqliteSchema(dataSource);

        return dataSource;
    }

    private HikariDataSource createMysqlDataSource() {
        log.info("========================================");
        log.info("   使用 MySQL 数据库");
        log.info("   主机: {}:{}", dbHost, dbPort);
        log.info("   数据库: {}", dbName);
        log.info("========================================");

        String useSsl = System.getProperty("DB_USE_SSL", "true");
        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=%s&requireSSL=%s",
                dbHost, dbPort, dbName, useSsl, useSsl
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setPoolName("CampusBlogHikariCP");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);
        config.setConnectionTimeout(30000);
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

    private void initSqliteSchema(DataSource dataSource) {
        try {
            ClassPathResource schemaResource = new ClassPathResource("schema-sqlite.sql");
            if (schemaResource.exists()) {
                DataSourceInitializer initializer = new DataSourceInitializer();
                initializer.setDataSource(dataSource);
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(schemaResource);
                populator.setSeparator(";");
                initializer.setDatabasePopulator(populator);
                initializer.afterPropertiesSet();
                log.info("SQLite Schema 初始化完成");
            } else {
                log.warn("未找到 SQLite Schema 文件: schema-sqlite.sql，请手动初始化数据库");
            }
        } catch (Exception e) {
            log.error("SQLite Schema 初始化失败: {}", e.getMessage(), e);
        }
    }
}
