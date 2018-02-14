package fr.lip6.move.gal.contribution.ui;

import java.io.IOException;


import fr.lip6.move.gal.contribution.orders.PTXMLTransformer;
import fr.lip6.move.gal.contribution.ui.handlers.OrderHandler;
import fr.lip6.move.pnml.ptnet.PetriNet;

public class AllVariables extends OrderHandler {


	@Override
	protected String getServiceName() {		
		return "Contribution";
	}

	@Override
	public void workOnSpec(PetriNet petriNet, String outpath) throws IOException {
		PTXMLTransformer ptx = new PTXMLTransformer();
		ptx.transform(petriNet, outpath);
	}

}
