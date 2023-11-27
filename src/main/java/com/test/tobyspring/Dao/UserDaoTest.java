package com.test.tobyspring.Dao;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.test.tobyspring.Domain.Level;
import com.test.tobyspring.Domain.User;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
//@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context;
	
	// bean을 인터페이스로 가져온다.
	@Autowired
	UserDao dao;
	
	@Autowired
	DataSource dataSource;
	
	//private UserDao dao;
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		System.out.println(this.context);
		System.out.println(this);
		
		//this.dao = context.getBean("userDao", UserDao.class);
		
		this.user1 = new User("gyumee", "박성철", "springno1", "mail1", Level.BASIC, 1, 0);
		this.user2 = new User("leegw700", "이길원", "springno2", "mail2",  Level.SILVER, 55, 10);
		this.user3 = new User("bumjin", "박범진", "springno3", "mail3", Level.GOLD, 100, 40);
	}

	@Test
	public void addAndGet() throws ClassNotFoundException, SQLException {
		dao.deleteAll();
		assertEquals(dao.getCount(), 0);

		dao.add(user1);
		dao.add(user2);
		assertEquals(dao.getCount(), 2);
			
		User userget1 = dao.get(user1.getId());
		checkSameUser(userget1, user1);

		User userget2 = dao.get(user2.getId());
		checkSameUser(userget2, user2);
		
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
	
	@Test(expected = DataAccessException.class)
	public void duplciateKey() {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);		// 예외 발생해야 한다. (중복키 발생)
	}
	
	@Test
	public void update() {
		dao.deleteAll();
		
		dao.add(user1);			// 수정할 사용자
		dao.add(user2);			// 수정하지 않을 사용자
		
		user1.setName("오민규");
		user1.setPassword("springno6");
		user1.setLevel(Level.GOLD);
		user1.setRecommend(999);
		dao.update(user1);
		
		User user1update = dao.get(user1.getId());
		checkSameUser(user1, user1update);
		User user2update = dao.get(user2.getId());
		checkSameUser(user2, user2update);
	}
	
	private void checkSameUser(User user1, User user2) {
		assertEquals(user1.getId(), user2.getId());
		assertEquals(user1.getName(), user2.getName());
		assertEquals(user1.getPassword(), user2.getPassword());
		assertEquals(user1.getLevel(), user2.getLevel());
		assertEquals(user1.getLogin(), user2.getLogin());
		assertEquals(user1.getRecommend(), user2.getRecommend());
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
		UserDaoJdbc dao = context.getBean("userDao", UserDaoJdbc.class);

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
		UserDaoJdbc dao1 = factory.userDao();
		UserDaoJdbc dao2 = factory.userDao();
		System.out.println(dao1);
		System.out.println(dao2);
		
		// 같은 오브젝트 생성
		// 매번 new 에 의해 새로운 UserDao가 만들어지지 않는다.
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
		UserDaoJdbc dao3 = context.getBean("userDao", UserDaoJdbc.class);
		UserDaoJdbc dao4 = context.getBean("userDao", UserDaoJdbc.class);
		System.out.println(dao3);
		System.out.println(dao4);
		System.out.println(dao3 == dao4);
	}
	
	
}
