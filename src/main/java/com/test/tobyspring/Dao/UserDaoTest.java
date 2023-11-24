package com.test.tobyspring.Dao;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.test.tobyspring.Domain.User;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations="/applicationContext.xml")
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	UserDao dao;
	
	//private UserDao dao;
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		System.out.println(this.context);
		System.out.println(this);
		
		//this.dao = context.getBean("userDao", UserDao.class);
		
		this.user1 = new User("gyumee", "박성철", "springno1");
		this.user2 = new User("leegw700", "이길원", "springno2");
		this.user3 = new User("bumjin", "박범진", "springno3");
	}

	@Test
	public void addAndGet() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		assertEquals(dao.getCount(), 0);

		dao.add(user1);
		dao.add(user2);
		assertEquals(dao.getCount(), 2);
			
		User userget1 = dao.get(user1.getId());
		assertEquals(userget1.getName(), user1.getName());
		assertEquals(userget1.getPassword(), user1.getPassword());

		User userget2 = dao.get(user2.getId());
		assertEquals(userget2.getName(), user2.getName());
		assertEquals(userget2.getPassword(), user2.getPassword());
		
	}
	
	@Test
	public void count() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		assertEquals(dao.getCount(), 0);
		
		dao.add(user1);
		assertEquals(dao.getCount(), 1);
		
		dao.add(user2);
		assertEquals(dao.getCount(), 2);
		
		dao.add(user3);
		assertEquals(dao.getCount(), 3);
	}
	
	@Test(expected = EmptyResultDataAccessException.class)
	public void getUserFailure() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		assertEquals(dao.getCount(), 0);
		
		// 존재하지 않는 아이디. -> 예외 던짐 
		dao.get("unknown_id");
	}
	
	public static void main(String[] args) {
		// 클래스 사용 방법. 이렇게 안하고 run As - jUnit Test 해도 된다.
		JUnitCore.main("com.test.tobyspring.Dao.UserDaoTest");
	}
	
	/**
	 * chapter 1
	 * 애플리케이션 컨텍스트를 적용한 버전.
	 * @RunWith, @ContextConfiguration 를 사용하지 않으려면 이렇게 작성해야 한다.
	 * @param args
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void main1(String[] args) throws ClassNotFoundException, SQLException {
		//AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class); 클래스 버전
		ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");			// xml 버전
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
		if (!user.getName().equals(user2.getName())) {
			System.out.println("테스트 실패 (name)");
		} else if (!user.getPassword().equals(user2.getPassword())) {
			System.out.println("테스트 실패 (password)");
		} else {
			System.out.println("조회 테스트 성공");
		}
	}
	
	/**
	 * 생성되는 두 개의 오브젝트가 같은 것인지 확인해보자.
	 * @param args
	 */
	public static void main1_1(String[] args) {
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
