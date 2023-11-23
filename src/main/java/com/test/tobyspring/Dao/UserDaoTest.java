package com.test.tobyspring.Dao;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.test.tobyspring.Domain.User;

public class UserDaoTest {

	// 애플리케이션 컨텍스트를 적용한 버전.
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		//AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class); 클래스 버전
		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");				// xml 버
		UserDao dao = context.getBean("userDao", UserDao.class);

		//ConnectionMaker connectionMaker = (ConnectionMaker) new DConnectionMaker();
		//UserDao dao = new UserDao(connectionMaker);
		//UserDao dao = new DaoFactory().userDao();	// 팩토리를 사용하도록 수정한.
		
		User user = new User();
		user.setId("whiteship");
		user.setName("백기선");
		user.setPassword("married");

		dao.add(user);
			
		System.out.println(user.getId() + " 등록 성공");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
			
		System.out.println(user2.getId() + " 조회 성공");
	}
	
	/**
	 * 생성되는 두 개의 오브젝트가 같은 것인지 확인해보자.
	 * @param args
	 */
	public static void main2(String[] args) {
		// 다른 오브젝트 생성
		DaoFactory factory = new DaoFactory();
		UserDao dao1 = factory.userDao();
		UserDao dao2 = factory.userDao();
		System.out.println(dao1);
		System.out.println(dao2);
		
		// 같은 오브젝트 생성
		// 매번 new 에 의해 새로운 UserDao가 만들어지지 않는다.
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		UserDao dao3 = context.getBean("userDao", UserDao.class);
		UserDao dao4 = context.getBean("userDao", UserDao.class);
		System.out.println(dao3);
		System.out.println(dao4);
		System.out.println(dao3 == dao4);
	}
	
	
}
