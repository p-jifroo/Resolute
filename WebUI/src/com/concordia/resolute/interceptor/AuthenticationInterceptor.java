package com.concordia.resolute.interceptor;

import java.util.Map;

import com.concordia.ssh.user.User;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class AuthenticationInterceptor implements Interceptor {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception {
	
		Map<String, Object> session = actionInvocation.getInvocationContext().getSession();
		User user=(User)session.get("user");
		if(user==null){
			return "login";
		}
					
		return actionInvocation.invoke();
	}

	
}
