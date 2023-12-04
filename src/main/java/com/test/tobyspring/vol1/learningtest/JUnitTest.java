package com.test.tobyspring.vol1.learningtest;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class JUnitTest {
	
	@Autowired
	ApplicationContext context;

	static Set<JUnitTest> testObjects = new HashSet<JUnitTest>();
	static ApplicationContext contextObject = null;
	
	@Test
	public void test1() {
		assertNotEquals(this, isException(this));
		testObjects.add(this);
		
		assertEquals(contextObject == null || contextObject == this.context, true);
		contextObject = this.context;
	}
	
	private Object isException(JUnitTest jUnitTest) {
		// TODO Auto-generated method stub
		return jUnitTest.hashCode();
	}

	@Test
	public void test2() {
		assertNotEquals(this, isException(this));
		testObjects.add(this);
		
		assertEquals(contextObject == null || contextObject == this.context, true);
		contextObject = this.context;
	}
	
	@Test
	public void test3() {
		assertNotEquals(this, isException(this));
		testObjects.add(this);
		
		//assertEquals(contextObject, either(is(nullValue())).or(is(this.context)));
	}
}
