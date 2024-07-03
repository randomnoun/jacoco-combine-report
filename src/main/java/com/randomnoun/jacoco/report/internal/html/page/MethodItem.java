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
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ISourceNode;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.ILinkable;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;
import com.randomnoun.jacoco.report.internal.html.table.ITableItem;

/**
 * Table items representing a method.
 */
final class MethodItem implements ITableItem {

	private final IMethodCoverage[] nodes;

	private final String label;

	private final ILinkable sourcePage;

	MethodItem(final IMethodCoverage[] nodes, final String label,
			final ILinkable sourcePage) {
		this.nodes = nodes;
		this.label = label;
		this.sourcePage = sourcePage;
	}

	public String getLinkLabel() {
		return label;
	}

	public String getLinkStyle() {
		return Styles.EL_METHOD;
	}

	public String getLink(final ReportOutputFolder base) {
		if (sourcePage == null) {
			return null;
		}
		final String link = sourcePage.getLink(base);
		final int first = nodes[0].getFirstLine();
		return first != ISourceNode.UNKNOWN_LINE ? link + "#L" + first : link;
	}

	public ICoverageNode[] getNodes() {
		return nodes;
	}

}
