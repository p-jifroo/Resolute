package com.concordia.resolute.action;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
//import org.springframework.context.ApplicationContext;
//import org.springframework.web.context.support.WebApplicationContextUtils;

import com.concordia.resolute.email.Email;
import com.concordia.ssh.user.User;
import com.concordia.ssh.user.UserDAO;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @version 1.1
 * @author Resolute Group
 * @since 2013/06/01
 */
public class Action extends ActionSupport implements SessionAware {

	private static final long serialVersionUID = 2L;

	// name used for login.
	private String name;
	private String password;

	// session
	private Map<String, Object> session;

	// resetPassword name used for reset password
	private String olderPassword;
	private String newPassword;
	private String reNewPassword;

	// variables for email information
	private String to;// the destination email address
	private String body;// the email content
	static final String emailName = "wangnanjinxiao@gmail.com";// server side
																// email address
	static final String emailPassword = "1Q2W3e4r";// email password
	static final String emailSubject = "Resolute sending your older password";// the constant email title
																				

	// domain model of email
	private Email email;

	// email standard definition
	static Properties properties = new Properties();
	static {
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.socketFactory.port", "465");
		properties.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "465");
	}

	/**
	 * This method using for sending the older password to the user. For first
	 * edition, in later edition, this will modified by encrypting the string
	 * info.
	 * 
	 * @return SUCCESS when success sending, INPUT when not success sending.
	 */
	public String sendingPassword() {
		String ret = SUCCESS;
		if(this.email.getEmailAddress()==null){
			this.addFieldError("empty", "invalid email address");
			return INPUT;
			
		}
		
		try {
			// get the current user's password
			ApplicationContext ac = WebApplicationContextUtils
					.getWebApplicationContext(ServletActionContext
							.getServletContext());
			// get the object from the ApplicationContext file.
			UserDAO userDao = UserDAO.getFromApplicationContext(ac);
			// List<User> listUser = userDao.findByUserName(this.getUserName());
			List<User> listUser = userDao.findByEmailAddress(this.email
					.getEmailAddress());
			
			if(listUser.size()==0){
				this.addFieldError("invalid", "invalid email address");
				return INPUT;
			}
			
			// all the userName should be unique
			for (int i = 0; i < listUser.size(); i++) {
				User user = listUser.get(i);
				this.setBody("Dear user, this is your password: "
						+ user.getPassWord());
				this.setTo(user.getEmailAddress());
			}

			// to safety authenticate the user name and password.
			Session session = Session.getDefaultInstance(properties,
					new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(emailName,
									emailPassword);
						}
					});
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			message.setSubject(emailSubject);
			message.setText(body);
			Transport.send(message);
		} catch (Exception e) {
			this.addFieldError("invalidSendPassword", "invalid user name");
			ret = INPUT;
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * This method used for set a new password.
	 * 
	 * @return successfully change the password return success. fail to
	 *  change the password, return input.
	 */
	public String resetPassword() {

		ApplicationContext ct = WebApplicationContextUtils
				.getWebApplicationContext(ServletActionContext
						.getServletContext());
		UserDAO dao = UserDAO.getFromApplicationContext(ct);
		// find the user information by user name
		
		List<User> userList = dao.findByUserName(((User)session.get("user")).getUserName());
		if (userList.isEmpty()) {
			this.addFieldError("invalidUserName", "invalid userName");
			return INPUT;
		} else {
			// here, we should make sure that all the user name
			// must be unique.
			for (int i = 0; i < userList.size(); i++) {
				User user = userList.get(i);
				user.setPassWord(this.newPassword);
				dao.merge(user);
			}
		}

		return SUCCESS;

	}

	/**
	 * This method used for check user information, if validated then return
	 * success
	 * 
	 * @return success successfully validated the user information to login,
	 *         input: fail login in, return back into the login page.
	 * 
	 */
	public String login() {

		// initialization, to clear the fieldErrors.
		this.clearFieldErrors();

		User user = (User) session.get("user");
		// user has already login
		if (user != null) {
			// Gate.init();
			return SUCCESS;
		}
		// user first time to login
		else {
			ApplicationContext ct = WebApplicationContextUtils
					.getWebApplicationContext(ServletActionContext
							.getServletContext());
			UserDAO dao = UserDAO.getFromApplicationContext(ct);
			List<User> list = dao.findAll();
			int n = list.size();
			for (int i = 0; i < n; i++) {
				User ut = list.get(i);
				// match with the user name and password, set session and return
				// success
				if (ut.getUserName().equals(this.name)
						&& ut.getPassWord().equals(this.password)) {
					User u = new User(this.name, this.password);
					session.put("user",u);		
//					System.out.println("login user name is:"+((User)session.get("user")).getUserName());

					return SUCCESS;
				}

			}
			// add field error with name invalidLogin.
			this.addFieldError("invalidLogin", "invalid username or password");
			return INPUT;

		}
	}
	
	/**
	 * Logout action
	 * @return SUCCESS
	 */
	public String logout(){
		if(session==null)
		{
			return SUCCESS;
		}
		session.remove("user");
		return SUCCESS;
	}
	

	/**
	 * This method used for linking to send password page 
	 * @return success link to send password page
	 */
	public String toSendPassword() {
		return SUCCESS;
	}

	/**
	 * this method for entering the reset password page
	 * 
	 * @return success link to reset password page
	 */
	public String toResetPassword() {
		return SUCCESS;
	}

	/**
	 * This method for linking to the home page
	 * 
	 * @return Success link to home page
	 */
	public String toHomePage() {
		return SUCCESS;
	}
	
	/**
	 * This method dispatch to facebook chat jsp
	 * @return
	 */
	public String toFacebookChat(){
		return SUCCESS;
	}
	

	/**
	 * This method is server side validation on user reset password.
	 */
	public void validateResetPassword() {

		if (this.olderPassword.isEmpty()) {
			this.addFieldError("olderPassword", "password empty!");
		}
		if (this.newPassword.isEmpty()) {
			this.addFieldError("newPassword", "new Password empty!");
		}
		if (this.reNewPassword.isEmpty()) {
			this.addFieldError("reNewPassword", "Password is empty!");
		}
		if (!this.newPassword.equals(this.reNewPassword)) {
			this.addFieldError("newMatch", "two password does not match!");
		}
		if (this.olderPassword.equals(this.newPassword)) {
			this.addFieldError("oldMatch",
					"new password should not be same with old one!");
		}

	}

	/**
	 * This method get the user name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		this.session = session;
	}

	public Map<String, Object> getSession() {
		return session;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getOlderPassword() {
		return olderPassword;
	}

	public void setOlderPassword(String olderPassword) {
		this.olderPassword = olderPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getReNewPassword() {
		return reNewPassword;
	}

	public void setReNewPassword(String reNewPassword) {
		this.reNewPassword = reNewPassword;
	}


	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

}