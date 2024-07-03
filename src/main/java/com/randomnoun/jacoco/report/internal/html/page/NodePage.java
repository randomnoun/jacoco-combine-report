/*******************************************************************************
 * Copyright (c) 2009, 2024 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package com.randomnoun.jacoco.report.internal.html.page;

import org.jacoco.core.analysis.ICoverageNode;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.IHTMLReportContext;
import com.randomnoun.jacoco.report.internal.html.resources.Resources;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;
import com.randomnoun.jacoco.report.internal.html.table.ITableItem;

/**
 * Report page that represents a coverage node.
 *
 * @param <NodeType>
 *            type of the node represented by this page
 */
public abstract class NodePage<NodeType extends ICoverageNode>
		extends ReportPage implements ITableItem {

	// private final NodeType node;
	private final NodeType[] nodes;

	/**
	 * Creates a new node page.
	 *
	 * @param node
	 *            corresponding node
	 * @param parent
	 *            optional hierarchical parent
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	protected NodePage(final NodeType[] nodes, final ReportPage parent,
			final ReportOutputFolder folder, final IHTMLReportContext context) {
		super(parent, folder, context);
		this.nodes = nodes;
	}

	// === ILinkable ===

	public String getLinkStyle() {
		if (isRootPage()) {
			return Styles.EL_REPORT;
		} else {
			return Resources.getElementStyle(nodes[0].getElementType());
		}
	}

	public String getLinkLabel() {
		return nodes[0].getName();
	}

	// === ICoverageTableItem ===

	
	public NodeType getNode() {
		return nodes[0];
	}
	

	public NodeType[] getNodes() {
		return nodes;
	}

}
