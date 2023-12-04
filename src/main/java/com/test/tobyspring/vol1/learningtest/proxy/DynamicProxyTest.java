package com.test.tobyspring.vol1.learningtest.proxy;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactoryBean;

public class DynamicProxyTest {

	@Test
	public void simpleProxy() {
		Hello hello = new HelloTarget();	// 타깃은 인터페이스를 통해 접근하는 습관을 들인다.
		
		assertEquals(hello.sayHello("Toby"), "Hello Toby");
		assertEquals(hello.sayHi("Toby"), "Hi Toby");
		assertEquals(hello.sayThankYou("Toby"), "Thank You Toby");
		
		Hello proxiedHello = new HelloUppercase(new HelloTarget()); 	// 프록시를 통해 타깃 오브젝트에 접근.
		
		assertEquals(proxiedHello.sayHello("Toby"), "HELLO TOBY");
		assertEquals(proxiedHello.sayHi("Toby"), "HI TOBY");
		assertEquals(proxiedHello.sayThankYou("Toby"), "THANK YOU TOBY");
		
		Hello dynamicProxiedHello = (Hello)Proxy.newProxyInstance(
				getClass().getClassLoader(), 				// 동적으로 생성되는 다이나믹 프록시 클래스의 로딩에 사용할 클래스 로
				new Class[] {Hello.class}, 					// 구현할 인터페이
				new UppercaseHandler(new HelloTarget()));	// 부가기능과 위임 코드를 담은 InvocationHandler
	}
	
	@Test
	public void proxyFactoryBean() {
		ProxyFactoryBean pfBean = new ProxyFactoryBean();
		pfBean.setTarget(new HelloTarget());
		pfBean.addAdvice(new UppercaseAdvice());
		
		Hello proxiedHello = (Hello) pfBean.getObject();
		
		assertEquals(proxiedHello.sayHello("Toby"), "HELLO TOBY");
		assertEquals(proxiedHello.sayHi("Toby"), "HI TOBY");
		assertEquals(proxiedHello.sayThankYou("Toby"), "THANK YOU TOBY");
	}
	
	static class UppercaseAdvice implements MethodInterceptor {
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String ret = (String)invocation.proceed();
			return ret.toUpperCase();
		}
	}
	
	/**
	 *  다이나믹 프록시를 만들어보자.
	 */
	static class UppercaseHandler implements InvocationHandler {
		Object target;
		
		private UppercaseHandler(Object target) {		// 어떤 종류의 인터페이스를 구현한 타깃에도 적용 가능하도록 Object 타입
			this.target = target;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			//String ret = (String) method.invoke(target, args);		// 타깃으로 위임. 인터페이스의 메소드 호출에 모두 적용.
			//return ret.toUpperCase();									// 부가기능 제공
			
			// 호출한 메소드의 리턴 타입이 String인 경우만 대문자 변경 기능 적용.
			Object ret = method.invoke(target, args);
			
			if (ret instanceof String) {
				return ((String)ret).toUpperCase();
			} else {
				return ret;
			}
		}
	}
	
	/*
	 * 데코레이터 패턴ㄴ을 적용해서 이 클래스에 부가기능을 추가하겠다.
	 * 위임과 기능 부가라는 두 가지 프록시의 기능을 모두 처리하는 전형적인 프록시 클래스이다.
	 */
	static class HelloUppercase implements Hello {
		Hello hello;	// 위임할 타깃 오브젝트. 여기서는 타깃 클래스의 오브젝트인 것은 알지만 다른 프록시를 추가할 수도 있으므로 인터페이스로 접근한다.
		
		public HelloUppercase(Hello hello) {
			this.hello = hello;
		}
		
		public String sayHello(String name) {
			return hello.sayHello(name).toUpperCase();	// 위임과 부가기능 적용
		}
		public String sayHi(String name) {
			return hello.sayHi(name).toUpperCase();
		}
		public String sayThankYou(String name) {
			return hello.sayThankYou(name).toUpperCase();
		}
	}
	
	// 타깃 클래스
	static class HelloTarget implements Hello {
		public String sayHello(String name) {
			return "Hello " + name;
		}
		public String sayHi(String name) {
			return "Hi " + name;
		}
		public String sayThankYou(String name) {
			return "Thank You " + name;
		}
	}
	
	static interface Hello {
		String sayHello(String name);
		String sayHi(String name);
		String sayThankYou(String name);
	}
}
