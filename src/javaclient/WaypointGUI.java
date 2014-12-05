package javaclient;

import javax.swing.*;

import org.json.*;

@SuppressWarnings("serial")
public class WaypointGUI extends JFrame
{
	protected JSONArray waypointsArray;

	protected JComboBox<String> fromWaypointBox;
	protected JComboBox<String> toWaypointBox;

	protected JTextField latIn;
	protected JTextField lonIn;
	protected JTextField eleIn;
	protected JTextField nameIn;

	protected JTextField distBearIn;
	protected JTextArea addrIn;
	
	protected JButton removeWPButt;
	protected JButton addWPButt;
	protected JButton modWPButt;
	
	protected JButton getAddrButt;
	protected JButton distBearButt;

	private JLabel latLab, lonLab, eleLab, nameLab, addrLab, fromLab, toLab;

	public WaypointGUI(String title) 
	{
		super(title);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		getContentPane().setLayout(null);
		setSize(500, 350);

		fromWaypointBox = new JComboBox<String>(); // From Waypoints List
		fromWaypointBox.setBounds(40, 10, 160, 25);
		getContentPane().add(fromWaypointBox);
		fromWaypointBox.addItem("From here");
		fromWaypointBox.setSelectedIndex(0);
		fromLab = new JLabel("From:");
		fromLab.setBounds(10, 10, 30, 25);
		getContentPane().add(fromLab);

		toWaypointBox = new JComboBox<String>();
		toWaypointBox.setBounds(40, 45, 160, 25);
		getContentPane().add(toWaypointBox);
		toWaypointBox.addItem("To here");
		toWaypointBox.setSelectedIndex(0);
		toLab = new JLabel("To:");
		toLab.setBounds(10, 45, 30, 25);
		getContentPane().add(toLab);

		removeWPButt = new JButton("Remove Waypoint");
		removeWPButt.setBounds(20, 80, 180, 25);
		removeWPButt.setActionCommand("Remove");
		getContentPane().add(removeWPButt);

		addWPButt = new JButton("Add Waypoint");
		addWPButt.setBounds(20, 115, 180, 25);
		addWPButt.setActionCommand("Add");
		getContentPane().add(addWPButt);

		modWPButt = new JButton("Modify Waypoint");
		modWPButt.setBounds(20, 150, 180, 25);
		modWPButt.setActionCommand("Modify");
		getContentPane().add(modWPButt);

		getAddrButt = new JButton("Get Addr for lat/lon");
		getAddrButt.setBounds(20, 185, 180, 25);
		getAddrButt.setActionCommand("GetAddr");
		getContentPane().add(getAddrButt);

		distBearButt = new JButton("Distance and Bearing");
		distBearButt.setBounds(15, 260, 190, 25);
		distBearButt.setActionCommand("Distance");
		getContentPane().add(distBearButt);
		

		latIn = new JTextField("lat");
		latIn.setBounds(250, 10, 230, 25);
		getContentPane().add(latIn);
		latLab = new JLabel("lat");
		latLab.setBounds(225, 10, 25, 25);
		getContentPane().add(latLab);

		lonIn = new JTextField("lon");
		lonIn.setBounds(250, 45, 230, 25);
		getContentPane().add(lonIn);
		lonLab = new JLabel("lon");
		lonLab.setBounds(225, 45, 25, 25);
		getContentPane().add(lonLab);

		eleIn = new JTextField("ele");
		eleIn.setBounds(250, 80, 230, 25);
		getContentPane().add(eleIn);
		eleLab = new JLabel("ele");
		eleLab.setBounds(225, 80, 25, 25);
		getContentPane().add(eleLab);

		nameIn = new JTextField("name");
		nameIn.setBounds(250, 115, 230, 25);
		getContentPane().add(nameIn);
		nameLab = new JLabel("name");
		nameLab.setBounds(210, 115, 35, 25);
		getContentPane().add(nameLab);

		addrIn = new JTextArea("addr");
		addrIn.setBounds(250, 150, 230, 70);
		getContentPane().add(addrIn);
		addrLab = new JLabel("addr");
		addrLab.setBounds(210, 150, 35, 25);
		getContentPane().add(addrLab);

		distBearIn = new JTextField("dist/bearing");
		distBearIn.setBounds(225, 260, 255, 25);
		getContentPane().add(distBearIn);
		
		setVisible(true);
	}
}
