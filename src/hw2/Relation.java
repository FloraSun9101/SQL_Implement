package hw2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hw1.Field;
import hw1.RelationalOperator;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * This class provides methods to perform relational algebra operations. It will be used
 * to implement SQL queries.
 * @author Doug Shook
 *
 */
public class Relation {

	private ArrayList<Tuple> tuples;
	private TupleDesc td;
	
	public Relation(ArrayList<Tuple> l, TupleDesc td) {
		this.tuples = l;
		this.td = td;
	}
	
	/**
	 * This method performs a select operation on a relation
	 * @param field number (refer to TupleDesc) of the field to be compared, left side of comparison
	 * @param op the comparison operator
	 * @param operand a constant to be compared against the given column
	 * @return
	 */
	public Relation select(int field, RelationalOperator op, Field operand) {
		ArrayList<Tuple> tuplesSelected = new ArrayList<Tuple>();
		for(int i = 0; i < this.tuples.size(); i++) {
			if(this.tuples.get(i).getField(field).compare(op, operand)) {
				tuplesSelected.add(this.tuples.get(i));
			}
		}
		this.tuples = tuplesSelected;
		return this;
	}
	
	/**
	 * This method performs a rename operation on a relation
	 * @param fields the field numbers (refer to TupleDesc) of the fields to be renamed
	 * @param names a list of new names. The order of these names is the same as the order of field numbers in the field list
	 * @return
	 * @throws Exception 
	 */
	public Relation rename(ArrayList<Integer> fields, ArrayList<String> names) throws Exception {
		int n = this.td.numFields();
		Type[] t = new Type[n];
		String[] name = new String[n];
		
		for(int i = 0; i < n; i++) {
			t[i] = this.td.getType(i);
			name[i] = this.td.getFieldName(i);
		}
		
		int indexName = 0;
		List<String> nameList = Arrays.asList(name);
		for(int i : fields) {
			if(names.get(indexName) != null && !names.get(indexName).equals("")) {
				if(names.get(indexName) != name[i] && nameList.contains(names.get(indexName))) {
					throw new Exception();
				}
				name[i] = names.get(i);
			}
			indexName++;
		}
		
		TupleDesc tdnew = new TupleDesc(t, name);
		this.td = tdnew;
		return this;
	}
	
	/**
	 * This method performs a project operation on a relation
	 * @param fields a list of field numbers (refer to TupleDesc) that should be in the result
	 * @return
	 * @throws IllegalArgumentException 
	 */
	public Relation project(ArrayList<Integer> fields) throws IllegalArgumentException {
		int n = fields.size();
		Type[] t = new Type[n];
		String[] name = new String[n];
		
		for(int i = 0; i < n; i++) {
			if(fields.get(i) >= this.td.numFields()) {
				throw new IllegalArgumentException();
			}
			t[i] = (this.td.getType(fields.get(i)));
			name[i] = (this.td.getFieldName(fields.get(i)));
		}
		
		TupleDesc tdNew = new TupleDesc(t, name);
		
		if(tdNew.getSize() == 0) { 
			ArrayList<Tuple> tuples = new ArrayList<Tuple>();
			this.tuples = tuples;
			this.td = tdNew;
			return this;
		}
		
		for(int i = 0; i < this.tuples.size(); i++) {
			Tuple tupleNew = new Tuple(tdNew);
			Tuple tuple = this.tuples.get(i);
			for(int j = 0; j < n; j++) {
				tupleNew.setField(j, tuple.getField(fields.get(j)));
				tupleNew.setId(tuple.getId());
				tupleNew.setPid(tuple.getPid());
			}
			this.tuples.set(i, tupleNew);
		}
		
		this.td = tdNew;
		return this;
	}
	
