package tvla;

import tvla.core.*;
import tvla.core.generic.AdvancedCoerce;
import tvla.exceptions.*;
import tvla.formulae.*;
import tvla.jeannet.expressions.*;
import tvla.io.*;
import tvla.predicates.*;
import tvla.jeannet.equationSystem.*;
import tvla.jeannet.util.Symbol;
import tvla.jeannet.util.AddUnderline;
import tvla.util.*;
import tvla.analysis.AnalysisStatus;
import java.util.*;
import java.io.*;

import tvla.analysis.Engine;
import tvla.analysis.IntraProcEngine;

/** This class represents a command-line interface for TVLA users.
 * @author Tal Lev-Ami
 * @author Roman Manevich
 * @author Bertrand Jeannet
 */
public class Runner {
    protected static String programName;
    protected static String inputFile;
    protected static String propertyName;
    protected static Collection specificPropertiesFiles = new ArrayList();
    protected static String engineType;

    /** Search path for the pre-processor.
     */
    protected static String searchPath;

    /** Used to supply information about the software version.
     */
    private static final String versionInfo;
    static {
        PropertiesEx props = new PropertiesEx("/tvla/version.properties");
        versionInfo = props.getProperty("version", "Unknown TVLA version");
    }

    /** Main entry point of TVLA.
     * @author Tal Lev-Ami
     */
    public static void main(String args[]) {
        try {
            AnalysisStatus.loadTimer.start(); // The new load timer starts here:
                System.out.println("*** This is the ITVLA-TVLA3 hybrid.");
            System.out.println( AddUnderline.add(versionInfo) );

            // Program properties
            loadProgramProperties(args);
            parseArgs(args);
            initProgramProperties(args);
            printLogHeader(args);

            //AnalysisStatus status = new AnalysisStatus();

            //Engine.activeEngine = new IntraProcEngine(); // XXXJ
            //((IntraProcEngine)Engine.activeEngine).tempMethodSuperInit(); // FIXME
            Engine.activeEngine = new tvla.analysis.equationSystem.EquationSystemEngine();
            Engine.activeEngine.init();

            // Loading TVP
            if (!AnalysisStatus.terse)
                Logger.print("Loading specification ... ");
            
            // The old load timer started here:
            //AnalysisStatus.loadTimer.start();

            int lastSep = programName.lastIndexOf(File.separator);
            if (lastSep >=0)
                searchPath += ";" + args[0].substring(0, lastSep+1);
            tvla.jeannet.language.TVP.parser.configure(programName,
                                               searchPath);

        // Changed by Jason Breck in 2014-04
            //IOFacade.instance().printProgram(EquationGraph.instance);

            ProgramProperties.setProperty("tvla.searchPath", searchPath);

            if (ProgramProperties.getBooleanProperty("tvla.log.addPropertiesToLog", false)) {
                String header = AddUnderline.add("TVLA Properties");
                ProgramProperties.list(Logger.getUnderlyingStream(), header);
            }

            if (!AnalysisStatus.terse)
                Logger.println("done"); // done loading the specification

            /*
            // Commented out by Jason
             
            // Adds Acyclicity Constraintsto the global set of constraints
            {
                Collection newConstraints = ConstraintsMisc.addAcyclicityConstraints();
                Constraints.getInstance().addAll(newConstraints);
            }
            */

            // Performs finite differencing
            //tvla.differencing.Runner.differencing();
                tvla.differencing.Differencing.differencing();
            
            // Do I need to add Engine.activeEngine.prepare(initial)...?

            // Loading TVS
            TVSFactory.getInstance().init();
            if (!AnalysisStatus.terse)
                Logger.print("Reading TVS files ... ");
            List initial1 = tvla.jeannet.language.TVS.parser.readStructures(inputFile);
            // Transform the List of Pair<Symbol,TVSSet> to a map from Symbol to Location
            Map initial = new HashMap(initial1.size());
            for (Iterator i = initial1.iterator(); i.hasNext(); ){
                Pair pair = (Pair)i.next();
                Symbol id = (Symbol)pair.first;
                TVSSet set = (TVSSet)pair.second;
                if (! EquationGraph.instance.graph.vertexSet().contains(id)){
                    throw new UserErrorException("Location " + id + " specified in TVS file does not appear in TVP file");
                }
                Location location = new Location(id, set);
                initial.put(id,location);
            }
            //AnalysisStatus.loadTimer.stop(); // We're now stopping the load timer inside evalDAG
            
            if (!AnalysisStatus.terse)
                Logger.println("done");

            // Analysis
            if (!AnalysisStatus.terse)
                Logger.println("Starting Analysis ...");
            //Map state = Fixpoint.instance.eval(initial);
            Map state = Fixpoint.instance.evalDAG(initial); // Changed by Jason Breck
            if (!AnalysisStatus.terse)
                Logger.println("done"); // done loading the specification

            // Output
            //IOFacade.instance().printAnalysisState(state,EquationGraph.instance.toPrint);
            //System.out.println("FIXME: analysis output is currently disabled.");
            //
            // Attempt to use TVLA3's output code to write our output.
            //   (We will convert from using Bertrand Jeannet's tvla.jeannet.equationSystem.Location
            //      to the TVLA3 type tvla.transitionSystem.Location)
            System.out.println("Preparing output...");
            //TreeSet<tvla.transitionSystem.Location> outputLocations = 
            //      new TreeSet<tvla.transitionSystem.Location>();
            ArrayList<tvla.transitionSystem.Location> outputLocations = 
                  new ArrayList<tvla.transitionSystem.Location>();
            for(Object element : state.entrySet()) {

                Map.Entry entry = (Map.Entry) element;

                String name = ((Symbol) entry.getKey()).toString();

                // Bertrand Jeannet's location type:
                tvla.jeannet.equationSystem.Location locJ = ((Location) entry.getValue());
                // TVLA3 location type:
                tvla.transitionSystem.Location loc3 = new tvla.transitionSystem.Location(name);
                
                loc3.structures = locJ.structures; // hurray for public members!
                
                outputLocations.add(loc3);

                System.err.println("Location " + name + ": " + loc3.structures.size() + " structures");
            }
            IOFacade.instance().printAnalysisState(outputLocations);

            Engine.activeEngine.printAnalysisInfo();
            //if (ProgramProperties.getBooleanProperty("tvla.log.implementationSpecificStatistics", false)) {
            //    TVSFactory.getInstance().printStatistics();
            //    Engine.activeEngine.printAnalysisInfo();
            //}
            
            //      if (EquationGraph.instance.graph != null)
            //  EquationGraph.instance.dump();
            
            //if (AdvancedCoerce.jasonTiming) {
                /*
                // ADDED BY JASON:
                TreeMap<Long, List> backwardsTimeMap = new TreeMap<Long,List>();
                for(Iterator i = AdvancedCoerce.jasonTimer.keySet().iterator(); i.hasNext();) {
                    Object k = i.next();
                    long v = (Long) AdvancedCoerce.jasonTimer.get(k);
                    if (!backwardsTimeMap.containsKey(v)) {
                        backwardsTimeMap.put(v, new ArrayList(1));
                    }
                    backwardsTimeMap.get(v).add(k);
                }
                long cumulative = 0;
                for(Iterator i = backwardsTimeMap.keySet().iterator(); i.hasNext(); ) {
                    Object k = i.next();
                    for(Iterator n = backwardsTimeMap.get(k).iterator(); n.hasNext(); ) {
                        cumulative += (long)(Long)k;
                        System.out.println(n.next() + " : " + ((1.0*(long)(Long)k) / 1000000000.0)  + " : " + (cumulative / 1000000000.0) );
                    }
                }
                // END PART ADDED BY JASON
                */
            //}
        }
        catch (Throwable t) {
            ExceptionHandler.instance().handleException(t);
        }
    }

