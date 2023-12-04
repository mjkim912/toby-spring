package com.test.tobyspring.vol1.service;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.test.tobyspring.vol1.domain.User;

/**
 * 트랜잭션 경계설정 작업만 담은 클래스이다.
 * 
 * transactionManager 는 UserServiceTx의 빈이,
 * userDao, mailSender 는 UserServiceImpl 빈이 각각 의존한다.
 * 
 * TxProxyFactoryBean 클래스를 사용함에따라 이 클래스는 사용하지 않는다.
 */
public class UserServiceTx implements UserService {
	// 타깃 오브젝
	UserService userService;
	
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	private PlatformTransactionManager transactionManager;
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	// 메소드 구현과 위임
	public void add(User user) {
		userService.add(user);
	}
	
	public void upgradeLevels() {
		// 트랜잭션 시작
		// 부가기능 수행
		TransactionStatus status = this.transactionManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			// 위임
			userService.upgradeLevels();
			
			// 트랜잭션 커밋
			// 부가기능 수행
			this.transactionManager.commit(status);
			
		} catch (RuntimeException e) {
			this.transactionManager.rollback(status);
			throw e;
		}
	}
}
