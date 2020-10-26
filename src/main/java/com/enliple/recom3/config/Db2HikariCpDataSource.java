package com.enliple.recom3.config;

import java.sql.SQLException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.enliple.recom3.common.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = { "com.enliple.recom3.jpa.db2.repository", "com.enliple.recom3.jpa.db2.domain" },
		  transactionManagerRef   = "DB2_TransactionManager"
		, entityManagerFactoryRef = "DB2_EntityManagerFactory")
@Slf4j
public class Db2HikariCpDataSource {
	
	@Autowired
	Config config;
	
	@Bean(name = "DB2_DataSource")
	public DataSource dataSource() throws SQLException {
//		HikariDataSource dataSource = DataSourceBuilder.create()
//				.type(HikariDataSource.class)
//				.url(config.getMysqlUrl())
//				.username(config.getMysqlUser())
//				.password(config.getMysqlPassword())
//				.driverClassName(config.getMysqlDriver())
//				.build() ;		
		HikariConfig hikariconfig = new HikariConfig();
		//hikariconfig.setDriverClassName(config.getMysqlDriver());
		hikariconfig.setJdbcUrl(config.getMysqlUrlForProduct());
		hikariconfig.setUsername(config.getMysqlUserForProduct());
		hikariconfig.setPassword(config.getMysqlPasswordForProduct());

//		hikariconfig.setConnectionTimeout(9000);//connect_timeout 10
//		hikariconfig.setIdleTimeout(9000);//connect_timeout 10
//		hikariconfig.setMaxLifetime(28000);//28800
//		hikariconfig.setValidationTimeout(9000);//connect_timeout 10

		log.info("DB2_DataSource {} => {}/{}",config.getMysqlUrlForProduct(),config.getMysqlUserForProduct(),config.getMysqlPasswordForProduct());
		HikariDataSource hikariDataSource = new HikariDataSource(hikariconfig);
		return hikariDataSource;
	}
	
	//@Primary
	@Bean(name = "DB2_EntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean Db2EntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier("DB2_DataSource") DataSource dataSource) {
		return builder.dataSource(dataSource)
				.packages("com.enliple.recom3.jpa.db2.repository", "com.enliple.recom3.jpa.db2.domain").build();

	}

	//@Primary
	@Bean(name = "DB2_TransactionManager")
	public PlatformTransactionManager Db1TransactionManager(
			@Qualifier("DB2_EntityManagerFactory") EntityManagerFactory entityManagerFactory) {

		return new JpaTransactionManager(entityManagerFactory);

	}
}
