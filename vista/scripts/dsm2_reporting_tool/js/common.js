
    /* to replace the data JavaScript file */
	function createjscssfile(filename, filetype){
      if (filetype=="js"){ //if filename is a external JavaScript file
       var fileref=document.createElement('script')
       fileref.setAttribute("type","text/javascript")
       fileref.setAttribute("src", filename)
      }
      else if (filetype=="css"){ //if filename is an external CSS file
      var fileref=document.createElement("link")
      fileref.setAttribute("rel", "stylesheet")
      fileref.setAttribute("type", "text/css")
      fileref.setAttribute("href", filename)
      }
      return fileref
    }
    function replacejscssfile(oldfilename, newfilename, filetype){
     var targetelement=(filetype=="js")? "script" : (filetype=="css")? "link" : "none" //determine element type to create nodelist using
     var targetattr=(filetype=="js")? "src" : (filetype=="css")? "href" : "none" //determine corresponding attribute to test for
     var allsuspects=document.getElementsByTagName(targetelement)
     for (var i=allsuspects.length; i>=0; i--){ //search backwards within nodelist for matching elements to remove
      if (allsuspects[i] && allsuspects[i].getAttribute(targetattr)!=null && allsuspects[i].getAttribute(targetattr).indexOf(oldfilename)!=-1){
       var newelement=createjscssfile(newfilename, filetype)
       allsuspects[i].parentNode.replaceChild(newelement, allsuspects[i])
      }
     }
    }
	/* to extract and parse the date */
    function extract_date(date_str){
        date_fields=date_str.split(",");
        return new Date(date_fields[0],date_fields[1],date_fields[2]);
    }
    function to_date_comma(calendar_date){
        fi = calendar_date.split("/");
        return fi[2]+","+fi[0]+","+fi[1];
    }
    function to_date_str(comma_date){
        fi = comma_date.split(",");
        return fi[1]+"/"+fi[2]+"/"+fi[0];    
    }
    function chk_type(){
     if (document.getElementById('ta').value=='STAGE' && document.getElementById('data-conversion').value=='daily_avg')
       document.getElementById('warning').innerHTML='Daily Average Stage is meaningless! Please select daily max/min for plotting.';
	 else
	   document.getElementById('warning').innerHTML='';
    }
	
	/* for Google Map */
    function initialize(e) {
      tab_name = document.getElementById('ta').value; 
      e.innerHTML=document.getElementById("map_canvas"+tab_name).style.display==''?'View Map':'Hide Map';
      document.getElementById("map_canvas"+tab_name).style.display=document.getElementById("map_canvas"+tab_name).style.display==''?'none':'';
      var map = new GMap2(document.getElementById("map_canvas"+tab_name));
      map.setCenter(new GLatLng(38.17, -121.6), 9);
      map.setUIToDefault();
      var bounds = map.getBounds();
      var southWest = bounds.getSouthWest();
      var northEast = bounds.getNorthEast();
      var lngSpan = northEast.lng() - southWest.lng();
      var latSpan = northEast.lat() - southWest.lat();
      var baseIcon = new GIcon(G_DEFAULT_ICON);
      baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
	  
     function createMarker(point, index, info) {
      //var letter = String.fromCharCode("A".charCodeAt(0) + index);
       if(index<10) size=5;
       else if(index<20) size=10;
       else if(index<50) size=15;
       else if(index<100) size=20;
       else if(index<200) size=25;
       else if(index<500) size=30;
       else size=40;
       baseIcon.iconSize = new GSize(size, size);
       baseIcon.shadowSize = new GSize(1,1);
       baseIcon.iconAnchor = new GPoint(1, 1);	  
       var letteredIcon = new GIcon(baseIcon);
       letteredIcon.image = "js/icon16.png";
       markerOptions = { icon:letteredIcon, title:info };
       var marker = new GMarker(point, markerOptions);
       GEvent.addListener(marker, "click", function() {
       marker.openInfoWindowHtml("<font size=2>" + info + "</font>");
       });
       return marker;
      }
      for(i=0; i < data_list.length; i++) {
	   if (data_list[i].data_type==tab_name && data_list[i].latitude!='nan') {	 
         var latlng = new GLatLng(data_list[i].latitude,data_list[i].longitude);
         map.addOverlay(createMarker(latlng,data_list[i].diff[0].perc_rmse,data_list[i].name));
	   }
      }
    }	
	function change_period(){ 
	   reload_js();
       setTimeout("clear_and_draw(extract_date(to_date_comma($('#SDate').val())),extract_date(to_date_comma($('#EDate').val())))",1000);
    }
