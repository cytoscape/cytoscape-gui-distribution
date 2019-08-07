package org.cytoscape.session;

/*
 * #%L
 * Cytoscape Session Impl Integration Test (session-impl-integration-test)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupSettingsManager.GroupViewType;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class Cy3GroupsSessionLoadingTest extends BasicIntegrationTest {

	@Before
	public void setup() throws Exception {
		sessionFile = File.createTempFile("test", ".cys");
		Files.copy(getClass().getResourceAsStream("/testData/session3x/groups.cys"), sessionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		checkBasicConfiguration();
		groupSettingsManager.setGroupViewType(GroupViewType.NONE);
	}

	@Ignore
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
		assertEquals(2, vmm.getAllVisualStyles().size());
		assertEquals(3, groupManager.getGroupSet(applicationManager.getCurrentNetwork()).size());
	}
	
	private void checkNetwork() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		checkNodeEdgeCount(applicationManager.getCurrentNetwork(), 6, 4, 1, 0);
		assertEquals("Directed", vmm.getVisualStyle(viewManager.getNetworkViews(net).iterator().next()).getTitle());
	}
	
	private void checkGroups() {
		final CyNetwork net = applicationManager.getCurrentNetwork();
		final CyRootNetwork root = ((CySubNetwork) net).getRootNetwork();
		
		// GROUP NODES
		final CyNode gn1 = getNodeByName(net, "Group 1");
		final CyNode gn2 = getNodeByName(root, "Group 2");
		final CyNode gn3 = getNodeByName(root, "Group 3");
		assertTrue(groupManager.isGroup(gn1, net));
		assertTrue(groupManager.isGroup(gn2, net));
		assertTrue(groupManager.isGroup(gn3, net));
		
		final CyGroup g1 = groupManager.getGroup(gn1, net);
		final CyGroup g2 = groupManager.getGroup(gn2, net);
		final CyGroup g3 = groupManager.getGroup(gn3, net);
		assertTrue(g1.isCollapsed(net));
		assertFalse(g2.isCollapsed(net));
		assertFalse(g3.isCollapsed(net));
		
		// GROUP NETWORKS
		assertEquals(gn1.getNetworkPointer(), g1.getGroupNetwork());
		assertEquals(gn2.getNetworkPointer(), g2.getGroupNetwork());
		assertEquals(gn3.getNetworkPointer(), g3.getGroupNetwork());
		// Make sure the group subnetworks have the correct save policy
		assertEquals(SavePolicy.SESSION_FILE, g1.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g2.getGroupNetwork().getSavePolicy());
		assertEquals(SavePolicy.SESSION_FILE, g3.getGroupNetwork().getSavePolicy());
		
		// TODO meta-edges and external edges
		// TODO check group network attributes
		// TODO check meta-edge attributes
	}
}