	/**
	 * This method performs a join between this relation and a second relation.
	 * The resulting relation will contain all of the columns from both of the given relations,
	 * joined using the equality operator (=)
	 * @param other the relation to be joined
	 * @param field1 the field number (refer to TupleDesc) from this relation to be used in the join condition
	 * @param field2 the field number (refer to TupleDesc) from other to be used in the join condition
	 * @return
	 */
	public Relation join(Relation other, int field1, int field2) {
		int nThis = this.tuples.size();
		int nOther = other.tuples.size();
		int fnThis = this.td.numFields();
		int fnOther = other.td.numFields();
		
		// set up new tupleDesc ========================================================================
//		int fieldsNum = fnThis + fnOther - 1;       // For the case we do not want to keep the duplicate column
		int fieldsNum = fnThis + fnOther;			// For the case we want to keep the duplicates column
		Type[] t = new Type[fieldsNum];
		String[] name = new String[fieldsNum];
		for (int i = 0; i < fnThis; i++) {
			t[i] = this.td.getType(i);
			name[i] = this.td.getFieldName(i);
		}
		for (int i = 0; i < fnOther; i++) {
/*			if(i != field2) {
				t[fnThis + i - ((i > field2)? 1 : 0)] = other.td.getType(i);
				name[fnThis + i - ((i > field2)? 1 : 0)] = other.td.getFieldName(i);
			}
*/   // For the case we do not want to keep the duplicate column
			
			t[fnThis + i] = other.td.getType(i);
			name[fnThis + i] = other.td.getFieldName(i);
		}
 		TupleDesc tdNew = new TupleDesc(t, name);
 		this.td = tdNew;
		
 		// set up new tuples ============================================================================
 		ArrayList<Tuple> tuplesNew = new ArrayList<Tuple>();
 		for(int i = 0; i < nThis; i++) {
 			Field fThis = this.tuples.get(i).getField(field1);
 			for(int j = 0; j < nOther; j++) {
 				if(other.tuples.get(j).getField(field2).compare(RelationalOperator.EQ, fThis)) {
 					Tuple tuple = new Tuple(td);
 					for(int k = 0; k < fnThis; k++) {
 						tuple.setField(k, this.tuples.get(i).getField(k));
 					}
 					for(int k = 0; k < fnOther; k++) {
/* 						if(k != field2) {
 							tuple.setField(fnThis + k - ((k > field2)? 1 : 0), other.tuples.get(j).getField(k));
 						}
*/   // For the case we do not want to keep the duplicate columns
 						tuple.setField(fnThis + k, other.tuples.get(j).getField(k));
 					}
 					tuplesNew.add(tuple);
 				}
 			}
 		}
 		
 		this.tuples = tuplesNew;
 		
		return this;
	}
	
	/**
	 * Performs an aggregation operation on a relation. See the lab write up for details.
	 * @param op the aggregation operation to be performed
	 * @param groupBy whether or not a grouping should be performed
	 * @return
	 */
	public Relation aggregate(AggregateOperator op, boolean groupBy) {
		Relation result = new Relation(this.tuples, this.td);
		try {
			Aggregator ag = new Aggregator(op, groupBy, this.td);
			for(Tuple t : this.tuples) {
				ag.merge(t);
			}
			result.tuples = ag.getResults();
			result.td = result.tuples.get(0).getDesc();
		}
		catch (ArithmeticException e) {
			System.out.println("A AVG operation can not be execute for String.");
		}
		return result;
	}
	
	public TupleDesc getDesc() {
		return this.td;
	}
	
	public ArrayList<Tuple> getTuples() {
		return this.tuples;
	}
	
	/**
	 * Returns a string representation of this relation. The string representation should
	 * first contain the TupleDesc, followed by each of the tuples in this relation
	 */
	public String toString() {
		String S = "The tuple description of the relation is: \n";
		S = S + this.td.toString();
		S = S + "The table is:";
		for (int i = 0; i < this.tuples.size(); i++) {
			S = S + this.tuples.get(i).toString();
		}
		return S;
	}
}
