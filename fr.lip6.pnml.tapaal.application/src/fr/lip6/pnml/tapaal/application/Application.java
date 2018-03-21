package fr.lip6.pnml.tapaal.application;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		String [] args = (String[]) context.getArguments().get(APPARGS);

		String inputff = null;
		String orderff = null;
		String tapaalff = null;
		String exam = null;
		
		for (int i=0; i < args.length ; i++) {
			if (INPUT_FILE.equals(args[i])) {
				inputff = args[++i];
			} else if (TAPAAL_PATH.equals(args[i])) {
				tapaalff = args[++i]; 
			} else if (ORDER_PATH.equals(args[i])) {
				orderff = args[++i]; 
			} else if (EXAMINATION.equals(args[i])) {
				exam = args[++i]; 
			} else {
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
		
		long time = System.currentTimeMillis();
		
		System.out.println("Successfully read input file : " + inputff +" in " + (time - System.currentTimeMillis()) + " ms.");
		
		String cwd = pwd + "/work";
		File fcwd = new File(cwd);
		if (! fcwd.exists()) {
			if (! fcwd.mkdir()) {
				System.err.println("Could not set up work folder in "+cwd);
			}
		}
		
		File queryFile = Files.createTempFile("query", "q").toFile();
		PrintWriter pw = new PrintWriter(queryFile);
		
		if ("ReachabilityDeadlock".equals(exam)) {
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" + 
			        "<property-set xmlns=\"http://tapaal.net/\">\n" + 
			        "<property>\n" + 
			        "    <id>DeadlockTesting</id>\n" + 
			        "    <description>testing the existance of a deadlock in the model</description>\n" + 
			        "    <formula>\n" + 
			        "      <exists-path>\n" + 
			        "        <finally>\n" + 
			        "          <deadlock/>\n" + 
			        "        </finally>\n" + 
			        "      </exists-path>\n" + 
			        "    </formula>\n" + 
			        "  </property>\n" + 
			        "</property-set>");
			pw.flush();
			pw.close();
		}
		
		
		VerifyWithProcess vwp = new VerifyWithProcess(null);
		vwp.doVerify(inputff, tapaalff, queryFile.getCanonicalPath());
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
