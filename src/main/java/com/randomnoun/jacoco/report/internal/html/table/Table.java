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
package com.randomnoun.jacoco.report.internal.html.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jacoco.core.analysis.ICoverageNode;

import com.randomnoun.jacoco.report.internal.ReportOutputFolder;
import com.randomnoun.jacoco.report.internal.html.HTMLElement;
import com.randomnoun.jacoco.report.internal.html.resources.Resources;
import com.randomnoun.jacoco.report.internal.html.resources.Styles;

/**
 * Renderer for a table of {@link ITableItem}s.
 */
public class Table {

	private final List<Column> columns;

	private Comparator<ITableItem> defaultComparator;

	/**
	 * Create a new table without any columns yet.
	 */
	public Table() {
		this.columns = new ArrayList<Table.Column>();
	}

	/**
	 * Adds a new column with the given properties to the table.
	 *
	 * @param headerTop
	 *            column top header caption
	 * @param header
	 *            column header caption
	 * @param style
	 *            optional CSS style class name for the td-Elements of this
	 *            column
	 * @param renderer
	 *            callback for column rendering
	 * @param defaultSorting
	 *            If <code>true</code>, this column is the default sorting
	 *            column. Only one column can be selected for default sorting.
	 *
	 */
	public void add(final String headerTop, final String header, final String style,
			final INewColumnRenderer renderer, final boolean defaultSorting) {
		columns.add(new Column(columns.size(), headerTop, header, style, renderer, defaultSorting));
		if (defaultSorting) {
			if (defaultComparator != null) {
				throw new IllegalStateException("Default sorting only allowed for one column.");
			}
			this.defaultComparator = renderer.getComparator();
		}
	}

	/**
	 * Renders a table for the given icon
	 *
	 * @param parent
	 *            parent element in which the table is created
	 * @param items
	 *            items that will make the table rows
	 * @param total
	 *            the summary of all coverage data items in the table static
	 *            resources that might be referenced
	 * @param resources
	 *            static resources that might be referenced
	 * @param base
	 *            base folder of the table
	 * @throws IOException
	 *             in case of IO problems with the element output
	 */
	public void render(final HTMLElement parent,
			final List<? extends ITableItem> items, final ICoverageNode[] total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final List<? extends ITableItem> sortedItems = sort(items);
		final HTMLElement table = parent.table(Styles.COVERAGETABLE);
		table.attr("id", "coveragetable");
		header(table, sortedItems, total);
		footer(table, total, resources, base);
		body(table, sortedItems, resources, base);
	}

	private void header(final HTMLElement table,
			final List<? extends ITableItem> items, final ICoverageNode[] total)
			throws IOException 
	{
		// was hoping to create both header cells at the same time but that's not doing to work is it.
		for (final Column c : columns) {
			c.init(items, total);
		}

		final HTMLElement trTop = table.thead().tr();
		for (final Column c : columns) {
			c.headerTop(trTop);
		}
		
		final HTMLElement tr = table.thead().tr();
		for (final Column c : columns) {
			c.header(tr);
		}
	}

	private void footer(final HTMLElement table, final ICoverageNode[] total,
			final Resources resources, final ReportOutputFolder base)
			throws IOException {
		final HTMLElement tr = table.tfoot().tr();
		for (final Column c : columns) {
			c.footer(tr, total, resources, base);
		}
	}

	private void body(final HTMLElement table,
			final List<? extends ITableItem> items, final Resources resources,
			final ReportOutputFolder base) throws IOException {
		final HTMLElement tbody = table.tbody();
		int idx = 0;
		for (final ITableItem item : items) { // hrm. this feels like an item is a row, which it isn't. maybe it is.
			final HTMLElement tr = tbody.tr();
			for (final Column c : columns) {
				c.body(tr, idx, item, resources, base);
			}
			idx++;
		}
	}

	private List<? extends ITableItem> sort(
			final List<? extends ITableItem> items) {
		if (defaultComparator != null) {
			final List<ITableItem> result = new ArrayList<ITableItem>(items);
			Collections.sort(result, defaultComparator);
			return result;
		}
		return items;
	}
	
	public interface INewColumnRenderer /*extends IColumnRenderer*/ {
		// colIndex now in constructor
		boolean init(List<? extends ITableItem> items, ICoverageNode[] total);

		
		//void headerTop(final HTMLElement trTop);
		//void header(final HTMLElement trTop);
		
		
		/**
		 * Renders the footer for this column.
		 *
		 * @param td
		 *            the parent table cell
		 * @param total
		 *            the summary of all coverage data items in the table
		 * @param resources
		 *            static resources that might be referenced
		 * @param base
		 *            base folder of the table
		 * @throws IOException
		 *             in case of IO problems with the element output
		 */
		void footer(HTMLElement td, ICoverageNode[] total, Resources resources,
				ReportOutputFolder base) throws IOException;

		/**
		 * Renders a single item in this column.
		 *
		 * @param td
		 *            the parent table cell
		 * @param item
		 *            the item to display
		 * @param resources
		 *            static resources that might be referenced
		 * @param base
		 *            base folder of the table
		 * @throws IOException
		 *             in case of IO problems with the element output
		 */
		void item(HTMLElement td, ITableItem item, Resources resources,
				ReportOutputFolder base) throws IOException;

		/**
		 * Returns the comparator to sort this table column.
		 *
		 * @return comparator for this column
		 */
		Comparator<ITableItem> getComparator();		
	}

	private static class Column {

		private final char idprefix;
		private final String topHeader;
		private final String header;
		private final INewColumnRenderer renderer;
		private final SortIndex<ITableItem> index;
		private final String style, headerStyle;

		private boolean visible;

		Column(final int idx, final String topHeader, final String header, final String style,
				final INewColumnRenderer renderer, final boolean defaultSorting) {
			this.idprefix = (char) ('a' + idx);
			this.topHeader = topHeader;
			this.header = header;
			this.renderer = renderer;
			index = new SortIndex<ITableItem>(renderer.getComparator());
			this.style = style;
			this.headerStyle = Styles.combine(
				defaultSorting ? Styles.DOWN : null, Styles.SORTABLE,
				style);
		}

		void init(final List<? extends ITableItem> items, final ICoverageNode[] total) throws IOException {
			
			visible = renderer.init(items, total);
			if (visible) {
				index.init(items);
			}	
		}
		void headerTop(final HTMLElement trTop) throws IOException {
			if (visible) {
				final HTMLElement tdTop = trTop.td(style); // or just .td("")
				tdTop.text(topHeader);
			}

		}
		void header(final HTMLElement tr) throws IOException {
			if (visible) {
				final HTMLElement td = tr.td(headerStyle);
				td.attr("id", String.valueOf(idprefix));
				td.attr("onclick", "toggleSort(this)");
				td.text(header);
			}
			
		}

		void footer(final HTMLElement tr, final ICoverageNode[] total,
				final Resources resources, final ReportOutputFolder base)
				throws IOException {
			if (visible) {
				renderer.footer(tr.td(style), total, resources, base);
			}
		}

		void body(final HTMLElement tr, final int idx, final ITableItem item,
				final Resources resources, final ReportOutputFolder base)
				throws IOException {
			if (visible) {
				final HTMLElement td = tr.td(style);
				td.attr("id",
						idprefix + String.valueOf(index.getPosition(idx)));
				renderer.item(td, item, resources, base);
			}
		}

	}

}
