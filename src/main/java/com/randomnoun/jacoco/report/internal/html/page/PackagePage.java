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

import java.io.IOException;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.report.ISourceFileLocator;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.HTMLElement;
import com.randomnoun.jacoco.report.internal.html.IHTMLReportContext;
import com.randomnoun.jacoco.report.internal.html.ILinkable;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;

/**
 * Page showing coverage information for a Java package. The page contains a
 * table with all classes of the package.
 */
public class PackagePage extends TablePage<IPackageCoverage> {

	private final PackageSourcePage packageSourcePage;
	private final boolean sourceCoverageExists;

	/**
	 * Creates a new visitor in the given context.
	 *
	 * @param node
	 *            coverage data for this package
	 * @param parent
	 *            optional hierarchical parent
	 * @param locator
	 *            source locator
	 * @param folder
	 *            base folder to create this page in
	 * @param context
	 *            settings context
	 */
	public PackagePage(final IPackageCoverage[] nodes, final ReportPage parent,
			final ISourceFileLocator locator, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(nodes, parent, folder, context);
		packageSourcePage = new PackageSourcePage(nodes, parent, locator, folder,
				context, this);
		
		// @TODO or them all together
		sourceCoverageExists = !nodes[0].getSourceFiles().isEmpty();
	}

	@Override
	public void render() throws IOException {
		if (sourceCoverageExists) {
			packageSourcePage.render();
		}
		renderClasses();
		super.render();
	}

	private void renderClasses() throws IOException {
		for (final IClassCoverage c : getNode().getClasses()) {
			if (!c.containsCode()) {
				continue;
			}
			String className = c.getName();
			IClassCoverage[] allClassCoverages = new IClassCoverage[getNodes().length];
			allClassCoverages[0] = c;
			for (int i=1; i<getNodes().length; i++) {
				allClassCoverages[i] = getNodes()[i].getClasses().stream()
					.filter(tmpCC -> tmpCC.getName().equals(className))
					.findFirst().orElse(null);
			}

			
			final ILinkable sourceFilePage = packageSourcePage
					.getSourceFilePage(c.getSourceFileName());
			final ClassPage page = new ClassPage(allClassCoverages, this, sourceFilePage,
					folder, context);
			page.render();
			addItem(page);
		}
	}

	@Override
	protected String getOnload() {
		return "initialSort(['breadcrumb', 'coveragetable'])";
	}

	@Override
	protected String getFileName() {
		return "index.html";
	}

	@Override
	public String getLinkLabel() {
		return context.getLanguageNames().getPackageName(getNode().getName());
	}

	@Override
	protected void infoLinks(final HTMLElement span) throws IOException {
		if (sourceCoverageExists) {
			final String link = packageSourcePage.getLink(folder);
			span.a(link, Styles.EL_SOURCE).text("Source Files");
		}
		super.infoLinks(span);
	}

}
