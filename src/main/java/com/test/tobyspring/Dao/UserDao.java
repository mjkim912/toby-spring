package com.test.tobyspring.Dao;

import java.util.List;

import com.test.tobyspring.Domain.User;

public interface UserDao {

	void add(User user);
	User get(String id);
	List<User> getAll();
	void deleteAll();
	int getCount();
}
