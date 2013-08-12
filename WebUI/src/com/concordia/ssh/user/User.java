package com.concordia.ssh.user;

/**
 * User entity. @author MyEclipse Persistence Tools
 */

public class User implements java.io.Serializable {

	// Fields

	private Integer userId;
	private String userName;
	private String passWord;
	private String userPriority;
	private String emailAddress;

	// Constructors

	/** default constructor */
	public User() {
	}

	/** full constructor */
	public User(String userName, String passWord, String userPriority,
			String emailAddress) {
		this.userName = userName;
		this.passWord = passWord;
		this.userPriority = userPriority;
		this.emailAddress = emailAddress;
	}

	public User(String userName, String passWord) {
		this.userName = userName;
		this.passWord = passWord;
//		this.userPriority = userPriority;
	}
	
	
	// Property accessors

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return this.passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getUserPriority() {
		return this.userPriority;
	}

	public void setUserPriority(String userPriority) {
		this.userPriority = userPriority;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}