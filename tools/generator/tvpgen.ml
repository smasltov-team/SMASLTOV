open Printf
open Sl 
open Printer 
open Gen

let print_tvp f fname =
  let tvp_file = (fname^"_gen.tvp") in
  let oc = open_out tvp_file in 

  fprintf oc "/* THIS FILE HAS BEEN AUTOMATICALLY GENERATED.*/\n";
  fprintf oc "/*\n varphi := %s\n" (toString f);
  fprintf oc ("where varphi is broken down using the following propositional\n");
  fprintf oc ("variables for the internal nodes of the formula:\n");
  fprintf oc "%s\n" ( triplets f );
  fprintf oc ("*/\n");

  fprintf oc ("// Sets\n");
  fprintf oc "%%s FormulaNodes %s\n" (fnodes    f);
  fprintf oc "%%s StackVars %s\n"   (stackvars f);
  fprintf oc "%%s HeapRegions %s\n" (hnodes    f);
  fprintf oc "\n";

  fprintf oc "#include \"predicates_nzzt.tvp\"\n";
  fprintf oc "#include \"sl_nzzt.tvp\"  \n\n";

  fprintf oc ("// Instances of the Integrity rules for this formula\n");
  (* fprintf oc (garbage_rule f); *)
  fprintf oc "%s\n" (generate_integrity f );

  fprintf oc "\n%%%%   \n\n#include \"functions_nzzt.tvp\"\n\n%%%%  \n\n";

  fprintf oc "%s\n" (proof_rules f);

  close_out oc;
  print_endline ("Generated "^tvp_file);
;;



let new_print_tvp f fname =
  let tvp_file = (fname^"_gen.tvp") in
  let oc = open_out tvp_file in 

  fprintf oc "/* THIS FILE HAS BEEN AUTOMATICALLY GENERATED.*/\n";
  fprintf oc "/*\n varphi := %s\n" (toString f);
  fprintf oc ("*/\n");

  fprintf oc ("// Sets\n");
  fprintf oc "%%s StackVars %s\n"   (stackvars f);
  fprintf oc "%%s HeapRegions %s\n" (new_hnodes    f);
  fprintf oc "\n";

  fprintf oc "#include \"predicates_thru.tvp\"\n";
  fprintf oc "#include \"sl_nzzt.tvp\"  \n\n";

  fprintf oc "\n%%%%   \n\n#include \"functions_nzzt.tvp\"\n\n%%%%  \n\n";

  fprintf oc "%s\n" (new_proof_rules f);

  close_out oc;
  print_endline ("Generated "^tvp_file);
;;

let batch_print_tvp f disj_f fname = (* here f      is a list of formulas, 
                                             disj_f is a single formula that is exactly the 
                                                    disjunction of all of the formulas in f. *)
  let tvp_file = (fname^"_gen.tvp") in
  let oc = open_out tvp_file in 

  fprintf oc "/* THIS FILE HAS BEEN AUTOMATICALLY GENERATED.*/\n";
  fprintf oc "/* This is a batch of multiple formulas.*/\n\n";

  fprintf oc ("// Sets\n");
  fprintf oc "%%s StackVars %s\n"   (stackvars disj_f);
  fprintf oc "%%s HeapRegions %s\n" (new_hnodes    disj_f);
  fprintf oc "\n";

  fprintf oc "#include \"predicates_thru.tvp\"\n";
  fprintf oc "#include \"sl_nzzt.tvp\"  \n\n";

  fprintf oc "\n%%%%   \n\n#include \"functions_nzzt.tvp\"\n\n%%%%  \n\n";

  reset_lblcount(); (* different batch elements need to get different location labels.  *)

  let clear_cache = true in 
  let po_label_lst = List.map (fun (e,ename) -> 
                              fprintf oc "\n/* %s */\n" ename;
                              fprintf oc "/*\n varphi := %s\n" (toString e);
                              fprintf oc ("*/\n");
                              let (proof, po_label) = 
                                  (batch_element_proof_rules e ename clear_cache) in
                              fprintf oc "%s\n" proof;
                              po_label
                              ) f in


  fprintf oc "\n\n%%%% \n\n///////////////////////////////////////////////////////////////////////// \n// Display only structures that arise at the following program locations.\n"; 

  List.iter (fun po_label -> fprintf oc "%s " po_label) po_label_lst;

  close_out oc;
  print_endline ("Generated "^tvp_file);
;;