    /** Informs the user how to use TVLA and what are the available options.
     */
    protected static void usage() {
        System.err.println("Usage: tvla <program name> [input file] [options]");
        System.err.println("Options:");

        System.err.println(" -d                      Turns on debug mode.");
        System.err.println(" -dstep                  Turns on step debug mode (more verbose).");
        System.err.println(" -action [f][c]pu[c]b    Determines the order of operations computed");
        System.err.println("                         by an action. The default is fpucb.");
        System.err.println("                         f - Focus,  c - Coerce, p - Precondition.");
        System.err.println("                         u - Update, b - Blur.");
        System.err.println(" -join [algorithm]       Determines the type of join method to apply.");
        System.err.println("                         rel  - Relational join.");
        System.err.println("                         part - Partial join.");
        System.err.println("                         ind  - Independent attributes (single structure).");
        System.err.println(" -ms <number>            Limits the number of structures.");
        System.err.println(" -mm <number>            Limits the number of messages.");
        System.err.println(" -save {back|ext|all}    Determines which locations store structures.");
        System.err.println("                         back - at every back edge (the default).");
        System.err.println("                         ext  - at every beginning of an extended block.");
        System.err.println("                         all  - at every program location.");
        System.err.println(" -noautomatic            Supresses generation of automatic constraints.");
        System.err.println(" -props <file name>      Can be used to specify a properties file.");
        System.err.println(" -log <file name>        Creates a log file of the execution.");
        System.err.println(" -tvs <file name>        Creates a TVS formatted output.");
        System.err.println(" -dot <file name>        Creates a DOT formatted output.");
        System.err.println(" -D<macro name>[(value)] Defines a C preprocessor macro.");
        System.err.println(" -terse                  Turns off on-line information printouts.");
        System.err.println(" -nowarnings             Causes all warnings to be ignored.");
        System.err.println(" -path <directory path>  Can be used to specify a search path.");
        System.exit(0);
    }

