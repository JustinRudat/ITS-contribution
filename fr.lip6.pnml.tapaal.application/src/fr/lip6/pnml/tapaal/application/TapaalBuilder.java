package fr.lip6.pnml.tapaal.application;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import android.util.SparseIntArray;
import fr.lip6.move.gal.nupn.PTNetReader;
import fr.lip6.move.gal.util.MatrixCol;
import fr.lip6.move.pnml.ptnet.PetriNet;
import fr.lip6.move.pnml.ptnet.Place;
import fr.lip6.move.pnml.ptnet.impl.PetriNetImpl;

public class TapaalBuilder {

	public static File buildTapaal(MatrixCol flowPT, MatrixCol flowTP, List<String> pnames, List<String> tnames,
			List<Integer> marks,String pt_name) {
	    PetriNet retour = null;
	    File xml_ptnet=null;
	    try {
	        List<SparseIntArray> col_pt = flowPT.getColumns();
	        List<SparseIntArray> col_tp = flowTP.getColumns();
	    
	    
            xml_ptnet = new File(pt_name+".pnml");
            if(!xml_ptnet.exists()) {
                xml_ptnet.createNewFile();
            }
            ArrayList<Place> places = new ArrayList<Place>();
            PrintWriter pw = new PrintWriter(xml_ptnet);
            pw.print("<pnml>\n" + 
                    "<net id=\""+pt_name+"\" type=\"P/T net\">\n");            
            for(String pname : pnames) {
                int i = pnames.indexOf(pname);
                Integer marking = marks.get(i);
                pw.print("<place id=\""+pname+"\" name=\""+pname+"\" invariant=\"&lt; inf\" initialMarking=\""+marking.intValue()+"\" />\n");
                
            }
            
            for(String transition : tnames) {
                pw.print("<transition id=\""+transition+"\" name=\""+transition+"\" urgent=\"false\"/>\n");
            }
            
            for(SparseIntArray sparseArray : col_tp) {
                for(int i=0;i<sparseArray.size();i++) {
                    if(sparseArray.get(i)!=0) { // ou >=1
                        int index = col_tp.indexOf(sparseArray);
                        pw.print("<inputArc source=\""+tnames.get(index)+"\" target=\""+pnames.get(i)+"\"><inscription><value>"+sparseArray.get(i)+"</value></inscription></inputArc>\n");
                    }
                }
            }
            for(SparseIntArray sparseArray : col_pt) {
                for(int i=0;i<sparseArray.size();i++) {
                    if(sparseArray.get(i)!=0) {
                        int index = col_pt.indexOf(sparseArray);
                        pw.print("<outputArc source=\""+pnames.get(i)+"\" target=\""+tnames.get(index)+"\"><inscription><value>"+sparseArray.get(i)+"</value></inscription></outputArc>\n");
                    }
                }
            }
            pw.print("</net>\n" + 
                    "</pnml>");
            pw.flush();
            PTNetReader ptreader = new PTNetReader(); 
            retour = ptreader.loadFromXML(new BufferedInputStream(new FileInputStream(xml_ptnet)));
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }   
	    
		return xml_ptnet;
	}

}
