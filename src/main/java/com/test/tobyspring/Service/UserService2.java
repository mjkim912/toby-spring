package com.test.tobyspring.Service;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.test.tobyspring.Dao.UserDao;
import com.test.tobyspring.Domain.Level;
import com.test.tobyspring.Domain.User;

/**
 * Chapter 5 까지의 내용.
 * 
 * userDao 빈을 DI 받는다. (UserDao : 인터페이스, userDao : 빈 오브젝트)
 * 
 * 트랜잭션 서비스의 추상화 방식을 도입하고, 이를 DI 를 통해 외부에서 제어하도록 만든 후
 * -> 사용자 관리 로직이 바뀌거나 추가되지 않는 한 이 클래스의 코드에는 손댈 이유가 없다.
 * 트랜잭션 기술이 바뀌고, 서버환경이 바뀌고, UserDao의 데이터를 가져오는 테이블이 바뀌고, JDBC 에서 JPA로 바뀌더라도 이 클래스는 수정할 이유가 없다.
 */
public class UserService2 {
	public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_FOR_GOLD = 30;

	UserDao userDao;
	
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	/*
	 * PlatformTransactionManager 를 사용하므로 이제 사용하지 않는다.
	 */
	private DataSource dataSource;
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * applicationContext.xml 에서
	 * DataSourceTransactionManager 는 dataSource 빈으로부터 Connection 을 가져와 트랜잭션을 처리해야 하기 때문에 dataSource 프로퍼티를 갖는다.
	 * userService 빈도 기존의 dataSource 프로퍼티를 없애고 transactionManager 빈을 DI 받는다.
	 */
	private PlatformTransactionManager transactionManager;
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	private MailSender mailSender;
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	/**
	 * 리펙토링을 해보자.
	 * -> upgradeLevels_before2
	 */
	public void upgradeLevels_before() {
		List<User> users = userDao.getAll();
		for (User user : users) {
			Boolean changed = null;
			if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
				user.setLevel(Level.SILVER);
				changed = true;
			} else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
				user.setLevel(Level.GOLD);
				changed = true;
			} else if (user.getLevel() == Level.GOLD) {
				changed = false;
			} else {
				changed = false;
			}
			
			if (changed) {
				userDao.update(user);
			}
		}
	}
	
	/**
	 * 트랜젝션 동기화 작업을 해보자.
	 * 리펙토링 -> upgradeLevels_before3
	 */
	public void upgradeLevels_before2() {
		List<User> users = userDao.getAll();
		for (User user : users) {
			if (canUpgradeLevel(user)) {
				upgradeLevel(user);
			}
		}
	}

	/**
	 * 트랜잭션 경계설정 메소드를 사용해 트랜잭션을 이용하는 전형적인 코드에 간단한 트랜잭션 동기화 작업만 붙여준다. 
	 * 이유는 모르겠지만 에러가 난다.
	 */
	public void upgradeLevels_before3() throws Exception {
		// 트랜젝션 동기화 작업 초기화.
		TransactionSynchronizationManager transactionSynchronizationManager = new TransactionSynchronizationManager(null);
		transactionSynchronizationManager.initSynchronization();
		
		
		Connection c = DataSourceUtils.getConnection(dataSource);
		c.setAutoCommit(false);
		
		try {
			List<User> users = userDao.getAll();
			for (User user : users) {
				if (canUpgradeLevel(user)) {
					upgradeLevel(user);
				}
			}
			// 정상적으로 작업을 마치면 트랜잭션 커밋
			c.commit();
		} catch (Exception e) {
			c.rollback();
			throw e;
		} finally {
			// 스프링 유틸리티를 통해 db connection을 안전하게 닫는다.
			DataSourceUtils.releaseConnection(c, dataSource);
			
			// 동기화 작업 종료 및 정리
			transactionSynchronizationManager.unbindResource(this.dataSource);
			transactionSynchronizationManager.clearSynchronization();
		}
	}

	/**
	 * 스프링의 트랜잭션 추상화 API를 적용한 버전.
	 * 최종의 최종 버전.
	 */
	public void upgradeLevels() {
		// 트랜잭션 경계설정
		// bean 으로 만들어서 사용하지 않는다.
		//PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		//PlatformTransactionManager txManager = new JtaTransactionManager(); 	JTA 버전
		
		// 트랜잭션 시작
		// DI 받은 트랜잭션 매니저를 공유해서 사용한다.
		// 멀티스레드 환경에서도 안전하다.
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 트랜잭션 안에서 진행되는 작업
			List<User> users = userDao.getAll();
			for (User user : users) {
				if (canUpgradeLevel(user)) {
					upgradeLevel(user);
				}
			}
			
			// 트랜잭션 커밋
			this.transactionManager.commit(status);
			
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
	}
	
	public void add(User user) {
		if (user.getLevel() == null) {
			user.setLevel(Level.BASIC);
		}
		
		userDao.add(user);
	}
	
	private boolean canUpgradeLevel(User user) {
		Level currentLevel = user.getLevel();
		
		switch (currentLevel) {
			case BASIC: return (user.getLogin() >= 50);
			case SILVER: return (user.getRecommend() >= 30);
			case GOLD: return false;
			default: throw new IllegalArgumentException("Unknown Level: " + currentLevel);
		}
	}
	
	/**
	 * 사용자 level과 레벨을 변경해준다는 로직이 너무 노골적으로 드러나 있다.
	 * 그리고 예외상황에 대한 처리가 없다.
	 * 레벨이 늘어나면 if문이 점점 길어지고 
	 * 레벨 변경 시 사용자 오브젝트에서 level 필드 외의 값도 같이 변경해야 한다면 조건 뒤에 붙는 내용도 점점 길어질 것이다.
	 * 
	 * 리펙토링 -> upgradeLevel
	 */
	private void upgradeLevel_before(User user) {
		if (user.getLevel() == Level.BASIC) user.setLevel(Level.SILVER);
		else if (user.getLevel() == Level.SILVER) user.setLevel(Level.GOLD);
		
		userDao.update(user);
	}
	
	private void sendUpgradeEMail(User user) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(user.getEmail());
		mailMessage.setFrom("useradmin@ksug.org");
		mailMessage.setSubject("Upgrade 안내");
		mailMessage.setText("사용자님의 등급이 " + user.getLevel().name());
		
		this.mailSender.send(mailMessage);
	}

	protected void upgradeLevel(User user) {
		user.upgradeLevel();
		userDao.update(user);
		sendUpgradeEMail(user);
	}
}