    /** Parses the arguments passed from the command-line.
     */
    static protected void parseArgs(String args[]) throws Exception {
        int i = 0;

        // Detect calls to the diff utility
        //if (args.length >=1 && args[0].equals("-diff")) {
        //   String newArgs [] = new String[args.length-1];
        //  System.arraycopy(args, 1, newArgs, 0, args.length-1);
        //  tvla.diffUtility.Runner.main(newArgs);
        //  System.exit(0);
        //}

        // help options
        if (args.length == 0 ||
            ( // args.length >= 1
             args[i].equals("-h") || args[i].equals("-help") ||
             args[i].equals("?") || args[i].equals("/?"))
            )
            usage();

        if (args[0].charAt(0) == '-') {
            System.err.println("Error: first argument should be the program name" +
                               "(saw " + args[0] + ")!");
            System.err.println();
            usage();
        }

        programName = args[0];
        propertyName = args[0];
        ProgramProperties.setProperty("tvla.programName", programName);
        ProgramProperties.setProperty("tvla.propertyName", propertyName);
        ++i;

        inputFile = args[0];
        propertyName = args[0];
        if (args.length > 1 && args[1].charAt(0) != '-') {
            inputFile = args[1];
            ++i;
        }

        for (; i  < args.length; i++) {
            if (args[i].equals("-mode")) {
                i++;
                if (i<args.length)
                    ProgramProperties.setProperty("tvla.engine.type", args[i]);
                continue;
            }
            else if (args[i].equals("-d"))
                ProgramProperties.setProperty("tvla.debug", "true");
            else if (args[i].equals("-dstep"))
                ProgramProperties.setProperty("tvla.debugstep", "true");
            else if (args[i].equals("-noautomatic"))
                ProgramProperties.setProperty("tvla.generateAutomaticConstraints", "false");
            else if (args[i].equals("-significant")) {
                ProgramProperties.setProperty("tvla.significantNodeNames", "true");
            } else if (args[i].equals("-join")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -join!");
                    usage();
                }
                String joinType = args[i];
                if (!joinType.equals("rel") &&
                    !joinType.equals("part") &&
                    !joinType.equals("part_embedding") &&
                    !joinType.equals("j3") &&
                    !joinType.equals("unbounded") && // ADDED BY jbreck
                    !joinType.equals("ind")) {
                    System.err.println("Invalid join option specified: " + joinType + "!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.joinType", joinType);
            } else if (args[i].equals("-backward")) {
                ProgramProperties.setProperty("tvla.cfg.backwardAnalysis", "true");
            } else if (args[i].startsWith("-D")) {
                if (args[i].length() > 2) {
                    String macro = args[i].substring(2, args[i].length());
                    ProgramProperties.appendToStringListProperty("tvla.parser.externalMacros", macro);
                }
            } else if (args[i].equals("-save")) {
                i++;
                ProgramProperties.setProperty("tvla.cfg.saveLocations", args[i]);
                if (args[i] == null ||
                    (!args[i].equals("back") && !args[i].equals("ext") && !args[i].equals("all"))) {
                    System.err.println("Invalid save option specified: " + args[i] + "!");
                    usage();
                }
            } else if (args[i].equals("-ms")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -ms!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.engine.maxStructures", args[i]);
            } else if (args[i].equals("-mm")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -mm!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.engine.maxMessages", args[i]);
            } else if (args[i].equals("-log")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -log!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.log.logFileName", args[i]);
            } else if (args[i].equals("-tvs")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -tvs!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.tvs.outputFile", args[i]);
            } else if (args[i].equals("-dot")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -dot!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.dot.outputFile", args[i]);
            } else if (args[i].equals("-terse")) {
                ProgramProperties.setProperty("tvla.terse", "true");
            } else if (args[i].equals("-nowarnings")) {
                ProgramProperties.setProperty("tvla.emitWarnings", "false");
            } else if (args[i].equals("-props")) {
                ++i;
            } else if (args[i].equals("-path")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -path!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.parser.searchPath", args[i]);
            } else if (args[i].equals("-action")) {
                i++;
                if (i >= args.length) {
                    System.err.println("Missing argument after -action!");
                    usage();
                }
                ProgramProperties.setProperty("tvla.engine.actionOrder", args[i]);
            }
            else {
                System.err.println("Unknown option " + args[i] + "!");
                System.err.println();
                usage();
            }
        }
    }

