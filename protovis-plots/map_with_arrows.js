var data = {};
data.legend = {
lines:["June 5 - 30, 2008","Historical Simulation"]
};
data.flows = [
	{lat: 37.89348, lon: -121.49158, flow: 822,  angle: 50, label:"Middle River @ Union Point", invert:false},
	{lat: 37.87172, lon: -121.57945, flow: 1044, angle: 115, label:"Old River Flow", invert:false},
	{lat: 37.87719, lon: -121.5172, flow: 630, angle: 330, label: "Victoria Canal", invert:true},
	{lat: 37.885, lon: -121.446, flow: 95, angle: 348, label: "Upstream of Union Point Barrier", invert:false},
	{lat:37.80595, lon: -121.31713, flow:958, angle: 240, label: "Upstream of Old River Head", invert: false},
	{lat:37.81843, lon:-121.3149, flow: 204, angle:100, label: "Downstream of Old River Head on San Joaquin River", invert:false},
	{lat: 37.81816, lon:-121.35979, flow: 694, angle: 180, label: "Downstream of Old River Head on Old River", invert:false},
	{lat: 37.81060, lon:-121.38760, flow: 596, angle: 220, label: "Downstream of Old River towards Grant Line Canal", invert:false},
	{lat:37.85632, lon:-121.37687, flow: 99, angle: 93, label:"Downstream of Old River split away from Grant Line Canal", invert:true},
	{lat:37.8198, lon:-121.4980, flow: 468, angle: 180, label: "Downstream of GLC Barrier on Grant Line Canal", invert:false},
	{lat:37.8235, lon: -121.5979, flow: 703, angle: 300, label: "SWP", invert:true},
	{lat:37.7995, lon: -121.5843, flow: 894, angle: 110, label: "CVP", invert:false}

];

data.stages = [
	{lat:37.885371,  lon: -121.48456, stage: -1.3, pos: ["down","left"], label:"Minimum Stage Downstream of Union Point Barrier"},
	{lat:37.88525,  lon: -121.4742, stage: -1.1, pos: ["up", "right"], label: "Minimum Stage Upstream of Union Point Barrier"},
	{lat:37.80555, lon:-121.32387, stage: 1.8, label: "Minimum Stage Upstream of Old River Head"},
	{lat:37.81026, lon: -121.33545, stage: 1.6, label: "Minimum Stage Downstream of Old River Head"},
	{lat:37.82924, lon:-121.38631, stage: 1.4, pos:["up","left"], label: "Minimum Stage Downstream of Old River Head away from GLC"},
	{lat:37.87817, lon:-121.38361, stage: 1.2, pos: ["up", "right"], label: "Minimum Stage on Old River Near Howard Road"},
	{lat:37.8117, lon:-121.4175, stage: 1.5, pos:["down","left"], label: ""},
	{lat: 37.8201, lon:-121.4460, stage: 1.5, pos:["up", "right"], label: "Minimum Stage upstream of GLC Barrier"},
	{lat: 37.82006, lon: -121.45854, stage: -1.5, label:"Minimum Stage downstream of GLC Barrier" },
];

data.barriers = [
	{lat: 37.885566, lon: -121.48224, label: "Barrier on Middle River near Union Point"},
	{lat: 37.81992, lon: -121.453, label: "Barrier on Grant Line Canal (GLC)"},

]