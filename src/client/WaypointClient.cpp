#include <jsonrpc/rpc.h>
#include <jsonrpc/json/json.h>
#include <vector>
#include <stdlib.h>
#include <string>
#include <sstream>
#include <iostream>
#include "WaypointGUI.cpp"

#include "waypointstubclient.h"

using namespace jsonrpc;
using namespace std;

class WaypointClient: public WaypointGUI, public waypointstubClient
{
	/** ClickedX is one of the callbacks for GUI controls.
	 * Callbacks need to be static functions. But, static functions
	 * cannot directly access instance data. This program uses "userdata"
	 * to get around that by passing the instance to the callback
	 * function. The callback then accesses whatever GUI control object
	 * that it needs for implementing its functionality.
	 */
	static void ClickedX(Fl_Widget * w, void * userdata)
	{
		cout << "You clicked Exit" << endl;
		exit(1);
	}

	static void ClickedRemoveWP(Fl_Widget* w, void* userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;
		Fl_Input_Choice* fromWPChoice = anInstance->frWps;
		Fl_Input_Choice* toWPChoice = anInstance->toWps;

		string selected(fromWPChoice->value());

		if (anInstance->removeWaypoint(selected) == true)
		{
			cout << "Removed: 	Waypoint(" << selected << "); \n" << endl;
		}
		else
		{
			cout << "Error:		Waypoint(" << selected << ") did not exist! \n" << endl;
		}

		buildWaypointList(anInstance);
	}

	static void ClickedAddWP(Fl_Widget * w, void * userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;
		Fl_Input_Choice * fromWPChoice = anInstance->frWps;
		Fl_Input_Choice * toWPChoice = anInstance->toWps;

		Fl_Input * theLat = anInstance->latIn;
		Fl_Input * theLon = anInstance->lonIn;
		Fl_Input * theEle = anInstance->eleIn;
		Fl_Input * theName = anInstance->nameIn;

		string lat(theLat->value());
		string lon(theLon->value());
		string ele(theEle->value());
		string name(theName->value());

		Json::Value wp;
		wp["name"] = name.c_str();
		wp["lat"] = atof(lat.c_str());
		wp["lon"] = atof(lon.c_str());
		wp["ele"] = atof(ele.c_str());


		if (anInstance->addWaypoint(wp) == true)
		{
			cout << "Added: 		Waypoint(" << name << "); \n" << endl;
		}
		else
		{
			cout << "Error:			Waypoint(" << name << ") could not be added! \n" << endl;
		}

		buildWaypointList(anInstance);
	}

	static void ClickedModifyWP(Fl_Widget * w, void * userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;

		Fl_Input * theLat = anInstance->latIn;
		Fl_Input * theLon = anInstance->lonIn;
		Fl_Input * theEle = anInstance->eleIn;
		Fl_Input * theName = anInstance->nameIn;

		string lat(theLat->value());
		string lon(theLon->value());
		string ele(theEle->value());
		string name(theName->value());

		Json::Value wp;
		wp["name"] = name.c_str();
		wp["lat"] = atof(lat.c_str());
		wp["lon"] = atof(lon.c_str());
		wp["ele"] = atof(ele.c_str());

		anInstance->removeWaypoint(name);
		anInstance->addWaypoint(wp);

		cout << "Modified:	" << name << "(" << lat << ", " << lon << ", " << ele << ", " << name << ");" << endl;
		buildWaypointList(anInstance);
	}

	static void SelectedFromWP(Fl_Widget * w, void * userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;
		Fl_Input_Choice* frWps = anInstance->frWps;

		string selected(frWps->value());
		cout << "Selected:	" << selected << ";" << endl;

		try
		{
			Json::Value result = anInstance->getWaypoint(selected);

			if (result.get("result","NOT ERROR") == "ERROR")
			{
				cout << "Unsuccessful grab" << endl;
				anInstance->nameIn->value("ERROR");
				anInstance->latIn->value("0");
				anInstance->lonIn->value("0");
				anInstance->eleIn->value("0");
			}
			else
			{
				string lat = to_string(result["lat"].asDouble());
				string lon = to_string(result["lon"].asDouble());
				string ele = to_string(result["ele"].asDouble());
				anInstance->nameIn->value(result["name"].asCString());
				anInstance->latIn->value(lat.c_str());
				anInstance->lonIn->value(lon.c_str());
				anInstance->eleIn->value(ele.c_str());
				cout << "Successful grab" << endl;
			}

		}
		catch (exception &e)
		{
			cout << e.what() << endl;

		}
	}

	static void SelectedToWP(Fl_Widget * w, void * userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;
		Fl_Input_Choice* toWps = anInstance->toWps;

		string selected(toWps->value());
		cout << "Selected:	" << selected << ";" << endl;

		try
		{
			Json::Value result = anInstance->getWaypoint(selected);

			if (result.get("result","NOT ERROR") == "ERROR")
			{
				cout << "Unsuccessful grab" << endl;
				anInstance->nameIn->value("ERROR");
				anInstance->latIn->value("0");
				anInstance->lonIn->value("0");
				anInstance->eleIn->value("0");
			}
			else
			{
				string lat = to_string(result["lat"].asDouble());
				string lon = to_string(result["lon"].asDouble());
				string ele = to_string(result["ele"].asDouble());
				anInstance->nameIn->value(result["name"].asCString());
				anInstance->latIn->value(lat.c_str());
				anInstance->lonIn->value(lon.c_str());
				anInstance->eleIn->value(ele.c_str());
				cout << "Successful grab" << endl;
			}

		}
		catch (exception &e)
		{
			cout << e.what() << endl;

		}
	}

	static void buildWaypointList(WaypointClient* userdata)
	{
		WaypointClient* anInstance = (WaypointClient*) userdata;
		Fl_Input_Choice * fromWPChoice = anInstance->frWps;
		Fl_Input_Choice * toWPChoice = anInstance->toWps;

		fromWPChoice->clear();
		toWPChoice->clear();
		Json::Value waypointsArray = anInstance->getWaypoints().get("waypoints",0);
		if (waypointsArray.size() == 0)
		{
			fromWPChoice->add("EMPTY");
			toWPChoice->add("EMPTY");
		}
		else
		{
			for (Json::ValueIterator itr = waypointsArray.begin(); itr != waypointsArray.end(); itr++)
			{
				Json::Value waypoint = *itr;
				string name = waypoint["name"].asString();
				fromWPChoice->add(name.c_str());
				toWPChoice->add(name.c_str());
			}

		}
		fromWPChoice->value(0);
		toWPChoice->value(0);
		SelectedFromWP(NULL, anInstance);
	}

public:
	WaypointClient(const char * name = 0, const char * host = 0) :
			WaypointGUI(name), waypointstubClient(new HttpClient(host))
	{
		removeWPButt->callback(ClickedRemoveWP, (void*) this);
		addWPButt->callback(ClickedAddWP, (void*) this);
		frWps->callback(SelectedFromWP, (void*) this);
		toWps->callback(SelectedToWP, (void*) this);
		modWPButt->callback(ClickedModifyWP, (void*) this);
		//importJSONButt->callback(ClickedImportJSON, (void*) this);
		callback(ClickedX);
		buildWaypointList(this);
	}
};

int main(int argc, char*argv[])
{
	string host = "http://127.0.0.1:8080";

	 if(argc > 1)
	 {
		 host = "http://" + string(argv[1])+ ":" + string(argv[2]);
		 cout << host << endl;
	 }

	WaypointClient cm("Jacob Dobkins' Waypoint Browser", host.c_str());

	return (Fl::run());
}

