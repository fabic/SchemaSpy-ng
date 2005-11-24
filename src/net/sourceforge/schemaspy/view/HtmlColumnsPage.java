package net.sourceforge.schemaspy.view;

import java.io.*;
import java.sql.*;
import java.util.*;
import net.sourceforge.schemaspy.model.*;
import net.sourceforge.schemaspy.util.*;

public class HtmlColumnsPage extends HtmlFormatter {
    private static HtmlColumnsPage instance = new HtmlColumnsPage();

    /**
     * Singleton - prevent creation
     */
    private HtmlColumnsPage() {
    }

    public static HtmlColumnsPage getInstance() {
        return instance;
    }

    public void write(Database database, Collection tables, boolean showRelationshipsGraph, boolean showOrphansGraph, LineWriter html) throws IOException, SQLException {
        Set columns = new TreeSet(new Comparator() {
            public int compare(Object object1, Object object2) {
                TableColumn column1 = (TableColumn)object1;
                TableColumn column2 = (TableColumn)object2;
                int rc = column1.getName().compareTo(column2.getName());
                if (rc == 0)
                    rc = column1.getTable().getName().compareTo(column2.getTable().getName());
                return rc;
            }
        });

        Set primaryColumns = new HashSet();
        Set indexedColumns = new HashSet();

        Iterator iter = tables.iterator();
        while (iter.hasNext()) {
            Table table = (Table)iter.next();
            columns.addAll(table.getColumns());

            primaryColumns.addAll(table.getPrimaryColumns());
            Iterator indexIter = table.getIndexes().iterator();
            while (indexIter.hasNext()) {
                TableIndex index = (TableIndex)indexIter.next();
                indexedColumns.addAll(index.getColumns());
            }
        }

        writeHeader(database, columns.size(), showRelationshipsGraph, showOrphansGraph, html);

        HtmlTableFormatter formatter = HtmlTableFormatter.getInstance();

        iter = columns.iterator();
        while (iter.hasNext()) {
            TableColumn column = (TableColumn)iter.next();
            formatter.writeColumn(column, column.getTable().getName(), primaryColumns, indexedColumns, false, html);
        }

        writeFooter(html);
    }

    private void writeHeader(Database db, int numberOfColumns, boolean hasRelationships, boolean hasOrphans, LineWriter html) throws IOException, SQLException {
        writeHeader(db, null, "Columns", hasRelationships, hasOrphans, html);

        html.writeln("<table width='100%' border='0'>");
        html.writeln("<tr><td class='container'>");
        writeGeneratedBy(db.getConnectTime(), html);
        html.writeln("</td><td class='container' rowspan='2' align='right' valign='top'>");
        writeLegend(false, false, html);
        html.writeln("</td></tr>");
        html.writeln("<tr valign='top'><td class='container' align='left' valign='top'>");
        html.writeln("<p/>");
        StyleSheet css = StyleSheet.getInstance();
        html.writeln("<form name='options' action=''>");
        html.writeln(" <input type=checkbox onclick=\"toggle(" + css.getOffsetOf(".tableKey") + ");\" id=showRelatedCols>Related columns");
        html.writeln(" <input type=checkbox onclick=\"toggle(" + css.getOffsetOf(".constraint") + ");\" id=showConstNames>Constraint names");
        html.writeln(" <input type=checkbox checked onclick=\"toggle(" + css.getOffsetOf(".legend") + ");\" id=showLegend>Legend");
        html.writeln("</form>");
        html.writeln("</table>");

        html.writeln("<div class='indent'>");
        html.write("<b>");
        html.write(db.getName());
        if (db.getSchema() != null) {
            html.write('.');
            html.write(db.getSchema());
        }
        html.write(" contains ");
        html.write(String.valueOf(numberOfColumns));
        html.write(" columns:</b>");
        Collection tables = db.getTables();
        boolean hasTableIds = tables.size() > 0 && ((Table)tables.iterator().next()).getId() != null;
        HtmlTableFormatter.getInstance().writeMainTableHeader(hasTableIds, true, html);
        html.writeln("<tbody valign='top'>");
    }

    protected void writeFooter(LineWriter html) throws IOException {
        html.writeln("</table>");
        html.writeln("</div>");
        super.writeFooter(html);
    }

    protected boolean isColumnsPage() {
        return true;
    }
}