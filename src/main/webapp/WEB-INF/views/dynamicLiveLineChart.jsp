<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript">
window.onload = function() {

var dataPoints = [];
var y = 50;
for(var i = 0; i < 10; i++){
	y += (Math.random() - 0.5) * 0.1;
	dataPoints.push({x: i, y: y});
}

var chart = new CanvasJS.Chart("chartContainer", {
	title: {
		text: "Download Speed"
	},
	axisX:{
		title: "Time in second"
	},
	axisY:{
		includeZero: false,
		suffix: " Mbps"
	},
	data: [{
		type: "line",
		yValueFormatString: "#,##0.0#",
		toolTipContent: "{y} Mbps",
		dataPoints: dataPoints
	}]
});
chart.render();

var updateInterval = 1000;
setInterval(function () { updateChart() }, updateInterval);

var xValue = dataPoints.length;
var yValue = dataPoints[dataPoints.length - 1].y;

function updateChart() {
	yValue += (Math.random() - 0.5) * 0.5;
	dataPoints.push({ x: xValue, y: yValue });
	xValue++;
	chart.render();
};

}
</script>
</head>
<body>
<div id="chartContainer" style="height: 370px; width: 100%;"></div>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
</body>
</html>