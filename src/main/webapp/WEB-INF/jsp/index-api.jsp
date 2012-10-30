<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html>
<head>
	<title>Havalo API</title>
	<style type="text/css">
		label { font-weight: bold }
	</style>	
</head>
<body>
	<h2>Havalo API</h2>
	<h3>Admin User Authentication Key & Secret</h3>
	<p>
		<label for="key">Access Key:</label>
		<span id="key">${HavaloBootstrap.adminKey}</span>
	</p>
	<p>
		<label for="secret">Access Secret:</label>
		<span id="secret">${HavaloBootstrap.adminSecret}</span>
	</p>
</body>
</html>