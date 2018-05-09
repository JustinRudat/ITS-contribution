package fr.pnml.tapaal.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeoutException;

import fr.lip6.move.gal.process.CommandLine;
import fr.lip6.move.gal.process.Runner;

public class VerifyWithProcess {
	
	/**
	 * Path to Tapaal's verifyPN binary executable.
	 */
    private String verifypath;

	public VerifyWithProcess(String verifypath) {
		this.verifypath = verifypath;
	}

	public void doVerify(String file_model, String file_query, List<String> orders) {
        // Creating the file names we need 
        try {// Creating a temporary file for the verifypn64 program
            /// temporary file is a .xml version of the .pnml chosen, for tapaal engine 

            // Exporting the file path_pnml to file_model using our Parser/Writer PNMLToTAPN for tapaal

            // defining query file path and options            
            String options = "-k 0 -s BestFS -r 1 -q 0 -ctl czero -x 1";
            String arguments = options+" "+file_model+" "+file_query;
            String commandLine = verifypath+" "+arguments;

            // creating command line for the runner
            CommandLine cl = new CommandLine();
            for(String str : commandLine.split(" ")) {
                cl.addArg(str);
            }
            File tempo_file =  File.createTempFile("temporary",".result");           

            long timeout = 300000;
            
            boolean errToOut = false;
            double timestamp = System.currentTimeMillis();
            System.out.println("Launching runner ...");
            
            Runner.runTool(timeout, cl, tempo_file, true);
            
            Files.lines(tempo_file.toPath()).forEach(line -> System.out.println(line));
            
            // displaying the memory consumption of the runtime environment
            System.gc();
            Runtime rt = Runtime.getRuntime();
            long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
            String str_tmp ="Memory usage : "+usedMB+"Mb";
            System.out.println(str_tmp);            
            printACSV(usedMB,System.currentTimeMillis()- timestamp, new File(file_model).getParent());
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
	}
    
//    public void doVerify(PetriNet net, String verifypath, String file_query) {
//        // Creating the file names we need 
//       
//
//        File file_tmp = null;
//        try {// Creating a temporary file for the verifypn64 program
//            /// temporary file is a .xml version of the .pnml chosen, for tapaal engine 
//
//            file_tmp = File.createTempFile(net.getName().getText(), ".xml");
//            String file_model = file_tmp.getAbsolutePath();
//
//            // Exporting the file path_pnml to file_model using our Parser/Writer PNMLToTAPN for tapaal
//            PNMLToTAPN exporter = new PNMLToTAPN(net,file_model,null);
//            exporter.toTAPN();
//
//            // defining query file path and options            
//            String options = "-k 0 -s BestFS -r 1 -q 0 -ctl czero -x 1";
//
//            String arguments = options+" "+file_model+" "+file_query;
//
//            String commandLine = verifypath+" "+arguments;
//            String[] commands = commandLine.split(" ");
//
//            // creating command line for the runner
//            CommandLine cl = new CommandLine();
//            for(String str : commands) {
//                cl.addArg(str);
//            }
//            File tempo_file =  File.createTempFile("temporary",".result");           
//
//            long timeout = 300000;
//            
//            boolean errToOut = false;
//            double timestamp = System.currentTimeMillis();
//            System.out.println("Launching runner ...");
//            
//            if(Runner.runTool(timeout, cl, tempo_file, true)!=0){
//            	System.err.println("Error while running the tool\n");
//            	if(null!=file_tmp) { // If the program run without error, delete the temporary file before exiting
//                    file_tmp.delete();
//                }
//            	return;
//            }
//            
//            Files.lines(tempo_file.toPath()).forEach(line -> System.out.println(line));
//            
//            // displaying the memory consumption of the runtime environment
//            System.gc();
//            Runtime rt = Runtime.getRuntime();
//            long usedMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
//            String str_tmp ="Memory usage : "+usedMB+"Mb";
//            System.out.println(str_tmp);            
//
//            printACSV(usedMB,System.currentTimeMillis()- timestamp, net.getName().getText());
//            
//        } catch (IOException | TimeoutException | InterruptedException e) {
//            e.printStackTrace();
//        }finally {
//            if(null!=file_tmp) { // If the program run without error, delete the temporary file before exiting
//                file_tmp.delete();
//            }
//        }
//    }
    
    
    
    
    //creer et ecrit un fichier result.csv
    private void printACSV(long memory,double time,String name){
    	String filename = "result.csv";
    	
    	File file = new File(filename);
    	if(!file.exists()){
    		try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	try {
			PrintWriter pw = new PrintWriter(file);
			pw.print(name+";"+memory+";"+time+"\n");
			pw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    }
}
