<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html>
<head>
	<title>Havalo</title>
	<style type="text/css">
		input { border: 1px solid #cccccc; padding: 2px; width: 300px; }
		input.wide { width: 650px; }
	</style>	
</head>
<body>
	<h2>Havalo</h2>
	<h3>Admin Authentication Credentials</h3>
	<p>
		<label for="key">Access Key:</label>
		<input type="text" id="key" name="key" value="${HavaloBootstrap.adminKey}" readonly/>
	</p>
	<p>
		<label for="secret">Access Secret:</label>
		<input type="text" class="wide" id="secret" name="secret" value="${HavaloBootstrap.adminSecret}" readonly/>
	</p>
</body>
</html>