    /** Resolves the mode/engine from the file extensions.
     */
    protected static void resolveMode() {
        boolean autoResolve = ProgramProperties.getBooleanProperty("tvla.engine.autoResolveType", true);
        engineType = ProgramProperties.getProperty("tvla.engine.type", "tvla");
        if (autoResolve) {
            File tvp = new File(programName + ".tvp");
            File tvm = new File(programName + ".tvm");
            File buc = new File(propertyName + ".buc");
            if (tvp.exists())
                engineType = "tvla";
            if (tvm.exists())
                engineType = "tvmc";
            if (buc.exists())
                engineType = "ddfs";
        }
    }

    /** Prints information regarding to the analysis mode.
     * @author Roman Manevich.
     * @since 30.7.2001 Initial creation.
     */
    protected static void printLogHeader(String [] args) {
        if (!ProgramProperties.getProperty("tvla.log.logFileName", "null").equals("null"))
            Logger.print(versionInfo + " ");

        if (!ProgramProperties.getProperty("tvla.log.logFileName", "null").equals("null")) {
            Logger.print("Arguments: ");
            for (int index=0; index<args.length; ++index)
                Logger.print(args[index] + " ");
            Logger.println();
        }
    }


    /** Initializes and validates program properties that are not handled by any other class
     * from command-line options.
     * @author Roman Manevich.
     * @since 22.11.2001 Initial creation.
     */
    protected static void initProgramProperties(String [] args) {
        String tvpName = null;
        if (args.length == 0 || args[0].charAt(0) == '-')
            usage();
        else
            tvpName = args[0];

        resolveMode();

        String propPath = ProgramProperties.getProperty("tvla.parser.searchPath", "");
        if (propPath.length() > 0)
            searchPath += ";" + propPath;
        searchPath += ";" + computeTvlaHome();

        // determine the name of the dot output file
        String outputFile;
        if (!optionSpecified(args, "-dot") &&
            ProgramProperties.getBooleanProperty("tvla.dot.enabled", true) &&
            tvpName != null) {
            int index = tvpName.indexOf(".tvp");
            if (index >=0)
                tvpName = tvpName.substring(index, tvpName.length()-1);
            outputFile = ProgramProperties.getProperty("tvla.dot.outputFile", "null");
            if (outputFile.equals("null"))
                outputFile = tvpName + ".dt";
            ProgramProperties.setProperty("tvla.dot.outputFile", outputFile);
        }
        outputFile = ProgramProperties.getProperty("tvla.dot.outputFile", "null");

        // check that -significant was not specified along with -join rel
        if (ProgramProperties.getBooleanProperty("tvla.significantNodeNames", false) &&
            ProgramProperties.getProperty("tvla.joinType", "rel").equals("rel")) {
            String message = "Using significant node names with relational join " +
                "might cause the analysis to generate an excessive " +
                "number of structures!\n" +
                "Use the -nowarnings option to ignore this warning.";
            if (ProgramProperties.getBooleanProperty("tvla.emitWarnings", true))
                throw new UserErrorException(message);
            else
                Logger.println(message);
        }
    }

