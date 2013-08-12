<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'liaoTian.jsp' starting page</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	
	<link rel="stylesheet" type="text/css" href="../css/FacebookFont.css">
	<link rel="stylesheet" type="text/css" href="../css/FacebookFramework.css">
	<script type="text/javascript" src="jquery-1.10.2.js"></script> 




<script type="text/javascript">


var xmlHttp;
var xmlHttp1;
var updateType;
function createXMLHttpRequest(){
	if(window.ActiveXObject){
		xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	else if(window.XMLHttpRequest){
		xmlHttp = new XMLHttpRequest();
	}
}

function createXMLHttpRequest1(){
	if(window.ActiveXObject){
		xmlHttp1 = new ActiveXObject("Microsoft.XMLHTTP");
	}
	else if(window.XMLHttpRequest){
		xmlHttp1 = new XMLHttpRequest();
	}
}



function submitChat(){
	var content=document.getElementById("content").value;
	//process the submit chat, followed with content and time
    var url = "/thomasResoluteProject/submitChat.action?content="+content+"&ts="+new Date().getTime();
    
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = submitChange;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
}
//process after responding from the server.
function submitChange(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			submitUpdate();
		}
	}
}
function submitUpdate(){
    chatClear();
    var xml=xmlHttp.responseXML;    
}
function chatClear(){
	document.getElementById("content").value="";
}






function ageDetection(){
	var url="/thomasResoluteProject/ageDetection.action?ts="+new Date().getTime();
	createXMLHttpRequest();
	xmlHttp.onreadystatechange = ageDetect;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
}

function ageDetect(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			hasAgeInfo();
		}
	}

}

function hasAgeInfo(){

    var xml=xmlHttp.responseXML;
    
    var age=xml.getElementsByTagName("age");
    
    var ageFlag=age[0].firstChild==null?"":age[0].firstChild.nodeValue;
    
    if(ageFlag=="true"){//new chatting
    	getAgeInfo();
    }
    

}


function getAgeInfo(){
    var url = "/thomasResoluteProject/getAgeInfo.action?ts="+new Date().getTime();
    
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = ageChange;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}
function ageChange(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			ageUpdate();
		}
	}
}
function ageUpdate(){
    
    var xml=xmlHttp.responseXML;
    
    var ageInfo=xml.getElementsByTagName("ageValue");
    
    var ageValue=ageInfo[0].firstChild==null?"":ageInfo[0].firstChild.nodeValue;
    alert("123");
    if(ageValue!="" && ageValue != null){
    	document.getElementById("NLPDetection").value=ageValue;
    	setTimeout("chuLiEr()",10000);
    }
    
}


///////////////////////////start/////////////////////////////////////////////////
function checkUpdate(){
  var url = "/thomasResoluteProject/checkUpdate.action?ts="+new Date().getTime();
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = respondChange;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}

function respondChange(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			updateChange();
		}
	}
}

function updateChange(){

    var xml=xmlHttp.responseXML;
    
    var hasChat=xml.getElementsByTagName("hasNewChat");
    var getAge=xml.getElementsByTagName("ageDetect");
    
    var chatFlag=hasChat[0].firstChild==null?"":hasChat[0].firstChild.nodeValue;
    var ageFlag=getAge[0].firstChild==null?"":getAge[0].firstChild.nodeValue;
    
  	if(chatFlag=="true" && ageFlag =="true"){
  	
  		updatingInfo('true', 'true');
  	}
  	else if(chatFlag=="true"){
  		updatingInfo('true', 'false');
  	}
  	else if(ageFlag=="true"){
  		updatingInfo('false','true');
  	}
  	else{
  	}
  	
  	

}

//
function updatingInfo(chatInfo, ageInfo){

	if(chatInfo=="true" && ageInfo=="true"){
		updateType=11;
	}
	else if(chatInfo=="true"){
		updateType=10;
	}
	else if(ageInfo=="true"){
		updateType=01;
	}
	else{
		updateType=00;
	}
	
    var url = "/thomasResoluteProject/updateInfo.action?updateType="+updateType+"&ts="+new Date().getTime();
    ////////////to be continue
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = infoRespondeUpdate;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}



function infoRespondeUpdate(){
	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			updateData();
		}
	}
}


function updateData(){

    var xml=xmlHttp.responseXML;
    var updatedChatContent=xml.getElementsByTagName("updatedChatContent");
    var updatedAgeInfo=xml.getElementsByTagName("updatedAgeInfo");
    
    var chatInfo=updatedChatContent[0].firstChild==null?"":updatedChatContent[0].firstChild.nodeValue;
    var ageInfo=updatedAgeInfo[0].firstChild==null?"":updatedAgeInfo[0].firstChild.nodeValue;
    
    alert(chatInfo);
    alert(ageInfo);
    
  	if(chatInfo!="" && chatInfo != null){
    	document.getElementById("ChatContent").value=chatInfo;
    	setTimeout("chuLiEr()",10000);
    }
    if(ageInfo!="" && ageInfo != null){
    	document.getElementById("NLPDetection").value=ageInfo;
    	setTimeout("chuLiEr()",10000);
    }
    


}



