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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceNode;

import com.randomnoun.jacoco.report.internal.html.HTMLElement;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;

/**
 * Creates a highlighted output of a source file.
 */
final class SourceHighlighter {

	private final Locale locale;

	private String lang;

	/**
	 * Creates a new highlighter with default settings.
	 *
	 * @param locale
	 *            locale for tooltip rendering
	 */
	public SourceHighlighter(final Locale locale) {
		this.locale = locale;
		lang = "java";
	}

	/**
	 * Specifies the source language. This value might be used for syntax
	 * highlighting. Default is "java".
	 *
	 * @param lang
	 *            source language identifier
	 */
	public void setLanguage(final String lang) {
		this.lang = lang;
	}

	/**
	 * Highlights the given source file.
	 *
	 * @param parent
	 *            parent HTML element
	 * @param source
	 *            highlighting information
	 * @param contents
	 *            contents of the source file
	 * @throws IOException
	 *             problems while reading the source file or writing the output
	 */
	public void render(final HTMLElement parent, IBundleCoverage[] bundles, final ISourceNode[] source,
			final Reader contents) throws IOException {
		final HTMLElement pre = parent
				.pre(Styles.SOURCE + " lang-" + lang + " linenums");
		final BufferedReader lineBuffer = new BufferedReader(contents);
		String line;
		int nr = 0;
		ILine[] lines = new ILine[source.length];
		while ((line = lineBuffer.readLine()) != null) {
			nr++;
			for (int i=0; i<source.length; i++) {
				lines[i] = source[i] == null ? null : source[i].getLine(nr);
			}
			renderCodeLine(pre, line, bundles, lines, nr); // source.getLine(nr)
		}
	}

	private void renderCodeLine(final HTMLElement pre, final String linesrc,
			IBundleCoverage[] bundles, final ILine[] line, final int lineNr) throws IOException {
		highlight(pre, bundles, line, lineNr).text(linesrc);
		pre.text("\n");
	}

	private String getStyle1(int counterStatus) {
		switch (counterStatus) {
			case ICounter.NOT_COVERED:
				return Styles.NOT_COVERED;
			case ICounter.FULLY_COVERED:
				return Styles.FULLY_COVERED;
			case ICounter.PARTLY_COVERED:
				return Styles.PARTLY_COVERED;
			default:
				return null;
		}
	}
	
	// this is going to be a barrel of laughs
	HTMLElement highlight(final HTMLElement pre, IBundleCoverage[] bundles, final ILine[] line, final int lineNr) throws IOException {
		final String style;
		
		// so it'll go no info -> not covered -> partly covered -> fully covered
		String style1s[] = new String[line.length];
		int allStatus = 0; // 0 == null
		for (int i=0; i<line.length; i++) {
			int s = line[i].getStatus();
			style1s[i] = getStyle1(s);
			if (i==0) {
				allStatus = switch (s) {
					case ICounter.FULLY_COVERED -> ICounter.FULLY_COVERED;
					case ICounter.PARTLY_COVERED -> ICounter.PARTLY_COVERED;
					case ICounter.NOT_COVERED -> ICounter.NOT_COVERED;
					default -> 0;
				};
			} else {
				allStatus = switch (s) {
					case ICounter.FULLY_COVERED -> ICounter.FULLY_COVERED;
					case ICounter.PARTLY_COVERED -> allStatus == 0 || allStatus ==  ICounter.NOT_COVERED ? ICounter.PARTLY_COVERED : allStatus;	
					case ICounter.NOT_COVERED -> allStatus == 0 ? ICounter.NOT_COVERED : allStatus;
					default -> allStatus;
				};
			}
		}
		style = getStyle1(allStatus);
		if (style == null) {
			for (int i = 0; i < line.length; i++) {
				HTMLElement span = pre.span("bskip");
				span.text(" ");
			}
			return pre; // no coverage in any bundle
		}
		
		// same again for branch counters
		for (int i=0; i<line.length; i++) {
			ICounter branches = line[i].getBranchCounter();
			final Integer missed = Integer.valueOf(branches.getMissedCount());
			final Integer total = Integer.valueOf(branches.getTotalCount());
			String s2 = null, t2 = null;
			switch (branches.getStatus()) {
				case ICounter.NOT_COVERED:
					s2 = Styles.BRANCH_NOT_COVERED; 
					t2 = String.format(locale, "All %2$d branches missed.", missed, total);
					break;
				case ICounter.FULLY_COVERED:
					s2 = Styles.BRANCH_FULLY_COVERED;
					t2 = String.format(locale, "All %2$d branches covered.", missed, total);
					break;
				case ICounter.PARTLY_COVERED:
					s2 = Styles.BRANCH_PARTLY_COVERED;
					t2 = String.format(locale, "%1$d of %2$d branches missed.", missed, total);
					break;
				default:
					s2 = null; t2 = null;
			}
			if (s2 != null) {
				HTMLElement span = pre.span(style1s[i] == null ? s2 : style1s[i] + " " + s2);
				span.attr("title", bundles[i].getName() + ": " + t2);
				span.text(" "); 
				// need text otherwise it doesn't close properly ( reports have DOCTYPE HTML in them, which doesn't allow self-closing spans )
				// also, those non-self-closing spans cause stack overflows in chrome
				// also, need a space character for the span to take up any space in the layout
			} else {
				HTMLElement span = pre.span(style1s[i] == null ? s2 : style1s[i] + " bskip");
				span.text(" ");
			}
		}

		
		final String lineId = "L" + Integer.toString(lineNr);
		HTMLElement span = pre.span(style, lineId);
		return span;
		
	}

}
