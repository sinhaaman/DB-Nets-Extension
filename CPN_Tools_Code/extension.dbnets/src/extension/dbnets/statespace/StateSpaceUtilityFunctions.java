package extension.dbnets.statespace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.cpntools.accesscpn.engine.Simulator;
import org.cpntools.accesscpn.engine.highlevel.HighLevelSimulator;
import org.cpntools.accesscpn.engine.highlevel.PacketGenerator;
import org.cpntools.accesscpn.engine.highlevel.instance.Binding;
import org.cpntools.accesscpn.engine.highlevel.instance.Instance;
import org.cpntools.accesscpn.engine.highlevel.instance.InstanceFactory;
import org.cpntools.accesscpn.engine.highlevel.instance.Marking;
import org.cpntools.accesscpn.engine.highlevel.instance.State;
import org.cpntools.accesscpn.engine.highlevel.instance.ValueAssignment;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelData;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelDataAdapterFactory;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstance;
import org.cpntools.accesscpn.engine.highlevel.instance.adapter.ModelInstanceAdapterFactory;
import org.cpntools.accesscpn.engine.protocol.Packet;
import org.cpntools.accesscpn.model.PetriNet;
import org.cpntools.accesscpn.model.PlaceNode;
import org.cpntools.accesscpn.model.Transition;
import org.cpntools.simulator.extensions.Channel;

import extension.dbnets.simulatorextension.PacketCreator;

public class StateSpaceUtilityFunctions {
	private Channel channel;
	private PetriNet petriNet;
	private ModelData modelData;
	private ModelInstance modelInstance;
	private HighLevelSimulator simulator;
	
	public StateSpaceUtilityFunctions(){
		
	}
	
	public StateSpaceUtilityFunctions(Channel c, PetriNet pNet, HighLevelSimulator highLevelSimulator){
		setChannel(c);
		setPetriNet(pNet);
		this.simulator = highLevelSimulator;
	}
	
	public static class Bindings {
		private final List<List<String>> variables;
		private final List<List<List<String>>> values;
		private final Instance<? extends Transition> ti;
		private static final Random r = new Random();

		Bindings(final Instance<? extends Transition> ti, final List<List<String>> variables,
		        final List<List<List<String>>> values) {
			this.ti = ti;
			this.variables = variables;
			this.values = values;
			assert variables.size() == values.size();
			for (int i = 0; i < variables.size(); i++) {
				for (int j = 0; j < values.get(i).size(); j++) {
					assert variables.get(i).size() == values.get(i).get(j).size();
				}
			}
		}

		public List<Binding> getAllBindings() {
			final List<String> vars = new ArrayList<String>();
			List<List<String>> vals = new ArrayList<List<String>>();
			vals.add(new ArrayList<String>());
			for (int i = 0; i < variables.size(); i++) {
				vars.addAll(variables.get(i));
				if (values.get(i).size() == 1) {
					for (final List<String> val : vals) {
						val.addAll(values.get(i).get(0));
					}
				} else {
					final List<List<String>> oldVals = vals;
					vals = new ArrayList<List<String>>();
					for (final List<String> value : values.get(i)) {
						for (final List<String> val : oldVals) {
							final List<String> newVal = new ArrayList<String>(val);
							newVal.addAll(value);
							vals.add(newVal);
						}
					}
				}
			}
			final List<Binding> result = new ArrayList<Binding>();
			for (final List<String> val : vals) {
				assert val.size() == vars.size();
				final List<ValueAssignment> valueAssignments = new ArrayList<ValueAssignment>();
				for (int i = 0; i < val.size(); i++) {
					valueAssignments.add(InstanceFactory.INSTANCE.createValueAssignment(vars.get(i), val.get(i)));
				}
				result.add(InstanceFactory.INSTANCE.createBinding(ti, valueAssignments));
			}
			return result;
		}

		public Binding getAnyBinding() {
			final List<ValueAssignment> valueAssignments = new ArrayList<ValueAssignment>();
			for (int i = 0; i < variables.size(); i++) {
				final List<String> value = values.get(i).get(r.nextInt(values.get(i).size()));
				for (int j = 0; j < value.size(); j++) {
					valueAssignments.add(InstanceFactory.INSTANCE.createValueAssignment(variables.get(i).get(j),
					        value.get(j)));
				}
			}
			return InstanceFactory.INSTANCE.createBinding(ti, valueAssignments);
		}

		public List<List<List<String>>> getValues() {
			return values;
		}

