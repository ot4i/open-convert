/**
 * 
 */
package com.ibm.etools.mft.conversion.esb.extensionpoint;

import java.util.Collection;
import java.util.HashMap;

import com.ibm.broker.config.appdev.Node;

/**
 * The Nodes contains a list of IIB nodes that are implementing a single
 * Export/Import binding or Mediation Primitive. Each IIB node should be
 * assigned a unique "role". The "role" string could be free form and it is the
 * key used to retrieve an IIB node from Nodes.
 * 
 * @author Zhongming Chen
 * 
 */
public class Nodes {

	private HashMap<String, Node> nodes = new HashMap<String, Node>();

	private HashMap<String, Object> properties = new HashMap<String, Object>();

	public void addNode(String role, Node node) {
		nodes.put(role, node);
	}

	/**
	 * Retrieve an IIB node by a specified role.
	 * 
	 * @param role
	 * @return
	 */
	public Node getNode(String role) {
		return nodes.get(role);
	}

	public Collection<Node> getAllNodes() {
		return nodes.values();
	}

	public HashMap<String, Object> getProperties() {
		return properties;
	}

}
