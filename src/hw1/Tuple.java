package hw1;

import java.sql.Types;
import java.util.HashMap;

/**
 * This class represents a tuple that will contain a single row's worth of information
 * from a table. It also includes information about where it is stored
 * @author Sam Madden modified by Doug Shook
 *
 */
public class Tuple {
	TupleDesc td;
	byte[] content;
	int Pid;
	int id;
	
	/**
	 * Creates a new tuple with the given description
	 * @param t the schema for this tuple
	 */
	public Tuple(TupleDesc t) {
		this.td = t;
		this.content = new byte[t.getSize()];
		this.Pid = -1;
		this.id = -1;
		
	}
	
	public TupleDesc getDesc() {
		return this.td;
	}
	
	/**
	 * retrieves the page id where this tuple is stored
	 * @return the page id of this tuple
	 */
	public int getPid() {
		return this.Pid;
	}

	public void setPid(int pid) {
		this.Pid = pid;
	}

	/**
	 * retrieves the tuple (slot) id of this tuple
	 * @return the slot where this tuple is stored
	 */
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setDesc(TupleDesc td) {
		this.td = td;
	}
	
	/**
	 * Stores the given data at the i-th field
	 * @param i the field number to store the data
	 * @param v the data
	 */
	public void setField(int i, Field v) {
		if (this.td.getType(i) != v.getType()) {  }      // If the type could not match, it could not set the value
		else {
			int locate = 0;
			for (int j = 0; j < i; j++) {
				Type t = this.td.getType(j);
				if(t == Type.INT) {
					locate += 4; 
				}
				else if(t == Type.STRING) {
					locate += 129;
				}
			}
			int n = (this.td.getType(i) == Type.INT) ? 4 : 129;
			byte[] valueInByte = v.toByteArray();
			for(int j = 0; j < n; j++) {
				this.content[locate + j] = valueInByte[j];
			}
		}
	}
	
	public Field getField(int i) {
		int locate = 0;
		for (int j = 0; j < i; j++) {
			Type t = this.td.getType(j);
			if(t == Type.INT) {
				locate += 4; 
			}
			else if(t == Type.STRING) {
				locate += 129;
			}
		}
		
		if(this.td.getType(i) == Type.INT) {
			int n = 4;
			byte[] valueInByte = new byte[n];
			for(int j = 0; j < n; j++) {
				 valueInByte[j] = this.content[locate + j];
			}
			IntField v = new IntField(valueInByte);
			return v;
		}
		else {
			int n = 129;
			byte[] valueInByte = new byte[n];
			for(int j = 0; j < n; j++) {
				 valueInByte[j] = this.content[locate + j];
			}
			StringField v = new StringField(valueInByte);
			return v;
		}
		
	}
	
	/**
	 * Creates a string representation of this tuple that displays its contents.
	 * You should convert the binary data into a readable format (i.e. display the ints in base-10 and convert
	 * the String columns to readable text).
	 */
	public String toString() {
		int n = 0;
		int i = 0;
		String s = "";
		while (n < this.content.length) {
			if(this.td.getType(i) == Type.INT) {
				byte[] valueInByte = new byte[4];
				for(int j = 0; j < 4; j++) {
					 valueInByte[j] = this.content[n + j];
				}
				IntField v = new IntField(valueInByte);
				s += v.toString();
				n += 4;
				i++;
			}
			else if(this.td.getType(i) == Type.STRING) {
				byte[] valueInByte = new byte[129];
				for(int j = 0; j < 129; j++) {
					 valueInByte[j] = this.content[n + j];
				}
				StringField v = new StringField(valueInByte);
				s += v.toString();
				n += 129;
				i++;
			}
		}
		return s;
	}
	
	public boolean equals(Tuple t) {
		boolean flag = true;
		int fieldsNum = this.getDesc().numFields();
		for (int i = 0; i < fieldsNum; i++) {
			if(!this.getField(i).equals(t.getField(i))) {
				flag = false;
				break;
			}
		}
		return flag;
	}
}
	