#!/usr/bin/python
import subprocess, sys, os, os.path, re, time

globalTimestamp = time.strftime("%Y_%m_%d_at_%H_%M_%S")

class DummyException(Exception) : pass

def slurpFileAsString(path) :
    with open(path, 'rb') as F :
        return F.read()

def slurpFileAsListOfLines(path) :
    with open(path, 'rb') as F :
        return [L.strip() for L in F if L.strip() != ""]

def dumpToFile(text, path) :
    with open(path, 'wb') as F :
        F.write(text)

#def scanForTimes(output) :
#    timingDict = dict()
#    for location, timing in re.findall("[(]@(L\d+)\s*Time taken = (\d+[.]?\d*) sec[)]", output) :
#        if location not in timingDict : timingDict[location] = 0
#        timingDict[location] += float(timing)
#    return timingDict

def scanForTimes(output) :
    timingDict = dict()
    lastCumulativeTime = 0.0
    for location, oldTime, cumulTime in re.findall("[(]@(L\d+)\s*Time\s+taken\s+=\s+(\d+[.]?\d*)\s+sec\s+Cumul\s+=\s+(\d+[.]?\d*)\s+sec[)]", output) :
        timing = float(cumulTime) - lastCumulativeTime
        lastCumulativeTime = float(cumulTime)
        # Note that our generator doesn't currently generate multiple equations with the
        #   same left-hand-side location.  (i.e. L5=THING(L4), L5=STUFF(L3) ) but previous
        #   versions did and future versions might do so.
        # Anyway, the value in timingDict[location] is supposed to be the sum of the times
        #   taken by all equations that stored their results in location.
        if location not in timingDict : timingDict[location] = 0
        timingDict[location] += float(timing)
    return timingDict

def scanForStructureCount(output, location) :
    for finding in re.findall("Location\s*" + location + "\s*:\s*(\d+)\s*structure", output) :
        return int(finding)
    return None

def the(s) :
    if len(s) == 1 : return s[0]
    return None

def scanForLoadCleanup(output) :
    return (
            float(the(re.findall("Load time\s*:\s*(\d+[.]?\d*)\s*seconds",output))),
            float(the(re.findall("Cleanup time\s*:\s*(\d+[.]?\d*)\s*seconds",output))),
           )

def strOrUnknown(x) :
    if x is None : return "(unknown)"
    return str(x)

def checkName(testName) :
    if testName.endswith(".dt.ps") :
        testName = testName[0:-len(".dt.ps")]
    elif any(testName.endswith(X) for X in [".tvp",".tvs",".tex",".txt",".dt",".pdf",".ps"]) :
        testName = os.path.splitext(testName)[0]

    tvpName = testName + ".tvp"
    tvsName = testName + ".tvs"

    if not os.path.exists(tvpName) :
        if os.path.exists(testName + "_gen.tvp") :
            testName += "_gen"
            tvpName = testName + ".tvp"
            tvsName = testName + ".tvs"
        else :
            if testName.endswith(".tv") and os.path.exists(testName + "p") :
                testName = os.path.splitext(testName)[0]
                tvpName = testName + ".tvp"
                tvsName = testName + ".tvs"
            else :
                print "Could not find TVP file: " + tvpName
                sys.exit(-1)
    if not os.path.exists(tvsName) :
        print "Could not find TVS file: " + tvsName
        sys.exit(-1)

    return testName, tvpName, tvsName

def log(x,logFile) :
    if logFile :
        print >>logFile, x

def logTee(logFile, s) :
    if logFile :
        print >>logFile, s
    print s

