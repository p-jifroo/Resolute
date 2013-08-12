<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Login</title>
</head>


<body>

<style type="text/css">
            #head{
            font: bold;
            color: maroon;
             text-decoration: blink;
            }
            
            #logo{
                position: relative;
                top:100%;
                bottom: 0%;
                
                
            }
            #ltable{
             margin-top: 200px;
            }
                 
          </style>
          
<div id ="head">

<h1 style="text-align: left; text-decoration-style: double;text-decoration-color: maroon;">Resolute</h1>
</div>
<div style="width: 1340px;height: 500px;border: 6px;border-style: solid; border-color: maroon" >
    
            <table align ="right" style="background-color:grey;color: white;" border="2" bordercolor="black">
                
                    <tr>
                        
                        <td><a href="manager.jsp">Manager</a></td>
                    </tr>
                
            </table>
  
     <div class="logo">
       
            
                <img src="http://cmac.concordia.ca/images/logos/logo_concordia_university.gif" width="200" height="100" />

            
        
        </div>

<div id ="ltable">
<s:form action="loginUser.action" method="post">
        <table  align ="center" border="0">
                      <tr>
                        <td style="text-align: left; "><b>Login</b></td>
                        
                    </tr>
                                                  
                    <tr>
                        <s:textfield name="name" label="User Name"></s:textfield>
                        
                    </tr>
                    <tr>
                        <s:password name="password" label="Password"></s:password>
                        
                        
                    </tr>
                    <tr>
                        <td><input type="submit" value="Submit" name="Submit" />  <input type="submit" value="Quit" name="Quit" /></td>
                        <td style="text-align: center"><a href="<s:url action="getPassword.action" />">Forget Password</a></td>
                    </tr>
                
            </table>
</s:form>
<br>

<table align="center" height="80" width="200">
	<font color=red align="center">
	<s:fielderror name="invalidLogin"/>
	</font>
</table>
	
     </div>

        </div>
    </body>
</html>
