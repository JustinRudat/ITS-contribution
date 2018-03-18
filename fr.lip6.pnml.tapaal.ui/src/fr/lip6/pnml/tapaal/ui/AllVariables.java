package fr.lip6.pnml.tapaal.ui;

import java.io.IOException;
import java.util.List;

import fr.lip6.move.gal.Specification;
import orders.OrderBuilder;
import orders.PTGALTransformer;
import fr.lip6.move.pnml.ptnet.PetriNet;
import fr.lip6.pnml.tapaal.ui.handlers.OrderHandler;

public class AllVariables extends OrderHandler {


	@Override
	protected String getServiceName() {		
		return "Contribution";
	}

	@Override
	public void workOnSpec(PetriNet petriNet, String outpath) throws IOException {
		
		PTGALTransformer ptg = new PTGALTransformer();
		ptg.transform(petriNet, outpath);
	}

}
