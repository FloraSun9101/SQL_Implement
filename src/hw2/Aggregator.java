package hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import hw1.Field;
import hw1.IntField;
import hw1.RelationalOperator;
import hw1.StringField;
import hw1.Tuple;
import hw1.TupleDesc;
import hw1.Type;

/**
 * A class to perform various aggregations, by accepting one tuple at a time
 * @author Doug Shook
 *
 */
public class Aggregator {
	private AggregateOperator o;
	private boolean groupBy;
	private TupleDesc td;
	private HashMap<Field, ArrayList<Field>> map;
	

	public Aggregator(AggregateOperator o, boolean groupBy, TupleDesc td) throws ArithmeticException {
		if(td.getType((groupBy)? 1 : 0) == Type.STRING && o == AggregateOperator.AVG) {
			throw new ArithmeticException();
		}
		this.groupBy = groupBy;
		this.o = o;
		this.td = td;
		this.map = new HashMap<Field, ArrayList<Field>>();
	}

	/**
	 * Merges the given tuple into the current aggregation
	 * @param t the tuple to be aggregated
	 */
	public void merge(Tuple t) {
		if(groupBy) {
			if(!this.map.containsKey(t.getField(0))) {
				ArrayList<Field> values = new ArrayList<Field>();
				values.add(t.getField(1));
				this.map.put(t.getField(0), values);
			}
			else {
				this.map.get(t.getField(0)).add(t.getField(1));
			}
		}
		else {
			IntField fake = new IntField(0);
			if(this.map.isEmpty()) {
				ArrayList<Field> values = new ArrayList<Field>();
				values.add(t.getField(0));
				this.map.put(fake, values);
			}
			else {
				this.map.get(fake).add(t.getField(0));
			}
		}
	}
	
	/**
	 * Returns the result of the aggregation
	 * @return a list containing the tuples after aggregation
	 */
	public ArrayList<Tuple> getResults() {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		switch(this.o) {
		case MAX:
			result = this.getMax();
			break;
			
		case MIN:
			result = this.getMin();
			break;
			
		case COUNT:
			result = this.getCount();
			break;
			
		case SUM:
			result = this.getSum();
			break;
			
		case AVG:
			result = this.getAvg();
			break;
		}
		return result;
	}
	
	private ArrayList<Tuple> getMax() {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		if(groupBy) {
			for(Field key : this.map.keySet()) {
				ArrayList<Field> ftemp = this.map.get(key);
				Field max = ftemp.get(0);
				for(int i = 1; i < ftemp.size(); i++) {
					if(ftemp.get(i).compare(RelationalOperator.GT, max)) {
						max = ftemp.get(i);
					}
				}
				TupleDesc tdNew = new TupleDesc(new Type[] {this.td.getType(0), this.td.getType(1)}, new String[] {this.td.getFieldName(0) ,this.td.getFieldName(1) + "_max"});
				Tuple t = new Tuple(tdNew);
				t.setField(0, key);
				t.setField(1, max);
				result.add(t);
			}
			return result;
		}
		else {
			IntField fake = new IntField(0);
			ArrayList<Field> ftemp = this.map.get(fake);
			Field max = ftemp.get(0);
			for(int i = 1; i < ftemp.size(); i++) {
				if(ftemp.get(i).compare(RelationalOperator.GT, max)) {
					max = ftemp.get(i);
				}
			}
			TupleDesc tdNew = new TupleDesc(new Type[] {max.getType()}, new String[] {this.td.getFieldName(0) + "_max"});
			Tuple t = new Tuple(tdNew);
			t.setField(0, max);
			result.add(t);
			return result;
		}
	}
	
	private ArrayList<Tuple> getMin() {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		if(groupBy) {
			for(Field key : this.map.keySet()) {
				ArrayList<Field> ftemp = this.map.get(key);
				Field min = ftemp.get(0);
				for(int i = 1; i < ftemp.size(); i++) {
					if(ftemp.get(i).compare(RelationalOperator.LT, min)) {
						min = ftemp.get(i);
					}
				}
				TupleDesc tdNew = new TupleDesc(new Type[] {this.td.getType(0), this.td.getType(1)}, new String[] {this.td.getFieldName(0) ,this.td.getFieldName(1) + "_min"});
				Tuple t = new Tuple(tdNew);
				t.setField(0, key);
				t.setField(1, min);
				result.add(t);
			}
			return result;
		}
		else {
			IntField fake = new IntField(0);
			ArrayList<Field> ftemp = this.map.get(fake);
			Field min = ftemp.get(0);
			for(int i = 1; i < ftemp.size(); i++) {
				if(ftemp.get(i).compare(RelationalOperator.LT, min)) {
					min = ftemp.get(i);
				}
			}
			TupleDesc tdNew = new TupleDesc(new Type[] {min.getType()}, new String[] {this.td.getFieldName(0) + "_min"});
			Tuple t = new Tuple(tdNew);
			t.setField(0, min);
			result.add(t);
			return result;
		}
	}
	
