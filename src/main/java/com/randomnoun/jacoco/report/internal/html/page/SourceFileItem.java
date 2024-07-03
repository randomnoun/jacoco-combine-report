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
import org.jacoco.core.analysis.ISourceFileCoverage;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;
import com.randomnoun.jacoco.report.internal.html.table.ITableItem;

/**
 * Table items representing a source file which cannot be linked.
 *
 */
final class SourceFileItem implements ITableItem {

	private final ICoverageNode[] nodes;

	SourceFileItem(final ISourceFileCoverage[] nodes) {
		this.nodes = nodes;
	}

	public String getLinkLabel() {
		return nodes[0].getName();
	}

	public String getLinkStyle() {
		return Styles.EL_SOURCE;
	}

	public String getLink(final ReportOutputFolder base) {
		return null;
	}

	public ICoverageNode[] getNodes() {
		return nodes;
	}

}
