package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Aaron Gember-Jacobson
 */

public class Switch extends Device
{

    ConcurrentHashMap<MACAddress, SwitchTableEntry> switchTable = new ConcurrentHashMap<MACAddress, SwitchTableEntry>();

	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host, logfile);
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
        MACAddress macAddressSource = etherPacket.getSourceMAC();
        MACAddress macAddressDestination = etherPacket.getDestinationMAC();

		/********************************************************************/

		//Check for timeout
		for(ConcurrentHashMap.Entry<MACAddress, SwitchTableEntry> mapEntry : switchTable.entrySet()) {
			if(((System.nanoTime() - mapEntry.getValue().getCreateTime()) / Math.pow(10, 9)) > 15){
				switchTable.remove(mapEntry.getKey());
			}
		}

		//check for destination MAC address
		if(switchTable.containsKey(macAddressDestination)){
			//forward
			SwitchTableEntry switchTableEntry =  switchTable.get(macAddressDestination);
			System.out.println("Entry found for destination address\n Forwarding packet on "+ switchTableEntry.getInIface());
			sendPacket(etherPacket, switchTableEntry.getInIface());
		}

		//broadcast to all interfces
		else{
			for(ConcurrentHashMap.Entry<MACAddress, SwitchTableEntry> mapEntry : switchTable.entrySet()) {
				if(mapEntry.getValue().getInIface() == inIface)
					continue;
				System.out.println("Broadcasting to :\t" + etherPacket.getDestinationMAC()+ " : " + mapEntry.getValue().getInIface());
				sendPacket(etherPacket, mapEntry.getValue().getInIface());
			}
		}

		//Add MacAddress to table
		if(!switchTable.containsKey(macAddressSource)){
			System.out.println("Adding " + macAddressSource.toString() + " to Switch Table.");
			switchTable.put(macAddressSource, new SwitchTableEntry(macAddressSource, inIface,  System.nanoTime()));
		}
		else {
			SwitchTableEntry switchTableEntry =  switchTable.get(macAddressSource);
			switchTableEntry.setCreateTime(System.nanoTime());
		}

		/********************************************************************/

	}

	class SwitchTableEntry{
		private MACAddress  macAddress;
		private Iface inIface;
		private long createTime;

		public SwitchTableEntry(MACAddress macAddress, Iface inIface, long createTime){
			this.macAddress = macAddress;
			this.inIface = inIface;
			this.createTime = createTime;
		}

		public MACAddress getMacAddress() {
			return macAddress;
		}

		public Iface getInIface() {
			return inIface;
		}

		public long getCreateTime() {
			return createTime;
		}

		public void setCreateTime(long createTime) {
			this.createTime = createTime;
		}
	}
}
