// tempted to split all this into a separate project
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
package com.randomnoun.jacoco.report.html;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode.CounterEntity;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.report.ILanguageNames;
import org.jacoco.report.IMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.JavaNames;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.HTMLGroupVisitor;
import com.randomnoun.jacoco.report.internal.html.IHTMLReportContext;
import com.randomnoun.jacoco.report.internal.html.ILinkable;
import com.randomnoun.jacoco.report.internal.html.index.ElementIndex;
import com.randomnoun.jacoco.report.internal.html.index.IIndexUpdate;
import com.randomnoun.jacoco.report.internal.html.page.BundlePage;
import com.randomnoun.jacoco.report.internal.html.page.ReportPage;
import com.randomnoun.jacoco.report.internal.html.page.SessionsPage;
import com.randomnoun.jacoco.report.internal.html.resources.Resources;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;
import com.randomnoun.jacoco.report.internal.html.table.BarColumn;
import com.randomnoun.jacoco.report.internal.html.table.CounterColumn;
import com.randomnoun.jacoco.report.internal.html.table.LabelColumn;
import com.randomnoun.jacoco.report.internal.html.table.PercentageColumn;
import com.randomnoun.jacoco.report.internal.html.table.Table;

/**
 * Formatter for coverage reports in multiple HTML pages.
 */
public class HTMLFormatter implements IHTMLReportContext {

	private ILanguageNames languageNames = new JavaNames();

	private Locale locale = Locale.getDefault();

	private String footerText = "";

	private String outputEncoding = "UTF-8";

	private Resources resources;

	private ElementIndex index;

	private SessionsPage sessionsPage;

	private Table table;

	/**
	 * New instance with default settings.
	 */
	public HTMLFormatter() {
	}

	/**
	 * Sets the implementation for language name display. Java language names
	 * are defined by default.
	 *
	 * @param languageNames
	 *            converter for language specific names
	 */
	public void setLanguageNames(final ILanguageNames languageNames) {
		this.languageNames = languageNames;
	}

	/**
	 * Sets the locale used for report rendering. The current default locale is
	 * used by default.
	 *
	 * @param locale
	 *            locale used for report rendering
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Sets the optional text that should be included in every footer page.
	 *
	 * @param footerText
	 *            footer text
	 */
	public void setFooterText(final String footerText) {
		this.footerText = footerText;
	}

	/**
	 * Sets the encoding used for generated HTML pages. Default is UTF-8.
	 *
	 * @param outputEncoding
	 *            HTML output encoding
	 */
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	// === IHTMLReportContext ===

	public ILanguageNames getLanguageNames() {
		return languageNames;
	}

	public Resources getResources() {
		return resources;
	}

	public Table getTable(IBundleCoverage[] bundles) {
		if (table == null) {
			table = createTable(bundles);
		}
		return table;
	}

	private Table createTable(IBundleCoverage[] bundles) {
		final Table t = new Table();
		t.add("", "Element", Styles.DIVIDER, new LabelColumn(), false);
		int numBundle = bundles.length;
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			t.add(bundles[i].getName(), "Missed Instructions", Styles.BAR, new BarColumn(i, CounterEntity.INSTRUCTION, locale), i == 0);
			t.add("", "Cov.", Styles.CTR2 + dividerStyle, new PercentageColumn(i, CounterEntity.INSTRUCTION, locale), false);
		}
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			t.add(bundles[i].getName(), "Missed Branches", Styles.BAR, new BarColumn(i, CounterEntity.BRANCH, locale), false);
			t.add("", "Cov.", Styles.CTR2 + dividerStyle, new PercentageColumn(i, CounterEntity.BRANCH, locale), false);
		}
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			addMissedTotalColumns(t, bundles[i].getName(), "Cxty", dividerStyle, i, CounterEntity.COMPLEXITY);
		}
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			addMissedTotalColumns(t, bundles[i].getName(), "Lines", dividerStyle, i, CounterEntity.LINE);
		}
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			addMissedTotalColumns(t, bundles[i].getName(), "Methods", dividerStyle, i, CounterEntity.METHOD);
		}
		for (int i = 0; i < numBundle; i++) {
			String dividerStyle = (i==numBundle-1 ? " " + Styles.DIVIDER : "");
			addMissedTotalColumns(t, bundles[i].getName(), "Classes", dividerStyle, i, CounterEntity.CLASS);
		}
		return t;
	}

	private void addMissedTotalColumns(final Table table, String headerLabel, final String label, String dividerStyle, int itemIdx, final CounterEntity entity) {
		table.add(headerLabel, "Missed", Styles.CTR1, CounterColumn.newMissed(itemIdx, entity, locale), false);
		table.add("", label, Styles.CTR2 + dividerStyle, CounterColumn.newTotal(itemIdx, entity, locale), false);
	}

	public String getFooterText() {
		return footerText;
	}

	public ILinkable getSessionsPage() {
		return sessionsPage;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}

	public IIndexUpdate getIndexUpdate() {
		return index;
	}

	public Locale getLocale() {
		return locale;
	}
	
	public interface INewReportVisitor extends IReportVisitor {
		public void visitBundles(final IBundleCoverage[] bundles,
			final ISourceFileLocator locator) throws IOException;
	}

	/**
	 * Creates a new visitor to write a report to the given output.
	 *
	 * @param output
	 *            output to write the report to
	 * @return visitor to emit the report data to
	 * @throws IOException
	 *             in case of problems with the output stream
	 */
	public INewReportVisitor createVisitor(final IMultiReportOutput output)
			throws IOException {
		final ReportOutputFolder root = new ReportOutputFolder(output);
		resources = new Resources(root);
		resources.copyResources();
		index = new ElementIndex(root);
		
		return new INewReportVisitor() {

			private List<SessionInfo> sessionInfos;
			private Collection<ExecutionData> executionData;

			private HTMLGroupVisitor groupHandler;

			public void visitInfo(final List<SessionInfo> sessionInfos,
					final Collection<ExecutionData> executionData)
					throws IOException {
				this.sessionInfos = sessionInfos;
				this.executionData = executionData;
			}

			public void visitBundle(final IBundleCoverage bundle,
					final ISourceFileLocator locator) throws IOException {
				throw new UnsupportedOperationException("not this one");
			}
			
			public void visitBundles(final IBundleCoverage[] bundles,
					final ISourceFileLocator locator) throws IOException {
				final BundlePage page = new BundlePage(bundles, null, locator,
						root, HTMLFormatter.this);
				createSessionsPage(page);
				page.render();
			}

			public IReportGroupVisitor visitGroup(final String name)
					throws IOException {
				groupHandler = new HTMLGroupVisitor(null, root,
						HTMLFormatter.this, name);
				createSessionsPage(groupHandler.getPage());
				return groupHandler;

			}

			private void createSessionsPage(final ReportPage rootpage) {
				sessionsPage = new SessionsPage(sessionInfos, executionData,
						index, rootpage, root, HTMLFormatter.this);
			}

			public void visitEnd() throws IOException {
				if (groupHandler != null) {
					groupHandler.visitEnd();
				}
				sessionsPage.render();
				output.close();
			}
		};
	}
}