	private ArrayList<Tuple> getCount() {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		if(groupBy) {
			for(Field key : this.map.keySet()) {
				int count = this.map.get(key).size();
				TupleDesc tdNew = new TupleDesc(new Type[] {key.getType(), Type.INT}, new String[] {this.td.getFieldName(0), this.td.getFieldName(1) + "_count"});
				Tuple t = new Tuple(tdNew);
				t.setField(0, key);
				t.setField(1, new IntField(count));
				result.add(t);
			}
			return result;
		}
		else {
			IntField fake = new IntField(0);
			int count = this.map.get(fake).size();
			TupleDesc tdNew = new TupleDesc(new Type[] {Type.INT}, new String[] {this.td.getFieldName(0) + "_count"});
			Tuple t = new Tuple(tdNew);
			t.setField(0, new IntField(count));
			result.add(t);
			return result;
		}
	}
	
	private ArrayList<Tuple> getSum() {
		ArrayList<Tuple> result = new ArrayList<Tuple>();
		if(groupBy) {
			for(Field key : this.map.keySet()) {
				ArrayList<Field> ftemp = this.map.get(key);
				TupleDesc tdNew = new TupleDesc(new Type[] {this.td.getType(0), this.td.getType(1)}, new String[] {this.td.getFieldName(0) ,"SUM"});
				Tuple t = new Tuple(tdNew);
				t.setField(0, key);
				if(this.td.getType(1) == Type.INT) {
					int sum = 0;
					for(int i = 0; i < ftemp.size(); i++) {
						IntField ttemp = (IntField)ftemp.get(i);
						sum += ttemp.getValue();
					}
					IntField sumF = new IntField(sum);
					t.setField(1, sumF);
				}
				
				if(this.td.getType(1) == Type.STRING) {
					String sum = "";
					for(int i = 0; i < ftemp.size(); i++) {
						StringField ttemp = (StringField)ftemp.get(i);
						sum = sum + ttemp.getValue();
					}
					StringField sumF = new StringField(sum);
					t.setField(1, sumF);
				}
				result.add(t);
			}
			return result;
		}
		else {
			IntField fake = new IntField(0);
			ArrayList<Field> ftemp = this.map.get(fake);
			TupleDesc tdNew = new TupleDesc(new Type[] {this.td.getType(0)}, new String[] {"SUM"});
			Tuple t = new Tuple(tdNew);
			if(this.td.getType(0) == Type.INT) {
				int sum = 0;
				for(int i = 0; i < ftemp.size(); i++) {
					IntField ttemp = (IntField)ftemp.get(i);
					sum += ttemp.getValue();
				}
				IntField sumF = new IntField(sum);
				t.setField(0, sumF);
			}
			
			if(this.td.getType(0) == Type.STRING) {
				String sum = "";
				for(int i = 0; i < ftemp.size(); i++) {
					StringField ttemp = (StringField)ftemp.get(i);
					sum = sum + ttemp.getValue();
				}
				StringField sumF = new StringField(sum);
				t.setField(0, sumF);
			}
			result.add(t);
			return result;
		}
	}
	
	private ArrayList<Tuple> getAvg() {
		ArrayList<Tuple> result = this.getSum();
		if(groupBy) {
			for(Tuple t : result) {
				if(t.getDesc().getType(1) == Type.INT) {
					IntField ftemp = (IntField) t.getField(1);
					t.setField(1, new IntField((ftemp.getValue() / this.map.get(t.getField(0)).size())));
					result.set(0, t);
				}
			}
			return result;
		}
		else {
			Tuple t = result.get(0);
			IntField fake = new IntField(0);
			if(t.getDesc().getType(0) == Type.INT) {
				IntField ftemp = (IntField) t.getField(0);
				t.setField(0, new IntField((ftemp.getValue() / this.map.get(fake).size())));
				result.set(0, t);
			}
			return result;
		}
	}
}
