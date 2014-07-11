#!/usr/bin/python

import sys, os.path, subprocess

def usage() :
    print "USAGE: python run_batch.py <table number or batch name>"
    print ""
    print "  For example:"
    print ""
    print "  python run_batch.py 4"
    print "    or "
    print "  python run_batch.py other"
    sys.exit(0)

if len(sys.argv) != 2 :
    usage()
    sys.exit(0)

batch_name = sys.argv[1]

toCheck = list()
if batch_name in ["4","5","6"] :
    batch_file = os.path.join(sys.path[0], "benchmarks", "batch_table" + batch_name + "_gen")
else :
    batch_file = os.path.join(sys.path[0], "benchmarks", "batch_" + batch_name)
    toCheck.append(batch_file + ".tvp")
    if not os.path.exists(toCheck[-1]) :
        batch_file = os.path.join(sys.path[0], "benchmarks", "batch_" + batch_name + "_gen")
        toCheck.append(batch_file + ".tvp")
        if not os.path.exists(toCheck[-1]) :
            print "Could not find files named: "
            print "  OR\n".join("   " + F + "\n" for F in toCheck)
            usage()

subprocess.call(["python", 
                 os.path.join(sys.path[0], "tools", "run_tvla.py"),
                 batch_file])

