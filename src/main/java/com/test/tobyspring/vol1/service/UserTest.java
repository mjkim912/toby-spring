package com.test.tobyspring.vol1.service;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.test.tobyspring.vol1.domain.Level;
import com.test.tobyspring.vol1.domain.User;

public class UserTest {
	User user;
	
	@Before
	public void setUp() {
		user = new User();
	}
	
	@Test
	public void upgradeLevel() {
		Level[] levels = Level.values();
		
		for (Level level : levels) {
			if (level.nextLevel() == null) continue;
			
			user.setLevel(level);
			user.upgradeLevel();
			assertEquals(user.getLevel(), level.nextLevel());
		}
	}
	
	// 업그레이드할 레벨이 없는 경우에 upgrade를 한다면 예외가 발생하는지 확인하는 테스트
	@Test(expected = IllegalStateException.class)
	public void cannotUpgradeLevel() {
		Level[] levels = Level.values();
		
		for (Level level : levels) {
			if (level.nextLevel() != null) continue;
			user.setLevel(level);
			user.upgradeLevel();
		}
	}
}
