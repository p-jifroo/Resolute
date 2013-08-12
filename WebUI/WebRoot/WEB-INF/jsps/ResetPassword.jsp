<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
  <body>

<li><a href="<s:url action="toHomePage.action" />">Home Page</a></li>

   <s:form action="resetPassword.action" theme="simple" >
   		Old         Password:<s:password name="olderPassword" label="Older Password" /><font color=red >${errors.olderPassword[0]}</font><br>
   		
   		New         Password:<s:password name="newPassword" label="New Password"/><font color=red >${errors.newPassword[0]} ${errors.oldMatch[0]}</font><br>
   		Reenter New Password:<s:password name="reNewPassword" label="Reenter New Password"/><font color=red >${errors.reNewPassword[0]}${errors.newMatch[0]}</font><br>
   		<s:submit value="Submit"></s:submit>
   </s:form>

  </body>
</html>
