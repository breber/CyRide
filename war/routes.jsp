<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="javax.jdo.PersistenceManager"%>
<%@ page import="javax.jdo.Query"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<title>CyRide Routes</title>
<link type="text/css" href="css/cyride.css" rel="stylesheet" />
<script type="text/javascript"
	src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"></script>
<script type="text/javascript" src="js/cyride.js"></script>
</head>

<body>
<div id="left">
<div id="top">
<h2>CyRide</h2>
<div id='loading' class="loading hide">
<img src='images/ajax-loader.gif' /></div>
</div>
<div id="selector">
	<label for="day">Day:</label><br/>
	<select id="day" name="day">
	<option id="0" value="0">Weekday</option>
	<option id="1" value="1">Saturday</option>
	<option id="2" value="2">Sunday</option>
	</select>
	<div id="route-wrapper">
	<label for="route">Route:</label><br/>
	<select id="route" name="route">
	</select>
	</div>
	<div id="station-wrapper">
	<label for="station">Station:</label><br/>
	<select id="station" name="station"></select>
	</div>
	<span id="changeStation" onclick="stationChange()">Back to full List</span>
</div>
</div>
<div id="all">
<div id="right">
<div id="wrongbrowser" style="display:none">
You are unable to view the list because your browser doesn't support HTML 5 SQL Databases.<br/>
Try using <a href="http://google.com/chrome">Google Chrome</a>
</div>
<table id="table" style="width:100%" border="1" cellpadding="2" cellspacing="0">
</table>
</div>
</div>
</body>
</html>
