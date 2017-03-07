
/* this code was used in our bio widget in a modal after a post */
/* it creates an image from the url specified in the bio, then crops and resizes it to specific dimensions */
/* saveImageFromURL.jsp, cropImage.jsp, resizeImage.jsp call methods in the ImageUtils class which in turn is a wrapper on top of the command-line tool "imagemagick" */

            // first, save the url to the local file system
            jQuery.post("utilities/saveImageFromURL.jsp", {'image_url':imageurl, 'site_id':siteId }, function (response) {
                responseObj = eval('(' + response + ')');
                savedImage = responseObj['image_name'];
                // then crop the image
                jQuery.post("utilities/cropImage.jsp", {'image_name':savedImage,
                                                        'site_id':siteId,
                                                        'start_x':x,
                                                        'end_x':x1,
                                                        'start_y':y,
                                                        'end_y':y1 }, function (response) {
                     responseObj = eval('(' + response + ')');
                     croppedImage = responseObj['image_name'];
                     // then resize the image
                     jQuery.post("utilities/resizeImage.jsp",{'image_name':croppedImage,
                                                              'site_id': siteId,
                                                              'resize_width': 176,
                                                              'resize_height': 220}, function(response) {
                         responseObj = eval('(' + response + ')');
                         resizedImage = responseObj['image_name'];
                         newimgurl = responseObj['public_path'];
                         jQuery("#bioimagesave").val(newimgurl);
                         jQuery(".bioWidgetImageEdit img").attr("src",newimgurl);
                         jQuery(".imageModal").dialog("close");
                     });
                });
            });
