open Printf
open Sl 
open Printer 
open Gen

let print_txt f fname = 
  let txt_file = fname^"_gen.txt" in
  let oc = open_out txt_file in
  fprintf oc "%s" (toString f);

  close_out oc;
  print_endline ("Generated "^txt_file);
;;
