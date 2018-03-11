package extension.dbnets.simulatorextension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.protocol.Packet;
import org.cpntools.accesscpn.model.Node;
import org.cpntools.accesscpn.model.Place;
import org.cpntools.accesscpn.model.PlaceNode;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.simulator.extensions.Channel;

import extension.dbnets.statespace.StateSpaceUtilityFunctions.Bindings;

/* 
 * 	Creates packets for communicating with CPN Tools. Don't try to change the content, instead extend it 
	and modify according to requirements. 
*/
/**
 * A class used to create packets for communicating with the CPN Tools.
 * @author Aman Sinha
 *
 */
public class PacketCreator{
	
	/**
	 * An instance of the Packet Creator class which can be called from outside and can be used to call other
	public functions.
	 */
	public static PacketCreator instance = new PacketCreator();
	
	/*
	 * Intentionally left blank in order to make it easily extensible.
	 */
	/**
	 * Constructor for PacketCreateor
	 */
	public PacketCreator(){
		
	}
	
	/**
	 * Constructs packets in order to set markings for the place.
	 * @param id A node identifier
	 * @param instanceNumber Instance number of the place
	 * @param initial Boolean to determine if the marking is initial.
	 * @return Returns a packet which can be used to a marking of a place
	 */
	public Packet constructSetMarking(final String id, final int instanceNumber, final boolean initial) {
		final Packet p = new Packet(500);
		p.addInteger(22);
		p.addBoolean(initial);
		p.addInteger(1); // # nodes, hardwired to 1
		p.addInteger(instanceNumber);
		p.addString(id);
		return p;
	}
	
	/*
	 * 
	 */
	/**
	 * Constructs packets in order to check marking for the place if it is compatible to the place.
	  When this packet is sent, the received packet can be checked for errors (if any)
	 * @param type The sort of the place
	 * @param marking Markings of the place
	 * @return A packet which can be used to check if the marking is compatible to the place
	 */
	public Packet constructCheckMarking(final String type, final String marking) {
		final Packet p = new Packet(400);
		p.addInteger(3);
		p.addString(marking);
		p.addString(type);
		return p;
	}
	
	/**
	 * Constructs packet useful for retrieve marking of a place
	 * @param pis Collection of place instances
	 * @return A packet which can be used to retrieve marking of a place
	 */
	public Packet constructGetMarking(
	        final Collection<? extends org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends PlaceNode>> pis) {
		final Packet p = new Packet(500);
		p.addInteger(31);
		addNodes(p, pis);
		return p;
	}

	/**
	 * This function is called internally to add nodes
	 * @param p A packet to be modified after adding nodes
	 * @param nis A collection of node instances
	 */
	private void addNodes(final Packet p,
	        final Collection<? extends org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends Node>> nis) {
		p.addInteger(nis.size());
		for (final org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends Node> ni : nis) {
			p.addInteger(ni.getInstanceNumber());
			p.addString(ni.getNode().getId());
		}
	}
	
	/**
	 * Constructs a packet which can be sent to the GUI to refresh the net.
	 * @param pis Collection of place instances
	 * @param tis Collection of transitions instances
	 * @param marking Marking for the collection of places
	 * @param c Channel through which the packets need to be sent
	 * @return Returns a packet which needs to be sent in order to refresh the GUI
	 */
	public Packet simulatorChange(final Collection<? extends org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends Place>> pis, final Collection<? extends org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends Transition>> tis, ArrayList<String> marking, Channel c){
		if(pis!=null && (pis.size() != marking.size())){
			return null;
		}
		Packet p = new Packet(3, 1);
		//a delay of 100 mili-second
		p.addInteger(100);
		//for number of transitions
		int no_inst = (tis == null)? 0 : tis.size();
		p.addInteger(no_inst);
		no_inst = (pis == null)? 0 : pis.size(); //for number of nodes
		p.addInteger(no_inst);
		p.addString("0"); //for stepstr
		p.addString("0"); //for timestr
		p = checkEnabled(tis,c,p);
		int index = 0;
		for (final Instance<? extends Place> ni : pis) {
			p.addInteger(ni.getInstanceNumber());
			List<TokenMarking> tokens = UtilityFunctions.parseMarking(marking.get(index));
			p.addInteger(TokenMarking.totalMarkings(tokens));
			p.addString(ni.getNode().getId());
			p.addString(marking.get(index++));
		}
		return p;
	}
	
	/**/
	/**
	 * Constructs Packet to check whether the list of the transition instance are enabled
	 * @param tis A collection of transition instances
	 * @param c The channel through which the packets need to be sent
	 * @param p The packet which needs to be modified for checking a transition enabledness
	 * @return Returns a packet after checking the if the transition is enabled
	 */
	private Packet checkEnabled(Collection<? extends Instance<? extends Transition>> tis, Channel c, Packet p) {
		if(tis == null){
			return p;
		}
		for (final Instance<? extends Transition> ti : tis){
			try{
				p.addBoolean(isEnabled(ti,c));
				p.addInteger(ti.getInstanceNumber());
				p.addString(ti.getNode().getId());
			}catch(Exception e){
				System.out.println("Some problem in checking the transition");
			}
		}
		return p;
		
	}
	
	/**
	 * Constructs a packet to check if a transition instance is enabled
	 * @param id Identifier of the transition
	 * @param trans_inst Transition instance
	 * @return A packet which could be used to check if a transition is enabled
	 */
	public Packet constructIsEnabled(final String id, final int trans_inst) {
		final Packet p = new Packet(500);
		p.addInteger(13);
		p.addString(id);
		p.addInteger(trans_inst);
		return p;
	}
	
	public Packet constructIsEnabled(
	        final Collection<? extends org.cpntools.accesscpn.engine.highlevel.instance.Instance<? extends Transition>> tis) {
		final Packet p = new Packet(500);
		p.addInteger(35);
		addNodes(p, tis);
		return p;
	}
	
	/*
	 * This could be moved from the PacketCreator class
	 */
	/**
	 * Function to check whether a particular transition is enabled. 
	 * @param ti Transition instance to be checked for
	 * @param c Channel through which the packets need to be sent
	 * @return Returns true if the transition is enabled, otherwise returns false
	 * @throws IOException
	 */
	public boolean isEnabled(final Instance<? extends Transition> ti, Channel c) throws IOException {
		Packet p = constructIsEnabled(ti.getNode().getId(), ti.getInstanceNumber());
		Packet r = c.send(p);
		return r.getBoolean();
	}
	
	public Packet constructExecute(final String id, final int instance2) {
		final Packet p = new Packet(500);
		p.addInteger(12);
		p.addString(id);
		p.addInteger(instance2);
		return p;
	}
	
	public Packet constructManualBinding(final Bindings bindings, final Binding binding) {
		final List<List<String>> variables = bindings.getVariables();
		final List<List<List<String>>> values = bindings.getValues();
		final Packet p = new Packet(500);
		p.clearI();
		for (int i = 0; i < variables.size(); i++) {
			final List<String> vars = variables.get(i);
			final List<List<String>> vals = values.get(i);
			int j = 0;
			outer: for (final List<String> val : vals) {
				inner: for (int k = 0; k < vars.size(); k++) {
					if (val.get(k).equals(binding.getValueAssignment(vars.get(k)).getValue())) {
						if (k == vars.size() - 1) {
							p.addInteger(j);
							break outer;
						}
					} else {
						break inner;
					}
				}
				j++;
			}
			if (j == vals.size()) { return null; }
		}
		p.addBoolean(true);
		return p;
	}

}
