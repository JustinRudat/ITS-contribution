package fr.pnml.tapaal.runner;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import fr.lip6.move.gal.nupn.NotAPTException;
import fr.lip6.move.gal.nupn.PTNetReader;
import fr.lip6.move.pnml.ptnet.Arc;
import fr.lip6.move.pnml.ptnet.PTMarking;
import fr.lip6.move.pnml.ptnet.Page;
import fr.lip6.move.pnml.ptnet.PetriNet;
import fr.lip6.move.pnml.ptnet.Place;
import fr.lip6.move.pnml.ptnet.PnObject;
import fr.lip6.move.pnml.ptnet.Transition;

public class PNMLToTAPN {

    public PetriNet net;
    public String net_name;
    public ArrayList<Arc> input_arcs;
    public ArrayList<Arc> output_arcs;
    public PrintWriter pw;
    public FileWriter fw;
    public List<String> orders;


    public PNMLToTAPN(String path,String newpath, List<String> orders) throws IOException {
        
        this.orders=orders;                                 // a list of orders calculated with a specific heuristic
        File file = new File(path);                         // linking from the pnml file path
        File file_tmp = new File(newpath);                  // linking the temporary file for the verifypn64 engine
        this.input_arcs = new ArrayList<>();                // initializing differents arraylist
        this.output_arcs = new ArrayList<>();               //
        try {

            this.pw = new PrintWriter(file_tmp);            // creating a prinwriter to write on the temporary file
        } catch (IOException e) {
            System.err.println("I/O Error while initializing a PNMLToTAPN instance.");
        }
        PTNetReader ptreader = new PTNetReader();           // creation of a Parser/Writer for PetriNet
        PetriNet net = null;
        try {
                                                            // retrieving of the net from the pnml file
            this.net = ptreader.loadFromXML(new BufferedInputStream(new FileInputStream(file)));
            
        } catch (NotAPTException | FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("could not parse PNML file :"+file.getCanonicalPath());
            throw new IOException(e);
        }
        
    }
    
    public PNMLToTAPN(PetriNet net,String newpath, List<String> orders) throws IOException {
        
        this.orders=orders;                                 // a list of orders calculated with a specific heuristic
        File file_tmp = new File(newpath);                  // linking the temporary file for the verifypn64 engine
        this.input_arcs = new ArrayList<>();                // initializing differents arraylist
        this.output_arcs = new ArrayList<>();               //
        try {

            this.pw = new PrintWriter(file_tmp);            // creating a prinwriter to write on the temporary file
        } catch (IOException e) {
            System.err.println("I/O Error while initializing a PNMLToTAPN instance.");
        }
        this.net = net;
        
    }

