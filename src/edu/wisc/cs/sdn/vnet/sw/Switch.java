package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;

/**
 * @author Aaron Gember-Jacobson
 */

public class Switch extends Device
{

	ConcurrentHashMap<MACAddress, SwitchTableEntry> switchTable = new ConcurrentHashMap<MACAddress, SwitchTableEntry>();
    private final int TIMEOUT_SECONDS = 15;

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
		//check for destination MAC address in Table
		if(switchTable.containsKey(macAddressDestination)){
			//forward
			SwitchTableEntry switchTableEntry =  switchTable.get(macAddressDestination);
			System.out.println("Entry found for destination address\n Forwarding packet on "+ switchTableEntry.getInIface());
			sendPacket(etherPacket, switchTableEntry.getInIface());
		}

		//broadcast to all interfaces
		else{
			for(Map.Entry<String, Iface> mapEntry : this.getInterfaces().entrySet()) {
				if(mapEntry.getValue() == inIface)
					continue;
				System.out.println("Broadcasting to :\t" + etherPacket.getDestinationMAC()+ " : " + mapEntry.getValue());
				sendPacket(etherPacket, mapEntry.getValue());
			}
		}

		//Add MacAddress to table
		if(!switchTable.containsKey(macAddressSource)){
			System.out.println("Adding " + macAddressSource.toString() + " to Switch Table.");
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
							   @Override
							   public void run() {
								   switchTable.remove(macAddressSource);
							   }
						   }, TIMEOUT_SECONDS * 1000);
			switchTable.put(macAddressSource, new SwitchTableEntry(inIface,  timer));
		}
		else {
			SwitchTableEntry switchTableEntry =  switchTable.get(macAddressSource);
			Timer timer = switchTableEntry.getTimer();
			timer.cancel();
			timer.purge();
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					switchTable.remove(macAddressSource);
				}
			}, TIMEOUT_SECONDS * 1000);
			switchTableEntry.setTimer(timer);
		}

		/********************************************************************/

	}

	class SwitchTableEntry{
		private Iface inIface;
		private Timer timer;

		public SwitchTableEntry(Iface inIface, Timer timer){
			this.inIface = inIface;
			this.timer = timer;
		}

		public Iface getInIface() {
			return inIface;
		}

		public Timer getTimer() {
			return timer;
		}

		public void setTimer(Timer timer) {
			this.timer = timer;
		}
	}
}
