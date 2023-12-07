package com.test.tobyspring.vol2.ioc;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class ApplicationContextTest {
	private String basePath = StringUtils.cleanPath(ClassUtils.classPackageAsResourcePath(getClass())) + "/";
	
	@Test
	public void registerBean() {
		StaticApplicationContext ac = new StaticApplicationContext();	// ioc 컨테이너 생성. 생성과 동시에 컨테이너로 동작한다.
		ac.registerSingleton("hello1", Hello.class);					// Hello 클래스를 hello1이라는 이름의 싱글톤 빈으로 컨테이너에 등록한다.
		
		Hello hello1 = ac.getBean("hello1", Hello.class);				// ioc 컨테이너가 등록한 빈을 생성했는지 확인하기 위해 빈을 요청하
		assertNotNull(hello1);											// null 값이 아닌지 확인한다.
		
		
		// BeanDefinition 을 이용한 빈 등
		BeanDefinition helloDef = new RootBeanDefinition(Hello.class);	// <bean class="com.test.tobyspr...Hello" /> 에 해당하는 메타정보설정
		helloDef.getPropertyValues().addPropertyValue("name", "Spring");
		ac.registerBeanDefinition("hello2", helloDef);					// 앞에 설정한 빈 메타정보를 등록한다. <bean id="hello2" .. /> 에 해당
		
		Hello hello2 = ac.getBean("hello2", Hello.class);
		assertEquals(hello2.sayHello(), "Hello Spring");
		assertNotEquals(hello1, hello2);								// 처음 등록한 빈과 두 번째 등록한 빈이 모두 동일한 Hello 클래스이지만 별개의 오브젝트로 생성됐다.
		assertEquals(ac.getBeanFactory().getBeanDefinitionCount(), 2);
	}
	
	// DI 정보 테스
	@Test
	public void registerBeanWithDependency() {
		StaticApplicationContext ac = new StaticApplicationContext();
		
		ac.registerBeanDefinition("printer", new RootBeanDefinition(StringPrinter.class)); 	// SringPrinter 클래스 타입이며 printer라는 이름을 가진 빈 등록
		
		BeanDefinition helloDef = new RootBeanDefinition(Hello.class);
		helloDef.getPropertyValues().addPropertyValue("name", "Spring");
		
		// 아이디가 printer인 빈에 대한 레퍼런스를 프로퍼티로 등록
		helloDef.getPropertyValues().addPropertyValue("printer", new RuntimeBeanReference("printer"));
		
		ac.registerBeanDefinition("hello", helloDef);
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertEquals(ac.getBean("printer").toString(), "Hello Spring");
		
  	}
	
	// GenericApplicationContext 테스트
	@Test
	public void genericApplicationContext() {
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.loadBeanDefinitions("com/test/tobyspring/vol2/ioc/genericApplicationContext.xml");
		
		ac.refresh();	// 모든 메타정보가 등록이 완료됐으니 애플리케이션 컨테이너를 초기화하라는 명령이다.
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertEquals(ac.getBean("printer").toString(), "Hello Spring");
	}
	
	// GenericXmlApplicationContext 테스트
	@Test
	public void genericXmlApplicationContext() {
		GenericApplicationContext ac = new GenericXmlApplicationContext("com/test/tobyspring/vol2/ioc/genericApplicationContext.xml");
		
		Hello hello = ac.getBean("hello", Hello.class);
		hello.print();
		
		assertEquals(ac.getBean("printer").toString(), "Hello Spring");
	}
	
	@Test
	public void contextHierarchy() {
		ApplicationContext parent = new GenericXmlApplicationContext(basePath + "parentContext.xml");
		
		// 자식 컨텍스트 생성
		GenericApplicationContext child = new GenericApplicationContext(parent);
		
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(child);
		reader.loadBeanDefinitions(basePath + "childContext.xml");
		child.refresh();		// reader를 사용해서 읽은 경우에는 반드시 초기화 해줘야 한다.
		
		// 자식 컨텍스트에 printer가 있는지 찾아보자.
		Printer printer = child.getBean("printer", Printer.class);
		assertNotNull(printer);
		
		Hello hello = child.getBean("hello", Hello.class);
		assertNotNull(hello);
		
		// Hello 는 둘 다 있기 때문에 자식에서 가져온다.
		hello.print();
		assertEquals(printer.toString(), "Hello Child");
	}
	
	@Test
	public void simpleBeanScanning() {
		// 스캔할 패키지를 넣어서 컨텍스트를 만들어준다. 생성과 동시에 자동으로 스캔과 등록이 진행된다.
		ApplicationContext ctx = new AnnotationConfigApplicationContext("com.test.tobyspring.vol2.ioc");
		
		AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
		
		assertNotNull(hello);
	}
	
	@Test
	public void configurationBean() {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(AnnotatedHelloConfig.class);
		
		AnnotatedHello hello = ctx.getBean("annotatedHello", AnnotatedHello.class);
		assertNotNull(hello);
		
		AnnotatedHelloConfig config = ctx.getBean("annotatedHelloConfig", AnnotatedHelloConfig.class);
		assertNotNull(config);
		
		assertNotEquals(config.annotatedHello(), sameInstance(hello));
	}
	
	// 싱글톤 빈 테스트 
	@Test
	public void singletonScope() {
		ApplicationContext ac = new AnnotationConfigApplicationContext(SingletonBean.class, SingletonClientBean.class);
		Set<SingletonBean> beans = new HashSet<SingletonBean>();	// 중복허용하지 않음.
		
		// DL 에서 싱글톤 확인 
		beans.add(ac.getBean(SingletonBean.class));
		beans.add(ac.getBean(SingletonBean.class));
		assertEquals(beans.size(), 1);
		
		// DI 에서 싱글톤 확인
		beans.add(ac.getBean(SingletonClientBean.class).bean1);
		beans.add(ac.getBean(SingletonClientBean.class).bean2);
		assertEquals(beans.size(), 1);
	}
	
	static class SingletonBean {}	// 싱글톤 스코프 빈. 스코프 빈 메타정보의 디폴트 값은 "싱글톤" 이기 때문에 별도의 스코프 설정은 필요 없다.
	static class SingletonClientBean {
		@Autowired SingletonBean bean1;		// 한 번 이상 DI 가 일어날 수 있도록 두 개의 DI 용 프로퍼티를 선언해뒀다.
		@Autowired SingletonBean bean2;
	}
	
	// 프로토타입 빈 테스트
	@Test
	public void prototypeScope() {
		ApplicationContext ac = new AnnotationConfigApplicationContext(PrototypeBean.class, PrototypeClientBean.class);
		Set<PrototypeBean> bean = new HashSet<PrototypeBean>();
		
		// DL 방식으로 컨테이너에 빈을 요청ㅇ할 때마다 새로운 빈 오브젝트가 만들어진다.
		bean.add(ac.getBean(PrototypeBean.class));
		assertEquals(bean.size(), 1);
		bean.add(ac.getBean(PrototypeBean.class));
		assertEquals(bean.size(), 2);
		
		// DI 방식으로 주입받는 프로퍼티마다 다른 오브젝트가 만들어진다.
		bean.add(ac.getBean(PrototypeClientBean.class).bean1);
		assertEquals(bean.size(), 3);
		bean.add(ac.getBean(PrototypeClientBean.class).bean2);
		assertEquals(bean.size(), 4);
	}
	
	@Scope("prototype")	// 프로토타입 빈 설정
	static class PrototypeBean {}
	static class PrototypeClientBean {
		@Autowired PrototypeBean bean1;
		@Autowired PrototypeBean bean2;
	}
	
	@Test
	public void simpleAtAutowired() {
		AbstractApplicationContext ac = new AnnotationConfigApplicationContext(BeanA.class, BeanB.class);
		
		BeanA beanA = ac.getBean(BeanA.class);
		assertNotNull(beanA.beanB);
	}
	
	private static class BeanA {
		@Autowired BeanB beanB;
	}
	
	private static class BeanB {
	}
}
