open Printf
open Sl 
open Printer 
open Gen

let print_tvs f fname =
  let tvs_file = (fname^"_gen.tvs") in
  let oc = open_out tvs_file in 

  fprintf oc "/* THIS FILE HAS BEEN AUTOMATICALLY GENERATED.*/\n";
  fprintf oc "/* A structure that models \n\n";

  fprintf oc "1. The formula varphi := %s\n" (toString f);
  fprintf oc "   where varphi is broken down using the following propositional\n";
  fprintf oc "   variables for the internal nodes of the formula:\n\n";
  fprintf oc "%s\n\n" ( triplets f );

  fprintf oc "2. An arbitrary heap that has at least one cell\n\n";

  fprintf oc "3. The stack variables %s\n\n" (stackvars f);

  fprintf oc "4. A hierarchical partition of the heap following the nodes of the formula   */\n\n";

  fprintf oc "%%location L1 = {\n\n";

  fprintf oc "%%n = { \n%s\n" (individuals f);
  fprintf oc "     }\n";

  fprintf oc "%%p = { \n%s\n" (predicates f);
  fprintf oc "     }\n";


  fprintf oc "}\n"; (*end location L1*)

  close_out oc;

  print_endline ("Generated "^tvs_file);
;;



let new_print_tvs f fname =
  let tvs_file = (fname^"_gen.tvs") in
  let oc = open_out tvs_file in 

  fprintf oc "/* THIS FILE HAS BEEN AUTOMATICALLY GENERATED.*/\n";
  fprintf oc "/* A structure that models \n\n";

  fprintf oc "1. The formula varphi := %s\n" (toString f);

  fprintf oc "2. An arbitrary heap that has at least one cell\n\n";

  fprintf oc "3. The stack variables %s\n\n" (stackvars f);

  fprintf oc "4. A hierarchical partition of the heap following the nodes of the formula   */\n\n";

  fprintf oc "%%location L_top = {\n\n";

  fprintf oc "%%n = { \n%s\n" (new_individuals f);
  fprintf oc "     }\n";

  fprintf oc "%%p = { \n%s\n" (new_predicates f);
  fprintf oc "     }\n";


  fprintf oc "}\n"; (*end location L1*)

  close_out oc;

  print_endline ("Generated "^tvs_file);
;;
