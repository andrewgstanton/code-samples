<%@ page import="com.ImageUtils"%>
<%

String image_file = (request.getParameter("image_name") != null) ? request.getParameter("image_name") : "";
String site_id = (request.getParameter("site_id") != null) ? request.getParameter("site_id") : "";

String start_x = (request.getParameter("start_x") != null) ? request.getParameter("start_x") : "";
String start_y = (request.getParameter("start_y") != null) ? request.getParameter("start_y") : "";

String end_x = (request.getParameter("end_x") != null) ? request.getParameter("end_x") : "";
String end_y = (request.getParameter("end_y") != null) ? request.getParameter("end_y") : "";

String result = "0";
String jsonResult = "{'status':'0','reason':'No data'}";

if (!image_file.equals("") && !site_id.equals("") && !start_x.equals("") && !end_x.equals("") && !start_y.equals("") && !end_y.equals("") ) {
       String croppedImageName = ImageUtils.cropImage(image_file, site_id, start_x, end_x, start_y, end_y);
       String croppedImagePath = ImageUtils.getSavedImagePathFromSiteId(site_id) + croppedImageName;
       String publicImagePath = ImageUtils.getPublicImagePathFromSiteId(site_id) + croppedImageName;
       jsonResult = "{'status':'1','image_name':'" + croppedImageName + "','system_path':'" + croppedImagePath + "','public_path':'" + publicImagePath + "'}";
}
out.print(jsonResult);
%>
