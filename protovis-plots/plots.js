/*
 * Data in this format
 *
var data_A = {
		"title": "Muscle vs Protein Plot",
		"series_names": ["Muscle","Protein"],
		"yaxis_name":"Strength (kg/s)",
		"xaxis_name": "Time",
		"values": [{x:new Date(1908,0,1),y1:400,y2:300}
		,{x:new Date(1918,1,1),y1:700,y2:600}
		,{x:new Date(1928,2,1),y1:900,y2:1100}
		,{x:new Date(1938,3,1),y1:1300,y2:1200}
		,{x:new Date(1948,4,1),y1:1700,y2:2000}
		,{x:new Date(1958,5,1),y1:1900,y2:2100}
		,{x:new Date(1968,6,1),y1:2000,y2:2400}
		,{x:new Date(1978,7,1),y1:2500,y2:2600}
		,{x:new Date(1988,8,1),y1:2700,y2:3300}
		,{x:new Date(1998,9,1),y1:3000,y2:3500}
		,{x:new Date(2008,10,1),y1:3800,y2:3700}]
}
 */
function time_series_plot(div_id, data){
/* Sizing and scales. */
var w = 800,
    h = 600,
    x = pv.Scale.linear(data.values, function(d) {return new Date(d.x)}).range(0, w),
    y = pv.Scale.linear(data.values, 
    	    function(d) {return Math.min(isNaN(d.y1)?0:d.y1,isNaN(d.y2)?0:d.y2)*0.95}, 
    	    function(d) {return Math.max(isNaN(d.y1)?0:d.y1,isNaN(d.y2)?0:d.y2)*1.10})
    	    .range(0, h);

var curves=[{"color":"red", "width":1},{"color":"blue", "width":1, "dashArray": "10,3"}]

/* The root panel. */
var vis = new pv.Panel()
	.canvas(div_id)
    .width(w)
    .height(h)
    .bottom(50)
    .left(60)
    .right(10)
    .top(50);
/* Add mouse zoom in/pan behavior */
vis.add(pv.Panel)
	.events("all")
    .event("mousedown", pv.Behavior.pan())
    .event("mousewheel", pv.Behavior.zoom())

/* Border around plot 
vis.add(pv.Area)
	.data([0,1])
	.bottom(0)
	.height(h)
	.left(function(d) {return d*w})
	.fillStyle(null)
	.strokeStyle("#000")
	.lineWidth(0.25);
	*/
/* X-axis ticks. */
vis.add(pv.Rule)
    	.data(x.ticks())
    	.left(x)
    	.strokeStyle("#eee")
	.add(pv.Rule)
    	.bottom(-10)
    	.height(5)
    	.strokeStyle("#000")
    .anchor("bottom").add(pv.Label)
    	.text(x.tickFormat);

/* X-axis label */
vis.add(pv.Label)
	.left(w/2)
	.bottom(-40)
	.text(data.xaxis_name)
	.font("18px sans-serif")
	.textAlign("center");

/* Y-axis ticks. */
  vis.add(pv.Rule)
    .data(y.ticks(8))
    .bottom(y)
    .strokeStyle("#eee")
  .anchor("left").add(pv.Label)
	    .text(y.tickFormat);
 
/* Y-axis label */
vis.add(pv.Label)
	.top(h/2)
	.left(-35)
	.text(data.yaxis_name)
	.font("18px sans-serif")
	.textAngle(-Math.PI/2)
	.textAlign("center");

/* Line 1 */
vis.add(pv.Line)
    .data(data.values)
    .interpolate("step-before")
    .segmented(true)
    .visible(function(d) {return isNaN(d.y1)? false: true})
    .left(function(d) {return x(d.x)})
    .bottom(function(d) {return y(d.y1)})
    .lineWidth(curves[0].width)
    .strokeStyle(curves[0].color)
    .dashArray(curves[0].dashArray);
    
/* Line 2 */
vis.add(pv.Line)
    .data(data.values)
    .interpolate("step-before")
    .segmented(true)
    .visible(function(d) {return isNaN(d.y2)? false: true})
    .left(function(d) {return x(d.x)})
    .bottom(function(d) {return y(d.y2)})
    .lineWidth(curves[1].width)
    .strokeStyle(curves[1].color)
    .dashArray(curves[1].dashArray);
/* Title */
vis.add(pv.Label)
	.right(function(d) {return w/2})
	.top(-15)
	.textAlign("center")
	.font("28px sans-serif")
	.text(data.title)
/* Legend*/
vis.add(pv.Rule)
	.left(12).top(20).width(40)
    .lineWidth(curves[0].width)
    .strokeStyle(curves[0].color)
    .dashArray(curves[0].dashArray)
	.anchor("right").add(pv.Label).text(data.series_names[0]);
vis.add(pv.Rule)
	.left(12).top(32).width(40)
    .lineWidth(curves[1].width)
    .strokeStyle(curves[1].color)
    .dashArray(curves[1].dashArray)
	.anchor("right").add(pv.Label).text(data.series_names[1]);
/* Render */
vis.render();
}
/**
 * Exceedance plot with reversed axis from 100% to 0%
 * 
 * var data_B = {
		"title": "Car vs Motorcycle Speeds on the Autobahn",
		"series_names": ["Car","Motorcycle"],
		"yaxis_name":"Speed (mph)",
		"xaxis_name": "Percent time at or above",
		"values": [{x:100,y1:10,y2:10}
		,{x:90,y1:20,y2:30}
		,{x:80,y1:30,y2:44}
		,{x:70,y1:55,y2:65}
		,{x:60,y1:65,y2:75}
		,{x:50,y1:70,y2:85}
		,{x:40,y1:90,y2:95}
		,{x:30,y1:120,y2:110}
		,{x:20,y1:140,y2:120}
		,{x:10,y1:150,y2:125}
		,{x:0,y1:160,y2:130}],
}
 * @param div_id
 * @param data
 * @return
 */
