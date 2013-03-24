package helpers;

public class Point {
	
	private double _lat;
	private double _lng;
	
	public Point(double lat, double lng){
		_lat = lat;
		_lng = lng;
	}

	public void setLat(double lat){
		_lat = lat;
	}
	
	public void setLng(double lng){
		_lng = lng;
	}
	
	public double getLat(){
		return _lat;
	}
	
	public double getLng(){
		return _lng;
	}
}
