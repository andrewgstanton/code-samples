/*
 * Copyright (c) 1997.  Sun Microsystems. All rights reserved.
 *
 * Author Andy Stanton
 */

package ****;

import java.io.*;
import java.util.*;

public class Serializer {

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException ,
                                                        ClassNotFoundException {
        byte [] data = Base64Coder.decode( s );
        ObjectInputStream ois = new ObjectInputStream( 
                                        new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return new String( Base64Coder.encode( baos.toByteArray() ) );


    }

    public static String setDataFromHash(Hashtable contentHashSet) {
        String widgetDataReturned = "";
        if (contentHashSet != null) {
           try {
               widgetDataReturned = (String) Serializer.toString(contentHashSet);
               widgetDataReturned = GenUtils.escapeString(widgetDataReturned);
            } catch (Exception ex) {
                 System.out.println("Serializer.setDataFromHash: Exception: " + ex.toString());
            }
         }
         return widgetDataReturned;
    }

     public static Hashtable setHashFromData(String widgetDataSet) {
//     	System.out.print("widgetDataSet Length:"+widgetDataSet.length());
        Hashtable contentHashReturned = null; 
        if (!widgetDataSet.equals("")) {
           try {
              contentHashReturned = (Hashtable) Serializer.fromString(GenUtils.unEscapeString(widgetDataSet));
            } catch (Exception ex) {
                 System.out.println("Serializer.setHashFromData: Exception: " + ex.toString());
            }
         }
         return contentHashReturned;
     }
};

