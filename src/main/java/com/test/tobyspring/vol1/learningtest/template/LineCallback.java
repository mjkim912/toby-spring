package com.test.tobyspring.vol1.learningtest.template;

public interface LineCallback<T> {
	T doSomethingWithLine(String line, T value);
}
