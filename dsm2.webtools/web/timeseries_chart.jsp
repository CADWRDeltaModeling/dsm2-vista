<%@ page import="java.net.URLEncoder"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Time Series Chart</title>

<!--  JQuery Libraries -->
<script src="js/jquery-1.12.4.min.js"></script>
<script src="js/jquery-ui-1.12.1.min.js"></script>
<link rel="stylesheet" href="css/jquery-ui-1.12.1.css">
<!--  Bootstrap libraries for layout -->
<link rel="stylesheet" href="bootstrap-3.3.7-dist/css/bootstrap.min.css">
<script src="bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<style type="text/css">
${
demo
.css
}
</style>
<%
	request.setCharacterEncoding("UTF-8");
%>
<script type="text/javascript">
        $(document).ready(function(){
    $.getJSON('data?dssfile=<%=URLEncoder.encode(request.getParameter("dssfile"), "UTF-8")%>&dsspath=<%=URLEncoder.encode(request.getParameter("dsspath"), "UTF-8")%>',
										function(data) {
											chart_options = {
												chart : {
													zoomType : 'x'
												},
												title : {
													text : 'Time series of '
															+ data.parameter
															+ ' @ '
															+ data.location
												},
												subtitle : {
													text : data.subParameter
															+ ', '
															+ data.subLocation
												},
												xAxis : {
													type : 'datetime',
													dateTimeLabelFormats : {
														millisecond : '%H:%M:%S.%L',
														second : '%H:%M:%S',
														minute : '%H:%M',
														hour : '%H:%M %b %y',
														day : '%e. %b %y',
														week : '%e. %b %y',
														month : '%b \'%y',
														year : '%Y'
													}
												},
												yAxis : {
													title : {
														text : data.parameter
																+ ' ('
																+ data.units
																+ ')'
													}
												},
												legend : {
													enabled : false
												},
												plotOptions : {
													lineWidth : 1,
													states : {
														hover : {
															lineWidth : 1
														}
													},
													threshold : null

												},
												series : [ {
													name : data.parameter
															+ ' @  '
															+ data.location,
													data : data.valueArray,
												} ]
											};
											if (data.type == 'PER-AVER'
													|| data.type == 'PER-VAL') {
												chart_options.series[0].step = 'right';
											}
											$('#container').highcharts(
													chart_options);
										});
					});
</script>
</head>
<body>
	<script src="https://code.highcharts.com/highcharts.js"></script>
	<script src="https://code.highcharts.com/modules/boost.js"></script>
	<script src="https://code.highcharts.com/modules/exporting.js"></script>

	<div id="container"
		style="min-width: 310px; height: 400px; margin: 0 auto"></div>

</body>
</html>
