<!DOCTYPE HTML>
<html>
<head>

<!-- https://canvasjs.com/javascript-candlestick-chart/ -->

<script>
window.onload = function () {

var dataPoints = [];

var chart = new CanvasJS.Chart("chartContainer", {
	animationEnabled: true,
	theme: "light2", // "light1", "light2", "dark1", "dark2"
	exportEnabled: true,
	title: {
		text: "Netflix Stock Price in 2016"
	},
	subtitles: [{
		text: "Weekly Averages"
	}],
	axisX: {
		interval: 1,
	},
	axisY: {
		includeZero: false,
		prefix: "$",
		title: "Price"
	},
	toolTip: {
		content: "Date: {x}<br /><strong>Price:</strong><br />Open: {y[0]}, Close: {y[3]}<br />High: {y[1]}, Low: {y[2]}"
	},
	data: [{
		type: "candlestick",
		yValueFormatString: "$##0.00",
		dataPoints: dataPoints
	}]
});

<!-- $.get("https://canvasjs.com/data/gallery/javascript/netflix-stock-price.csv", getDataPointsFromCSV); -->
$.get("http://localhost:8080/data", getDataPointsFromCSV);

function getDataPointsFromCSV(csv) {
	var csvLines = points = [];
	csvLines = csv.split(/[\r?\n|\r|\n]+/);
	for (var i = 0; i < csvLines.length; i++) {
		if (csvLines[i].length > 0) {
			points = csvLines[i].split("\t");
			dataPoints.push({
				x: new Date(
					parseInt(points[0].split(".")[0]),
					parseInt(points[0].split(".")[1]),
					parseInt(points[0].split(".")[2]),
					parseInt(points[1].split(":")[0]),
					parseInt(points[1].split(":")[1]),
					parseInt(points[1].split(":")[2])
				),
				y: [
					parseFloat(points[2]),
					parseFloat(points[3]),
					parseFloat(points[4]),
					parseFloat(points[5])
				]
			});
		}
	}
	chart.render();
}

}
</script>
</head>
<body>
<div id="chartContainer" style="height: 300px; width: 100%;"></div>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
<script src="https://canvasjs.com/assets/script/jquery-1.11.1.min.js"></script>
</body>
</html>