package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

public class WaypointJavaServerConnection implements Runnable
{
	JSONArray waypoints;
	Socket socket;
	BufferedReader reader;
	OutputStream output;
	
	public WaypointJavaServerConnection(Socket socket, JSONArray waypoints)
	{
		this.waypoints = waypoints;
		this.socket = socket;
	}
	
	public void run()
	{		
		try 
		{
			InputStream stream = socket.getInputStream();
			//ObjectOutputStream output =  new ObjectOutputStream(socket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(stream));
			String input = "{";
			char i;
			int c = 0;
			while(true){
				i = (char)stream.read();
				if(i == '{'){
					c++;
					while(c != 0){
						i = (char)stream.read();
						if(i == '}'){
							c--;
						}
						if(i == '{'){
							c++;
						}
						input += i;
					}
					break;
				}
			}
			build(input);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public void build(String input)
	{
		try
		{
			JSONObject obj = new JSONObject(input);
			String method = (String)obj.get("method");
			int id = obj.getInt("id");
			
			if(method.equals("getWaypoints"))
			{
				send(packageRequest(getWaypoints(),id)); 
			}
			else if(method.equals("getWaypoint"))
			{
				String name = obj.getJSONArray("params").getString(0);
				
				send(packageRequest(getWaypoint(name),id)); 
			}
			else if(method.equals("removeWaypoint"))
			{
				String name = obj.getJSONArray("params").getString(0);
				send(packageRequest(remove(name),id));
			}
			else if (method.equals("add"))
			{
				JSONArray params = obj.getJSONArray("params");
				double lat = params.getDouble(0);
				double lon = params.getDouble(1);
				double ele = params.getDouble(2);
				String name = params.getString(3);
				
				send(packageRequest(add(lat,lon,ele,name),id));
			}
			else if (method.equals("addWaypoint"))
			{
				JSONObject params = obj.getJSONArray("params").getJSONObject(0);
				double lat = params.getDouble("lat");
				double lon = params.getDouble("lon");
				double ele = params.getDouble("ele");
				String name = params.getString("name");
				
				send(packageRequest(add(lat,lon,ele,name),id));
			}
			else if (method.equals("getDistanceGCTo"))
			{
				JSONArray params = obj.getJSONArray("params");
				
				JSONObject waypoint1 = getWaypoint(params.getString(0));
				JSONObject waypoint2 = getWaypoint(params.getString(1));
				
				Waypoint waypoint1Actual = new Waypoint(waypoint1.getDouble("lat"),waypoint1.getDouble("lon"),waypoint1.getDouble("ele"),waypoint1.getString("name"));
				Waypoint waypoint2Actual = new Waypoint(waypoint2.getDouble("lat"),waypoint2.getDouble("lon"),waypoint2.getDouble("ele"),waypoint2.getString("name"));
				
				send(packageRequest(waypoint1Actual.distanceGCTo(waypoint2Actual, 0),id));
			}
			else if (method.equals("getBearingGCInitTo"))
			{
				JSONArray params = obj.getJSONArray("params");
				
				JSONObject waypoint1 = getWaypoint(params.getString(0));
				JSONObject waypoint2 = getWaypoint(params.getString(1));
				
				Waypoint waypoint1Actual = new Waypoint(waypoint1.getDouble("lat"),waypoint1.getDouble("lon"),waypoint1.getDouble("ele"),waypoint1.getString("name"));
				Waypoint waypoint2Actual = new Waypoint(waypoint2.getDouble("lat"),waypoint2.getDouble("lon"),waypoint2.getDouble("ele"),waypoint2.getString("name"));
				
				send(packageRequest(waypoint1Actual.bearingGCInitTo(waypoint2Actual, 0),id));
			}
			else
			{
				//send("", id);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public JSONObject packageRequest(Object result, int id) throws IOException
	{
		JSONObject response = new JSONObject();
		response.put("id", id);
		response.put("jsonrpc", "2.0");
		response.put("result", result);
		return response;
		
	}
	
	public void send(JSONObject str) throws IOException
	{
		OutputStream out = socket.getOutputStream();
		
		String header = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ";
		header += str.toString().length() + "\r\n\r\n";
		
		out.write(header.getBytes());
		out.write(str.toString().getBytes());
		
		out.flush();
		out.close();
		
		socket.close();
 	}
	
	JSONObject getWaypoints()
	{
		JSONObject waypointsObject = new JSONObject();
		
		waypointsObject.put("waypoints", waypoints);
		
		System.out.println("SENT:		" + waypointsObject + "\n");
		
		return waypointsObject;
	}
	
	boolean add(double latInput, double lonInput, double eleInput, String nameInput)
	{
		JSONObject temp = new JSONObject();
		
		temp.put("name", nameInput);
		temp.put("lat", latInput);
		temp.put("lon", lonInput);
		temp.put("ele", eleInput);
		
		if (getWaypointIndex(nameInput) == -1)
		{
			waypoints.put(temp);
			System.out.println("Added:		" + "Waypoint(" + latInput + ", " + lonInput + ", " + eleInput + ", " + nameInput + ");" + "\n");
			return true;
		}
		else
		{
			System.out.println("ERROR:		" + "Waypoint(" + latInput + ", " + lonInput + ", " + eleInput + ", " + nameInput + ") already exists;" + "\n");
			return false;
		}
	}
	
	boolean remove(String nameInput)
	{
		int index = getWaypointIndex(nameInput);
		
		if (waypoints.remove(index) != null)
		{
			System.out.println("Removed:	" + "Waypoint(" + nameInput + ");"+ "\n");
			return true;
		}
		else 
		{
			System.out.println("ERROR:		" + "Could not remove Waypoint(" + nameInput + ");"+ "\n");
			return false;
		}
	}
	
	JSONObject getWaypoint(String nameInput)
	{
		int index = getWaypointIndex(nameInput);
		if (index != -1)
		{
			return waypoints.getJSONObject(index);
		}
		else
		{
			JSONObject errorResponse = new JSONObject();
			errorResponse.put("result", "ERROR");
			return errorResponse;
		}
	}
	
	int getWaypointIndex(String nameInput)
	{
		for (int i = 0; i < waypoints.length(); i++)
		{
			JSONObject temp = waypoints.getJSONObject(i);
			if (nameInput.compareTo(temp.getString("name")) == 0)
			{
				return i;
			}
		}
		return -1;
	}
}
