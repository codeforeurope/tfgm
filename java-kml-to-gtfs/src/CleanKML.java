/*
 * Author: Olli Aro
 * Date: 24/03/2013
 * Email: olli_aro@yahoo.co.uk
 * 
 * License: This code is available under GNU General Public License (http://www.gnu.org/licenses/gpl.html)
 * 
 * This class cleans TfGM provided KML file, so that it can be consumed by the Convert class.
 *
 * Usage:
 * 
 * Give source and target file names and locations and run this class.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;


public class CleanKML {

	//CONFIGURATION STARTS
	static final String sourceFileLocation = "c:\\temp\\temp\\OpenData_BusRoutes.KML";
	static final String targetFileLocation = "c:\\temp\\temp\\OpenData_BusRoutes_clean.KML";
	//CONFIGURATION ENDS
	
	public static void main(String[] args) {
		try{
			StringBuffer targetText = new StringBuffer();
			BufferedReader br = new BufferedReader( new FileReader(sourceFileLocation));
		    String strLine = "";
		    boolean readCoordinates = false;
		    boolean changedStatus = false;
		    String coordinates = "";
	        while( (strLine = br.readLine()) != null){
	        	if(strLine.toLowerCase().indexOf("<coordinates>")>-1){
	        		readCoordinates = true;
	        		targetText.append(strLine.substring(0, strLine.toLowerCase().indexOf("<coordinates>")+13).trim());
	        		if(strLine.toLowerCase().indexOf("<coordinates>")+13==strLine.length())
	        			coordinates = "";
	        		else
	        			coordinates = strLine.substring(strLine.toLowerCase().indexOf("<coordinates>")+13,strLine.length()-1).trim();
	        		changedStatus = true;
	        	}
	        	else if(strLine.toLowerCase().indexOf("</coordinates>")>-1){
	        		readCoordinates = false;
	        		targetText.append(coordinates.trim()+" ");
	        		targetText.append(strLine.substring(0,strLine.toLowerCase().indexOf("</coordinates>")).trim());
	        		targetText.append(strLine.substring(strLine.toLowerCase().indexOf("</coordinates>"),strLine.length()-1).trim());
	        		changedStatus = true;
	        	}else if(strLine.toLowerCase().indexOf("<coordinates>")>-1 && strLine.toLowerCase().indexOf("</coordinates>")>-1){
	        		targetText.append(strLine.trim());
	        		changedStatus = true;
	        	}
	        	
	        	if(!changedStatus){
		        	if(readCoordinates){
		        		if(!coordinates.equals(""))
		        			coordinates += " ";
		        		coordinates += strLine.trim();
		        	}else
		        		targetText.append(strLine.trim());
	        	}
	        	
	        	changedStatus = false;
	        }
	        
	       FileWriter fstream = new FileWriter(targetFileLocation);
           BufferedWriter out = new BufferedWriter(fstream);
           out.write(targetText.toString()+">");
           out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
