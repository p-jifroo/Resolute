package com.concordia.resolute.test;

import com.concordia.resolute.action.Action;

import junit.framework.TestCase;

public class unitTest_Login extends TestCase {
	
	public void testLogin(){
		Action ac= new Action();
		assertEquals("login_suceess",ac.login());
		
	}
	

}
