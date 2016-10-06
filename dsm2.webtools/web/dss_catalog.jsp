<%@ page import="java.net.URLEncoder"%>
<%@ page import="hec.heclib.dss.CondensedReference"%>
<%@ page import="hec.heclib.dss.DSSPathname"%>
<%@ page import="java.util.ArrayList"%>
<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Time Series Chart</title>
</head>

<body>

<table>
<thead>
	<tr>
		<th> A PART</th> <th> B PART </th> <th> C PART </th> <th> D PART </th> <th> E PART </th> <th> F PART </th>
	</tr>
</thead>
<tbody>
<% ArrayList<CondensedReference> refs = (ArrayList<CondensedReference>) request.getAttribute("refs"); %>
<%for (CondensedReference r: refs){ %>
<% DSSPathname p = new DSSPathname(r.getNominalPathname()); %>
<tr>
<td> <%=p.getAPart()%> <td>
<td> <%=p.getBPart()%> <td> 
<td> <%=p.getCPart()%> <td>
<td> <%=p.getDPart()%> <td>
<td> <%=p.getEPart()%> <td>
<td> <%=p.getFPart()%> <td>
${pageContext.request.contextPath}
</tr>
<%} %>
</tbody>
</table>
</body>
<!--  JQuery Libraries -->
<script src="js/jquery-1.12.4.min.js"></script>
<script src="js/jquery-ui-1.12.1.min.js"></script>
<link rel="stylesheet" href="css/jquery-ui-1.12.1.css">
<!--  JQuery Tablesorter -->
<script src="js/jquery.tablesorter.min.js"></script>
<!--  Bootstrap libraries for layout -->
<link rel="stylesheet" href="bootstrap-3.3.7-dist/css/bootstrap.min.css">
<script src="bootstrap-3.3.7-dist/js/bootstrap.min.js"></script>
<!--  Leaflet for maps -->
<link rel="stylesheet" href="css/leaflet.css" />
<script type="text/javascript" src="js/leaflet-0.7.7.js"></script>
<!-- D3 for svg rendering -->
<script type="text/javascript" src="js/d3-4.2.2.js"></script>
<script type="text/javascript" src="js/d3-legend.js"></script>
</html>