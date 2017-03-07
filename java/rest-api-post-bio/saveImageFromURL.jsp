<%@ page import="com.ImageUtils"%>
<%

String image_url = (request.getParameter("image_url") != null) ? request.getParameter("image_url") : "";
String site_id = (request.getParameter("site_id") != null) ? request.getParameter("site_id") : "";

String result = "0";
String jsonResult = "{'status':'0','reason':'No data'}";
if (!image_url.equals("") && !site_id.equals("")) {
     String savedFileName = ImageUtils.saveImageFromURL(image_url, site_id);
     String savedImagePath = ImageUtils.getSavedImagePathFromSiteId(site_id) + savedFileName;
     String publicImagePath = ImageUtils.getPublicImagePathFromSiteId(site_id) + savedFileName;
     jsonResult = "{'status':'1','image_name':'" + savedFileName + "','system_path':'" + savedImagePath + "','public_path':'" + publicImagePath + "'}";
}
out.print(jsonResult);
%>