def runWithLimits(cmd, timeout=None, memory=None) : 
    """timeout is in seconds, memory is in MB.
       attempt to return stdout, stderr, returncode."""
    if platform.system() == "Linux" :
        rawLimitCommand = "timeout " # works best for timing, but can't limit memory
        #rawLimitCommand = "ulimit " # works; can limit memory
        #rawLimitCommand = "runlim -k " # doesn't work
        limitCommand = rawLimitCommand
        if timeout :
            limitCommand += " " + str(timeout) + "s"
        if memory :
            print "(Feature not implemented: memory limitation)"
            sys.exit(0)
        if limitCommand == rawLimitCommand :
            limitCommand = ""
        else :
            limitCommand += " "
        fullCommand = limitCommand + cmd
        print "        " + fullCommand
        child = subprocess.Popen(fullCommand, shell=True,
                                 stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        stdout, stderr = child.communicate()
    else :
        print "Currently this function (Jason's runWithLimits) only supports Linux."
        print "But, we could write something similar for Windows..."
        sys.exit(0)
    return stdout, stderr, child.returncode

def logResults(testName, logFile, tvlaError, stderr, posOver, posUnder, runningTime, resultPath, batchLogFile) :
    indent = "    "
    if runningTime is None :
        timeTaken = "(unknown)"
    else :
        timeTaken = ("%.6f" % runningTime) 
        #timeTaken = ("%.4f" % runningTime) 
    if tvlaError:
        unsatResult = "(ERROR)"
        satResult = "(ERROR)"
        outcome = "ERROR"
    else :
        unsatResult = "(no-unsat)"
        satResult = "(no-sat)"
        outcome = "UNKNOWN"
        if posOver :
            overOutcome = scanForStructureCount(stderr, posOver)
            if overOutcome == 0 :
                outcome = "UNSAT"
                unsatResult = "UNSAT"
            else :
                outcome = "(no proof of unsat)"
        if posUnder :
            underOutcome = scanForStructureCount(stderr, posUnder)
            if underOutcome and underOutcome > 0 :
                if outcome == "UNKNOWN" : 
                    outcome = ""
                else :
                    outcome += "\n" + indent
                outcome += "SAT"
                satResult = "SAT"
            else :
                if outcome == "UNKNOWN" :
                    outcome = ""
                else :
                    outcome += "\n" + indent
                outcome += "(no proof of sat)"
    logTee(batchLogFile, indent + outcome)
    logTee(batchLogFile, indent + timeTaken + " sec")

    #result = outcome + "\t" + timeTaken 
    resultLine = testName + "\t" + unsatResult + "\t" + satResult + "\t" + timeTaken 
    dumpToFile(resultLine, resultPath)
    log(resultLine, logFile)
    #for finding in re.findall("Total analysis time\s*:\s*([0-9.]+)", stderr) :
    #    formula['time'] = finding
    if logFile : logFile.flush()

# This is used in the main chunk of code, as well as this function:
batch_outcome_regex = r"Outcome:\s*(\S+)\s*PosOver\@\s*(\S+)"

def runTest(testName, logFile, **kwArgs) :

    testName, tvpName, tvsName = checkName(testName)

    stdoutPath = testName + "_stdout.txt"
    stderrPath = testName + "_stderr.txt"
    resultPath = testName + "_result.txt"
    batchLogPath = testName + "_batch_log_" + globalTimestamp + ".txt"
    tvsOutputPath = testName + "_output.tvs"

    tvpContents = slurpFileAsString(tvpName)
    try :
        posOver = re.findall(r"PosOver\@\s*(\S+)", tvpContents)[0]
    except :
        posOver = None
    try :
        posUnder = re.findall(r"PosUnder\@\s*(\S+)", tvpContents)[0]
    except :
        posUnder = None
    try :
        batchOutcomes = re.findall(batch_outcome_regex, tvpContents)
    except :
        batchOutcomes = None

    try :
        startTime = time.time()
        TVLA_HOME = os.path.join(sys.path[0], "tvla")
        command = ["java", "-Dtvla.home="+TVLA_HOME,"-mx6000m","-jar",os.path.join(TVLA_HOME,"lib","tvla.jar"), testName, tvsName, "-noautomatic"]
        #command = ["tvla", testName, tvsName, "-noautomatic"]
        if 'debugMode' in kwArgs and kwArgs['debugMode'] :
            command.append("-d")
        if 'tvs' in kwArgs and kwArgs['tvs'] :
            command.append("-tvs")
            command.append(tvsOutputPath)

        startTime = time.time()
        if 'useTimeout' in kwArgs :
            textCommand = " ".join(command)
            runWithLimits(textCommand, timeout=kwArgs['useTimeout'])
        else :
            child = subprocess.Popen(command, # " ".join(command),shell=True,
                                     stdout=subprocess.PIPE,stderr=subprocess.PIPE)
            stdout, stderr = child.communicate()
        endTime = time.time()
        totalTime = (endTime - startTime)

        dumpToFile(stdout, stdoutPath)
        dumpToFile(stderr, stderrPath)

        tvlaError = False

        ## The below lines are now redundant, because the sanity check
        ##   has been made into a fatal error.
        #if "sanity check failed" in stderr :
        #    tvlaError = True
        #    print stderr
        #    print "************* TVLA sanity check failed ***************"

        if "Analysis finished." not in stderr :
            tvlaError = True
            print stderr
            print "************* TVLA ERROR ***************"
            print "(waiting three seconds for user cancel (ctrl-C)...)"
            time.sleep(3)

        if batchOutcomes :
            # First figure out batch timings...
            formulaTimes = dict()
            timeAccountedFor = 0.0
            try :
                # batchOutcomes is a list of pairs (formulaName, locationName)
                maxOutcomeLocationNumber = max(int(BO[1][1:]) for BO in batchOutcomes if BO[1][0] == 'L')
                outcomeLocationToFormula = dict()
                for formulaName, locationName in batchOutcomes :
                    if locationName[0] != 'L' : continue # skip dummy locations whose names don't start with 'L'
                    if locationName not in outcomeLocationToFormula :
                        outcomeLocationToFormula[locationName] = list()
                    outcomeLocationToFormula[locationName].append(formulaName)
                #outcomeLocationToFormula = dict( (BO[1], BO[0]) for BO in batchOutcomes if BO[1][0] == 'L')
                locationTimes = scanForTimes(stderr)
                #print batchOutcomes
                #print maxOutcomeLocationNumber
                #print outcomeLocationToFormula
                #print locationTimes
                formulaTotal = 0.0
                for L in range(1, 1 + maxOutcomeLocationNumber) :
                    LL = 'L' + str(L)
                    if LL in locationTimes :
                         currentStepTime = locationTimes[LL]
                         timeAccountedFor += currentStepTime
                         if formulaTotal is not None :
                             formulaTotal += currentStepTime
                    else :
                         formulaTotal = None
                    if LL in outcomeLocationToFormula :
                         for formulaName in outcomeLocationToFormula[LL] :
                             #formulaName = outcomeLocationToFormula[LL]
                             formulaTimes[formulaName] = formulaTotal
                             formulaTotal = 0.0
            except DummyException as e: 
                print "EXCEPTION while trying to parse batch timing information: ", e
            with open(batchLogPath, 'w') as batchLogFile :
                loadTime, cleanupTime = scanForLoadCleanup(stderr)
                loadPlusCleanup = 0.0
                if loadTime :
                    loadPlusCleanup += loadTime
                if cleanupTime :
                    loadPlusCleanup += cleanupTime
                logTee(batchLogFile, "Grand total batch time: " + str(totalTime))
                #logTee(batchLogFile, "Batch time not assigned to any formula: " + str(totalTime - timeAccountedFor))
                logTee(batchLogFile, "Load+cleanup time: " + str(loadPlusCleanup) 
                                  + " (= " + strOrUnknown(loadTime) + " + " + strOrUnknown(cleanupTime) + ")")
                logTee(batchLogFile, "       Slack time: " + str(totalTime - timeAccountedFor - loadPlusCleanup))
                logTee(batchLogFile, "      Non-formula: " + str(totalTime - timeAccountedFor) + " = load+cleanup+slack")
                # Now print batch output
                for formulaName, posOver in batchOutcomes :
                    logTee(batchLogFile, "Batch Outcome " + formulaName)
                    resultPath = formulaName + "_result.txt"
                    formulaTime = formulaTimes.get(formulaName, None)
                    logResults(formulaName, logFile, tvlaError, stderr, posOver, None, formulaTime, resultPath, batchLogFile)
        else :
            logResults(testName, logFile, tvlaError, stderr, posOver, posUnder, totalTime, resultPath, None)
            
    except DummyException as e:
        # Try to keep going, no matter what. 
        print "EXCEPTION:", e
        status = "EXCEPTION: " + str(e)
# ------------------------------------------------------

def usage() :
    print "USAGE: run.py [ <testNames> | -f <fileContainingTestNames> ] [--use-timeout <seconds>] [--debug]"
    sys.exit(-1)


if __name__ == '__main__' :
    args = sys.argv[1:]
    if len(args) == 0 :
        usage()
    filenameArgs = list()
    iArg = 1
    logPath = None
    kwArgs = dict()
    while iArg <= len(args)  :
        arg = sys.argv[iArg]
        if arg == "-f" :
            filenameArgs += slurpFileAsListOfLines(sys.argv[iArg + 1])
            iArg += 2
        #elif arg == "--tvs" :
        #    kwArgs['tvs'] = int(sys.argv[iArg + 1])
        #    iArg += 2
        elif arg == "--use-timeout" :
            kwArgs['useTimeout'] = int(sys.argv[iArg + 1])
            iArg += 2
        elif arg == "--tvs" :
            iArg += 1
            kwArgs['tvs'] = True
        elif arg == "--debug" :
            iArg += 1
            kwArgs['debugMode'] = True
        elif arg == "--logname" :
            logPath = sys.argv[iArg + 1]
            iArg += 2
        else :
            filenameArgs += [ arg ]
            iArg += 1
    testNames = filenameArgs 
    realTestNames = list()
    for testName in testNames :
        if testName in ["predicates_thru.tvp", "functions_nzzt.tvp", "sl_nzzt.tvp"] :
            print "Ignoring: " + testName
            continue
        realTestNames.append(checkName(testName)[0])
    is_batch_so_force_log = False
    if len(testNames) == 1 :
        dummy1, tvpName, dummy3 = checkName(testName)
        tvpContents = slurpFileAsString(tvpName)
        batchOutcomes = re.findall(batch_outcome_regex, tvpContents)
        if batchOutcomes :
            is_batch_so_force_log = True
    if logPath or len(testNames) > 1 or is_batch_so_force_log:
        if not logPath : # if one wasn't specified on the command line, generate one with a timestamp
            logPath = "run_log_" + globalTimestamp + ".txt"
            #logPath = time.strftime("run_log_%Y_%m_%d_at_%H_%M_%S.txt") 
        logFile = open(logPath,'wb')
        print "Logging this run to: " + logPath
    else :
        logFile = None
    uniqueTestNames = list()
    setOfTestNames = set()
    for testName in realTestNames :
        if testName not in setOfTestNames :
            uniqueTestNames.append(testName)
            setOfTestNames.add(testName)
    for testName in uniqueTestNames :
        #if len(testNames) > 1 :
        print "Running " + testName
        runTest(testName, logFile, **kwArgs)
    if logFile : logFile.close()
