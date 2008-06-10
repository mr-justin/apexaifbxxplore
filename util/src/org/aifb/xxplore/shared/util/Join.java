package org.aifb.xxplore.shared.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Join {
	public static class DataSet {
		String[] m_variables;
		List<Row> m_rows;
		Map<String,Integer> m_var2ColIdx;
		
		public DataSet(String[] vars) {
			m_variables = vars;
			m_rows = new ArrayList<Row>();
			
			m_var2ColIdx = new HashMap<String,Integer>();
			for (int i = 0; i < m_variables.length; i++) {
				m_var2ColIdx.put(m_variables[i], i);
			}
		}

		public DataSet(String[] vars, List<Row> data) {
			this(vars);
			m_rows = data;
		}
		
		public int size() {
			return m_rows.size();
		}
		
		public int getColumnForVariable(String var) {
			return m_var2ColIdx.get(var);
		}

		public Iterator<Row> iterator() {
			return m_rows.iterator();
		}
		
		public Row get(int row) {
			return m_rows.get(row);
		}
		
		public void addRow(Row r) {
			m_rows.add(r);
		}
		
		public void addRow(Object[] data) {
			m_rows.add(new Row(this, data));
		}
		
		public String[] getVars() {
			return m_variables;
		}
	}
	
	public static class Row {
		private Object[] m_data;
		private DataSet m_parent;
		
		public Row(DataSet parent, Object[] data) {
			m_parent = parent;
			m_data = data;
		}
		
		public Object get(int col) {
			return m_data[col];
		}
		
		public Object get(String var) {
			return m_data[m_parent.getColumnForVariable(var)];
		}
		
		public Object[] get(String[] vars) {
			Object[] data = new Object[vars.length];
			for (int i = 0; i < vars.length; i++)
				data[i] = get(vars[i]);
			return data;
		}

		public Object[] getData() {
			return m_data;
		}

		@Override
		public String toString() {
			String s = "[";
			String comma = "";
			for (Object o : m_data) {
				s += comma + o;
				comma = ",";
			}
			return s + "]";
		}
	}
	
	public static class Tuple {
		Object[] m_data;
		
		public Tuple(Object[] data) {
			m_data = data;
		}
		
		public Object[] getData() {
			return m_data;
		}
		
		public int length() {
			return m_data.length;
		}
		
		@Override
		public int hashCode() {
			int h = 0;
			for (Object o : m_data)
				h += o.hashCode();
			return h;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Tuple))
				return false;
			
			Tuple t = (Tuple)o;
			
			if (t.length() != this.length())
				return false;
			
			for (int i = 0; i < m_data.length; i++) {
				if (!m_data[i].equals(t.getData()[i]))
					return false;
			}
			
			return true;
		}
		
		@Override
		public String toString() {
			String s = "(";
			String comma = "";
			for (Object o : m_data) {
				s += comma + o;
				comma = ",";
			}
			return s + ")";
		}
	}
	
	public static DataSet hashJoin(String[] joinVars, DataSet left, DataSet right) {

		if (joinVars == null || joinVars.length == 0 || left == null || right == null) return null;
		// always use the smaller set to generate the hash
		if (left.size() < right.size()) {
			DataSet t = left;
			left = right;
			right = t;
		}
		
		Map<Tuple,List<Row>> t2r = new HashMap<Tuple,List<Row>>();
		
		for (Iterator<Row> i = right.iterator(); i.hasNext(); ) {
			Row r = i.next();
			Tuple t = new Tuple(r.get(joinVars));
			
			List<Row> rl = t2r.get(t);
			if (rl == null) {
				rl = new ArrayList<Row>();
				t2r.put(t, rl);
			}
			rl.add(r);
		}
		
		Set<String> joinVarsSet = new HashSet<String>(Arrays.asList(joinVars));
		List<String> joinedVarsList = new ArrayList<String>();
		List<String> leftVarsList = new ArrayList<String>();
		List<String> rightVarsList = new ArrayList<String>();
		
		for (String v : left.getVars())
			if (!joinVarsSet.contains(v))
				leftVarsList.add(v);
		
		for (String v : right.getVars())
			if (!joinVarsSet.contains(v))
				rightVarsList.add(v);

		joinedVarsList.addAll(leftVarsList);
		joinedVarsList.addAll(rightVarsList);
		joinedVarsList.addAll(Arrays.asList(joinVars));
		
		String[] joinedVars = joinedVarsList.toArray(new String[] {});
		String[] leftVars = leftVarsList.toArray(new String[] {});
		String[] rightVars = rightVarsList.toArray(new String[] {});
		
		DataSet result = new DataSet(joinedVars);
		
		for (Iterator<Row> i = left.iterator(); i.hasNext(); ) {
			Row lr = i.next();
			Tuple lt = new Tuple(lr.get(joinVars));
			
			List<Row> rrs = t2r.get(lt);
			if (rrs != null) {
				for (Row rr : rrs) {
					Object[] newRow = new Object [joinedVars.length];
					System.arraycopy(lr.get(leftVars), 0, newRow, 0, leftVars.length);
					System.arraycopy(rr.get(rightVars), 0, newRow, leftVars.length, rightVars.length);
					System.arraycopy(lt.getData(), 0, newRow, leftVars.length + rightVars.length, joinVars.length);
					result.addRow(new Row(result, newRow));
				}
			}
		}
		
		return result;
	}
	
	public static void printSet(DataSet r) {
		for (String v : r.getVars())
			System.out.print(v + "\t");
		System.out.println();
		for (Iterator<Row> i = r.iterator(); i.hasNext(); ) {
			Row row = i.next();
			for (int j = 0; j < r.getVars().length; j++)
				System.out.print(row.get(j) + "\t");
			System.out.println();
		}
	}
	
	public void test() {
		DataSet rs1 = new DataSet(new String[] {"a", "b", "c" });
		DataSet rs2 = new DataSet(new String[] {"d", "b", "c" });
		
		rs1.addRow(new String[] {"1", "2", "3"});
		rs1.addRow(new String[] {"3", "5", "5"});
		rs1.addRow(new String[] {"1", "3", "2"});
		rs1.addRow(new String[] {"6", "3", "4"});
		rs1.addRow(new String[] {"0", "3", "4"});

		rs2.addRow(new String[] {"2", "3", "2"});
		rs2.addRow(new String[] {"2", "3", "4"});
		rs2.addRow(new String[] {"5", "5", "4"});
		rs2.addRow(new String[] {"1", "3", "3"});
		rs2.addRow(new String[] {"4", "3", "7"});
		
		printSet(rs1);
		printSet(rs2);
		
		DataSet res = Join.hashJoin(new String[] {"c", "b"}, rs1, rs2);
	
		printSet(res);
	}
	
	public static void main(String[] args) {
		Join j = new Join();
		j.test();
	}
}
