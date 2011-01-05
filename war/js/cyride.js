/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.  
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

cyride = {};
cyride.web = {};
cyride.web.db = null;
cyride.web.open = function() {
	var dbSize = 5 * 1024 * 1024;
	cyride.web.db = openDatabase("CyRide", "", "cyride", dbSize);
};
cyride.web.createTable = function() {
	cyride.web.db.transaction(function(tx) {
		tx.executeSql('CREATE TABLE IF NOT EXISTS cyride(ID INTEGER PRIMARY KEY ASC, routeid INTEGER, routename TEXT, station TEXT, stationid TEXT, timestring TEXT, time INTEGER, dayofweek INTEGER, rownum INTEGER)', []);
	});
};
cyride.web.addRecord = function(recordText) {
	cyride.web.db.transaction(function(tx){
		tx.executeSql('INSERT INTO cyride(routeid, routename, station, stationid, timestring, time, dayofweek, rownum) VALUES (?,?,?,?,?,?,?,?)', 
				[recordText.routeid, recordText.routename, recordText.station, recordText.stationid, recordText.timestring, recordText.time, recordText.dayofweek, recordText.rownum],
				cyride.web.doNothing, cyride.web.onError);
	});
};
cyride.web.doNothing = function(){}
cyride.web.getItemsByDate = function() {
	var day = document.getElementById('day').selectedIndex;
	cyride.web.db.transaction(function(tx) { tx.executeSql('SELECT * FROM cyride WHERE dayofweek = \''+day+'\' ORDER BY routeid', [], cyride.web.onSuccess, cyride.web.onError);	});
};
cyride.web.getItemsByRoute = function(search) {
	var day = document.getElementById('day').selectedIndex;
	cyride.web.db.transaction(function(tx) { tx.executeSql('SELECT * FROM cyride WHERE routeid = \''+search+'\' AND dayofweek = \''+day+'\' ORDER BY routeid, stationid, rownum', [], cyride.web.fillInStations, cyride.web.onError);	});
};
cyride.web.getItemsByRowNum = function(routeid, rownum) {
	var day = document.getElementById('day').selectedIndex;
	cyride.web.db.transaction(function(tx) { tx.executeSql('SELECT * FROM cyride WHERE routeid = \''+routeid+'\' AND rownum = \''+rownum+'\' AND dayofweek = \''+day+'\' ORDER BY routeid, stationid, rownum', [], cyride.web.loadRecordItemsRowNum, cyride.web.onError);	});
};
cyride.web.getItemsByRouteAndStation = function(search) {
	var day = document.getElementById('day').selectedIndex;
	cyride.web.db.transaction(function(tx) { tx.executeSql('SELECT * FROM cyride WHERE routeid = \''+search.route+'\' AND stationid = \''+search.station+'\' AND dayofweek = \''+day+'\'  ORDER BY routeid, stationid, rownum', [], cyride.web.loadRecordItems, cyride.web.onError);	});
};
cyride.web.getAllItems = function() {
	cyride.web.db.transaction(function(tx) { tx.executeSql('SELECT * FROM cyride ORDER BY routeid, stationid, time', [], cyride.web.loadRecordItems, cyride.web.onError);	});
};
cyride.web.deleteAllItems = function() {
	cyride.web.db.transaction(function(tx){ tx.executeSql('DELETE FROM cyride',[], cyride.web.onSuccess, cyride.web.onError);	});
};
cyride.web.onError = function(tx, e) {
	alert('Something unexpected happened in the database: ' + e.message);
};
cyride.web.onSuccess = function(tx, rs) {
	var stationNames = [];
	var stationIds = [];
	var stationsCount = 0;
	for (var i = 0; i < rs.rows.length; i++) {
		var temp = {};
		if (!stationIds.contains(rs.rows.item(i).routeid)) {
			stationNames[stationsCount] = rs.rows.item(i).routename;
			stationIds[stationsCount] = rs.rows.item(i).routeid;
			stationsCount++;
		}
	}
	var temp = "<option name='noroute' value='0'>---</option>";
	for (var i = 0; i < stationNames.length; i++) {
		temp += "<option name='" + stationNames[i] + "' value='"+stationIds[i]+"_"+ stationNames[i] + "' id='route" + stationIds[i] + "'>" + stationNames[i] + "</option>";
	}
	$("#route").html(temp);
	$("#route-wrapper").show();
	stopLoadingAnimation()
};
cyride.web.loadRecordItems = function(tx, rs) {
	var rowOutput = "<tr><td>Station</td><td>Time</td></tr>";
	for (var i = 0; i < rs.rows.length; i++) {
		rowOutput += renderRecord(rs.rows.item(i), true);
	}
	var recordItems = document.getElementById('table');
	recordItems.innerHTML = rowOutput;
	
	setHeaderColor();
	
	stopLoadingAnimation();
};
cyride.web.loadRecordItemsRowNum = function(tx, rs) {
	var rowOutput = "<tr><td>Station</td><td>Time</td></tr>";
	for (var i = 0; i < rs.rows.length; i++) {
		rowOutput += renderRecord(rs.rows.item(i), false);
	}
	var recordItems = document.getElementById('table');
	recordItems.innerHTML = rowOutput;
	
	setHeaderColor();
	
	$("#changeStation").show();
	stopLoadingAnimation();
};
cyride.web.fillInStations = function(tx,rs) {
	var stationNames = [];
	var stationIds = [];
	var stationsCount = 0;
	for (var i = 0; i < rs.rows.length; i++) {
		var temp = rs.rows.item(i).stationid;
		if (!stationIds.contains(temp)) {
			stationNames[stationsCount] = rs.rows.item(i).station;
			stationIds[stationsCount] = rs.rows.item(i).stationid;
			stationsCount++;
		}
	}
	var temp = "<option name='nostation' value='nostation'>---</option>";
	for (var i = 0; i < stationNames.length; i++) {
		temp += "<option name='" + stationNames[i] + "' value='" + stationIds[i] + "_" + stationNames[i] + "'>" + stationNames[i] + "</option>";
	}
	$("#station").html(temp);
	$("#station-wrapper").show();
	stopLoadingAnimation();
};
cyride.web.getData = function(){
	$.getJSON('/getroutes', function(e){
		startLoadingAnimation();
		for (var i = 0; i < e.count; i++) {
			var temp = e.records[i];
			cyride.web.addRecord(temp);
		}
		setTimeout(cyride.web.getItemsByDate, 1000);
	});
};
function stationChange() {
	$().ready(function() {
		if (this.selectedIndex === 0) {
			$("#table").hide();
		} else {
			var obj = {};
			obj.route = $("#route").val().substring(0,1);
			obj.station = $("#station").val().substring(0,1);
			cyride.web.getItemsByRouteAndStation(obj);

			$("#table").show();
		}
		$("#changeStation").hide();
	});
};
function setHeaderColor() {
	var color = "transparent";
	if ($("#route").val().indexOf("Blue") != -1) color = "blue";
	if ($("#route").val().indexOf("Red") != -1) color = "red";
	if ($("#route").val().indexOf("Brown") != -1) color = "brown";
	if ($("#route").val().indexOf("Green") != -1) color = "green";
	if ($("#route").val().indexOf("Yellow") != -1) color = "yellow";
	$("#table tr:first").css("background-color", color);
};
window.onload = function() {
	var today = new Date();
	if (today.getDay() == '0') sessionStorage['currentDay'] = 2;
	else if (today.getDay() == 6) sessionStorage['currentDay'] = 1;
	else sessionStorage['currentDay'] = 0;
	document.getElementById("day").selectedIndex = sessionStorage['currentDay'];
	if (window.openDatabase) {
		$("body").ready(function() {
			cyride.web.open();
			cyride.web.createTable();
			cyride.web.deleteAllItems();
			cyride.web.getData();
			stopLoadingAnimation();
			
			$("#station-wrapper, #route-wrapper, #table").hide();
			$("#day").change(function() {
				$("#station-wrapper, #route-wrapper").hide();
				cyride.web.getItemsByDate();
			});
			$("#route").change(function(){
				$("#station-wrapper").hide();
				$("#table").hide();
				cyride.web.getItemsByRoute(this.options[this.selectedIndex].id.substring(5));
			});
			$("#station").change(function(){
				if (this.selectedIndex === 0) {
					$("#table").hide();
				} else {
					var obj = {};
					obj.route = document.getElementById("route").options[document.getElementById("route").selectedIndex].id.substring(5);
					obj.station = this.value.substring(0,3);
					cyride.web.getItemsByRouteAndStation(obj);

					$("#table").show();
				}
			});
		});	
	} else {
		alert("You can't run this in your current browser.\n\nTry Google Chrome.");
		$("body").ready(function() {
			$("#table").hide();
			$("#wrongbrowser").show();
		});
	}
};
function renderRecord(row, onclick) {
	if (onclick) {
		return '<tr onclick=\'cyride.web.getItemsByRowNum('+row.routeid+','+row.rownum+')\' id=\''+row.rownum+'\'><td>'+row.station+'</td><td>'+row.timestring+'</td></tr>';
	} else {
		return '<tr id=\''+row.rownum+'\'><td>'+row.station+'</td><td>'+row.timestring+'</td></tr>';
	}
};
function startLoadingAnimation() {
	if (sessionStorage['loading'] != 'true') {
		$("#loading").removeClass("hide");
		sessionStorage['loading'] = 'true';
	}
};
function stopLoadingAnimation() {
	$("#loading").addClass("hide");
	sessionStorage['loading'] = 'false';
};
Array.prototype.contains = function(obj) {
	var i = this.length;
	while (i--) {
		if (this[i] === obj) {
			return true;
		}
	}
	return false;
};