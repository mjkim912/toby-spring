package com.test.tobyspring.vol1.learningtest.template;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class CalcSumTest {

	Calculator calculator;
	String numFilepath;
	
	@Before public void setUp() {
		this.calculator = new Calculator();
		this.numFilepath = getClass().getResource("numbers.txt").getPath();
	}
	
	@Test public void sumOfNumbers() throws IOException {
		assertEquals(Double.valueOf(calculator.calcSum(this.numFilepath)), Double.valueOf(10));
	}
	
	@Test public void multiplyOfNumbers() throws IOException {
		assertEquals(Double.valueOf(calculator.calcMultiply(this.numFilepath)), Double.valueOf(24));
	}
	
	@Test public void concatenateStrings() throws IOException {
		assertEquals(Double.valueOf(calculator.concatenate(this.numFilepath)), Double.valueOf("1234"));
	}
}