		public List<List<String>> getVariables() {
			return variables;
		}
	}
	
	public Packet send(final Packet p) throws IOException{
		return getChannel().send(p);
	}
	
	public Binding executeAndGet(final Instance<Transition> ti) throws IOException {
		@SuppressWarnings("hiding")
		final List<Binding> bindings = getBindings(ti);
		Collections.shuffle(bindings);
		try {
			for (final Binding binding : bindings) {
				if (execute(binding)) { return binding; }
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Binding> getBindings(final Instance<? extends Transition> ti) {
		Packet result;
		try {
			result = send(PacketGenerator.instance.constructGetBindings(ti.getNode().getId(), ti.getInstanceNumber()));
			send(PacketGenerator.instance.constructCancelManualBinding());
		} catch (final Exception e) {
			return Collections.emptyList();
		}
		@SuppressWarnings("hiding")
		final Bindings bindings = getBindings(ti, result);
		return bindings.getAllBindings();
	}
	
	public Binding executeAndGet1(final Instance<Transition> ti) throws Exception {
		@SuppressWarnings("hiding")
		final Bindings bindings = getBindings(
				ti,
				send(PacketGenerator.instance.constructGetBindings(ti.getNode().getId(),
						ti.getInstanceNumber())));
		final Binding binding = bindings.getAnyBinding();
		final Packet packet = PacketCreator.instance.constructManualBinding(bindings, binding);
		assert packet != null;
		send(packet);
		return binding;
	}
	
	private Bindings getBindings(final Instance<? extends Transition> ti, final Packet result) {
		final List<List<String>> variables = new ArrayList<List<String>>();
		final List<List<List<String>>> values = new ArrayList<List<List<String>>>();
		result.getInteger(); // RESPTAG = 2
		result.getInteger(); // RESPKIND = 60
		final int groups = result.getInteger();
		for (int i = 0; i < groups; i++) {
			final int varCount = result.getInteger();
			final int bindingsCount = result.getInteger();
			final List<String> vars = new ArrayList<String>();
			final List<List<String>> vals = new ArrayList<List<String>>();
			for (int j = 0; j < varCount; j++) {
				vars.add(result.getString());
			}
			for (int j = 0; j < bindingsCount; j++) {
				final List<String> value = new ArrayList<String>();
				for (int k = 0; k < varCount; k++) {
					value.add(result.getString());
				}
				vals.add(value);
			}
			variables.add(vars);
			values.add(vals);
		}
		return new Bindings(ti, variables, values);
	}
	
	public boolean execute(final Binding binding) throws IOException {
		final Instance<Transition> ti = binding.getTransitionInstance();
		if (isEnabled(ti)) {
			@SuppressWarnings("hiding")
			final Bindings bindings = getBindings(ti,
			        send(PacketGenerator.instance.constructGetBindings(ti.getNode().getId(), ti.getInstanceNumber())));
			final Packet packet = PacketCreator.instance.constructManualBinding(bindings, binding);
			if (packet != null) {
				send(packet);
				return true;
			} else {
				send(PacketGenerator.instance.constructCancelManualBinding());
			}
		}
		return false;
	}
	
	/**
	 * Set marking of place instance if it is legal. There is no need to call checkMArking (as the manual says) as this
	 * takes care of that.
	 * 
	 * @param pi
	 *            place instance
	 * @param marking
	 *            marking
	 * @throws IOException
	 *             on error
	 */
	public boolean setMarking(final Instance<? extends PlaceNode> pi, final String marking) throws IOException {
		try {
			if (checkMarking(pi.getNode(), marking)) {
				Packet to_send = PacketCreator.instance.constructSetMarking(pi.getNode().getId(), pi.getInstanceNumber(), false);
				//to_send.setOpcode(13);
				send(to_send);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * @param state
	 * @throws IOException
	 */
	public void setMarking(final State state) throws IOException {
		try {
			for (final Marking m : state.getAllMarkings()) {
				setMarking(m.getPlaceInstance(), m);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @param pi
	 * @param marking
	 * @return
	 * @throws IOException
	 */
	public boolean setMarking(final Instance<? extends PlaceNode> pi, final Marking marking) throws IOException {
		return setMarking(pi, marking.getMarking());
	}
	
	/**
	 * @param p
	 *            place
	 * @param marking
	 *            marking
	 * @return whether marking is legal for place
	 * @throws IOException
	 *             on error
	 */
	public boolean checkMarking(final PlaceNode p, final String marking) throws IOException {
		Packet to_send = PacketCreator.instance.constructCheckMarking(p.getSort().getText(), marking);
		//to_send.setOpcode(13);
		return simulator.send(to_send).getBoolean();
	}

	/**
	 * @return the channel
	 */
	public Channel getChannel() {
		return channel;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public boolean isEnabled(final Instance<? extends Transition> ti) throws IOException {
		return send(PacketCreator.instance.constructIsEnabled(ti.getNode().getId(), ti.getInstanceNumber()))
		        .getBoolean();
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public State getMarking() throws Exception {
		return getMarking(getAllPlaceInstances());
	}
	
	/**
	 * @param pis
	 * @return
	 * @throws Exception
	 */
	public State getMarking(final Collection<? extends Instance<? extends PlaceNode>> pis) throws Exception {
		final Packet p = send(PacketCreator.instance.constructGetMarking(pis));
		if (p.getInteger() == 1) {
			final int noMarkings = p.getInteger();
			assert noMarkings == pis.size();
			final List<Marking> markings = new ArrayList<Marking>();
			for (final Instance<? extends PlaceNode> pi : pis) {
				final int tokens = p.getInteger();
				final String marking = p.getString();
				if (tokens < 0) {
					System.err.println("Place "+pi+" has "+tokens+", "+marking);
					throw new Exception(marking);
				}
				final Marking m = InstanceFactory.INSTANCE.createMarking(tokens, marking);
				m.setPlaceInstance(pi);
				markings.add(m);
			}
			return InstanceFactory.INSTANCE.createState(markings);
		}
		return null;
	}
	
	/**
	 * @return all place instances of the PetriNet of this simulator
	 * @throws Exception
	 *             if no Petri net is associated with this simulator
	 */
	public List<Instance<PlaceNode>> getAllPlaceInstances() throws Exception {
		if (getModelData() != null) {
			final List<Instance<PlaceNode>> allPlaceInstances = getModelData().getAllPlaceNodeInstances();
			if (allPlaceInstances != null) { return allPlaceInstances; }
		}
		throw new Exception("You did not give a PetriNet on creation, so you cannot execute arbitrary bindings.");

	}
	
	/**
	 * @param petriNet
	 */
	public void setPetriNet(final PetriNet petriNet) {
		this.petriNet = petriNet;
		if (petriNet != null) {
			setModelData((ModelData) ModelDataAdapterFactory.getInstance().adapt(petriNet, ModelData.class));
			setModelInstance((ModelInstance) ModelInstanceAdapterFactory.getInstance().adapt(petriNet,
			        ModelInstance.class));
		} else {
			setModelData(null);
			setModelInstance(null);
		}
	}
	
	/**
	 * @return all transition isntances of the PetriNet of this simulator
	 * @throws Exception
	 *             if no Petrinet is associated with this simulator
	 */
	public List<Instance<Transition>> getAllTransitionInstances() throws Exception {
		if (modelData != null) {
			final List<Instance<Transition>> allTransitionInstances = modelData.getAllTransitionInstances();
			if (allTransitionInstances != null) { return allTransitionInstances; }
		}
		throw new Exception("You did not give a PetriNet on creation, so you cannot execute arbitrary bindings.");

	}
	
	/**
	 * @param tis
	 *            list of transition instances to check enabledness for
	 * @return list of enabled transition instances from the list
	 * @throws IOException
	 *             on error
	 */
	public List<Instance<? extends Transition>> isEnabled(final Collection<? extends Instance<? extends Transition>> tis)
	        throws IOException {
		final Packet p = send(PacketCreator.instance.constructIsEnabled(tis));
		final List<Instance<? extends Transition>> result = new ArrayList<Instance<? extends Transition>>();
		for (final Instance<? extends Transition> ti : tis) {
			if (p.getBoolean()) {
				result.add(ti);
			}
		}
		return result;
	}

	/**
	 * @return the modelData
	 */
	public ModelData getModelData() {
		return modelData;
	}

	/**
	 * @param modelData the modelData to set
	 */
	public void setModelData(ModelData modelData) {
		this.modelData = modelData;
	}

	/**
	 * @return the modelInstance
	 */
	public ModelInstance getModelInstance() {
		return modelInstance;
	}

	/**
	 * @param modelInstance the modelInstance to set
	 */
	public void setModelInstance(ModelInstance modelInstance) {
		this.modelInstance = modelInstance;
	}

}
