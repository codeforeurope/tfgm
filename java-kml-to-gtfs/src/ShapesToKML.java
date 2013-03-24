/*
 * Author: Olli Aro
 * Date: 24/03/2013
 * Email: olli_aro@yahoo.co.uk
 * 
 * License: This code is available under GNU General Public License (http://www.gnu.org/licenses/gpl.html)
 * 
 * This class converts GTFS shapes to KML file, so it is easier to check on map how they look like.
 *
 * Usage:
 * 
 * Configure all file locations below and run the class.
 */

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;


public class ShapesToKML {

	//CONFIGURATION STARTS
	static final String sourceFileLocation = "c:\\temp\\temp\\shapes.txt";
	static final String targetFileLocation = "c:\\temp\\temp\\shapes.KML";
	//CONFIGURATION ENDS
	
	public static void main(String[] args) {
		try{
			Random rand = new Random();
			StringBuffer targetText = new StringBuffer();
			targetText.append("<?xml version=\"1.0\" encoding=\"utf-8\"?><kml xmlns=\"http://earth.google.com/kml/2.1\"><Folder><description><![CDATA[Shape in KML]]></description>");
			BufferedReader br = new BufferedReader( new FileReader(sourceFileLocation));
		    String strLine = "";
		    String shapeId = "";
		    boolean firstShape = true;
		    while( (strLine = br.readLine()) != null){
	        	String[] fields = strLine.split(",");
	        	if(!shapeId.equals(fields[0])){
	        		float r = rand.nextFloat();
	        		float g = rand.nextFloat();
	        		float b = rand.nextFloat();
	        		Color randomColor = new Color(r, g, b);
	        		String randomColourHex = toHex(randomColor.getRed(),randomColor.getGreen(),randomColor.getBlue());
	        		if(!firstShape)
	        			targetText.append("</coordinates></LineString></Placemark>");
	        		firstShape = false;
	        		targetText.append("<Placemark><name><![CDATA["+fields[0]+"]]></name><description><![CDATA["+fields[0]+"]]></description><visibility>1</visibility><open>0</open><Style><LineStyle><color>"+randomColourHex+"</color><width>3</width></LineStyle></Style><LineString><extrude>1</extrude><altitudeMode>clampToGround</altitudeMode><tessellate>1</tessellate><coordinates>");
	        		shapeId = fields[0];
	        	}
	        	targetText.append(" "+fields[2]+","+fields[1]);
	        }
		    targetText.append("</coordinates></LineString></Placemark></Folder></kml>");
		    
		    FileWriter fstream = new FileWriter(targetFileLocation);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(targetText.toString());
            out.close();
        }catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String toHex(int r, int g, int b) {
	    return "FF"+ toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b);
	  }

	  private static String toBrowserHexValue(int number) {
	    StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
	    while (builder.length() < 2) {
	      builder.append("0");
	    }
	    return builder.toString().toUpperCase();
	  }
}
