package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

/**
 * @author Aaron Gember-Jacobson
 */


/*public class switchTable{
	//string hostname;
	Object  macAddress;
	Iface inIface;

	public switchTable(Object macAddress, Iface inIface){
		this.macAddress = macAddress;
		this.inIface = inIface;
	}
}
*/
public class Switch extends Device
{
	//public static timeout = 15;

	//switchTable table = new switchTable()
	Map<Object, Iface> macToPort = new HashMap<Object, Iface>();

   // assuming table size to be no more than 1024
  // private static final int maxTableSize = 1024;

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		Object macAddressSource = etherPacket.getSourceMAC();
		Object macAddressDestination = etherPacket.getDestinationMAC();

		/********************************************************************/
		/* TODO: Handle packets                                             */
		
		/********************************************************************/


		//check for destination MAC address
		if(macToPort.containsKey(macAddressDestination)){
			//forward
			Iface iface = macToPort.get(macAddressDestination);
			System.out.println("Entry found for destination address\n Forwarding packet on "+iface);
			try{
				sendPacket(etherPacket, iface);
			}
			catch (Exception e){
				System.out.println("Error in forwarding");
			}

		}

		//broadcast to all interfces
		else{
			//Map<String,Iface> interfaces = getInterfaces();
			for (Map.Entry<String, Iface> entry : getInterfaces().entrySet()){
				System.out.println("Broadcasting to :\t" + etherPacket.getDestinationMAC()+ " : " + entry.getValue());
				try{
					sendPacket(etherPacket, entry.getValue());
				}
				catch (Exception e){
					System.out.println("Error in broadcasting");
				}
			}
			//macToPort.put(macAddress, inIface);
		}

		//check for source MAC address
		if(!macToPort.containsKey(macAddressSource)){
			macToPort.put(macAddressSource, inIface);
		}
	}

	//TODO timeout

}
