package fr.lip6.pnml.tapaal.application;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import android.util.SparseIntArray;
import fr.lip6.move.gal.util.MatrixCol;

public class TapaalBuilder {

	public static File buildTapaal(MatrixCol flowPT, MatrixCol flowTP, List<String> pnames, List<String> tnames,
			List<Integer> marks,String pt_name) throws IOException {
		List<SparseIntArray> col_pt = flowPT.getColumns();
		List<SparseIntArray> col_tp = flowTP.getColumns();


		File xml_ptnet = new File(pt_name);
		PrintWriter pw = new PrintWriter(xml_ptnet);
		pw.print("<pnml>\n" + 
				"<net id=\""+pt_name+"\" type=\"P/T net\">\n");            
		
		int pi = 0;
		for(String pname : pnames) {
			Integer marking = marks.get(pi);
			pw.print("<place id=\""+pname+"\" name=\""+pname+"\" invariant=\"&lt; inf\" initialMarking=\""+marking.intValue()+"\" />\n");
			pi++;
		}

		for(String transition : tnames) {
			pw.print("<transition id=\""+transition+"\" name=\""+transition+"\" urgent=\"false\"/>\n");
		}

		for (int tindex = 0 ; tindex  < col_pt.size() ; tindex++) {
			SparseIntArray sparseArray = col_pt.get(tindex);
			for(int i=0;i<sparseArray.size();i++) {
				
				int pindex = sparseArray.keyAt(i);
				int tokens = sparseArray.valueAt(i);
				
				pw.print("<inputArc source=\""+pnames.get(pindex)+"\" target=\""+tnames.get(tindex)+"\"><inscription><value>"+tokens+"</value></inscription></inputArc>\n");
			}			
		}
		for (int tindex = 0 ; tindex  < col_tp.size() ; tindex++) {
			SparseIntArray sparseArray = col_tp.get(tindex);
			for(int i=0;i<sparseArray.size();i++) {
				
				int pindex = sparseArray.keyAt(i);
				int tokens = sparseArray.valueAt(i);
				
				pw.print("<outputArc source=\""+tnames.get(tindex)+"\" target=\""+pnames.get(pindex)+"\"><inscription><value>"+tokens+"</value></inscription></inputArc>\n");
			}			
		}
		pw.print("</net>\n" + 
				"</pnml>");
		pw.flush();
		pw.close();


		return xml_ptnet;
	}

}
