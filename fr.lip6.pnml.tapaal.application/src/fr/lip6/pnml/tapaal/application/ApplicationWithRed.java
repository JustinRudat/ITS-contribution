package fr.lip6.pnml.tapaal.application;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.lip6.move.gal.Specification;
import fr.lip6.move.gal.pnml.togal.PnmlToGalTransformer;
import fr.lip6.move.gal.semantics.IDeterministicNextBuilder;
import fr.lip6.move.gal.semantics.INextBuilder;
import fr.lip6.move.gal.structural.NoDeadlockExists;
import fr.lip6.move.gal.structural.StructuralReduction;
import fr.pnml.tapaal.runner.PNMLToTAPN;
import fr.pnml.tapaal.runner.VerifyWithProcess;
import fr.lip6.move.pnml.ptnet.PetriNet;

public class ApplicationWithRed implements IApplication {
	private static final String APPARGS = "application.args";
	private static final String INPUT_FILE = "-i"; 
	private static final String QUERY_FILE = "-q";
	private static final String TAPAAL_PATH = "-tapaalpath";
	private static final String ORDER_PATH = "-order";
	private static final String EXAMINATION = "-examination";
	private static final String REDUCTION= "-red";
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		String [] args = (String[]) context.getArguments().get(APPARGS);

		String inputff = null;
		String orderff = null;
		String tapaalff = null;
		String queryff = null;
		String exam = null;
		
		boolean doRed = false;
		// retrieving the arguments
		for (int i=0; i < args.length ; i++) {
			if (INPUT_FILE.equals(args[i])) {
				inputff = args[++i];
			} else if (TAPAAL_PATH.equals(args[i])) {
				tapaalff = args[++i];
			} else if (ORDER_PATH.equals(args[i])) {
				orderff = args[++i];
			} else if (EXAMINATION.equals(args[i])) {
				exam = args[++i];
			} else if (QUERY_FILE.equals(args[i])) {
                queryff = args[++i];
            } else if(REDUCTION.equals(args[i])){
                doRed = true;
            }else {
				System.err.println("Unrecognized argument :" + args[i]);
			}
		}
		
		if (inputff == null) {
			System.err.println("Please provide input file with -i option");
			return null;
		}

		File ff = new File(inputff);
		if (! ff.exists()) {
			System.err.println("Input file "+inputff +" does not exist");
			return null;
		}
		String pwd = ff.getParent();
		
		String modelName = ff.getName().replace(".pnml", "");

		PetriNet pn=null;
		String examName = ff.getName().replace("model.pnml", exam+".xml");
		PNMLToTAPN totapn=null;
		// load PNML to Specification
		if (ff != null && ff.exists()) {
			System.out.println("Parsing pnml file : " + ff.getAbsolutePath());

			PnmlToGalTransformer trans = new PnmlToGalTransformer();
			Specification spec = trans.transform(ff.toURI());	
			
			if (spec.getMain() == null) {
				spec.setMain(spec.getTypes().get(spec.getTypes().size()-1));
			}
			
			// Perform reductions
			INextBuilder nb = INextBuilder.build(spec);
			IDeterministicNextBuilder idnb = IDeterministicNextBuilder.build(nb);			
			StructuralReduction sr = new StructuralReduction(idnb);
			
			try {
			    if(doRed) { // do the reduction
			        sr.reduce();
    				if (sr.getTnames().isEmpty()) {
    					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    					DocumentBuilder doc = factory.newDocumentBuilder();
    					File examff = new File(examName);
    					Document xml = doc.parse(examff);
    					NodeList id_list = xml.getElementsByTagName("id");
    					Node elm = id_list.item(0);
    					
    					String formulaname = elm.getTextContent();
    					
    					System.out.println( "FORMULA " + formulaname  + " TRUE TECHNIQUES TOPOLOGICAL STRUCTURAL_REDUCTION");
    					return null;
    				}
    				Specification reduced = sr.rebuildSpecification();
			    }
				// export to PetriNet for Tapaal
				pn =  TapaalBuilder.buildTapaal(sr.getFlowPT(), sr.getFlowTP(), sr.getPnames(), sr.getTnames(),sr.getMarks());
				
				
			} catch (NoDeadlockExists e) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder doc = factory.newDocumentBuilder();
				File examff = new File(examName);
				Document xml = doc.parse(examff);
				NodeList id_list = xml.getElementsByTagName("id");
				Node elm = id_list.item(0);
				
				String formulaname = elm.getTextContent();// was : reader.getSpec().getProperties().get(0).getName()
				System.out.println( "FORMULA " + formulaname  + " FALSE TECHNIQUES TOPOLOGICAL STRUCTURAL_REDUCTION");
				return null;
			}
			
			
		} else {
			throw new IOException("Cannot open file "+ff.getAbsolutePath());
		}
		
		long time = System.currentTimeMillis();
		
		System.out.println("Successfully read input file : " + inputff +" in " + (time - System.currentTimeMillis()) + " ms.");
		
		String cwd = pwd + "/work";
		File fcwd = new File(cwd);
		if (! fcwd.exists()) {
			if (! fcwd.mkdir()) {
				System.err.println("Could not set up work folder in "+cwd);
			}
		}
		
		queryff = inputff.replace("model.pnml", "");
		queryff += exam +".xml";
		
		// invoke Tapaal
		VerifyWithProcess vwp = new VerifyWithProcess(null);
		if(pn!=null){
		    vwp.doVerify(pn, tapaalff, queryff); //appel à toTapn() in the doVerify method
		}else{
			System.err.println("Parameter PetriNet is currently null\n");
		}
		time = System.currentTimeMillis();
		
		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
	}
}
