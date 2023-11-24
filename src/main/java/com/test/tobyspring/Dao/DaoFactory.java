package com.test.tobyspring.Dao;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

/**
 * DI 컨테이너라고 할 수 있다. UserDao 에 DConnectionMaker를 주입해주기 때문.
 * @author mjkim
 *
 */
@Configuration	// 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정정보라는 표시.
public class DaoFactory {

	@Bean		// 오브젝트 생성을 담당하는 IoC용 메소드라는 표시.
	public UserDao userDao() {
		/*
		ConnectionMaker connectionMaker = new DConnectionMaker();
		UserDao userDao = new UserDao(connectionMaker);
		return userDao;
		*/
		
		// return new UserDao(connectionMaker());
		
		UserDao userDao = new UserDao();
		userDao.setConnectionMaker(connectionMaker());
		return userDao;
	}
	
	@Bean
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource ();

		//dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
		dataSource.setUrl("jdbc:mysql://localhost/toby?characterEncoding=UTF-8");
		dataSource.setUsername("root");
		dataSource.setPassword("rhantls");

		return dataSource;
	}
	
	@Bean
	public ConnectionMaker connectionMaker() {
		return new DConnectionMaker();
	}
}
