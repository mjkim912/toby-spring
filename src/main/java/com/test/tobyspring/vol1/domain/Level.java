package com.test.tobyspring.vol1.domain;

public enum Level {
	GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);	// 레벨의 int 값과 다음 단계의 레벨 정보 
	
	private final int value;
	private final Level next;
	
	Level(int value, Level next) {
		this.value = value;
		this.next = next;
	}
	
	public int intValue() {
		return value;
	}
	
	public Level nextLevel() {
		return this.next;
	}
	
	public static Level valueOf(int value) {
		switch(value) {
			case 1: return BASIC;
			case 2: return SILVER;
			case 3: return GOLD;
			default: throw new AssertionError("Unknown value: " + value);
		}
	}
}
