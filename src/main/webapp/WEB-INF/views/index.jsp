<!--https://canvasjs.com/jsp-charts/-->
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>

<!DOCTYPE HTML>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        window.onload = function() {

            var dps = [];

            var chart = new CanvasJS.Chart("chartContainer1", {
                animationEnabled: true,
                exportEnabled: true,
                zoomEnabled: true,
                title: {
                    text: "The Procter & Gamble Company Stock Price"
                },
                subtitles: [{
                    text: "2012 to 2017"
                }],
                axisX: {
                    valueFormatString: "MMM YYYY"
                },
                axisY: {
                    title: "Price (in USD)",
                    includeZero: false,
                    prefix: "$"
                },
                data: [{
                    type: "candlestick",
                    xValueFormatString: "MMM YYYY",
                    yValueFormatString: "$#,##0.00",
                    xValueType: "dateTime",
                    dataPoints: dps
                }]
            });

            $.getJSON("https://canvasjs.com/data/gallery/jsp/the-procter-gamble-company-stock-price.json", parseData);

            function parseData(result) {
                for (var i = 0; i < result.length; i++) {
                    dps.push({
                        x: result[i].x,
                        y: result[i].y
                    });
                }
                chart.render();
            }

        }
    </script>
    <script type="text/javascript">
    window.onload = function() {

    var dataPoints = [];
    var y = 50;
    for(var i = 0; i < 10; i++){
    	y += (Math.random() - 0.5) * 0.1;
    	dataPoints.push({x: i, y: y});
    }

    var chart = new CanvasJS.Chart("chartContainer2", {
    	title: {
    		text: "Prediction in %"
    	},
    	axisX:{
    		title: "Time in second"
    	},
    	axisY:{
    		includeZero: false,
    		suffix: " %"
    	},
    	data: [{
    		type: "line",
    		yValueFormatString: "#,##0.0#",
    		toolTipContent: "{y} %",
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
<div id="chartContainer1" style="height: 370px; width: 100%;"></div>
<div id="chartContainer2" style="height: 370px; width: 100%;"></div>
<script src="https://canvasjs.com/assets/script/jquery-1.11.1.min.js"></script>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
</body>
</html>
<br>
timestampAsInteger: ${timestampAsInteger}
<br>
dataFromTimestamp: ${dataFromTimestamp}
<br>
base currency: ${base_currency}
<br>
rates: ${rates}

<!--form method="post">
Zahl 1: <input type="text" name="zahl1">
Operation: <input type="text" name="operation">
Zahl 2: <input type="text" name="zahl2">
Ergebnis: ${result} -->
<br>
<!--input type="submit" id="button">
</form-->

<br>
<%
int a = 5;
int b = 11;
out.println("Hallo Welt: " + (a+b));
%>