//////////////end/////////////////////////////////////////////////////////



function check(){
    var url = "/thomasResoluteProject/checkChatUpdate.action?ts="+new Date().getTime();
    
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = check_change;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}
function check_change(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			check_update();
		}
	}
}
function check_update(){
    check_clear();
    
    var xml=xmlHttp.responseXML;
    
    var app_flags=xml.getElementsByTagName("app_flag");
    
    var app_flag=app_flags[0].firstChild==null?"":app_flags[0].firstChild.nodeValue;
    
    if(app_flag=="true"){//new chatting
    	hasNewDialog();
    }
    
}
function check_clear(){
	
}



function hasNewDialog(){
    var url = "/thomasResoluteProject/processDialog.action?ts="+new Date().getTime();
    
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = dialogChange;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}
function dialogChange(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			dialogUpdate();
		}
	}
}
function dialogUpdate(){
	
    duQu_clear();
    
    var xml=xmlHttp.responseXML;
    
    var app_contents=xml.getElementsByTagName("app_content");
    
    var app_content=app_contents[0].firstChild==null?"":app_contents[0].firstChild.nodeValue;
    
    if(app_content!="" && app_content != null){//有人说话了，添加新的聊天内容
    	document.getElementById("ChatContent").value=app_content;
    	setTimeout("chuLiEr()",10000);
    }
    
}
function duQu_clear(){
	
}



function facebookLogin(){  
	
	var facebookUser =document.getElementById("facebookUserName").value;
	var facebookPassword = document.getElementById("facebookPassword").value;
	
	var url = "/thomasResoluteProject/facebookLogin.action?facebookUser="+facebookUser+"&facebookPassword="+facebookPassword+"&ts="+new Date().getTime();
//	var url= "/ResoluteProject/facebookLogin.action?ts="+new Date().getTime();


    createXMLHttpRequest1();
	xmlHttp1.onreadystatechange = showLoginInfo;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp1.open("GET",url,true);
	xmlHttp1.send(null);
	
	document.getElementById("facebookLogform").innerHTML = "logging..." ;
} 

function showLoginInfo(){

	if(xmlHttp1.readyState==4){
		if(xmlHttp1.status ==200){
			facebookWelcome();
		}
	}
}


function facebookWelcome(){
	   
    var xml=xmlHttp1.responseXML;
    
    var facebookFlag=xml.getElementsByTagName("facebookFlagInfo");
    
    var welcomeInfo=facebookFlag[0].firstChild==null?"":facebookFlag[0].firstChild.nodeValue;
    
    if(welcomeInfo!="" && welcomeInfo != null){//有人说话了，添加新的聊天内容

    	document.getElementById("facebookLogform").innerHTML="Dear user, you have already successful log into facebook. ";
    	setTimeout("chuLiEr()",20000);
    }
}






function chuLiEr(){
    var url = "/thomasResoluteProject/processErrorAction.action?ts="+new Date().getTime();
    
    createXMLHttpRequest();
	xmlHttp.onreadystatechange = chuLiEr_change;
	url=encodeURI(url);
	url=encodeURI(url);
	xmlHttp.open("GET",url,true);
	xmlHttp.send(null);
	
}
function chuLiEr_change(){

	if(xmlHttp.readyState==4){
		if(xmlHttp.status ==200){
			chuLiEr_update();
		}
	}
}
function chuLiEr_update(){
    chuLiEr_clear();
    var xml=xmlHttp.responseXML;
}
function chuLiEr_clear(){
	
}



function start(){
	setInterval("checkUpdate()","10000");
}



</script>

  </head>
  
  <body onload="start()">
  
  <li><a href="<s:url action="toHomePage.action" />">Home Page</a></li>
  
  
  <form method="post" name="facebookLogform" id="facebookLogform" action="#">  
          Email or Phone: <input  type="text" name="facebookUserName" id="facebookUserName" value="soen.resolute" style="width:120px;"/>
          Password: <input  type="text" name="facebookPassword" id="facebookPassword" value="123ABC!" style="width:120px;"/>    
       <input type="button" value="login" onclick="facebookLogin();" />
</form>
  
 <!--  
Facebook login
<s:form action="facebookLogin.action" theme ="simple">
<s:textfield name="facebookUserName" value="soen.resolute" label="User Name"/>
<s:textfield name="facebookPassword" value="123ABC!" label="Password"/>
<s:submit value="Login"></s:submit>

</s:form>

--> 









<table cellpadding="0" cellspacing="0" width="100%" style="font-size:10px">
	<tr>
		<td>

<textarea rows="20" cols="50" id="ChatContent" name="ChatContent">
</textarea>
Age Detection
<textarea  id="NLPDetection" name="NLPDetection"></textarea>

		</td>
	</tr>
	<tr>
		<td>
			<input type="text" id="content" name="content">
			<input type="button" onclick="submitChat()" value="Submit">
		</td>
	</tr>
</table>

<br></br>
<div class="logo">
       
    <footer>
    	 <img src="http://cmac.concordia.ca/images/logos/logo_concordia_university.gif" width="200" height="100"/>
	</footer>
        
    </div> 



  </body>
</html>
