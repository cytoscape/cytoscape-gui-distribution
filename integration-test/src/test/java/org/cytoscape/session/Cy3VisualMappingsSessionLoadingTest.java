package org.cytoscape.session;

/*
 * #%L
 * Cytoscape Session Impl Integration Test (session-impl-integration-test)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import static org.cytoscape.model.CyNetwork.DEFAULT_ATTRS;
import static org.cytoscape.model.CyNetwork.HIDDEN_ATTRS;
import static org.cytoscape.model.CyNetwork.LOCAL_ATTRS;
import static org.cytoscape.model.CyNetwork.NAME;
import static org.cytoscape.model.CyNetwork.SELECTED;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_ATTRS;
import static org.cytoscape.model.subnetwork.CyRootNetwork.SHARED_DEFAULT_ATTRS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cytoscape.equations.Equation;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.TaskIterator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class Cy3VisualMappingsSessionLoadingTest extends BasicIntegrationTest {

	private static final int NODE_COUNT = 4;
	private static final int EDGE_COUNT = 6;

	@Before
	public void setup() throws Exception {
		sessionFile = File.createTempFile("test", ".cys");
		Files.copy(getClass().getResourceAsStream("/testData/session3x/visualMappings.cys"), sessionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		checkBasicConfiguration();
	}

	@Ignore
	@Test
	public void testLoadSession() throws Exception {
		final TaskIterator ti = openSessionTF.createTaskIterator(sessionFile);
		tm.execute(ti);
		confirm();
	}

	private void confirm() {
		// test overall status of current session.
		checkGlobalStatus();
		
		Set<CyNetwork> networks = networkManager.getNetworkSet();
		final Iterator<CyNetwork> itr = networks.iterator();
		CyNetwork net = itr.next();
		
		checkNetwork(net);
		checkRootNetwork(((CySubNetwork) net).getRootNetwork());
	}
	
	private void checkGlobalStatus() {
		assertEquals(1, networkManager.getNetworkSet().size());
		assertEquals(1, viewManager.getNetworkViewSet().size());
		// Since this test runs in headless mode, this should be zero.
		assertEquals(0, renderingEngineManager.getAllRenderingEngines().size());
		// 3 public tables per subnetwork
		assertEquals(3, tableManager.getAllTables(false).size());
		
		// At least root+base-network; there can be other (private) networks
		final int totalNet = networkTableManager.getNetworkSet().size();
		assertTrue(totalNet >= 2);
		
		for (CyNetwork net : networkTableManager.getNetworkSet())
			checkNetworkTables(net);
	}
	
	private void checkNetwork(final CyNetwork net) {
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		
		assertEquals(NODE_COUNT, net.getNodeCount());
		assertEquals(EDGE_COUNT, net.getEdgeCount());
		
		// Network attributes
		assertEquals("NET 1", net.getDefaultNetworkTable().getRow(net.getSUID()).get(NAME, String.class));
		assertEquals("NET 1", net.getTable(CyNetwork.class, LOCAL_ATTRS).getRow(net.getSUID()).get(NAME, String.class));
		
		// Node attributes
		CyNode node = getNodeByName(net, "Node 2");
		Object raw = net.getDefaultNodeTable().getRow(node.getSUID()).getRaw("score");
		assertTrue(raw instanceof Equation);
		assertNull(net.getDefaultNodeTable().getRow(node.getSUID()).get("score", Integer.class)); // This equation is invalid and should return null
		assertNull(net.getDefaultNodeTable().getRow(node.getSUID()).get("width", Double.class));
		
		// Selection state
		Collection<CyRow> selectedNodes = net.getDefaultNodeTable().getMatchingRows(SELECTED, true);
		Collection<CyRow> selectedEdges = net.getDefaultEdgeTable().getMatchingRows(SELECTED, true);
		assertEquals(0, selectedNodes.size());
		assertEquals(0, selectedEdges.size());
		
		// View test
		Collection<CyNetworkView> views = viewManager.getNetworkViews(net);
		assertEquals(1, views.size());
		
		final CyNetworkView view = views.iterator().next();
		assertEquals(NODE_COUNT, view.getNodeViews().size());
		assertEquals(EDGE_COUNT, view.getEdgeViews().size());
		
		// Visual Style
		assertEquals(16, vmm.getAllVisualStyles().size());
		final VisualStyle style = vmm.getVisualStyle(view);
		checkVisualStyle(style);
		
		checkView(view);
		
		// Check default tables
		assertTrue(net.getTable(CyNetwork.class, DEFAULT_ATTRS).isPublic());
		assertTrue(net.getTable(CyNode.class, DEFAULT_ATTRS).isPublic());
		assertTrue(net.getTable(CyEdge.class, DEFAULT_ATTRS).isPublic());
	}
	
	private void checkView(final CyNetworkView view) {
		checkNetworkVisualProperties(view, "NET 1", Color.WHITE, 872d, 404d, -41.00305366516113d, -21.237794876098633d, 2.4071676095245285d);

		// Rendered visual property values:
		// Note: The main purpose here is making sure that all Node mappings were applied correctly,
		// specially because some mapped columns have null values and invalid equations (see bug #2501).
		{
			View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "Node 1"));
			assertEquals(NodeShapeVisualProperty.ELLIPSE, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
			assertEquals(Double.valueOf(1.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
			assertEquals(new Color(0x006699), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
			assertEquals(Double.valueOf(30.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
			assertEquals(Double.valueOf(18.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
			assertEquals(new Color(0xff0000), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR));
			assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
			assertEquals(new Color(0xffffff), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
			assertEquals(Integer.valueOf(12), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		}
		{
			// This node has some mapped columns with null values!
			View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "Node 2"));
			assertEquals(NodeShapeVisualProperty.ROUND_RECTANGLE, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE)); // Should have default value!
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
			assertEquals(Double.valueOf(0.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH)); // Should have default value!
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
			assertEquals(new Color(0x006699), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
			assertEquals(Double.valueOf(40.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH)); // Should have default value!
			assertEquals(Double.valueOf(40.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT)); // Should have default value!
			assertEquals(new Color(0x0099cc), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR)); // Should have default value!
			assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
			assertEquals(new Color(0xffffff), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
			assertEquals(Integer.valueOf(12), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		}
		{
			View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "Node 3"));
			assertEquals(NodeShapeVisualProperty.DIAMOND, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
			assertEquals(Double.valueOf(3.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
			assertEquals(new Color(0x006699), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
			assertEquals(Double.valueOf(50.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
			assertEquals(Double.valueOf(36.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
			assertEquals(new Color(0x55aa00), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR));
			assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
			assertEquals(new Color(0xffffff), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
			assertEquals(Integer.valueOf(12), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		}
		{
			View<CyNode> nv = view.getNodeView(getNodeByName(view.getModel(), "Node 4"));
			assertEquals(NodeShapeVisualProperty.DIAMOND, nv.getVisualProperty(BasicVisualLexicon.NODE_SHAPE));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_TRANSPARENCY));
			assertEquals(Double.valueOf(4.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_WIDTH));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY));
			assertEquals(new Color(0x006699), nv.getVisualProperty(BasicVisualLexicon.NODE_BORDER_PAINT));
			assertEquals(Double.valueOf(70.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_WIDTH));
			assertEquals(Double.valueOf(72.0d), nv.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT));
			assertEquals(new Color(0x00ff00), nv.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR));
			assertEquals(view.getModel().getRow(nv.getModel()).get(CyNetwork.NAME, String.class), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL));
			assertEquals(Integer.valueOf(255), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY));
			assertEquals(new Color(0xffffff), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR));
			assertEquals(Integer.valueOf(12), nv.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE));
		}
	}
	
	private void checkRootNetwork(final CyRootNetwork net) {
		assertEquals(SavePolicy.SESSION_FILE, net.getSavePolicy());
		
		assertNotNull(net.getTable(CyNetwork.class, DEFAULT_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, LOCAL_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, HIDDEN_ATTRS));
		assertNotNull(net.getTable(CyNetwork.class, SHARED_ATTRS));
		
		Set<CyTable> allTables = tableManager.getAllTables(true);
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, DEFAULT_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, LOCAL_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, HIDDEN_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNetwork.class, SHARED_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyNode.class, SHARED_ATTRS)));
		assertTrue(allTables.contains(net.getTable(CyEdge.class, SHARED_ATTRS)));
	}
	
	private void checkVisualStyle(final VisualStyle style) {
		assertNotNull(style);
		assertEquals(vmm.getDefaultVisualStyle(), style);
		
		assertEquals(3, style.getAllVisualPropertyDependencies().size());
		
		Collection<VisualMappingFunction<?, ?>> mappings = style.getAllVisualMappingFunctions();
		assertEquals(6, mappings.size());
		
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_BORDER_WIDTH);
			assertTrue(mapping instanceof PassthroughMapping);
			assertEquals("score", mapping.getMappingColumnName());
			assertEquals(Integer.class, mapping.getMappingColumnType());
		}
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_FILL_COLOR);
			assertTrue(mapping instanceof ContinuousMapping);
			assertEquals("score", mapping.getMappingColumnName());
			assertEquals(Number.class, mapping.getMappingColumnType());
			assertEquals(2, ((ContinuousMapping)mapping).getAllPoints().size());
		}
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_SHAPE);
			assertTrue(mapping instanceof ContinuousMapping);
			assertEquals("score", mapping.getMappingColumnName());
			assertEquals(Number.class, mapping.getMappingColumnType());
			assertEquals(1, ((ContinuousMapping)mapping).getAllPoints().size());
		}
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_HEIGHT);
			assertTrue(mapping instanceof DiscreteMapping);
			assertEquals("score", mapping.getMappingColumnName());
			assertEquals(Integer.class, mapping.getMappingColumnType());
		}
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_WIDTH);
			assertTrue(mapping instanceof PassthroughMapping);
			assertEquals("width", mapping.getMappingColumnName());
			assertEquals(Double.class, mapping.getMappingColumnType());
		}
		{
			VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
			assertTrue(mapping instanceof PassthroughMapping);
			assertEquals(NAME, mapping.getMappingColumnName());
			assertEquals(String.class, mapping.getMappingColumnType());
		}
	}
	
	private void checkNetworkTables(final CyNetwork net) {
		Map<String, CyTable> tables = networkTableManager.getTables(net, CyNetwork.class);
		
		for (Map.Entry<String, CyTable> entry : tables.entrySet()) {
			String namespace = entry.getKey();
			CyTable tbl = entry.getValue();
			
			if (namespace.equals(LOCAL_ATTRS) || namespace.equals(SHARED_ATTRS) || namespace.equals(HIDDEN_ATTRS))
				assertEquals(SavePolicy.SESSION_FILE, tbl.getSavePolicy());
			else
				assertEquals(namespace + " should have DO_NOT_SAVE policy", SavePolicy.DO_NOT_SAVE, tbl.getSavePolicy());
		}
		
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, DEFAULT_ATTRS)));
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, LOCAL_ATTRS)));
		assertTrue(tables.containsValue(net.getTable(CyNetwork.class, HIDDEN_ATTRS)));
		
		// These tables are always private
		assertFalse(net.getTable(CyNetwork.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyNetwork.class, HIDDEN_ATTRS).isPublic());
		assertEquals(1, net.getTable(CyNetwork.class, DEFAULT_ATTRS).getAllRows().size());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(tables.containsValue(net.getTable(CyNetwork.class, SHARED_ATTRS)));
			assertTrue(tables.containsValue(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyNetwork.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyNetwork.class, SHARED_DEFAULT_ATTRS).isPublic());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, SHARED_ATTRS).getAllRows().size());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, SHARED_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS).getAllRows().size());
		} else {
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, LOCAL_ATTRS).getAllRows().size());
			assertEquals(NODE_COUNT, net.getTable(CyNode.class, DEFAULT_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, LOCAL_ATTRS).getAllRows().size());
			assertEquals(EDGE_COUNT, net.getTable(CyEdge.class, DEFAULT_ATTRS).getAllRows().size());
		}
		
		Map<String, CyTable> nodeTables = networkTableManager.getTables(net, CyNode.class);
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, DEFAULT_ATTRS)));
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, LOCAL_ATTRS)));
		assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, HIDDEN_ATTRS)));
		
		assertFalse(net.getTable(CyNode.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyNode.class, HIDDEN_ATTRS).isPublic());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, SHARED_ATTRS)));
			assertTrue(nodeTables.containsValue(net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyNode.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyNode.class, SHARED_DEFAULT_ATTRS).isPublic());
		}
		
		Map<String, CyTable> edgeTables = networkTableManager.getTables(net, CyEdge.class);
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, DEFAULT_ATTRS)));
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, LOCAL_ATTRS)));
		assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, HIDDEN_ATTRS)));
		
		assertFalse(net.getTable(CyEdge.class, LOCAL_ATTRS).isPublic());
		assertFalse(net.getTable(CyEdge.class, HIDDEN_ATTRS).isPublic());
		
		if (net instanceof CyRootNetwork) {
			assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, SHARED_ATTRS)));
			assertTrue(edgeTables.containsValue(net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS)));
			assertFalse(net.getTable(CyEdge.class, SHARED_ATTRS).isPublic());
			assertFalse(net.getTable(CyEdge.class, SHARED_DEFAULT_ATTRS).isPublic());
		}
	}
}

