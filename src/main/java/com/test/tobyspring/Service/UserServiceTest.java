package com.test.tobyspring.Service;

import static com.test.tobyspring.Service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static com.test.tobyspring.Service.UserService.MIN_RECOMMEND_FOR_GOLD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import com.test.tobyspring.Dao.UserDao;
import com.test.tobyspring.Domain.Level;
import com.test.tobyspring.Domain.User;;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserServiceTest {

	@Autowired
	UserService userService;
	@Autowired
	UserDao userDao;
	//@Autowired
	//DataSource dataSource;
	@Autowired
	PlatformTransactionManager transationManager;
	
	@Autowired
	MailSender mailSender;
	
	List<User> users;
	
	// userService 빈의 주입을 확인하는 테스트
	@Test
	public void bean() {
		assertNotNull(this.userService);
	}
	
	@Before
	public void serUp() {
		users = Arrays.asList(
				new User("bumjin", "박범진", "p1", "mail1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0),
				new User("joytouch", "강명성", "p2", "mail2", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
				new User("erwins", "신승한", "p3", "mail3", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1),
				new User("madnite1", "이상호", "p4", "mail4", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD),
				new User("green", "오민규", "p5", "mail5", Level.GOLD, 100, Integer.MAX_VALUE)
				);
				
	}
	
	@Test
	public void upgradeLevels() {
		userDao.deleteAll();
		
		for (User user : users) userDao.add(user);
		
		userService.upgradeLevels();
		
		CheckLevelUpgraded(users.get(0), false);
		CheckLevelUpgraded(users.get(1), true);
		CheckLevelUpgraded(users.get(2), false);
		CheckLevelUpgraded(users.get(3), true);
		CheckLevelUpgraded(users.get(4), false);
	}
	
	/**
	 * 처음 가입하는 사용자는 BASIC 레벨이어야 한다.
	 * 이 로직을 담을 곳을 정하고 테스트한다.
	 */
	@Test
	public void add() {
		userDao.deleteAll();
		
		User userWithLevel = users.get(4);		// Gold level. 이미 지정된 사용자면 초기화 하지 않는다.
		User userWithoutLevel = users.get(0);	// 레벨이 비어 있는 사용자. 등록중에 BASIC 레벨도 설정돼야 한다.
		userWithoutLevel.setLevel(null);
		
		userService.add(userWithLevel);
		userService.add(userWithoutLevel);
		
		User userWithLevelRead = userDao.get(userWithLevel.getId());
		User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());
		
		assertEquals(userWithLevelRead.getLevel(), userWithLevel.getLevel());
		assertEquals(userWithoutLevelRead.getLevel(), Level.BASIC);
	}
	
	/**
	 * 강제 예외 발생 테스트.
	 * 사용자 레벨 업그레이드를 시도하다가 중간에 예외가 발생했을 경우,
	 * 그 전에 업그레이드했던 사용자도 다시 원래 상태로 돌아갔는지를 확인하는 작업이다.
	 */
	@Test
	public void upgradeAllOrNothing() {
		UserService testUserService = new TestUserService(users.get(3).getId());
		testUserService.setUserDao(this.userDao);		// userDao를 수동 DI 해준다.
		testUserService.setTransactionManager(this.transationManager);	// 트랜잭션 동기화에 필요한 transactionManager를 DI 해준다.
		testUserService.setMailSender(mailSender);
		
		userDao.deleteAll();
		
		for (User user : users) userDao.add(user);
		
		try {
			testUserService.upgradeLevels();
			fail("TestUserServiceException expected");
		} catch (TestUserServiceException e) {
			
		}
		
		CheckLevelUpgraded(users.get(1), false);
	}
	
	private void CheckLevelUpgraded(User user, boolean upgraded) {
		User userUpdate = userDao.get(user.getId());
		
		if (upgraded) {
			// 업그레이드가 일어났는지 확인
			assertEquals(userUpdate.getLevel(), user.getLevel().nextLevel());
		} else {
			// 업그레이드가 일어나지 않았는지 확인
			assertEquals(userUpdate.getLevel(), user.getLevel());
		}
	}
	
	/**
	 * 사용자 정보 중 두 번째와 네 번째가 업그레이드 대상이다.
	 * 네 번째 사용자를 처리하는 중에 예외를 발생시키고, 그 전에 처리한 두 번째 사용자의 정보가 취소됐는지, 아니면 그대로 남았는지 확인하자.
	 * 테스트에서만 사용할 클래스라서 스태틱 클래스로 만드는 것이 간편하다. 
	 * UserService에 필요한 기능이 있으므로 상속받는다.
	 */
	static class TestUserService extends UserService {
		private String id;
		
		// 예외를 발생시킬 id를 지정할 수 있게 만든다.
		private TestUserService(String id) {
			this.id = id;
		}
		
		protected void upgradeLevel(User user) {
			// 지정된 id가 발견되면 예외를 던져서 작업을 강제로 중단시킨다.
			if (user.getId().equals(this.id)) throw new TestUserServiceException();
			super.upgradeLevel(user);
		}
	}
	
	static class TestUserServiceException extends RuntimeException {
	}
}
