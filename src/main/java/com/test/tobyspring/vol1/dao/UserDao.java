package com.test.tobyspring.vol1.dao;

import java.util.List;

import com.test.tobyspring.vol1.domain.User;

public interface UserDao {

	void add(User user);
	User get(String id);
	List<User> getAll();
	void deleteAll();
	int getCount();
	void update(User user);
}
