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
			if(method.equals("getWaypoint"))
			{
				String name = obj.getJSONArray("params").getString(0);
				int index = getWaypointIndex(name);
				JSONObject temp = waypoints.getJSONObject(index);
				send(packageRequest(temp,id)); 
			}
			else if(method.equals("removeWaypoint"))
			{
				String name = obj.getJSONArray("params").getString(0);
				int index = getWaypointIndex(name);
				waypoints.remove(index);
				boolean result = true;
				send(packageRequest(result,id));
			}
			else if (method.equals("add"))
			{
				JSONArray params = obj.getJSONArray("params");
				double lat = params.getDouble(0);
				double lon = params.getDouble(1);
				double ele = params.getDouble(2);
				String name = params.getString(3);
				
				boolean result = add(lat,lon,ele,name);
				send(packageRequest(result,id));
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
		out.write("HTTP/1.1 200 OK\r\nDate: Mon, 23 May 2005 22:38:34 GMT\r\nContent-Encoding: gzip\r\n\r\n".getBytes());
		GZIPOutputStream gzip = new GZIPOutputStream(out);

		gzip.write(str.toString().getBytes("utf-8"));
		gzip.close();
		out.flush();
		out.close();
		socket.close();
 	}
	
	JSONObject getWaypoints()
	{
		JSONObject waypointsObject = new JSONObject();
		
		waypointsObject.put("waypoints", waypoints);
		
		System.out.println(waypointsObject);
		
		return waypointsObject;
	}
	
	boolean add(double latInput, double lonInput, double eleInput, String nameInput)
	{
		JSONObject temp = new JSONObject();
		temp.put("name", nameInput);
		temp.put("lat", latInput);
		temp.put("lon", lonInput);
		temp.put("ele", eleInput);
		waypoints.put(temp);
		System.out.println("Added:		" + "Waypoint(" + latInput + ", " + lonInput + ", " + eleInput + ", " + nameInput + ");" );
		return true;
	}
	
	int getWaypointIndex(String name)
	{
		for (int i = 0; i < waypoints.length(); i++)
		{
			JSONObject temp = waypoints.getJSONObject(i);
			if (name.compareTo(temp.getString("name")) == 0)
			{
				return i;
			}
		}
		return -1;
	}
}
