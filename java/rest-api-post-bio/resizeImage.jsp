<%@ page import="com.ImageUtils"%>
<%

String image_file = (request.getParameter("image_name") != null) ? request.getParameter("image_name") : "";
String site_id = (request.getParameter("site_id") != null) ? request.getParameter("site_id") : "";

String resize_height = (request.getParameter("resize_height") != null) ? request.getParameter("resize_height") : "";
String resize_width = (request.getParameter("resize_width") != null) ? request.getParameter("resize_width") : "";

String result = "0";
String jsonResult = "{'status':'0','reason':'No data'}";

if (!image_file.equals("") && !site_id.equals("")  && !resize_width.equals("") && !resize_height.equals("") ) {
       String resizedImageName = ImageUtils.resizeImage(image_file, site_id, resize_width, resize_height);
       String resizedImagePath = ImageUtils.getSavedImagePathFromSiteId(site_id) + resizedImageName;
       String publicImagePath = ImageUtils.getPublicImagePathFromSiteId(site_id) + resizedImageName;
       jsonResult = "{'status':'1','image_name':'" + resizedImageName + "','system_path':'" + resizedImagePath + "','public_path':'" + publicImagePath + "'}";
}
out.print(jsonResult);
%>
