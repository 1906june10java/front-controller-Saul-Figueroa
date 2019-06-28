package com.revature.repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.revature.model.Customer;
import com.revature.util.ConnectionUtil;

/* JDBC implementation for DAO contract for Customers data access */
public class CustomerRepositoryJdbc implements CustomerRepository {
	
	private static Logger lOGGER = Logger.getLogger(CustomerRepositoryJdbc.class);
	
	/*Singleton transformation of JDBC implementation object */
	private static CustomerRepositoryJdbc customerDaoJdbc;
	
	private CustomerRepositoryJdbc() {
		
	}
	
	public static CustomerRepositoryJdbc getCustomerDaoJdbc() {
		if(customerDaoJdbc == null) {
			customerDaoJdbc = new CustomerRepositoryJdbc();
		}
		
		return customerDaoJdbc;
	}
	
	/* Regular insert statement for Customer */
	@Override
	public boolean insert(Customer customer) {
		try(Connection connection = ConnectionUtil.getConnection()) {
			int statementIndex = 0;
			String command = "INSERT INTO CUSTOMER VALUES(NULL,?,?,?,?)";

			PreparedStatement statement = connection.prepareStatement(command);

			//Set attributes to be inserted
			statement.setString(++statementIndex, customer.getFirstName().toUpperCase());
			statement.setString(++statementIndex, customer.getLastName().toUpperCase());
			statement.setString(++statementIndex, customer.getUsername().toLowerCase());
			statement.setString(++statementIndex, customer.getPassword());

			if(statement.executeUpdate() > 0) {
				return true;
			}
		} catch (SQLException e) {
			lOGGER.warn("Exception creating a new customer", e);
		}
		return false;
	}

	/* Insert a customer using the stored procedure we created */
	@Override
	public boolean insertProcedure(Customer customer) {
		try(Connection connection = ConnectionUtil.getConnection()) {
			int statementIndex = 0;
			
			//Pay attention to this syntax
			String command = "{CALL INSERT_CUSTOMER(?,?,?,?)}";
			
			//Notice the CallableStatement
			CallableStatement statement = connection.prepareCall(command);
			
			//Set attributes to be inserted
			statement.setString(++statementIndex, customer.getFirstName().toUpperCase());
			statement.setString(++statementIndex, customer.getLastName().toUpperCase());
			statement.setString(++statementIndex, customer.getUsername().toLowerCase());
			statement.setString(++statementIndex, customer.getPassword());
			
			if(statement.executeUpdate() > 0) {
				return true;
			}
		} catch (SQLException e) {
			lOGGER.warn("Exception creating a new customer with stored procedure", e);
		}
		return false;
	}

	/* Select Customer based on his username */
	@Override
	public Customer select(Customer customer) {
		try(Connection connection = ConnectionUtil.getConnection()) {
			int statementIndex = 0;
			String command = "SELECT * FROM CUSTOMER WHERE C_USERNAME = ?";
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(++statementIndex, customer.getUsername());
			ResultSet result = statement.executeQuery();

			while(result.next()) {
				return new Customer(
						result.getInt("C_ID"),
						result.getString("C_FIRST_NAME"),
						result.getString("C_LAST_NAME"),
						result.getString("C_USERNAME"),
						result.getString("C_PASSWORD")
						);
			}
		} catch (SQLException e) {
			lOGGER.warn("Exception selecting a customer", e);
		}
		return new Customer();
	}

	/* Select all customers */
	public List<Customer> selectAll() {
		try(Connection connection = ConnectionUtil.getConnection()) {
			String command = "SELECT * FROM CUSTOMER";
			PreparedStatement statement = connection.prepareStatement(command);
			ResultSet result = statement.executeQuery();

			List<Customer> customerList = new ArrayList<>();
			while(result.next()) {
				customerList.add(new Customer(
						result.getInt("C_ID"),
						result.getString("C_FIRST_NAME"),
						result.getString("C_LAST_NAME"),
						result.getString("C_USERNAME"),
						result.getString("C_PASSWORD")
						));
			}

			return customerList;
		} catch (SQLException e) {
			lOGGER.warn("Exception selecting all customers", e);
		} 
		return new ArrayList<>();
	}

	/* Get a customer hash consuming the user defined function we created */
	@Override
	public String getCustomerHash(Customer customer) {
		try(Connection connection = ConnectionUtil.getConnection()) {
			int statementIndex = 0;
			String command = "SELECT GET_CUSTOMER_HASH(?,?) AS HASH FROM DUAL";
			PreparedStatement statement = connection.prepareStatement(command);
			statement.setString(++statementIndex, customer.getUsername());
			statement.setString(++statementIndex, customer.getPassword());
			ResultSet result = statement.executeQuery();

			if(result.next()) {
				return result.getString("HASH");
			}
		} catch (SQLException e) {
			lOGGER.warn("Exception getting customer hash", e);
		} 
		return new String();
	}
	
	public static void main(String[] args) {
		
		CustomerRepositoryJdbc repository = new CustomerRepositoryJdbc();
		
		
		//repository.insert(new Customer(1,"Saul","Figueroa","SAULEFF","1234"));
		
		lOGGER.info(repository.select(new Customer()));
	
		
	}
	
}
