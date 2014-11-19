package server;

import java.lang.Math;
import java.io.*;
import java.util.*;
import java.text.NumberFormat;

/**
 * Class to perform various earth surface calculations. Given lat/lon points
 * this class can perform distance and bearing calculations - Great Circle, and
 * Rhumb-line.
 **/

public class Waypoint extends Object implements java.io.Serializable
{

	public final static int STATUTE = 0;
	public final static int NAUTICAL = 1;
	public final static int KMETER = 2;
	public final static double radiusE = 6371;

	public double lat; // degrees lat in DD.D format (+ north, - south)
	public double lon; // degrees lon in DD.D format (+ east, - west)
	public double ele; // elevation in feet MSL
	public String name; // a name for the waypoint

	public Waypoint(double lat, double lon, double ele, String name) 
	{
		this.lat = lat;
		this.lon = lon;
		this.ele = ele;
		this.name = name;
	}
	
	public double distanceGCTo(Waypoint wp, int scale) 
	{
		double ret = 0.0;
		double dlatRad = Math.toRadians(wp.lat - lat);
		double dlonRad = Math.toRadians(wp.lon - lon);
		double latOrgRad = Math.toRadians(lat);
		double lonOrgRad = Math.toRadians(lon);
		double a = Math.sin(dlatRad / 2) * Math.sin(dlatRad / 2)
				+ Math.sin(dlonRad / 2) * Math.sin(dlonRad / 2)
				* Math.cos(latOrgRad) * Math.cos(Math.toRadians(wp.lat));
		ret = radiusE * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
		// ret is in kilometers. switch to either Statute or Nautical?
		switch (scale) {
		case STATUTE:
			ret = ret * 0.62137119;
			break;
		case NAUTICAL:
			ret = ret * 0.5399568;
			break;
		}
		return ret;
	}

	public double bearingGCInitTo(Waypoint wp, int scale) 
	{
		double ret = 0.0;
		double dlatRad = Math.toRadians(wp.lat - lat);
		double dlonRad = Math.toRadians(wp.lon - lon);
		double latOrgRad = Math.toRadians(lat);
		double lonOrgRad = Math.toRadians(lon);
		double y = Math.sin(dlonRad) * Math.cos(Math.toRadians(wp.lat));
		double x = Math.cos(latOrgRad) * Math.sin(Math.toRadians(wp.lat))
				- Math.sin(latOrgRad) * Math.cos(Math.toRadians(wp.lat))
				* Math.cos(dlonRad);
		ret = Math.toDegrees(Math.atan2(y, x));
		ret = (ret + 360.0) % 360.0;
		return ret;
	}

	public double bearingRhumbTo(Waypoint wp, int scale) 
	{
		double latDiff = Math.toRadians(wp.lat - lat); // Δφ
		double lonDiff = Math.toRadians(wp.lon - lon); // Δλ
		double lat1R = Math.toRadians(lat); // Original latitude
		double lat2R = Math.toRadians(wp.lat); // Desitination latitude

		double projectedLatDiff = Math.log(Math.tan(Math.PI / 4 + lat2R / 2)
				/ Math.tan(Math.PI / 4 + lat1R / 2)); // Δψ

		double q = Math.abs(projectedLatDiff) > 10 * Math.exp(-12) ? latDiff
				/ projectedLatDiff : Math.cos(lat1R); 

		// if dLon over 180° take shorter rhumb across anti-meridian:
		if (Math.abs(lonDiff) > Math.PI)
			lonDiff = lonDiff > 0 ? -(2 * Math.PI - lonDiff)
					: (2 * Math.PI + lonDiff);

		double rhumbBearing = Math.toDegrees(Math.atan2(lonDiff,
				projectedLatDiff));
		
		while ((rhumbBearing < 0) || (rhumbBearing > 360)) 
		{
			if (rhumbBearing < 0)
			{
				rhumbBearing = 360 + rhumbBearing;
			}
			else if (rhumbBearing > 360)
			{
				rhumbBearing = 360 - rhumbBearing;
			}
		}

		return rhumbBearing;
	}

	public double distanceRhumbTo(Waypoint wp, int scale) 
	{
		double latDiff = Math.toRadians(wp.lat - lat); // Δφ
		double lonDiff = Math.toRadians(wp.lon - lon); // Δλ
		double lat1R = Math.toRadians(lat); // Original latitude
		double lat2R = Math.toRadians(wp.lat); // Desitination latitude

		double projectedLatDiff = Math.log(Math.tan(Math.PI / 4 + lat2R / 2)
				/ Math.tan(Math.PI / 4 + lat1R / 2));
		double q = Math.abs(projectedLatDiff) > 10 * Math.exp(-12) ? latDiff
				/ projectedLatDiff : Math.cos(lat1R);

		// if dLon over 180° take shorter rhumb across anti-meridian:
		if (Math.abs(lonDiff) > Math.PI)
			lonDiff = lonDiff > 0 ? -(2 * Math.PI - lonDiff)
					: (2 * Math.PI + lonDiff);

		double rhumbLineDist = Math.sqrt(latDiff * latDiff + q * q * lonDiff
				* lonDiff)
				* radiusE;

		return rhumbLineDist;
	}

	public void print() {
		System.out.println("Waypoint " + name + ": lat " + lat + " lon " + lon
				+ " elevation " + ele);
	}

	public static void main(String args[]) {
		try {
			String url = "http://127.0.0.1:8080/";
			if (args.length > 0) {
				url = args[0];
			}
			Vector<Waypoint> points = new Vector<Waypoint>();
			Waypoint wp;
			BufferedReader stdin = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.print("Enter waypoint lat lon ele name or calc>");
			String inStr = stdin.readLine();
			StringTokenizer st = new StringTokenizer(inStr);
			String opn = st.nextToken();
			double lat, lon, ele = 0;
			String name;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(2);
			while (!opn.equalsIgnoreCase("calc")) {
				lat = Double.parseDouble(opn);
				lon = Double.parseDouble(st.nextToken());
				ele = Double.parseDouble(st.nextToken());
				name = st.nextToken();
				wp = new Waypoint(lat, lon, ele, name);
				wp.print();
				points.add(wp);
				System.out.print("Enter waypoint lat lon ele name or calc>");
				inStr = stdin.readLine();
				st = new StringTokenizer(inStr);
				opn = st.nextToken();
			}
			Waypoint next, last = null;
			for (Enumeration<Waypoint> e = points.elements(); e
					.hasMoreElements();) {
				next = e.nextElement();
				if (last != null) {
					System.out.println("GC distance from "
							+ last.name
							+ " to "
							+ next.name
							+ " is "
							+ nf.format(last.distanceGCTo(next,
									Waypoint.STATUTE))
							+ " bearing "
							+ nf.format(last.bearingGCInitTo(next,
									Waypoint.STATUTE)));

					System.out.println("Rhumb distance from "
							+ last.name
							+ " to "
							+ next.name
							+ " is "
							+ nf.format(last.distanceRhumbTo(next,
									Waypoint.STATUTE))
							+ " bearing "
							+ nf.format(last.bearingRhumbTo(next,
									Waypoint.STATUTE)));

				}
				last = next;
			}
		} catch (Exception e) {
			System.out.println("Oops, you didn't enter the right stuff");
		}
	}

}
