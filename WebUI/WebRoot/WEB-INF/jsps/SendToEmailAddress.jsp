<%-- 
    Document   : reset_password
    Created on : Jul 23, 2013, 2:05:39 PM
    Author     : kalyan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
    
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Reset password</title>
    </head>
    <body>
        <style type="text/css">
            #head{
            font: bold;
            color: maroon;
             
            }
            
            #logo{
                margin-bottom: 10px;
                margin-top: 100px;
                
                
                
            }
                
          </style>
          
<div id ="head">

<h1 style="text-align: left; text-decoration-style: double;text-decoration-color: maroon;">Resolute</h1>
</div>
<div style="width: 1490px;height: 600px;border: 6px;border-style: solid; border-color: maroon" >
        <p><b>kindly enter you valid email</b></p>
<s:form action="emailSend.action" theme="simple" >

    <s:textarea name="email.emailAddress" label="Enter your email ID" cols="30" rows="1"></s:textarea>

        <td><input type="submit" value="Send" name="reset" /></td>
 </s:form>
<div class="logo">
       
    <footer>
        <img src="http://cmac.concordia.ca/images/logos/logo_concordia_university.gif" width="200" height="100"/>

            </footer>
        
        </div>    

</body>
    
</html>
