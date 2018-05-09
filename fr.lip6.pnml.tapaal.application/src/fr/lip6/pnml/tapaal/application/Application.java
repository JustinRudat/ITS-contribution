package fr.lip6.pnml.tapaal.application;

import java.io.File;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import fr.pnml.tapaal.runner.VerifyWithProcess;

public class Application implements IApplication {
	private static final String APPARGS = "application.args";
	private static final String INPUT_FILE = "-i"; 
	private static final String QUERY_FILE = "-q";
	private static final String TAPAAL_PATH = "-tapaalpath";
	private static final String ORDER_PATH = "-order";
	private static final String EXAMINATION = "-examination";
	private static final String REDUCTION= "-red";
	private static final String RAWMODE= "-raw";
	
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
		
		// do we do Structural Reduction
		boolean doRed = false;
		// do we use Raw transformation or via Specification
		boolean doRaw = false;
		
		
		// defining the arguments
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
			} else if(RAWMODE.equals(args[i])){
				doRaw = true;
			} else {
				System.err.println("Unrecognized argument :" + args[i]);
			}
		}
		
		if (inputff == null) {
			System.err.println("Please provide input file with -i option");
			return null;
		}
		
		// retrieving the installed input folder
		File ff = new File(inputff);
		
		if (! ff.exists()) {
			System.err.println("Input file "+inputff +" does not exist");
			return null;
		}
		
		
		String pwd = ff.getParent();
		
		String modelName = ff.getName().replace(".pnml", "");
		
		long time = System.currentTimeMillis();
		
		//creating a new workfolder
		String cwd = pwd + "/work";
		File fcwd = new File(cwd);
		if (! fcwd.exists()) {
			if (! fcwd.mkdir()) {
				System.err.println("Could not set up work folder in "+cwd);
			}
		}

		PNMLToTapaalTransformer btt ;
		if (doRaw) {
			btt = new BasicTapaalTransformer();
			if (doRed) {
				System.err.println("Warning : Raw mode is incompatible with -red Structural reductions flag.");
			}
		} else {
			btt = new GalTapaalTransformer(doRed);
		}

		btt.setWorkFolder(fcwd.getCanonicalPath());
		// setting the queryfile path from model path
		queryff = inputff.replace("model.pnml", "");
		queryff += exam +".xml";
				
		if (! btt.loadTransformPNML(inputff, queryff)) {
			System.out.println("Successfully read and converted input file : " + inputff +" to folder "+cwd+" in " + (System.currentTimeMillis()-time) + " ms.");
			time = System.currentTimeMillis();
			
			VerifyWithProcess vwp = new VerifyWithProcess(tapaalff);
			vwp.doVerify(btt.getPathToTapaalNet(),btt.getPathToTapaalQuery(), null);
			
			System.out.println("Successfully verified : " + inputff +" in " + (System.currentTimeMillis()-time) + " ms.");
			
		} else {
			System.out.println("Successfully read and converted input file : " + inputff +" to folder "+cwd+" in " + (System.currentTimeMillis()-time) + " ms.");
			System.out.println("Problem fully solved using structural reductions.");
		}

		
		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
	}
}
