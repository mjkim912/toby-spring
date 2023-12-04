package com.test.tobyspring.vol1.learningtest.proxy;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

public class ReflectionTest {

	@Test
	public void invokeMethod() throws Exception {
		String name = "Spring";
		
		// length()
		assertEquals(name.length(), 6);
		
		// 리플렉션 방식으로 호출
		Method lengthMethod = String.class.getMethod("length");
		assertEquals(lengthMethod.invoke(name), 6);
		
		// chartAt()
		assertEquals(name.charAt(0), 'S');
		
		// 리플렉션 방식으로 호출
		Method charAtMethod = String.class.getMethod("charAt", int.class);
		assertEquals(charAtMethod.invoke(name, 0), 'S');
	}
}
