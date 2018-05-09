package fr.lip6.pnml.tapaal.application;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.lip6.move.gal.Specification;
import fr.lip6.move.gal.instantiate.GALRewriter;
import fr.lip6.move.gal.pnml.togal.PnmlToGalTransformer;
import fr.lip6.move.gal.semantics.IDeterministicNextBuilder;
import fr.lip6.move.gal.semantics.INextBuilder;
import fr.lip6.move.gal.structural.NoDeadlockExists;
import fr.lip6.move.gal.structural.StructuralReduction;

public class GalTapaalTransformer implements PNMLToTapaalTransformer {

	private String cwd;
	private boolean doRed;
	private String pn_file;
	private String formulaPath;

	public GalTapaalTransformer(boolean doRed) {
		this.doRed = doRed;
	}

	@Override
	public void setWorkFolder(String path) {
		this.cwd = path;
	}

	@Override
	public boolean loadTransformPNML(String modelPath, String formulaPath) throws IOException {
		this.formulaPath = formulaPath;
		PnmlToGalTransformer trans = new PnmlToGalTransformer();   // creation of a gal transformer setting up the Specification 
		Specification spec = trans.transform(new File(modelPath).toURI());	       // we will built a StructuralReduction out of.
		if (spec.getMain() == null) {
			spec.setMain(spec.getTypes().get(spec.getTypes().size()-1));
		}
		GALRewriter.flatten(spec, true);

		// building StructuralReduction instance from the Specification
		INextBuilder nb = INextBuilder.build(spec);
		IDeterministicNextBuilder idnb = IDeterministicNextBuilder.build(nb);

		StructuralReduction sr = new StructuralReduction(idnb);

			if(doRed) { // perform reduction

				String formulaName;
				try {
					formulaName = getFormulaName(formulaPath);			
				} catch (Exception e) {
					e.printStackTrace();
					throw new IOException(e);
				}
				try {
					sr.reduce();
				} catch (NoDeadlockExists e) {
					System.out.println( "FORMULA " + formulaName  + " FALSE TECHNIQUES TOPOLOGICAL STRUCTURAL_REDUCTION");
					return true;
				}
				if (sr.getTnames().isEmpty()) {
					System.out.println( "FORMULA " + formulaName  + " TRUE TECHNIQUES TOPOLOGICAL STRUCTURAL_REDUCTION");
					return true;
				}
			}				
			String targetModel = cwd+"/model.pnml";
			pn_file = TapaalBuilder.buildTapaal(sr.getFlowPT(), sr.getFlowTP(), sr.getPnames(), sr.getTnames(),sr.getMarks(),targetModel).getCanonicalPath();
			return false;
	}

	private String getFormulaName(String formulaPath) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc = factory.newDocumentBuilder();
		File examff = new File(formulaPath);
		Document xml = doc.parse(examff);
		NodeList id_list = xml.getElementsByTagName("id");
		Node elm = id_list.item(0);

		return elm.getTextContent();		
	}

	@Override
	public String getPathToTapaalNet() {
		return pn_file;
	}

	@Override
	public String getPathToTapaalQuery() {
		return formulaPath;
	}

}
