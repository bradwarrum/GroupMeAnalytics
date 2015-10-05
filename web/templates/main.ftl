<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Bootstrap 101 Template</title>

    <!-- Bootstrap -->
	<!-- Latest compiled and minified CSS -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">

	<!-- Optional theme -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
	
	<style>
	<!--
	@media (min-width: 1200px) {
	  .container {
		width: 970px;
	  }
	}
	.list-group-item {
		margin-top:10px;
	}
	.centered-date {
		text-align:left;
		font-size:10px;
		color:gray;
		text-decoration:italic;
	}
	.thumbnail {
		width:80%;
		margin-left:auto;
		margin-right:auto;
		margin-bottom:15px;
	}
	.avatar {
		width:30px;
		height:30px;
	}	
	-->
	</style>
  </head>
  <body>
    <nav class="navbar navbar-default">
		<div class="container">
			<div class="navbar-header">
				<div class="navbar-brand">${botname}</div>
			</div>
		</div>
		
	</nav>
	
	<div class="container">
		<h4>${title}</h4>
		<ul class="list-group">
			<#list messages as message>
				<div class="list-group-item<#if message.messageID == activeElem> active</#if>" style="overflow:auto;">
					<div style="display:inline-block;float:left;margin-right:10px;">
					<#if message.avatarURL?has_content>					
					<img class="img-circle" src="${message.avatarURL}.avatar"/>		
					</#if>
					</div>
					<div style="overflow:hidden;">
					<h6 class="centered-date">${message.date}</h6>
					<b>${message.sender}</b><br>
					<#if message.imageURL?has_content>
					<a href="${message.imageURL}" class="thumbnail">
					<img src="${message.imageURL}"/>
					</a>
					</#if>
					${message.text}
					</div>
				</div>
			</#list>
		</ul>	
	</div>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
	<!-- Latest compiled and minified JavaScript -->
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  </body>
</html>