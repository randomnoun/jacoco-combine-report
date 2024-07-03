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

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.report.ISourceFileLocator;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.HTMLElement;
import com.randomnoun.jacoco.report.internal.html.IHTMLReportContext;

/**
 * Page showing coverage information for a bundle. The page contains a table
 * with all packages of the bundle.
 */
public class BundlePage extends TablePage<ICoverageNode> {

	private final ISourceFileLocator locator;

	private IBundleCoverage[] bundles;

	/**
	 * Creates a new visitor in the given context.
	 *
	 * @param bundle
	 *            coverage date for the bundle
	 * @param parent
	 *            optional hierarchical parent
	 * @param locator
	 *            source locator
	 * @param folder
	 *            base folder for this bundle
	 * @param context
	 *            settings context
	 */
	public BundlePage(final IBundleCoverage[] bundles, final ReportPage parent,
			final ISourceFileLocator locator, final ReportOutputFolder folder,
			final IHTMLReportContext context) {
		super(getPlainCopies(bundles), parent, folder, context);
		this.bundles = bundles;
		this.locator = locator;
	}

	private static ICoverageNode[] getPlainCopies(IBundleCoverage[] bundles2) {
		ICoverageNode[] result = new ICoverageNode[bundles2.length];
		for (int i=0; i<result.length; i++) {
			result[i] = bundles2[i].getPlainCopy();
		}
		return result;
	}

	@Override
	public void render() throws IOException {
		renderPackages();
		super.render();
		// Don't keep the bundle structure in memory
		bundles = null;
	}

	private void renderPackages() throws IOException {
		for (final IPackageCoverage p : bundles[0].getPackages()) {
			if (!p.containsCode()) {
				continue;
			}
			final String packagename = p.getName();
			// @TODO get all packages with the same name across all bundles
			IPackageCoverage[] allPackages = new IPackageCoverage[bundles.length];
			allPackages[0] = p;
			for (int i=1; i<bundles.length; i++) {
				allPackages[i] = bundles[i].getPackages().stream()
					.filter(tmpP -> tmpP.getName().equals(packagename))
					.findFirst().orElse(null);
			}
			final String foldername = packagename.length() == 0 ? "default"
					: packagename.replace('/', '.');
			final PackagePage page = new PackagePage(allPackages, this, locator,
					folder.subFolder(foldername), context);
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
	
	public IBundleCoverage[] getBundles() {
		return bundles;
	}

	@Override
	protected void content(HTMLElement body) throws IOException {
		if (bundles[0].getPackages().isEmpty()) {
			body.p().text("No class files specified.");
		} else if (!bundles[0].containsCode()) {
			body.p().text(
					"None of the analyzed classes contain code relevant for code coverage.");
		} else {
			super.content(body);
		}
	}

}
