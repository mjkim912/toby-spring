package com.test.tobyspring.Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;

import com.test.tobyspring.Domain.User;

/**
 * ConnectionMaker 인터페이스에게만 직접 의존한다. DConnectionMaker는 알지도 못한다.
 * 결합도가 낮다고 할 수 있다.
 * @author mjkim
 *
 */
public class UserDao {

	//private SimpleConnectionMaker simpleConnectionMaker;
	private ConnectionMaker connectionMaker;			// 읽기전용 인스턴스 변수
	public void setConnectionMaker(ConnectionMaker connectionMaker) {
		//simpleConnectionMaker = new SimpleConnectionMaker();
		//connectionMaker = (ConnectionMaker) new DConnectionMaker();
		this.connectionMaker = connectionMaker;
	}
	
	private DataSource dataSource;
	private JdbcContext jdbcContext;
	
	public void setDataSource(DataSource dataSource) {	// JdbcContext 에 대한 생성, DI 작업을 동시에 수행한다.
		this.jdbcContext = new JdbcContext();
		this.jdbcContext.setDataSource(dataSource);		// 의존 오브젝트 주입 (DI)
		this.dataSource = dataSource;
	}
	
	
	
	public void jdbcContextWithStatementStrategy(StatementStrategy stmt) throws SQLException, ClassNotFoundException {
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = connectionMaker.makeConnection();

			ps = stmt.makePreparedStatement(c);
		
			ps.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (ps != null) { try { ps.close(); } catch (SQLException e) {} }
			if (c != null) { try {c.close(); } catch (SQLException e) {} }
		}
	}
	
	public void add(final User user) throws ClassNotFoundException, SQLException {
		// JdbcContext 클래스 분리
		this.jdbcContext.workWithStatementStrategy(
				new StatementStrategy() {
					public PreparedStatement makePreparedStatement(Connection c) 
							throws SQLException {
						PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?, ?, ?)");
						ps.setString(1, user.getId());
						ps.setString(2, user.getName());
						ps.setString(3, user.getPassword());
						
						return ps;
					}
				}
		);
	}
	
	public User get(String id) throws ClassNotFoundException, SQLException {
		//Connection c = getConnection();
		//Connection c = simpleConnectionMaker.makeNewConnection();
		Connection c = connectionMaker.makeConnection();
		
		PreparedStatement ps = c.prepareStatement(
				"select * from users where id = ?");
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		User user = null;
		
		if (rs.next()) {
			user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
		}
		
		rs.close();
		ps.close();
		c.close();
		
		if (user == null) throw new EmptyResultDataAccessException(1);
		
		return user;
	}
	
	public void deleteAll() throws SQLException, ClassNotFoundException {
		this.jdbcContext.executeSql("delete from users");
		
		/*
		jdbcContextWithStatementStrategy(
				new StatementStrategy() {
					public PreparedStatement makePreparedStatement(Connection c)
							throws SQLException {
						return c.prepareStatement("delete from users");
					}
				}
		);
		*/
	}
	
	public int getCount() throws ClassNotFoundException, SQLException {
		Connection c = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			c = connectionMaker.makeConnection();
			ps = c.prepareStatement("select count(*) from users");
			rs = ps.executeQuery();
			rs.next();
			
			return rs.getInt(1);
		} catch (SQLException e) {
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					
				}
			}
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					
				}
			}
		}
	}
	
	private Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection c = DriverManager.getConnection("jdbc:mysql://localhost/toby", "root", "rhantls");
		return c;
	}
	
	/*
	 * 초기 테스트 방법 -> UserDaoTest 로 옮김.
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		UserDao dao = new UserDao();
		
		User user = new User();
		user.setId("whiteship");
		user.setName("백기선");
		user.setPassword("married");
		
		dao.add(user);
		
		System.out.println(user.getId() + " 등록성공");
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		System.out.println(user2.getPassword());
		
		System.out.println(user2.getId() + " 조회 성공");
	}
	*/
}
