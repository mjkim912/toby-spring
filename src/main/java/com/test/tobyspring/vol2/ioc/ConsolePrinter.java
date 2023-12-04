package com.test.tobyspring.vol2.ioc;

public class ConsolePrinter implements Printer{

	public void print(String message) {
		System.out.println(message);
	}
}
