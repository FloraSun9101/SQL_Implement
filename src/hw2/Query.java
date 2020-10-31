package hw2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hw1.Catalog;
import hw1.Database;
import hw1.Field;
import hw1.HeapFile;
import hw1.RelationalOperator;
import hw1.StringField;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.deparser.SelectDeParser;

public class Query {

	private String q;
	
	public Query(String q) {
		this.q = q;
	}
	
	public Relation execute()  {
		Statement statement = null;
		try {
			statement = CCJSqlParserUtil.parse(q);						// Extract table names from SQL 
		} catch (JSQLParserException e) {
			System.out.println("Unable to parse query");
			e.printStackTrace();
		}
		Select selectStatement = (Select) statement;
		PlainSelect sb = (PlainSelect)selectStatement.getSelectBody();
		
		// (FROM) =========================================
		Catalog c = Database.getCatalog();
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
		ArrayList<Relation> rs = new ArrayList<Relation>();
		for(String table : tableList) {
			HeapFile hf = c.getDbFile(c.getTableId(table));
			rs.add(new Relation(hf.getAllTuples(), hf.getTupleDesc()));
		}
		
		// join(JOIN) ======================================
		List<Join> joins = sb.getJoins();
		String tableName = sb.getFromItem().toString();
		
		Relation r = rs.get(0);
		if(joins != null) {
			for(Join join : joins) {
				WhereExpressionVisitor w = new WhereExpressionVisitor();
				join.getOnExpression().accept(w);
				
				StringField cR = (StringField)w.getRight();	
				String cL = w.getLeft();
				String[] tAndC = cR.getValue().split("\\.");
				
				if(tAndC[0].equals(tableName)) {				
					// deal with the situation such as "SELECT * FROM test JOIN S ON S.s1 = test.c2" 
					// where the join keys' order is not same as join order.
					String tem = cL;
					cL = tAndC[1];
					tAndC[1] = tem;
				}
				int field1 = rs.get(tableList.indexOf(tableName)).getDesc().nameToId(cL);
				
				Relation rOther = rs.get(tableList.indexOf(join.getRightItem().toString()));
				int field2 = rOther.getDesc().nameToId(tAndC[1]);
				r = r.join(rOther, field1, field2);
			}
		}
		
		// select(WHERE) ===================================
		Expression where = sb.getWhere();
		if(where != null) {
			WhereExpressionVisitor wev = new WhereExpressionVisitor();
			where.accept(wev);
		
			// get op 
			RelationalOperator op = wev.getOp();
			// get field
			int f = r.getDesc().nameToId(wev.getLeft());
			// get operand
			Field operand = wev.getRight();
			r = r.select(f, op, operand);
		}
				
		// project(SELECT) ==================================
		List<SelectItem> cols = sb.getSelectItems();
		ColumnVisitor cv = new ColumnVisitor();
		ArrayList<Integer> fields = new ArrayList<Integer>();
		for(SelectItem col : cols) {
			//ColumnVisitor cv = new ColumnVisitor();
			col.accept(cv);
			String colName = cv.getColumn();
			if(!colName.equals("*")) {
				fields.add(r.getDesc().nameToId(colName));
			}
			else {
				for (int i = 0; i < r.getDesc().numFields(); i++) {
					fields.add(i);
				}
			}
		}
		r = r.project(fields);
		
		// aggregate(MAX, MIN ... GROUP BY) ==================
		Boolean flag = false;
		
		for(SelectItem col : cols) {
			col.accept(cv);
			if(cv.isAggregate()) {
				flag = true;
			}
		}
		if(flag) {
			AggregateOperator aop = cv.getOp();
			r = r.aggregate(aop, (sb.getGroupByColumnReferences() != null));
		}

		return r;
		
	}
}