    /** Loads the property files.
     * The directory path for the default properties file (tvla.properties)
     * is extracted from the java.class.path property.
     * @param The program arguments are needed in order to extract information needed
     * to access the desired property files.
     * @author Roman Manevich.
     * @since 14.10.2001 Initial creation.
     */
    protected static void loadProgramProperties(String [] args) throws Exception {
        for (int i = 0; i < args.length; ++i) {
            if (i == 0)
                programName = args[i];
            if (args[i].equals("-props")) {
                ++i;
                if (i > args.length) {
                    System.err.println("-props was specified without a file name!");
                    usage();
                }
                File file = new File(args[i]);
                if (!file.exists()) {
                    System.err.println("-props specified the file " + args[i] + ", which does not exist!");
                    usage();
                }
                specificPropertiesFiles.add(args[i]);
            }
        }

        String tvlaHome = computeTvlaHome();
        String fileSeparator = System.getProperty("file.separator");

        // add the default properties file tvla.properties from the application's home directory,
        String defaultPropertiesFile = tvlaHome;
        if (!defaultPropertiesFile.endsWith(fileSeparator))
            defaultPropertiesFile = defaultPropertiesFile + fileSeparator;
        defaultPropertiesFile = defaultPropertiesFile + "tvla.properties";
        ProgramProperties.addPropertyFile(defaultPropertiesFile);

        // add the user properties file user.properties from the application's home directory,
        String userPropertiesFile = tvlaHome;
        if (!userPropertiesFile.endsWith(fileSeparator))
            userPropertiesFile = userPropertiesFile + fileSeparator;
        userPropertiesFile = userPropertiesFile + "user.properties";
        ProgramProperties.addPropertyFile(userPropertiesFile);

        // Add a run-specific property file if one was specified in the command-line.
        if (!specificPropertiesFiles.isEmpty()) {
            for (Iterator propIter = specificPropertiesFiles.iterator(); propIter.hasNext(); ) {
                String propertyFile = (String) propIter.next();
                File f = new File(propertyFile);
                if (f.exists())
                    ProgramProperties.addPropertyFile(propertyFile);
            }
        }
        ProgramProperties.load();
    }

    /** Computes the path of the TVLA homr directory.
     * @author Roman Manevich.
     * @since November 17 2001 Initial creation.
     */
    protected static String computeTvlaHome() {
        // Check the program properties
        String tvlaHome = System.getProperty("tvla.home", null);

        // If failed try to deduce it from the class path
        if (tvlaHome == null) {
            String classPath = System.getProperty("java.class.path");
            File tvlaHomeFile = new File(classPath);
            if (tvlaHomeFile.isFile()) { // a .jar file
                String fileName = tvlaHomeFile.getName();
                int i = classPath.indexOf(fileName);
                tvlaHome = classPath.substring(0, i);
            }
            else {
                tvlaHome = classPath;
            }
        }

        return tvlaHome;
    }

    /** Searchs the command-line options for a specific argument.
     * @author Roman Manevich.
     * @since 24.11.2001 Initial creation.
     */
    protected static boolean optionSpecified(String [] args, String option) {
        for (int index = 0; index < args.length; ++index) {
            if (args[index].equals(option))
                return true;
        }
        return false;
    }
}
