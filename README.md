SMASLTOV
========
Satisfiability Modulo Abstraction for Separation Logic ThrOugh Valuation

This package of files was originally produced to accompany the publication 
 in SPIN 2014 entitled "Satisfiability Modulo Abstraction for Separation
 Logic with Linked Lists" by Thakur, Breck, and Reps.  
 (see http://spin2014.org)

FILES CONTAINED IN THIS PACKAGE
===============================

  --+-- benchmarks\
    |     |
    |     +-- *.tvp, *.tvs  TVLA input files for tables 4, 5, and 6 of the
    |                          SPIN 2014 publication, along with files that
    |                          are needed to run any formula, including new
    |                          formulas created by a user.
    |     
    +-- tools\
    |     |
    |     +-- tvla\          A modified version of TVLA.
    |     |     |
    |     |     +-- etc\     README and LICENSE files for TVLA and libraries.
    |     |
    |     +-- generator\     The TVLA-input-generator, written in OCaml, which
    |                          produces input files that direct TVLA to check 
    |                          particular formulas for unsatisfiability.  
    | 
    +-- README               This README file.
    |
    +-- LICENSE              License file for the semi-decision procedure.
                               Note that other license files are found under
                               tools/tvla/ and tools/tvla/etc/.

RUNNING THE SEMI-DECISION PROCEDURE
===================================

  System requirements:
  
  * Java (version 1.7 or perhaps others) is required to run the semi-decision 
      procedure (it is used by TVLA).

  * Python (version 2.6+) is required to run the run_benchmark and run_table 
      scripts.

  * OCaml is required if you want to run the TVLA-input-generator, which 
      produces TVLA input files for specific formulas.  However, the 
      TVLA-input-generator has already been run on the formulas described 
      in the CAV 2014 submission, and the resulting TVLA input files can be 
      found under benchmarks/table3, benchmarks/table4, and benchmarks/table5.

  * This has been tested on Linux and (very briefly) on Windows.

  - - -

  To run a batch of formulas, use the run_batch.py script, like:

       python run_batch.py 4

              to run the batch of formulas that constitutes table 4
              of the SPIN 2014 publication

              or:

       python run_batch.py benchmarks/batch_other

              to run a (user-created) batch of formulas called "batch_other"

  - - -

  To run the TVLA-input generator, change directory to tools/generator, and run

      make

    followed by
     
      ./main.native

    This will produce several types of files.  In particular, the ".tvs" and
      ".tvp" files are required to run TVLA.  Such files could be copied to
      the provided benchmarks/common_files directory (which contains other
      files needed by the semi-decision procedure, not produced by the 
      TVLA-input-generator) and then run with a command similar to the
      example invocation of run_benchmark.py above.

    If you wish to run the semi-decision procedure on your own formulas, you 
      will have to modify the list of formulas in the TVLA-input-generator's 
      source code.  (That is to say, formulas are currently hard-coded in the
      generator's source code.)  Instructions about how to do this can be
      found in a comment near the end of the file "main.ml" in the directory
      "tools/generator".  Look for the text "ADD NEW FORMULAS HERE" and the
      comments following that point.


