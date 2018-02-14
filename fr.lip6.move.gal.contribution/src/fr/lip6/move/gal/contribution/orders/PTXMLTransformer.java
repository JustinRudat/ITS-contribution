package fr.lip6.move.gal.contribution.orders;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import fr.lip6.move.pnml.ptnet.PTMarking;
import fr.lip6.move.pnml.ptnet.Page;
import fr.lip6.move.pnml.ptnet.Arc;
import fr.lip6.move.pnml.ptnet.PetriNet;
import fr.lip6.move.pnml.ptnet.Place;
import fr.lip6.move.pnml.ptnet.PnObject;
import fr.lip6.move.pnml.ptnet.Transition;


public class PTXMLTransformer {
	
	private static Logger getLog() {
		return Logger.getLogger("fr.lip6.move.gal");
	}

	public void transform(PetriNet petriNet, String path) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new File(path));			
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
			pw.println("<pnml xmlns=\"http://www.informatik.hu-berlin.de/top/pnml/ptNetb\">");
			pw.println(" <net active=\"true\" id=\""+petriNet.getName()+"\" type=\"P/T net\">");
			for (Page p : petriNet.getPages()) {
				handlePage(p, pw);
			}
			pw.println("  <k-bound bound=\"3\"/>");
			pw.println(" </net>");
			pw.println("</pnml>");
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}


	public static String normalizeName(String text) {
		String res = text.replace(' ', '_');
		res = res.replace('-', '_');
		res = res.replace('/', '_');
		res = res.replace('*', 'x');
		res = res.replace('=', '_');
		
		return res;
	}



	private void handlePage(Page page, PrintWriter pw) {
		int nbplace = 0;
		for (PnObject n : page.getObjects()) {
			if (n instanceof Place) {
				Place p = (Place) n;
				String name = normalizeName(p.getId());
				if (p.getName() != null) {
					name =normalizeName(p.getName().getText());
				}
				pw.println("  <place displayName=\"true\" id=\""+normalizeName(p.getId())+"\" initialMarking=\""+interpretMarking(p.getInitialMarking())+"\" invariant=\"&lt; inf\" "
						+ "markingOffsetX=\"0.0\" markingOffsetY=\"0.0\" name=\""+name+"\" "
								+ "nameOffsetX=\"0.0\" nameOffsetY=\"-10.0\" positionX=\"100.0\" "
										+ "positionY=\"100.0\"/>");
				nbplace++;
			}
		}

		
		getLog().info("Transformed "+ nbplace  + " places.");
		for (PnObject pnobj : page.getObjects()) {
			if (pnobj instanceof Transition) {
				Transition t = (Transition) pnobj;
				String name = normalizeName(t.getId());
				if (t.getName() != null) {
					name =normalizeName(t.getName().getText());
				}
				String tmp = "  <transition angle=\"0\" displayName=\"true\" id=\""+normalizeName(t.getId())+"\" infiniteServer=\"false\" "
						+ "name=\""+name +"\" nameOffsetX=\"0.0\" nameOffsetY=\"-10.0\" positionX=\"100.0\" "
						+ "positionY=\"100.0\" priority=\"0\" urgent=\"false\"/>";
				pw.println(tmp);
			}
				
			
		}
		for (PnObject pnobj : page.getObjects()) {
			if (pnobj instanceof Transition) {
				Transition t = (Transition) pnobj;
				
				for (Arc arc : t.getInArcs()) {
					printArc(pw, arc);
				}
				for (Arc arc : t.getOutArcs()) {
					printArc(pw, arc);
				}
				
			}
		}
		

	}

	private void printArc(PrintWriter pw, Arc arc) {
		int value = 1;
		if (arc.getInscription() != null
				&& arc.getInscription().getText() != null) {
			value = Math.toIntExact(arc.getInscription().getText());
		}
		pw.println("  <arc id=\""+normalizeName(arc.getId())+"\" inscription=\""+value+"\" source=\""+normalizeName(arc.getSource().getId())+"\" target=\""+normalizeName(arc.getTarget().getId())+"\" "
				+ "type=\"normal\" weight=\"1\">");
		
		// inflexion points
		pw.println("   <arcpath arcPointType=\"false\" id=\"0\" xCoord=\"106\" yCoord=\"117\"/>");
		pw.println("   <arcpath arcPointType=\"false\" id=\"1\" xCoord=\"101\" yCoord=\"122\"/>");
		
    	pw.println("  </arc>");
	}

	private int interpretMarking(PTMarking ptMarking) {
		if (ptMarking == null || ptMarking.getText() == null) {
			return 0;
		}
		return Math.toIntExact(ptMarking.getText());
	}

}