function exceedancePlot(div_id, data){
	/* Sizing and scales. */
	var w = 800,
	    h = 600,
	    x = pv.Scale.linear(100,0).range(0, w),
	    y = pv.Scale.linear(data.values, function(d) {return Math.min(d.y1,d.y2)}, function(d) {return Math.max(d.y1,d.y2)}).range(0, h);

	var curves=[{"color":"red", "width":3},{"color":"blue", "width":3, "dashArray": "10,3"}]

	/* The root panel. */
	var vis = new pv.Panel()
		.canvas(div_id)
	    .width(w)
	    .height(h)
	    .bottom(50)
	    .left(60)
	    .right(10)
	    .top(50);
	/* Border around plot */
	vis.add(pv.Area)
		.data([0,1])
		.bottom(0)
		.height(h)
		.left(function(d) {return d*w})
		.fillStyle(null)
		.strokeStyle("#000");
	/* X-axis ticks. */
	vis.add(pv.Rule)
	    	.data(x.ticks())
	    	.visible(function(d) {return d >= 0})
	    	.left(x)
	    	.strokeStyle("#eee")
		.add(pv.Rule)
	    	.bottom(-10)
	    	.height(5)
	    	.strokeStyle("#000")
	    .anchor("bottom").add(pv.Label)
	    	.text(function(d) {return x.tickFormat(d)+'%'});

	/* X-axis label */
	vis.add(pv.Label)
		.left(w/2)
		.bottom(-40)
		.text(data.xaxis_name)
		.font("18px sans-serif")
		.textAlign("center");

	/* Y-axis ticks. */
	  vis.add(pv.Rule)
	    .data(y.ticks(5))
	    .bottom(y)
	    .strokeStyle("#999")
	  .anchor("left").add(pv.Label)
		    .text(y.tickFormat);
	 
	/* Y-axis label */
	vis.add(pv.Label)
		.top(h/2)
		.left(-35)
		.text(data.yaxis_name)
		.font("18px sans-serif")
		.textAngle(-Math.PI/2)
		.textAlign("center");

	/* Line 1 */
	vis.add(pv.Line)
	    .data(data.values)
	    .interpolate("linear")
	    .left(function(d) {return x(d.x)})
	    .bottom(function(d) {return y(d.y1)})
	    .lineWidth(curves[0].width)
	    .strokeStyle(curves[0].color)
	    .dashArray(curves[0].dashArray);
	    
	/* Line 2 */
	vis.add(pv.Line)
	    .data(data.values)
	    .interpolate("linear")
	    .left(function(d) {return x(d.x)})
	    .bottom(function(d) {return y(d.y2)})
	    .lineWidth(curves[1].width)
	    .strokeStyle(curves[1].color)
	    .dashArray(curves[1].dashArray);
	/* Title */
	vis.add(pv.Label)
		.right(function(d) {return w/2})
		.top(-15)
		.textAlign("center")
		.font("28px sans-serif")
		.text(data.title)
	/* Legend*/
	vis.add(pv.Rule)
		.left(12).top(20).width(40)
	    .lineWidth(curves[0].width)
	    .strokeStyle(curves[0].color)
	    .dashArray(curves[0].dashArray)
		.anchor("right").add(pv.Label).text(data.series_names[0]);
	vis.add(pv.Rule)
		.left(12).top(32).width(40)
	    .lineWidth(curves[1].width)
	    .strokeStyle(curves[1].color)
	    .dashArray(curves[1].dashArray)
		.anchor("right").add(pv.Label).text(data.series_names[1]);
	/* Render */
	vis.render();
}

