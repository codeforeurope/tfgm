/*
 * Author: Olli Aro
 * Date: 24/03/2013
 * Email: olli_aro@yahoo.co.uk
 * 
 * License: This code is available under GNU General Public License (http://www.gnu.org/licenses/gpl.html)
 * 
 * This class converts route shapes from KML format to GTFS shapes.
 *
 * Usage:
 * 
 * Configure all file locations below and run the class.
 */

import helpers.Point;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Convert {
	
	//CONFIGURATION STARTS
	static final String sourceFileLocation = "c:\\temp\\temp\\OpenData_BusRoutes_clean.KML";
	static final String routesFileLocation = "c:\\temp\\temp\\routes.txt";
	static final String shapesFileLocation = "c:\\temp\\temp\\shapes.txt";
	static final String tripsCurrentFileLocation = "c:\\temp\\temp\\trips.txt";
	static final String tripsNewFileLocation = "c:\\temp\\temp\\trips.txt.new";
	static final String coordinateSeparator = " ";
	static final boolean generateFiles = true;
	static final boolean kmlLngBeforeLat = true;
	static final int firstSoManyShapesOnly = -1;
	//CONFIGURATION ENDS
	
	static HashMap<String, String> shapes;

	public static void main(String argv[]) {
		
		shapes = new HashMap<String, String>();
		 
	    try {
	    	
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
	 
		DefaultHandler handler = new DefaultHandler() {
	 
		boolean placemark = false;
		boolean description = false;
		boolean coordinates = false;
		
		String identifier = "";
		ArrayList<ArrayList<Point>> coordinatesValue = new ArrayList<ArrayList<Point>>();
		
		public void startElement(String uri, String localName,String qName, 
	                Attributes attributes) throws SAXException {
	 
			//System.out.println("Start Element :" + qName);
			
			if (qName.equalsIgnoreCase("Placemark")) {
				//System.out.println("START");
				coordinatesValue = new ArrayList<ArrayList<Point>>();
			}
	 
			if (qName.equalsIgnoreCase("DESCRIPTION")) {
				description = true;
			}
			
			if (qName.equalsIgnoreCase("coordinates")) {
				coordinates = true;
			}
	
		}
	 
		public void endElement(String uri, String localName,
			String qName) throws SAXException {
			
			if (qName.equalsIgnoreCase("Placemark")) {
				String coordinatesValueString = "";
				boolean firstItem = true;
				coordinatesValue = cleanDublicates(coordinatesValue);
				if(coordinatesValue.size()>0){
					coordinatesValue = sortcoordinatesValue(coordinatesValue);
					for(ArrayList<Point> inner:coordinatesValue){
						for(Point item : inner){
							if(!firstItem)
								coordinatesValueString += " ";
							else
								firstItem = false;
							coordinatesValueString += item.getLat()+","+item.getLng();
						}
					}
					//Only add the first shape for the route. Ignore different shapes e.g. for the weekend.
					if(shapes.get(identifier)==null){
						System.out.println("Identifier:"+identifier);
						System.out.println("Coordinates:"+coordinatesValueString);
						shapes.put(identifier, coordinatesValueString);
					}
				}
			}
	 
		}
	 
		public void characters(char ch[], int start, int length) throws SAXException {
	 
			if (description) {
				String value = new String(ch, start, length).trim();
				if(value.indexOf("Route_Desc</td><td>")>0){
					value = value.substring(value.indexOf("Route_Desc</td><td>")+19,value.length());
					identifier = value.substring(0,value.indexOf("</td>"));
				}
				
				//System.out.println("Description : " + new String(ch, start, length));
				description = false;
			}
			
			if (coordinates) {
				String value = new String(ch, start, length).trim();
				if(value.indexOf(",")>-1){
					ArrayList<Point> innerList = new ArrayList<Point>();
					for(String pair : value.split(coordinateSeparator)){
						String[] pairItem = pair.split(",");
						if(kmlLngBeforeLat)
							innerList.add(new Point(Double.parseDouble(pairItem[1]),Double.parseDouble(pairItem[0])));
						else
							innerList.add(new Point(Double.parseDouble(pairItem[0]),Double.parseDouble(pairItem[1])));
					}
					//if(innerList.size()>3)
					coordinatesValue.add(innerList);
				}
				coordinates = false;
			}
		}
	 
	     };
	 
	       //Parse the XML.
	       saxParser.parse(sourceFileLocation, handler);
	       
	       if(generateFiles){
	       
		       //Read the routes file.
		       HashMap<String,String> routes = new HashMap<String,String>();
		       BufferedReader br = new BufferedReader( new FileReader(routesFileLocation));
		       String strLine = "";
	           while( (strLine = br.readLine()) != null){
	        	   String[] line = strLine.split(",");
	        	   if(shapes.containsKey(line[3]))
	        		   routes.put(line[3], line[0]);
	           }
	           
	           //Create the shapes file.
	           HashMap<String,String> shapeIds = new HashMap<String,String>();
	           StringBuffer shapesText = new StringBuffer();
	           shapesText.append("shape_id,shape_pt_lat,shape_pt_lon,shape_pt_sequence\n");
	           int sequence = 1;
	           int shapeCount = 0;
	           for(String key : shapes.keySet()){
	        	   if(firstSoManyShapesOnly==-1 || shapeCount<firstSoManyShapesOnly){
		        	   shapeIds.put(routes.get(key),routes.get(key)+"_shap");
		        	   for(String pair : shapes.get(key).split(" ")){
		        		   String[] latLng = pair.split(",");
		        		   shapesText.append(routes.get(key)+"_shap,"+latLng[0]+","+latLng[1]+","+sequence+"\n");
		        		   sequence++;
		        	   }
	        	   }
	        	   shapeCount++;
	           }
	           FileWriter fstream = new FileWriter(shapesFileLocation);
	           BufferedWriter out = new BufferedWriter(fstream);
	           out.write(shapesText.toString());
	           out.close();
	           
	           //Create new version of trips.txt
	           br = new BufferedReader( new FileReader(tripsCurrentFileLocation));
	           StringBuffer tripsText = new StringBuffer();
	           tripsText.append("route_id,service_id,trip_id,trip_headsign,shape_id\n");
		       strLine = "";
		       br.readLine();
		       int lineCount = 0;
		       int printCounter = 0;
	           while( (strLine = br.readLine()) != null){
	        	   String[] line = strLine.split(",");
	        	   tripsText.append(strLine + ",");
	        	   if(shapeIds.keySet().contains(line[0]))
	        		   tripsText.append(shapeIds.get(line[0]));
	        	   tripsText.append("\n");
	        	   lineCount++;
	        	   printCounter++;
	        	   if(printCounter==1000){
	        		   System.out.println(lineCount);
	        		   printCounter = 0;
	        	   }
	           }
	           System.out.println(lineCount);
	           fstream = new FileWriter(tripsNewFileLocation);
	           out = new BufferedWriter(fstream);
	           out.write(tripsText.toString());
	           out.close();
	       }
           
           System.out.println("DONE");
	 
	     } catch (Exception e) {
	       e.printStackTrace();
	     }
	 
	   }
	
	private static ArrayList<ArrayList<Point>> cleanDublicates(ArrayList<ArrayList<Point>> coordinatesValue){
		HashMap<String,Integer> indexHash = new HashMap<String,Integer>();
		ArrayList<Integer> dublicates = new ArrayList<Integer>();
		int index = 0;
		for(ArrayList<Point> list : coordinatesValue){
			String forwardString = "";
			String reverseString = "";
			//for(Point point : list)
			//	forwardString += point.getLng()+","+point.getLat()+" ";
			forwardString = list.get(0).getLng()+","+list.get(0).getLat()+" "+list.get(list.size()-1).getLng()+","+list.get(list.size()-1).getLat();
			Collections.reverse(list);
			//for(Point point : list)
			//	reverseString += point.getLng()+","+point.getLat()+" ";
			reverseString = list.get(0).getLng()+","+list.get(0).getLat()+" "+list.get(list.size()-1).getLng()+","+list.get(list.size()-1).getLat();
			if(indexHash.get(forwardString)==null && indexHash.get(reverseString)==null){
				indexHash.put(forwardString, new Integer(index));
				indexHash.put(reverseString, new Integer(index));
			}else
				dublicates.add(new Integer(index));
			index++;
		}
		
		ArrayList<ArrayList<Point>> cleaned = new ArrayList<ArrayList<Point>>();
		index = 0;
		for(ArrayList<Point> list : coordinatesValue){
			if(!dublicates.contains(new Integer(index)))
				cleaned.add(list);
		}
		return cleaned;
	}
	
	private static ArrayList<ArrayList<Point>> sortcoordinatesValue(ArrayList<ArrayList<Point>> coordinatesValue){
		ArrayList<ArrayList<Point>> sorted = new ArrayList<ArrayList<Point>>();
		int firstIndex = 0;
		double previousTotalDistance = 0;
		for(int j=0;j<coordinatesValue.size();j++){
			double totalDistance = 0;
			for(int i=0;i<coordinatesValue.size();i++){
				double distance = distance(coordinatesValue.get(j).get(0).getLat(), coordinatesValue.get(j).get(0).getLng(), coordinatesValue.get(i).get(coordinatesValue.get(i).size()-1).getLat(), coordinatesValue.get(i).get(coordinatesValue.get(i).size()-1).getLng()); 
				totalDistance += distance;
			}
			if(totalDistance>previousTotalDistance){
				previousTotalDistance = totalDistance;
				firstIndex = j;
			}
		}
		//System.out.println("FIRST INDEX:"+firstIndex);
		ArrayList<Point> firstList = coordinatesValue.get(firstIndex);
		sorted.add(firstList);
		coordinatesValue.remove(firstIndex);
		ArrayList<Point> previousList = firstList;
		while(coordinatesValue.size()>0){
			int removeIndex = 0;
			double previousDistance = -1;
			for(int i=0;i<coordinatesValue.size();i++){
				double distance = 0;
				for(Point point : previousList)
					distance += distance(point.getLat(), point.getLng(), coordinatesValue.get(i).get(0).getLat(), coordinatesValue.get(i).get(0).getLng()); 
				//System.out.println(previousDistance+" <> "+distance);
				if(previousDistance==-1 || previousDistance>distance){
					//System.out.println(previousDistance+" > "+distance);
					removeIndex = i;
					previousDistance = distance;
				}
			}
			//System.out.println("WINNER: "+previousDistance);
			sorted.add(coordinatesValue.get(removeIndex));
			previousList = coordinatesValue.get(removeIndex);
			coordinatesValue.remove(removeIndex);
		}
	
		return sorted;
	}
	
	private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return dist;
      }

      private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
      }

      private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
      }
}
