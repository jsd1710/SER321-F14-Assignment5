package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import org.json.JSONArray;

public class WaypointJavaServer 
{
	private static JSONArray waypoints;
	protected ServerSocket server = null;
	
	public WaypointJavaServer(int port)
	{
		waypoints = new JSONArray();
		try
		{
			server = new ServerSocket(port);
			while (true)
			{
				Socket socket = server.accept();
				new Thread(new WaypointJavaServerConnection(socket,waypoints)).start();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		int port = 8080;
		
		if (args.length > 1)
		{
			port = Integer.parseInt(args[1]);
		}
		
		new WaypointJavaServer(port);
	}
}
