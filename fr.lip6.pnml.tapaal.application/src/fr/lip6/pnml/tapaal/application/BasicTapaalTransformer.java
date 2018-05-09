package fr.lip6.pnml.tapaal.application;

import java.io.IOException;

import fr.pnml.tapaal.runner.PNMLToTAPN;

public class BasicTapaalTransformer implements PNMLToTapaalTransformer {

	private String cwd;
	private String formulaPath;

	@Override
	public void setWorkFolder(String path) {
		this.cwd = path;
	}

	@Override
	public boolean loadTransformPNML(String modelPath, String formulaPath)  throws IOException {
        // Exporting the file path_pnml to file_model using our Parser/Writer PNMLToTAPN for tapaal		
        PNMLToTAPN exporter = new PNMLToTAPN(modelPath,cwd+"/model.pnml",null);       
        exporter.toTAPN();
        // Queries are not translated in this version !
        this.formulaPath = formulaPath;
        return false;
	}


	@Override
	public String getPathToTapaalNet() {
		return cwd+"/model.pnml";
	}

	@Override
	public String getPathToTapaalQuery() {
		return formulaPath;
	}



}
