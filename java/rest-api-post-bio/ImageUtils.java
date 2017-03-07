//
// Copyright (c) 2011.  Emochila, Inc. All rights reserved.
//
// Author Geoff
//

package com;

// for general string manipulation functions
import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;
import java.util.Calendar;
import java.util.Date;

// for email validation methods
import java.util.regex.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

// for xml parsing functions
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

 /**
 * Image Utils for manipulating images via image magick
 * 
 */

public class ImageUtils {

	public static String getDefaultSavedImagePath() {
		String imagePath = "/var/lib/tomcat5.5/webapps/swserve/siteAssets/";
		return imagePath;
	}

        public static String getSavedImagePathFromSiteId(String siteId) {
             String imagePath = ""; 
             if (!siteId.equals("")) {
		 // create the image path if it doesn't exist
                 imagePath = "/var/lib/tomcat5.5/webapps/swserve/siteAssets/site" + siteId + "/images/";
		 File fileImagePath = new File(imagePath);
		 // create the image path if it doesn't exist
		if (!fileImagePath.exists()) {
			GenUtils.createDirectory(fileImagePath);
		}
             }
             return imagePath;
        }

        public static String getPublicImagePathFromSiteId(String siteId) {
             String imagePath = "";
             if (!siteId.equals("")) {
                 //domainName = DBUtils.getPrimaryDomainFromSiteId(siteId)
                 imagePath = "https://secure.emochila.com/swserve/siteAssets/site" + siteId + "/images/";
             }
             return imagePath;
        }
	
       
        public static Hashtable getImageParms(String imageUrl) {

               Hashtable imageParms = new Hashtable();
               String imagePath = "";
               String imageName = imageUrl;  
               int i = 0;             
               if (!imageUrl.equals("")) {
                  String[] imageParmsArray = imageUrl.split("/");
                  if (imageParmsArray.length > 1) {
                    for (i=0;i<imageParmsArray.length-1;i++) {
                         if (i==0) {
                            imagePath = imageParmsArray[i];
                         } else {
                            imagePath = imagePath + "/" + imageParmsArray[i];
                         }
                    }
                    imageName = imageParmsArray[i];
                  }
               } 
               imageParms.put("imagePath", imagePath);
               imageParms.put("imageName", imageName); 
       
               return imageParms; 
        } 

	public static String saveImageFromURL(String imageUrl, String siteId) {

             String inputImageName = "";
             String inputImagePath = "";
             String savedImageName = "";
             String savedImagePath = "";

             if (!siteId.equals("") && !imageUrl.equals("")) {
                Hashtable imageParms = getImageParms(imageUrl);
                inputImageName = (imageParms.get("imageName") != null) ? (String) imageParms.get("imageName") : "";
                inputImagePath = (imageParms.get("imagePath") != null) ? (String) imageParms.get("imagePath") : "";
                
                if (!inputImageName.equals("") && !inputImagePath.equals("")) {

                    String fileType = inputImageName.substring(inputImageName.length() - 4);

                    // get timestamp -- make this part of the file for uniqueness
                    java.util.Date now = new java.util.Date();
                    java.sql.Timestamp ts = new java.sql.Timestamp(now.getTime());

                    savedImageName = "image_" + ts.getTime() + "_saved" + fileType;
                    savedImagePath = getSavedImagePathFromSiteId(siteId) + savedImageName;

                    String save_command = "/usr/bin/convert " + imageUrl + " " + savedImagePath;
		    // run the command on the linux shell
		    GenUtils.runShellCommand(save_command);
                 } 
            }
            return savedImageName;
      }


      public static String cropImage(String imageName, 
                                     String siteId,
                                     String startX,
                                     String endX,
                                     String startY,
                                     String endY) {


            String inputImagePath = "";
            String croppedImageName = "";
            String croppedImagePath = "";
            
            if (!imageName.equals("") && !siteId.equals("") && !startX.equals("") && !endX.equals("") && !startY.equals("") && !endY.equals("") ) {

                    String fileType = imageName.substring(imageName.length() - 4);

                    // get timestamp -- make this part of the file for uniqueness
                    java.util.Date now = new java.util.Date();
                    java.sql.Timestamp ts = new java.sql.Timestamp(now.getTime());

                    inputImagePath = getSavedImagePathFromSiteId(siteId) + imageName;
                    croppedImageName = "image_" + ts.getTime() + "_cropped" + fileType;
                    croppedImagePath = getSavedImagePathFromSiteId(siteId) + croppedImageName;

                    // get cropping parameters for image magick
                    int cropWidth = Integer.parseInt(endX) - Integer.parseInt(startX);
                    int cropHeight = Integer.parseInt(endY) - Integer.parseInt(startY);

                    String geometry = cropWidth + "x" + cropHeight + "+" + startX + "+" + startY;
                    String crop_command = "/usr/bin/convert " + inputImagePath + " -crop '" + geometry + "' +repage " + croppedImagePath;
		    // crop the image via imagemagick
		    GenUtils.runShellCommand(crop_command); 
           }
           return croppedImageName; 
      }

      public static String resizeImage(String imageName, String siteId, String resizeWidth, String resizeHeight) {

         String inputImagePath = "";
         String resizedImageName = "";
         String resizedImagePath = "";

         if (!imageName.equals("") && !siteId.equals("") && !resizeWidth.equals("") && !resizeHeight.equals("") ) {

                    String fileType = imageName.substring(imageName.length() - 4);

                    // get timestamp -- make this part of the file for uniqueness
                    java.util.Date now = new java.util.Date();
                    java.sql.Timestamp ts = new java.sql.Timestamp(now.getTime());

                    inputImagePath = getSavedImagePathFromSiteId(siteId) + imageName;
                    resizedImageName = "image_" + ts.getTime() + "_resized" + fileType;
                    resizedImagePath = getSavedImagePathFromSiteId(siteId) + resizedImageName;

                    String command = "/usr/bin/convert " + inputImagePath + " -resize " + resizeWidth + "x" + resizeHeight + " " + resizedImagePath;
                    //out.print("/emoadmin/sw/utilities/resizeImage: command: " + command + "<br />\n");
		    GenUtils.runShellCommand(command);
           }
           return resizedImageName;
      }

};

