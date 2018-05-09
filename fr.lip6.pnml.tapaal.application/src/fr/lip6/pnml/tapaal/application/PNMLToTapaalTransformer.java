package fr.lip6.pnml.tapaal.application;

import java.io.IOException;

public interface PNMLToTapaalTransformer {
	/** Folder where output files will be produced. */
	public void setWorkFolder(String path);
	/**
	 * Load (parse) the input PNML file, which MUST be P/T not COL.
	 * If load was successful, create Tapaal format files for both Petri net and queries.
	 * @param modelPath absolute path to PNML file
	 * @param formulaPath absolute path to Model Checking Contest format XML property file	
	 * @return true if the problem is now solved, false if we need to verify more.
	 * @exception UnsupportedOp if a COL Petri net is passed. 
	 * @exception IOEx if problems with work folder write
	 */
	public boolean loadTransformPNML(String modelPath, String formulaPath) throws IOException;
	/** 
	 * Path to a Tapaal format net representing the original net.
	 * @return a path to a file, in current work folder.
	 */
	public String getPathToTapaalNet();
	/** 
	 * Path to a Tapaal/PNMCC format net representing the queries.
	 * @return a path to a file, in current work folder.
	 */
	public String getPathToTapaalQuery();
}
