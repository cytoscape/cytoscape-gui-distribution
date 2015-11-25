package org.cytoscape.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests nested groups, all of them expanded.
 */
public class Cy3ExpandedGroupsSessionLoadingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = File.createTempFile("test", ".cys");
		Files.copy(getClass().getResourceAsStream("/testData/session3x/nestedGroups_expanded.cys"), sessionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		checkBasicConfiguration();
		groupSettingsManager.setGroupViewType(GroupViewType.NONE);
	}

	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
		confirm();
	}

	private void confirm() {
		checkGlobalStatus();
		checkNetwork();
		checkGroups();
	}
	
	private void checkGlobalStatus() {
		assertEquals(1, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());
		assertEquals(1, applicationManager.getSelectedNetworks().size());
		assertEquals(1, applicationManager.getSelectedNetworkViews().size());
		assertEquals(getNetworkByName("Network"), applicationManager.getCurrentNetwork());
		assertNotNull(applicationManager.getCurrentNetworkView());
		assertEquals("default", vmm.getDefaultVisualStyle().getTitle());
		assertEquals(2, groupManager.getGroupSet(applicationManager.getCurrentNetwork()).size());
	}
	
	private void checkNetwork() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		checkNodeEdgeCount(net, 4, 3, 0, 0);
	}
	
	private void checkGroups() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		final CyRootNetwork root = ((CySubNetwork) net).getRootNetwork();
		
		// Regular nodes
		final CyNode n1 = getNodeByName(root, "Node 1");
		final CyNode n2 = getNodeByName(root, "Node 2");
		final CyNode n3 = getNodeByName(root, "Node 3");
		final CyNode n4 = getNodeByName(root, "Node 4");
		assertTrue(net.containsNode(n1));
		assertTrue(net.containsNode(n2));
		assertTrue(net.containsNode(n3));
		assertTrue(net.containsNode(n4));
		assertFalse(groupManager.isGroup(n1, net));
		assertFalse(groupManager.isGroup(n2, net));
		assertFalse(groupManager.isGroup(n3, net));
		assertFalse(groupManager.isGroup(n4, net));
		
		// GROUP NODES
		final CyNode gn1 = getNodeByName(root, "G1");
		final CyNode gn2 = getNodeByName(root, "G2");
		assertFalse(net.containsNode(gn1));
		assertFalse(net.containsNode(gn2));
		assertTrue(groupManager.isGroup(gn1, net));
		assertTrue(groupManager.isGroup(gn2, net));
		
		final CyGroup g1 = groupManager.getGroup(gn1, net);
		final CyGroup g2 = groupManager.getGroup(gn2, net);
		assertFalse(g1.isCollapsed(net));
		assertFalse(g2.isCollapsed(net));
		
		// GROUP NETWORKS
		assertEquals(gn1.getNetworkPointer(), g1.getGroupNetwork());
		assertEquals(gn2.getNetworkPointer(), g2.getGroupNetwork());
		// Make sure the group subnetworks have the correct save policy
		assertEquals(SavePolicy.SESSION_FILE, g1.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g2.getGroupNetwork().getSavePolicy());
		
		// Child nodes
		final List<CyNode> childNodes1 = g1.getNodeList();
		final List<CyNode> childNodes2 = g2.getNodeList();
		assertEquals(2, childNodes1.size());
		assertEquals(2, childNodes2.size());
		
		// External/Internal edges
		final Set<CyEdge> externalEdges1 = g1.getExternalEdgeList();
		final List<CyEdge> internalEdges1 = g1.getInternalEdgeList();
		assertEquals(1, externalEdges1.size());
		assertEquals(1, internalEdges1.size());
		
		final Set<CyEdge> externalEdges2 = g2.getExternalEdgeList();
		final List<CyEdge> internalEdges2 = g2.getInternalEdgeList();
		assertEquals(2, externalEdges2.size());
		assertEquals(1, internalEdges2.size());
	}
}
