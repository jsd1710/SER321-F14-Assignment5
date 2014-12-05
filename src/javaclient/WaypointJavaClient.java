package javaclient;

import java.io.*;
import java.util.*;
import java.net.URL;

import org.json.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.text.DecimalFormat;

public class WaypointJavaClient extends WaypointGUI implements WaypointInterface, ActionListener, ItemListener 
{
	private DecimalFormat format = new DecimalFormat("#.000");
	
	public String serviceURL;
	public JsonRpcRequestViaHttp server;
	public static int id = 0;

	public WaypointJavaClient(String serviceURL) 
	{
		super("JDobkins");
		removeWPButt.addActionListener(this);
		addWPButt.addActionListener(this);
		modWPButt.addActionListener(this);
		getAddrButt.addActionListener(this);
		distBearButt.addActionListener(this);
		
		fromWaypointBox.addItemListener(this);
		toWaypointBox.addItemListener(this);
		
		this.serviceURL = serviceURL;
		try 
		{
			this.server = new JsonRpcRequestViaHttp(new URL(serviceURL));
			buildWaypointsList();
			System.out.println();
		} 
		catch (Exception ex) 
		{
			System.out.println("Malformed URL " + ex.getMessage());
		}
	}
	

	private boolean packageWaypointCallBoolean(String method, String params) 
	{
		boolean result = false;
		try 
		{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("jsonrpc", "2.0");
			jsonObj.put("method", method);
			jsonObj.put("id", ++id);
			
			String almost = jsonObj.toString();

			String toInsert = ",\"params\":" + params;

			String begin = almost.substring(0, almost.length() - 1);
			String end = almost.substring(almost.length() - 1);

			String ret = begin + toInsert + end;
			String resultString = server.call(ret);
			
			JSONObject resultJSON = new JSONObject(resultString);
			result = resultJSON.getBoolean("result");
			
			return result;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private double packageWaypointCallDouble(String method, String params) 
	{
		double result = 0;
		try 
		{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("jsonrpc", "2.0");
			jsonObj.put("method", method);
			jsonObj.put("id", ++id);
			
			String almost = jsonObj.toString();

			String toInsert = ",\"params\":" + params;

			String begin = almost.substring(0, almost.length() - 1);
			String end = almost.substring(almost.length() - 1);

			String ret = begin + toInsert + end;
			String resultString = server.call(ret);
			
			JSONObject resultJSON = new JSONObject(resultString);
			result = resultJSON.getDouble("result");
			
			return result;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return result;
		}
	}
	

	private JSONObject packageWaypointCallJSON(String method, String params) 
	{
		JSONObject result = new JSONObject();
		result.put("result","ERROR");
		
		try 
		{
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("jsonrpc", "2.0");
			jsonObj.put("method", method);
			jsonObj.put("id", ++id);
			
			String almost = jsonObj.toString();

			String toInsert = ",\"params\":" + params;

			String begin = almost.substring(0, almost.length() - 1);
			String end = almost.substring(almost.length() - 1);

			String ret = begin + toInsert + end;
			String resultString = server.call(ret);
			System.out.println(resultString);
			JSONObject resultJSON = new JSONObject(resultString);
			result = resultJSON.getJSONObject("result");
			
			return result;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return result;
		}
	}
	
	
	public boolean add(double lat, double lon, double ele, String name) 
	{
		String toInsert = 
				"[" +
				String.valueOf(lat) + 
				", " + String.valueOf(lon) + 
				", " + String.valueOf(ele) + 
				", \"" + name + "\"" +
				"]";
		
		boolean added = packageWaypointCallBoolean("add", toInsert);
		if (added) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
	public boolean removeWaypoint(String name) 
	{
		String toInsert = "[\"" + name + "\"]";
		
		boolean removed = packageWaypointCallBoolean("removeWaypoint", toInsert);
		if (removed) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
	public boolean addWaypoint(String jsonString) 
	{
		JSONObject waypointJSON = new JSONObject(jsonString);
		
		String toInsert = "[" + jsonString + "]";
		
		boolean added = packageWaypointCallBoolean("addWaypoint", toInsert);
		if (added) 
		{
			return true;
		} 
		else 
		{
			return false;
		}
	}
	
	public JSONObject getWaypoint(String name)
	{
		String toInsert = "[\"" + name + "\"]";
		JSONObject waypointObject = packageWaypointCallJSON("getWaypoint", toInsert);
		
		return waypointObject;
	}
	
	public JSONArray getWaypoints()
	{
		JSONArray waypointArray = new JSONArray();
		
		JSONObject callObject = packageWaypointCallJSON("getWaypoints", "[]");
		
		if (callObject.has("result"))
		{
			System.out.println("There was an error retrieving the Waypoints!");
		}
		else
		{
			waypointArray = callObject.getJSONArray("waypoints");
			System.out.println("Successfully retrieved Waypoints Array");
		}
		
		return waypointArray;
	}
	
	public double getDistanceGCTo(String w1, String w2)
	{
		String toInsert = "[\"" + w1 + "\",\"" + w2 + "\"]";
		return packageWaypointCallDouble("getDistanceGCTo", toInsert);
	}

	public double getBearingGCInitTo(String w1, String w2)
	{
		String toInsert = "[\"" + w1 + "\",\"" + w2 + "\"]";
		return packageWaypointCallDouble("getBearingGCInitTo", toInsert);
	}

	public String serviceInfo() 
	{
		return "Service information";
	}
	
	public void actionPerformed(ActionEvent ae) 
	{
		String action = ae.getActionCommand();
		
		if (action.equals("Remove")) 
		{
			try 
			{
				if (removeWaypoint(fromWaypointBox.getSelectedItem().toString()))
				{
					System.out.println("Successfully removed: " + fromWaypointBox.getSelectedItem().toString());
					buildWaypointsList();
				}
				else
				{
					System.out.println("Failed to remove: " + fromWaypointBox.getSelectedItem().toString());
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else if (action.equals("Add")) 
		{
			try 
			{
				if (add(Double.parseDouble(latIn.getText()), Double.parseDouble(lonIn.getText()), Double.parseDouble(eleIn.getText()), nameIn.getText()))
				{
					System.out.println("Successfully added: " + nameIn.getText());
					buildWaypointsList();
				}
				else
				{
					System.out.println("Failed to add: " + nameIn.getText());
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
		}
		else if (action.equals("Modify"))
		{
			try 
			{
				if (!removeWaypoint(nameIn.getText()))
				{
					System.out.println("Cannot modify waypoint \"" + nameIn.getText() + "\" because it does not exist!");
					System.out.println("Instead of modifying, \"" + nameIn.getText() + "\" will be added instead.");
				}
				add(
						Double.parseDouble(latIn.getText()), 
						Double.parseDouble(lonIn.getText()), 
						Double.parseDouble(eleIn.getText()), 
						nameIn.getText()
						);
				
				System.out.println("Successfully modified: " + nameIn.getText());
				buildWaypointsList();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		else if (action.equals("Distance"))
		{
			try 
			{
				String w1 = fromWaypointBox.getSelectedItem().toString();
				String w2 = toWaypointBox.getSelectedItem().toString();
				distBearIn.setText(format.format(getDistanceGCTo(w1, w2)) + "/" + format.format(getBearingGCInitTo(w1, w2)));
				System.out.println("Successfully calculated distance/bearing: " + distBearIn.getText());
				buildWaypointsList();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				distBearIn.setText("dist/bear");
				System.out.println("Failed to calculate distance/bearing");
			}
		}
		
		System.out.println();
	}
	
	public void itemStateChanged(ItemEvent e) 
	{
		if(e.getStateChange() == ItemEvent.SELECTED)
		{
			String waypointItem = (String)e.getItem();
			
			if (e.getItem() != "No Waypoints")
			{
				try 
				{
					JSONObject waypointJSONSelected = getWaypoint(waypointItem);
					if (waypointJSONSelected.has("result"))
					{
						System.out.println("Error Selecting " + waypointItem);
						latIn.setText("0");
						lonIn.setText("0");
						eleIn.setText("0");
						nameIn.setText("ERROR");
					}
					else
					{
						latIn.setText(String.valueOf(waypointJSONSelected.getDouble("lat")));
						lonIn.setText(String.valueOf(waypointJSONSelected.getDouble("lon")));
						eleIn.setText(String.valueOf(waypointJSONSelected.getDouble("ele")));
						nameIn.setText(waypointJSONSelected.getString("name"));
					}
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				latIn.setText("0");
				lonIn.setText("0");
				eleIn.setText("0");
				nameIn.setText("No Waypoint selected");
			}
			
		}
	}

	private void buildWaypointsList() 
	{
		try 
		{
			waypointsArray = getWaypoints();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		if (waypointsArray.length() != 0)
		{
			fromWaypointBox.removeAllItems();
			for (int i = 0; i < waypointsArray.length(); i++) // Adding From Waypoints
			{
				JSONObject temp = waypointsArray.getJSONObject(i);
				fromWaypointBox.addItem(temp.get("name"));	
			}
			
			toWaypointBox.removeAllItems();
			for (int i = 0; i < waypointsArray.length(); i++) // Adding To Waypoints
			{
				JSONObject temp = waypointsArray.getJSONObject(i);
				toWaypointBox.addItem(temp.get("name"));
			}
			System.out.println("Rebuilt Waypoint Dropdowns.");
		}
		else
		{
			fromWaypointBox.removeAllItems();
			fromWaypointBox.addItem("No Waypoints");
			toWaypointBox.removeAllItems();
			toWaypointBox.addItem("No Waypoints");
			System.out.println("There are no waypoints.");
		}
	}
	
	public static void main(String args[]) 
	{
		try 
		{
			String url = "http://127.0.0.1:8080/";

			if (args.length > 1) 
			{
				url = "http://" + args[0] + ":" + args[1] + "/";
			}
			
			System.out.println("Connection to: " + url);
			WaypointJavaClient wjc = new WaypointJavaClient(url);
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Oops, you didn't enter the right stuff");
		}
	}
}
