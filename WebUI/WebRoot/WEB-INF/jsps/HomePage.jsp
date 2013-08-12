<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'home_k.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

  </head>
  
  <body>
  <form name ="login">
  <style type="text/css">
            #logo{
                font: bold;
                text-decoration-style: solid;
                color: maroon;
                
            }
            #footer{
                position: absolute;
                top: 90%;
                bottom: 10%
            }
            
                 
          </style>
          
<div id ="logo">

<h1 style="text-align: left; text-decoration-style: double;text-decoration-color: maroon;">Resolute</h1>
</div>
<div style="width: 1340 px;height: 400px;border: 5px;border-style: solid; border-color: maroon" >
    <header>
            <table align ="right" border="0">
                
                    <tr>
                        <td>&nbsp;</td>
                        <td><a href="#" title="skype"><img alt="" src="http://zapt4.staticworld.net/images/article/2012/10/skype_log-100010026-gallery.jpg"width="20" height ="18"></a></td>
                    </tr>
                
            </table>
  
</header>
<br><a>Hello<s:property value="#session['user'].userName"/></a>
<br>
<center>
        <p>
          <input type="submit" value="Check User" name="check" />
          <input type="submit" value="Analyze" name="analyze" />
          <input type="submit" value="Save" name="save" />
          <input type="submit" value="Report" name="report" />
          <a href="toResetPassword">Reset Password</a>
          <a href="logout">Logout</a>        </p>
        <p><a href="toFacebookChat"><img alt="" src="http://www.anac.on.ca/images/facebook_icon.gif" width="20" height ="20"></a> </p>
</center>
        
          
        
        <footer>
            <div class="logo">
                <img src="http://cmac.concordia.ca/images/logos/logo_concordia_university.gif" width="100" height="50"/>

            </div>
        </footer>
  </div>

  </body>
</html>
