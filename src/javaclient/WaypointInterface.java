package javaclient;
import org.json.*;
/**
 * Purpose: demonstrate connecting to a CPP JsonRpc http service
 * @author Tim Lindquist (tim.lindquist@asu.edu), ASU Software Engineering
 * @version July 2014
 */
public interface WaypointInterface 
{
   public boolean add(double lat, double lon, double ele, String name);
   public boolean addWaypoint(String jsonString);
   
   public boolean removeWaypoint(String name);
   
   public JSONObject getWaypoint(String name);
   public JSONArray getWaypoints();
   
   public double getDistanceGCTo(String w1, String w2);
   public double getBearingGCInitTo(String w1, String w2);
   
   public String serviceInfo();
}