    // Converting a pnml file into a temporary xml file structured as a pnml for the TAPAAL engine
    public void toTAPN(){
        if(null==net){
            System.err.println("Net is null.\n");
            return;
        }
        this.net_name = net.getName().getText().toString().replace("-","_dash_") ;              // normalizing the net name and id with TAPAAL's norm
        pw.println("<pnml>");                                                                   // and defining name of the net and the file's header 
        pw.println("<net id=\"" + net_name + "\" type=\"P/T net\">"); 

        for (Page p : net.getPages()) {                                                         // Loop handling the net's pages
            handlePage(p,pw);
        }
        
        // the input and output arc are stored now and can be dealt with to respect TAPAAL's format
        for (Arc a : input_arcs){                                                               // Dealing with input arcs
            pw.print("<inputArc ");
            pw.print("source=\"" + net_name +"_"+a.getSource().getId().toString() + "\" ");
            pw.print("target=\"" + net_name +"_"+a.getTarget().getId().toString() + "\" >");
            pw.println("<inscription><value>1</value></inscription></inputArc>");
        }
        for (Arc a : output_arcs){                                                              // Dealing with output arcs
            pw.print("<outputArc ");
            pw.print("source=\"" + net_name +"_"+a.getSource().getId().toString() + "\" ");
            pw.print("target=\"" + net_name +"_"+a.getTarget().getId().toString() + "\" >");
            pw.println("<inscription><value>1</value></inscription></outputArc>");
        }
        pw.println("</net>");                                                                   // writing the eng of the file
        pw.println("</pnml>");  
        pw.flush();                                                                             
    }
    
    
    // PageHandler based on Lip6 GAL's contribution adapted to use orders and for TAPAAL engine
    public void handlePage(Page page, PrintWriter pw){
        ArrayList<Place> places_array = new ArrayList<>();                                      // new list of places
        ArrayList<Transition> transitions = new ArrayList<>();                                  // new list of transition
        for (PnObject n : page.getObjects()) {                                                  // for every object in the net's pages
            if (n instanceof Place) {                                                           // we deal with each different classes :
                Place p = (Place) n;                                                            // adding place to an array to be dealt with later
                places_array.add(p);

            }
            if (n instanceof Transition) {                                                      // adding transition to an array to be dealt with later
                Transition t = (Transition) n;
                transitions.add(t);

                for(Arc a : ((Transition) n).getInArcs()){                                      // filling up the input arcs array
                    input_arcs.add(a);
                }
                for(Arc b : ((Transition) n).getOutArcs()){                                     // filling up the output arcs array
                    output_arcs.add(b);
                }

            }
        }

        handleOrder(places_array);                                                              // handling the place's writing with orders

        if(!transitions.isEmpty()) {                                                            // handling the transition's writing
            for(Transition t: transitions) {
                pw.print("<transition ");
                pw.print("id=\"" + net_name+"_"+t.getId() + "\" ");
                pw.print("name=\"" + net_name+"_"+t.getId() + "\" ");
                pw.print("urgent=\"false\"/>");
            }
        }
    }
    
    
    //TODO not tested yet, expect for orders -> null
    // Handler for rewriting the temporary xml file 
    public void handleOrder(ArrayList<Place> places) {
        if(null==orders||orders.isEmpty()) {                                                        // If there is not specific order defined
            for(Place p : places) {                                                                 // handle the order the original 
                pw.print("<place ");                                                                // as the original file was made
                pw.print("id=\"" + net_name+"_"+p.getId() + "\" ");
                pw.print("name=\"" + net_name+"_"+p.getId() + "\" ");
                pw.print("invariant=\"&lt; inf\" ");
                pw.print("initialMarking=\""+interpretMarking(p.getInitialMarking())+"\"");
                pw.println(" />");
            }
        }else {                                                                                    // Else ...!
            while(!orders.isEmpty()) {                                                             // While we didn't check through all the orders 
                for(Place place : places) {                                                        // in the list then :
                    if(orders.get(0).equals(place.getName())) {                                    // We check in the net's places tab which places 
                        pw.print("<place ");                                                       // come's next and write it in the file
                        pw.print("id=\"" +net_name+"_"+ place.getId() + "\" ");
                        pw.print("name=\"" + net_name+"_"+place.getId() + "\" ");
                        pw.print("invariant=\"&lt; inf\" ");
                        pw.print("initialMarking=\""+place.getInitialMarking().getText()+"\"");
                        pw.println(" />");
                        orders.remove(0);                                                          // Removing the order of the place we dealt with
                    }
                }
            }
        }
    }

	private int interpretMarking(PTMarking ptMarking) {
		if (ptMarking == null || ptMarking.getText() == null) {
			return 0;
		}
		return Math.toIntExact(ptMarking.getText());
	}
		// Maybe not usefull anymore since we are using the MCC queries
//	public void writeQuery(File query_file) {
//	    try {
//	        
//            PrintWriter pw_tmp = new PrintWriter(query_file);
//            pw_tmp.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
//                    "<property-set xmlns=\"http://tapaal.net/\">");
//            pw_tmp.println("<property>");
//            pw_tmp.println("    <id></id>");
//            pw_tmp.println("    <description></description>");
//            pw_tmp.println("    <formula>");
//            queryFormulaPrinter(pw_tmp);
//            pw_tmp.println("    </formula>");
//            pw_tmp.println("</property>");
//            pw_tmp.print("</property-set>");
//            
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//	}

	
}