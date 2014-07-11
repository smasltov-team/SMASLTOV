(* Generate integrity rules *)

open Sl
open Utils
open Printer
open Nnf 

(* id to propositional relation string *)
let pstr pid = "p"^ (string_of_int pid)
;;

(* id to heap relation string *)
let hstr hid = "d"^ (string_of_int hid)
;;

(* id to string for individual that modeels propositional relation*)
let pind_str pid = "up"^ (string_of_int pid)
;;

(* label id to string *)
let lstr lbl = 
  if lbl == -1 then failwith "Invalid label used"
  else "L"^(string_of_int (lbl))
;;

(* individual for summary heap individual *)
let sum_heap () = "uh0"
;;

let rec fsize_ f = 
  match f with
      True | False | Emp | SingletonHeap _ | Ls _ | Eq _
	-> 1
    | Not(f1) (* | Exists(_,f1)  *)
        -> fsize_ f1 + 1
    | And(f1, f2) | Or(f1, f2) | Iff(f1, f2) | SepAnd(f1, f2) | Septract(f1, f2) 
        -> fsize_ f1 + fsize_ f2 + 1
    | Ite(f1, f2, f3) 
	-> fsize_ f1 + fsize_ f2 + fsize_ f3 + 1

;;
	
(* number of nodes in formula *)
let formula_size f =
  fsize_ f 
;;

(* Serialized list of triplets *)
let rec triplets_ id0 f = 
  pstr id0 ^ " <-> " ^
  match f with
      True -> "True"
    | False -> "False"
    | And(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      pstr id1 ^ " & " ^ pstr id2 ^" \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 
    | Or(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      pstr id1 ^ " | " ^ pstr id2 ^" \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 
    | Iff(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      "(" ^ pstr id1 ^ " <-> " ^ pstr id2 ^") \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 
    | Ite(f1, f2, f3) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      let id3 = id2+formula_size f2 in
      "(" ^pstr id1 ^ " ? "^ pstr id2 ^" : " ^ pstr id3 ^ ") \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 ^ "\n" ^ triplets_ id3 f3 
    | Not(f1) ->
      let id1 = id0+1 in
      "!" ^ pstr id1 ^ "\n" ^ triplets_ id1 f1
    | Emp -> "Emp"

    | SepAnd(f1, f2) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      pstr id1 ^ " * " ^ pstr id2 ^" \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 
    | Septract(f1, f2) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      pstr id1 ^ " --(*) " ^ pstr id2 ^" \n" ^ triplets_ id1 f1 ^ "\n" ^ triplets_ id2 f2 
    | SingletonHeap(v1, v2) ->
      "[ " ^ var_to_str v1 ^" |-> " ^ var_to_str v2 ^ "]"
    | Ls(v1, v2) ->
      "Ls(" ^ var_to_str v1 ^", " ^ var_to_str v2 ^ ")"
    | Eq(v1, v2) ->
      var_to_str v1 ^" == " ^ var_to_str v2 
;;

(* list of ids for each subformula *)
let rec nodes_ id0 f = 
  id0::
  match f with
      True -> 
	[]
    | False -> 
      []
    | And(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      List.append (nodes_ id1 f1) (nodes_ id2 f2)
    | Or(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      List.append (nodes_ id1 f1) (nodes_ id2 f2)
    | Iff(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      List.append (nodes_ id1 f1) (nodes_ id2 f2)
    | Ite(f1, f2, f3) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      let id3 = id2+formula_size f2 in
      List.append (List.append (nodes_ id1 f1) (nodes_ id2 f2)) (nodes_ id3 f3)
    | Not(f1) ->
      let id1 = id0+1 in
      (nodes_ id1 f1)
    | Emp -> 
      []
    | SepAnd(f1, f2) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      List.append (nodes_ id1 f1) (nodes_ id2 f2)
    | Septract(f1, f2) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      List.append (nodes_ id1 f1) (nodes_ id2 f2)
    | SingletonHeap(v1, v2) ->
      []
    | Ls(v1, v2) ->
      []
    | Eq(v1, v2) ->
      []
;;


module StringSet = Set.Make(
  struct 
    let compare = Pervasives.compare
    type t = string
  end)

let rec stackvars_  f = 
  match f with
      True | False | Emp -> StringSet.add "null" StringSet.empty
    | And(f1, f2) | Or(f1, f2) | Iff(f1, f2) | SepAnd(f1, f2) | Septract(f1, f2) 
      -> StringSet.union (stackvars_ f1) (stackvars_ f2)
    | Ite(f1, f2, f3) ->
      StringSet.union (StringSet.union (stackvars_ f1) (stackvars_ f2)) (stackvars_ f3)
    | Not(f1) -> stackvars_ f1
    | SingletonHeap(v1, v2) | Ls(v1, v2) | Eq(v1, v2) ->
      StringSet.add "null" (StringSet.add (var_to_str v2) (StringSet.add (var_to_str v1) StringSet.empty))
;;


(* Generate integrity constraints *)
let rec gen id0 f =
  match f with
      True -> gen_ "SLTrue" id0 []
    | False -> gen_ "SLFalse" id0 []
    | And(f1, f2)  -> gen_ "SLAndIntegrity" id0 [f1;f2]
    | Or(f1, f2) -> gen_ "SLOrIntegrity" id0 [f1;f2]
    | Iff(f1, f2) -> gen_ "SLEquivIntegrity" id0 [f1;f2]
    | Ite(f1, f2, f3) -> gen_ "SLIteIntegrity" id0 [f1;f2;f3]
    | Not(f1) -> gen_ "SLNotIntegrity" id0 [f1]
    | Emp -> gen_ "Emp" id0 []
    | SepAnd(f1, f2) -> gen_ "StarAndIntegrity" id0 [f1;f2]
    | Septract(f1, f2) -> gen_ "ExistentialMagicWandIntegrity" id0 [f1;f2]
    | SingletonHeap(v1, v2) -> "PointsTo" ^ args2str [ pstr id0; var_to_str v1; var_to_str v2; hstr id0]
    | Ls(v1, v2) -> let pred_def = "LsPredicateDef" ^ args2str [var_to_str v2; hstr id0] ^ "\n" in
                    let ls = "Ls" ^ args2str [ pstr id0; var_to_str v1; var_to_str v2; hstr id0] in
		    (pred_def ^ ls)
    | Eq(v1, v2) -> "Eq" ^ args2str [ pstr id0; var_to_str v1; var_to_str v2; hstr id0] 

and gen_ funcstr id0 flist =
  let idlist =  
    (* TODO Make this more general *)
    match flist with 
	[] -> [id0]
      | [f1] -> [id0; id0+1]
      | [f1; f2] -> [id0; id0+1; id0+1+formula_size f1]
      | [f1; f2; f3] -> [id0; id0+1; id0+1+formula_size f1; id0+1+formula_size f1+formula_size f2]
  in 
  let pargs = List.map (fun x -> pstr x) idlist in
  let hargs = List.map (fun x -> hstr x) idlist in
  let args = List.append pargs hargs in
  let curr_constraint = funcstr ^ args2str args in
  (* Handle children *)
  let childlist = 
    (* TODO Make this more general *)
    match flist with
	[] -> []
      | [f1] -> [ (id0+1, f1)]
      | [f1; f2] -> [ (id0+1, f1); (id0+1+formula_size f1, f2)] 
      | [f1; f2; f3] -> [ (id0+1, f1); (id0+1+formula_size f1, f2); (id0+1+formula_size f1+formula_size f2,f3)] 
  in
  let child_constraints = List.map (fun (id,f) -> gen id f) childlist in
  let integrity = List.fold_left ( fun acc x -> acc ^ "\n" ^ x) curr_constraint child_constraints in
  integrity 
;;

(* Serialized integrity constraints for the formula *)
let generate_integrity f = 
  let c = gen 0 f in
  let null_c = if StringSet.mem "null" (stackvars_ f) then "\nNullVariable(null)\n" else "" in
  c^null_c


(* Serialized triplets p <-> (q op r) *)
let triplets = triplets_ 0

(* Return string of {p0, p1, ...} *)
let fnodes f = 
  args2str_curly (List.map (fun x -> pstr x) (nodes_ 0 f))


let findividuals f = 
  args2str_none (List.map (fun x -> pind_str x) (nodes_ 0 f))

(* Return string of {h0, h1, ...} *)
let hnodes f = 
  args2str_curly (List.map (fun x -> hstr x) (nodes_ 0 f))

(* Return string of {x, y, ...} *)
let stackvars f =
  args2str_curly (StringSet.elements (stackvars_ f))

(* rule that sets nongarbage to false *)
let garbage_rule f = 
(*  %r heapSort(v) & !d0(v) & !r[n,a1](v) & !r[n,a2](v) ==> !nongarbage[d0](v) *)
  let rule1 = "%r heapSort(v) & !" ^ (hstr 0) ^ "(v)" in
  let vars = StringSet.elements (stackvars_ f) in
  let rule2 = List.fold_left (fun acc x -> (acc ^ " & !r[n," ^ x ^ "](v) ")) rule1 vars in
  let rule3 = rule2 ^ " ==> !nongarbage[" ^ (hstr 0) ^ "](v)" in
  rule3


let create_eqn lblout x lblin = (lstr lblout)^" = " ^x^ "("^(lstr lblin)^")\n"   

let proof_seq lst lblin = 
  List.fold_left (fun (lbl,p) x ->  (lbl+1, p^(lstr (lbl+1))^" = " ^x^ "("^(lstr lbl)^")\n"   ))  (lblin,"") lst

(*Id of output *)
(*List of proof rules (strings) *)
let rec proof_ id0 lbl0 f =
  (*WARNING: ugly hard-coded variable ahead*)
  (* use_meet should be passed in as a parameter *)

  let use_meet() = true in

  match f with
      True | False -> (lbl0,"")
    | And(f1, f2) | Or(f1, f2) | Iff(f1, f2) ->  
      let id1 = id0 + 1 in
      let id2 = id1 + formula_size f1 in
      let (lbl00,proof0) =  proof_seq [("OneSaturateCartesian" ^ args2str_sq [pstr id1])] lbl0 in  
      let (lbl1, proof1) = proof_ id1 (lbl00) f1 in
      let (lbl2, proof2) = proof_ id2 (lbl1)   f2 in
      (lbl2, proof0 ^ proof1 ^ proof2)

    | Ite(f1, f2, f3) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      let id3 = id2+formula_size f2 in

      let (lbl00,proof0) = proof_seq ["OneSaturateCartesian" ^ args2str_sq [pstr id0]] lbl0 in
      let (lbl1, proof1) = proof_ id1  lbl00 f1 in
      let (lbl2, proof2) = proof_ id2  lbl1  f2 in
      let (lbl3, proof3) = proof_ id3  lbl2  f3 in
      (lbl3, proof0  ^ proof1  ^ proof2  ^ proof3)

    | Not(f1) ->
      let id1 = id0+1 in
      let (lbl00,proof0) = proof_seq ["OneSaturateCartesian" ^ args2str_sq [pstr id1]] lbl0 in
      let lbl1, proof1 = proof_ id1 (lbl0+1) f1 in
      (lbl1, proof0  ^ proof1)

    | Emp -> proof_seq ["OneSaturateEmp" ^ args2str_sq [hstr id0]] lbl0

    | SepAnd(f1, f2) ->

      if use_meet() then

	let id1 = id0+1 in
	let id2 = id1+formula_size f1 in

	let (lbl1, proof1) = proof_seq ["OneSaturateCartesian" ^ args2str_sq [pstr id1]] lbl0 in

	let (lbl2, proof2) = proof_seq ["SetRegionToBeRelevant" ^ args2str_sq [hstr id1]] lbl1 in
	let (lbl3, proof3) = proof_ id1 lbl2 f1 in

	let (lbl4, proof4) = (lbl3+1, create_eqn (lbl3+1) ("SetRegionToBeRelevant" ^ args2str_sq [hstr id2]) lbl1 ) in
	let (lbl5, proof5) = proof_ id2 lbl4 f2 in

	let lbl55 = lbl5 + 1 in
	let proof55 = "L"^(string_of_int (lbl55))^" = Meet[](L"^(string_of_int lbl3)^", L"^(string_of_int lbl5)^ ")\n" in


	let (lbl6, proof6) = proof_seq ["BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl55 in

	let (lbl7, proof7) = proof_seq ["CheckExistentialBinaryTrue" ^args2str_sq [(hstr id0); (hstr id1); hstr id2; pstr id0; "formula"]] lbl6 in
	let (lbl7, proof8) = proof_seq ["CheckExistentialBinaryFalse" ^args2str_sq [(hstr id0); (hstr id1); hstr id2; pstr id0; "formula"]] lbl6 in

	let (lbl8, proof9) = proof_seq ["BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl7 in

	(lbl8, proof1 ^ proof2 ^ proof3 ^ proof4 ^ proof5 ^ proof55 ^ proof6 ^ proof7 ^ proof8 ^ proof9)

      else

	let id1 = id0+1 in
	let id2 = id1+formula_size f1 in

	let (lbl1, proof1) = proof_seq ["OneSaturateCartesian" ^ args2str_sq [pstr id1]] lbl0 in

	let (lbl2, proof2) = proof_seq ["SetRegionToBeRelevant" ^ args2str_sq [hstr id1]] lbl1 in
	let (lbl3, proof3) = proof_ id1 lbl2 f1 in

	let (lbl4, proof4) = proof_seq ["SetRegionToBeRelevant" ^ args2str_sq [hstr id2]] lbl3 in
	let (lbl5, proof5) = proof_ id2 lbl4 f2 in

	let (lbl6, proof6) = proof_seq ["BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl5 in

	let (lbl7, proof7) = proof_seq ["CheckExistentialBinaryTrue" ^args2str_sq [(hstr id0); (hstr id1); hstr id2; pstr id0; "formula"]] lbl6 in
	let (lbl7, proof8) = proof_seq ["CheckExistentialBinaryFalse" ^args2str_sq [(hstr id0); (hstr id1); hstr id2; pstr id0; "formula"]] lbl6 in

	let (lbl8, proof9) = proof_seq ["BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl7 in

	(lbl8, proof1 ^ proof2 ^ proof3 ^ proof4 ^ proof5 ^ proof6 ^ proof7 ^ proof8 ^ proof9)


    | Septract(f1, f2) ->

      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in

      let (lbl1, proof1) = proof_seq ["OneSaturateCartesian" ^ args2str_sq [pstr id1];
				      "SetRegionToBeRelevant" ^ args2str_sq [hstr id2]
                                     ] lbl0 in
      let (lbl2, proof2) = proof_ id1 lbl1 f1 in
      let (lbl3, proof3) = proof_ id2 lbl2 f2 in

      let (lbl4, proof4) = proof_seq ["BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl3 in

      let (lbl5, proof5) = proof_seq ["CheckExistentialBinaryTrue" ^args2str_sq [hstr id2; (hstr id0); (hstr id1); pstr id0; "formula"]] lbl4 in
      let (lbl5, proof6) = proof_seq ["CheckExistentialBinaryFalse" ^args2str_sq [hstr id2; (hstr id0); (hstr id1); pstr id0; "formula"]] lbl4 in
      let (lbl6, proof7) = proof_seq ["HavocRegion" ^ args2str_sq [hstr id1];
				      "BlurDomains" ^ args2str_sq [hstr id1; hstr id2]] lbl5 in
      (lbl6, proof1 ^ proof2  ^ proof3 ^proof4 ^ proof5 ^ proof6 ^ proof7)

    | SingletonHeap(v1, v2) ->
      proof_seq  ["OneSaturatePointsToLiteral" ^ args2str_sq [var_to_str v1; var_to_str v2; hstr id0 ]] lbl0

    | Ls(v1, v2) ->
      proof_seq ["OneSaturateLs" ^ args2str_sq [var_to_str v1; var_to_str v2; hstr id0]] lbl0

    | Eq(v1, v2) ->
      proof_seq ["OneSaturateEq" ^ args2str_sq [var_to_str v1; var_to_str v2; hstr id0] ] lbl0
;;

let rec range n acc = 
  if n < 1 then acc else range (n-1) (n::acc)

let proof_rules f =
  let (lbl,pre) =  proof_seq ["SetHeapSortHalfRelevant"^args2str_sq[];
		    "SetRegionToBeRelevant"^args2str_sq[ hstr 0];
		    "SetToTrue"^args2str_sq [ pstr 0];
                    "FocusUnary"^args2str_sq [ hstr 0]] 1  in
  let (last, proof) = proof_ 0 lbl f in
  let prooflist = pre ^ proof in
  let before_locs = "\n\n%% \n\n///////////////////////////////////////////////////////////////////////// \n// Display only structures that arise at the following program locations.\n" in
  let locs = "L"^(string_of_int last) in
  prooflist ^ before_locs ^ locs


(********************************************************)

let lblcount = ref 1;;

let nxtlbl () = let c = !lblcount in lblcount := !lblcount +1; c
;;    

let reset_lblcount () = lblcount := 1; 
;;    


let leaf x proof = 
  let lblout = nxtlbl() in
  (lblout, proof ^ (lstr lblout)^" = " ^x^ "(L_top)\n")


let meet lbl1 lbl2 proof = 
  let lbl3 = nxtlbl() in
  (lbl3,  proof ^ (lstr lbl3)^" = Meet[]("^(lstr lbl1)^", "^(lstr lbl2)^ ")\n")

let meet3 lbl1 lbl2 lbl3 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = Meet3[]"^args2str[(lstr lbl1);(lstr lbl2); (lstr lbl3)] ^"\n")

let partition id0 id1 id2 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = StarPartition" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str["L_top"]^"\n")

let overlap id0 id1 id2 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = MagicWandOverlap" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str["L_top"]^"\n")


let meet_partition id0 id1 id2 lbl_in proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = MeetPartition" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str[(lstr lbl_in);"L_top"]^"\n")

let meet_partition_both id0 id1 id2 lbl1 lbl2 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = MeetPartitionBoth" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str[(lstr lbl1);(lstr lbl2);"L_top"]^"\n")


let meet_overlap id0 id1 id2 lbl_in proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = MeetOverlap" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str[(lstr lbl_in); "L_top"]^"\n")


let project_region id0 id1 id2 lbl_in proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = ProjectDownToRegion" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str[(lstr lbl_in)]^"\n")


let join lbl1 lbl2 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = Join[]" ^args2str[(lstr lbl1); (lstr lbl2)] ^ "\n")

let join3 lbl1 lbl2 lbl3 proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = Join3[]" ^args2str[(lstr lbl1); (lstr lbl2); (lstr lbl3)] ^ "\n")


(* compute join of p1 and p2, using their negations n1 and n2*)
let splt_join p1 p2 n1 n2 proof =
  let m1,proof =  meet p1 n2 proof in
  let m2,proof =  meet n1 p2 proof in
  let m3,proof =  meet p1 p2 proof in
  let j1, proof = join3 m1 m2 m3 proof in
  (j1, proof)

let diff_over lbl1 lbl2 proof =
  let lbl3 = nxtlbl() in
  (lbl3,  proof ^ (lstr lbl3)^" = DiffOver[]"^ args2str[(lstr lbl1);(lstr lbl2)]^ "\n")

let diff_under lbl1 lbl2 proof =
  let lbl3 = nxtlbl() in
  (lbl3,  proof ^ (lstr lbl3)^" = DiffUnder[]"^args2str[(lstr lbl1);(lstr lbl2)] ^ "\n")

let havoc_outside id0 lbl_in proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = HavocOutsideWorld" ^ args2str_sq [hstr id0] ^args2str[(lstr lbl_in)] ^ "\n")

let copy_from idf idto lbl_in proof = 
  let lbl_out = nxtlbl() in
  (lbl_out, proof ^ (lstr lbl_out)^" = CopyFrom" ^ args2str_sq [hstr idf; hstr idto] ^args2str[(lstr lbl_in)] ^ "\n")

let update_region_1 id0 id1 id2 lbl_in proof = 
  let lbl_out = nxtlbl() in
  (lbl_out, proof ^ (lstr lbl_out)^" = UpdateSubRegion" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^args2str[(lstr lbl_in)] ^ "\n")

let update_region_2 id0 id1 id2 lbl_in proof = 
  let lbl_out = nxtlbl() in
  (lbl_out, proof ^ (lstr lbl_out)^" = UpdateSubRegionAgain" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^args2str[(lstr lbl_in)] ^ "\n")
;;


let havoc_partition id0 id1 id2 lbl_in proof = 
  let (c1, proof) = copy_from id1 id0 lbl_in proof in
  let (c2, proof) = copy_from id2 id0 lbl_in proof in
  let (c3, proof) = meet3 lbl_in c1 c2 proof in
  project_region id0 id1 id2 c3 proof
;;

(*
let havoc_partition id0 id1 id2 lbl_in proof =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = HavocPartition" ^ args2str_sq [hstr id0; hstr id1; hstr id2] ^ args2str[(lstr lbl_in)]^"\n")
*)


let update_region id0 id1 id2 lbl_in proof = 
  let u1, proof = update_region_1 id0 id1 id2 lbl_in proof in
  let u2, proof = update_region_2 id0 id2 id2 u1 proof in
  (u2, proof)
;;

(*rename regions from id1 to id2 *)
let rename_region id1 id2 lbl_in proof  =
  let lbl_out = nxtlbl() in
  (lbl_out,  proof ^ (lstr lbl_out)^" = RenameRegion" ^ args2str_sq [hstr id1; hstr id2] ^ args2str[(lstr lbl_in)]^"\n")
;;
 
let rename_valid_region fid tid lbl_in proof =
  if lbl_in == -1 then (-1, proof)
  else rename_region fid tid lbl_in proof
;;

(*Focus using meet. *)
(* pos join neg is top *)
let focus_structure s pos neg proof = 
  let f1, proof = meet s pos  proof in
  let f2, proof = meet s neg  proof in
  let j, proof = join f1 f2 proof in
  (j,  proof) 
;;   


let rpo_cache = Hashtbl.create 20;;
let rpu_cache = Hashtbl.create 20;;
let rno_cache = Hashtbl.create 20;;
let rnu_cache = Hashtbl.create 20;;

  
let lookup_cache tbl req id f = 
  if not req then 
    (-1, req)
  else
    try
      (let lbl = Hashtbl.find tbl (id,f) in
      (lbl, not req))
    with Not_found -> (-1, req)
;;

let update_cache tbl id f lbl =
  match lbl 
  with
      -1 -> ()
    | n -> Hashtbl.add tbl (id, f) n
;;

let rec wrapper_proof splt rpo rpu rno rnu id0 f proof =
  let _po, _rpo = lookup_cache rpo_cache rpo id0 f  in
  let _pu, _rpu = lookup_cache rpu_cache rpu id0 f  in
  let _no, _rno = lookup_cache rno_cache rno id0 f  in
  let _nu, _rnu = lookup_cache rnu_cache rnu id0 f  in
  let po, pu, no, nu, proof = new_proof splt _rpo _rpu _rno _rnu id0 f proof in
  update_cache rpo_cache id0 f po;
  update_cache rpu_cache id0 f pu;
  update_cache rno_cache id0 f no;
  update_cache rnu_cache id0 f nu;
  (max po _po, max pu _pu, max no _no, max nu _nu, proof)
and

(* rpo: require postive over *)
   (* rpu: require positive under *)
   (* rno: require negative over *)
   (* rnu: require negative under *)
    (* splt: use more precise interpretation of or *)
(* returns positive over, positive under, negative over, negative under *)
(* proof rules as string appended after given proof string "proof"*)
(* -1 if any of the values are not required *)
(* the relevant heap is id0 *)
new_proof splt rpo rpu rno rnu id0 f proof = 
  if not (id0 == 0)
  then
    let po, pu, no, nu, proof = wrapper_proof splt rpo rpu rno rnu 0 f proof in
    let po, proof = rename_valid_region 0 id0 po proof in
    let pu, proof = rename_valid_region 0 id0 pu proof in
    let no, proof = rename_valid_region 0 id0 no proof in
    let nu, proof = rename_valid_region 0 id0 nu proof in
    (po, pu, no, nu, proof)
  else (
  match f with 
       True ->  
	 let (po3, proof) = 
	   if rpo 
	   then leaf ("OverTrue"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (pu3, proof) = 
	   if rpu 
	   then leaf ("UnderTrue"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf ("OverFalse"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (nu3, proof4) = 
	   if rnu 
	   then leaf ("UnderFalse"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 (po3, pu3, no3, nu3, proof)
	   
    |  False -> 
	 let (po3, proof) = 
	   if rpo 
	   then leaf ("OverFalse"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in
	 let (pu3, proof) = 
	   if rpu 
	   then leaf ("UnderFalse"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf ("OverTrue"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (nu3, proof) = 
	   if rnu 
	   then leaf ("UnderTrue"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 (po3, pu3, no3, nu3, proof)

    | Emp -> 
	 let (po3, proof) = 
	   if rpo  
	   then leaf ("OverEmp"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (pu3, proof) = 
	   if rpu
	   then leaf ("UnderEmp"^args2str_sq [hstr id0])  proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf ("OverNotEmp"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 let (nu3, proof) = 
	   if rnu 
	   then leaf ("UnderNotEmp"^args2str_sq [hstr id0]) proof
	   else (-1, proof) in

	 (po3, pu3, no3, nu3, proof)

    | SingletonHeap(v1, v2) ->
	 let (po3, proof) = 
	   if rpo 
	   then leaf ("OverPointsTo"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in
	 let (pu3, proof) = 
	   if rpu 
	   then leaf ("UnderPointsTo"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf ("OverNotPointsTo"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (nu3, proof) = 
	   if rnu 
	   then leaf ("UnderNotPointsTo"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 (po3, pu3, no3, nu3, proof)

    | Ls(v1, v2) ->
	 let (po3, proof) = 
	   if rpo 
	   then leaf ("OverLs"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2])  proof
	   else (-1, proof) in
	 let (pu3, proof) = 
	   if rpu 
	   then leaf ("UnderLs"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf ("OverNotLs"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (nu3, proof) = 
	   if rnu 
	   then leaf ("UnderNotLs"^args2str_sq [hstr id0; var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in


	 (po3, pu3, no3, nu3, proof)

    | Eq(v1, v2) ->
	 let (po3, proof) = 
	   if rpo 
	   then leaf ("OverEq"^args2str_sq [var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (pu3, proof) = 
	   if rpu 
	   then leaf  ("UnderEq"^args2str_sq [var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (no3, proof) = 
	   if rno 
	   then leaf  ("OverNotEq"^args2str_sq [var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 let (nu3, proof) = 
	   if rnu 
	   then leaf ("UnderNotEq"^args2str_sq [var_to_str v1; var_to_str v2]) proof
	   else (-1, proof) in

	 (po3, pu3, no3, nu3, proof)

    | And(f1, f2) ->

      let rpo_c = rpo || (rno && splt) in
      let rpu_c = rpu || (rnu && splt) in
      let rno_c = rno in
      let rnu_c = rnu in
      let child_proof = wrapper_proof splt rpo_c rpu_c rno_c rnu_c in

      let (po1, pu1, no1, nu1, proof) =  child_proof id0 f1 proof in

      let (po2, pu2, no2, nu2, proof) = child_proof id0 f2 proof in

      let (po3, proof) = 
      if rpo 
      then meet po1 po2 proof
      else (-1, proof) in

      let (pu3, proof) =
	if rpu 
	then meet  pu1 pu2 proof
	else (-1, proof) in

      let (no3, proof) = 
	if rno && splt then 
	  splt_join no1 no2 po1 po2 proof 
	else if rno && not splt then 
	  join no1 no2 proof
	else 
	  (-1, proof) in

      let (nu3, proof) = 
	if rnu && splt then 
	  splt_join nu1 nu2 pu1 pu2 proof 
	else if rnu && not splt then 
	  join nu1 nu2 proof
	else 
	  (-1, proof) in

      (po3, pu3, no3, nu3, proof)

    | Or(f1, f2) -> 
      wrapper_proof splt rpo rpu rno rnu id0 (Not(And(Not f1, Not f2))) proof

(*  
    (* Special case for phi * True *)
    (* This seems to completely wrong. (ptsto*true) & !(pts*true) gives sat.*)
   | SepAnd(f1, True) | SepAnd(True, f1) -> 
      let rno = rno || rnu in (* We calculate neg under using negative over *)
      let rpu = rpu || rno in (* We calculate neg over using positive under *)
      let rpo = rpo || rnu in (* We calculate neg under using positive under*)

      let rpo_c = rpo in
      let rpu_c = rpu  in
      let rno_c = rno in
      let rnu_c = false in (* This seems strange, but is correct *)

      let child_proof = wrapper_proof splt rpo_c rpu_c rno_c rnu_c in

      let id1 = id0+1 in
      let id2 = id1+1 in
      let (po1, pu1, no1, nu1, proof) =  child_proof id1 f1 proof in

      let (po3, proof) = 
      if rpo 
      then (
        let (po1_c, proof) = copy_from id1 id0 po1 proof in
        let (po3_meet, proof) = meet_partition id0 id1 id2 po1_c proof in
	let (po3_havoc, proof) = project_region id0 id1 id2 po3_meet proof in
  	(po3_havoc, proof))
      else (-1, proof) in
      

      let (pu3, proof) = 
      if rpu 
      then (
        let (pu1_c, proof) = copy_from id1 id0 pu1 proof in
        let (pu3_meet, proof) = meet_partition id0 id1 id2 pu1_c proof in
	let (pu3_havoc, proof) = project_region id0 id1 id2 pu3_meet proof in
  	(pu3_havoc, proof))
      else (-1, proof) in

      let (no3, proof) = 
	if rno 
	then  (
	  (* poor overapproximation of negation *)
	  (* !(phi*true) is oveapprximated by !phi *)
	  (*let no3_bad, proof = rename_region id1 id0 no1 proof in *)
 	  let no3_bad, proof = copy_from id1 id0 no1 proof in
          let no3_meet, proof = meet_partition id0 id1 id2 no3_bad proof in
          let no3_h, proof    = project_region id0 id1 id2 no3_meet proof in          

	  let (no3_do, proof) = diff_over no3_h pu3 proof in
	  (no3_do, proof))
	else 
	  (-1, proof) in

      let (nu3, proof) = 
	if rnu
	  (*TODO We could focus here too *)
	then diff_under no3 po3 proof
	else (-1, proof) in

      (po3, pu3, no3, nu3, proof)
*)

	(*   *)

    | SepAnd(f1, f2) -> 

      let rno = rno || rnu in (* We calculate neg under using negative over *)
      let rpu = rpu || rno in (* We calculate neg over using positive under *)
      let rpo = rpo || rnu in (* We calculate neg under using positive under*)

      let rpo_c = rpo || (rno (*&& splt*)) in
      let rpu_c = rpu in
      let rno_c = rno in
      let rnu_c = false in (* This seems strange, but is correct *)

      let child_proof = wrapper_proof splt rpo_c rpu_c rno_c rnu_c in

      let id1 = id0+1 in
      let id2 = id1+1 in

      let (po1, pu1, no1, nu1, proof) =  child_proof id1 f1 proof in

      let (po2, pu2, no2, nu2, proof) = child_proof id2 f2 proof in

      let (po3, proof) = 
      if rpo 
      then (
        let (po1_c, proof) = copy_from id1 id0 po1 proof in
        let (po2_c, proof) = copy_from id2 id0 po2 proof in
        let (po3_meet, proof) = meet_partition_both id0 id1 id2 po1_c po2_c proof in
	let (po3_havoc, proof) = havoc_partition id0 id1 id2 po3_meet proof in
  	(po3_havoc, proof))
      else (-1, proof) in
      

      let (pu3, proof) = 
      if rpu 
      then (
        let (pu1_c, proof) = copy_from id1 id0 pu1 proof in
        let (pu2_c, proof) = copy_from id2 id0 pu2 proof in
        let (pu3_meet, proof) = meet_partition_both id0 id1 id2 pu1_c pu2_c proof in

	let (pu3_havoc, proof) = havoc_partition id0 id1 id2 pu3_meet proof in
  	(pu3_havoc, proof))
      else (-1, proof) in

      let (no3, proof) = 
	if rno && splt 
	then  (
	  (* poor overapproximation of negation *)
          let (no1_c, proof) = copy_from id1 id0 no1 proof in
          let (no2_c, proof) = copy_from id2 id0 no2 proof in
          let (po1_c, proof) = copy_from id1 id0 po1 proof in
          let (po2_c, proof) = copy_from id2 id0 po2 proof in
	  let no1_m, proof = meet_partition id0 id1 id2 no1_c proof in
	  let no2_m, proof = meet_partition id0 id1 id2 no2_c proof in
	  let no3_j, proof = splt_join no1_m no2_m po1_c po2_c proof in
	  
	  (*havoc *)
	  let (no3_h, proof) = havoc_partition id0 id1 id2 no3_j proof in
	  
	  (* focus before the diff *)

	  let (po1_r, proof) = rename_region id1 id0 po1 proof in
	  let (no1_r, proof) = rename_region id1 id0 no1 proof in

	  let (po2_r, proof) = rename_region id2 id0 po2 proof in
	  let (no2_r, proof) = rename_region id2 id0 no2 proof in

	  let (no3_f1, proof) = focus_structure no3_h  po1_r no1_r proof in
	  let (no3_f2, proof) = focus_structure no3_f1 po2_r no2_r proof in

	  let (no3_do, proof) = diff_over no3_f2 pu3 proof in
	  (no3_do, proof))

	else if rno && not splt
	then (
	  (* poor overapproximation of negation *)
          let (no1_c, proof) = copy_from id1 id0 no1 proof in
          let (no2_c, proof) = copy_from id2 id0 no2 proof in
	  let no1_m, proof = meet_partition id0 id1 id2 no1_c proof in
	  let no2_m, proof = meet_partition id0 id1 id2 no2_c proof in
	  let no3_j, proof = join no1_m no2_m proof in
	  (* Havoc *)
	  let no3_h, proof = havoc_partition id0 id1 id2 no3_j proof in
	  (* diff over *)
	  let no3_d, proof = diff_over no3_h pu3 proof in
	  (no3_d, proof))
	else 
	  (-1, proof) in

      let (nu3, proof) = 
	if rnu
	  (*TODO We could focus here too *)
	then diff_under no3 po3 proof
	else (-1, proof) in

      (po3, pu3, no3, nu3, proof)


    | Septract(f1, f2) -> 

      let rno = rno || rnu in (* We calculate neg under using negative over *)
      let rpu = rpu || rno in (* We calculate neg over using positive under *)
      let rpo = rpo || rnu in (* We calculate neg under using positive over *)

      let rpo_c = rpo || (rno && splt) in
      let rpu_c = rpu  in
      let rno_c = rno in
      let rnu_c = false in (* This seems strange, but is correct *)

      let child_proof = wrapper_proof splt rpo_c rpu_c rno_c rnu_c in
      let id1 = id0+1 in
      let id2 = id1+1 in

      let (po1, pu1, no1, nu1, proof) =  child_proof id1 f1 proof in


      let (po2, pu2, no2, nu2, proof) = child_proof id2 f2 proof in


      let (po3, proof) = 
      if rpo 
      then (
	let po1_c, proof = copy_from id1 id2 po1 proof in
	let po3_m, proof = meet_partition_both id2 id0 id1 po1_c po2 proof in
	let po3_u, proof = update_region id0 id1 id2 po3_m proof in
	let po3_p, proof = project_region id0 id1 id2 po3_u proof in
	(po3_p, proof))
      else (-1, proof) in
      
      let (pu3, proof) = 
      if rpu 
      then (
	let pu1_c, proof = copy_from id1 id2 pu1 proof in
	let pu3_m, proof = meet_partition_both id2 id0 id1 pu1_c pu2 proof in
	let pu3_u, proof = update_region id0 id1 id2 pu3_m proof in
	let pu3_p, proof = project_region id0 id1 id2 pu3_u proof in
	(pu3_p, proof))
      else (-1, proof) in

      let (no3, proof) = 
	if rno
	then  (
	  let ov, proof = overlap id0 id1 id2 proof in
	  let part, proof = partition id0 id1 id2 proof in
	  let po1_c, proof = copy_from id1 id2 po1 proof in
	  let no1_c, proof = copy_from id1 id2 no1 proof in
	  let bound, proof = splt_join ov no1_c part po1_c proof in
	  let body, proof = meet_partition_both id2 id0 id1 po1_c no2 proof in
	  let no3_j, proof = join bound body proof in
	  let no3_u, proof = update_region id0 id1 id2 no3_j proof in
	  let no3_p, proof = project_region id0 id1 id2 no3_u proof in

	  let (po1_r, proof) = rename_region id1 id0 po1 proof in
	  let (no1_r, proof) = rename_region id1 id0 no1 proof in
	  
	  let (po2_r, proof) = rename_region id2 id0 po2 proof in
	  let (no2_r, proof) = rename_region id2 id0 no2 proof in
	  
	  let (no3_f1, proof) = focus_structure no3_p po1_r no1_r proof in
	  let (no3_f2, proof) = focus_structure no3_f1 po2_r no2_r proof in
	  
	  let (no3_do, proof) = diff_over no3_f2 pu3 proof in
	  (no3_do,proof))
	else (-1, proof) in

      let (nu3, proof) = 
	if rnu
	  (* TODO We could focus here too *)
	then diff_under no3 po3 proof 
	else (-1, proof) in
      (po3, pu3, no3, nu3, proof)

    | Not f1 ->
      let rpo_c = rno in
      let rpu_c = rnu in
      let rno_c = rpo in
      let rnu_c = rpu in
      let child_proof = wrapper_proof splt rpo_c rpu_c rno_c rnu_c in      
      let (po1, pu1, no1, nu1, proof) =  child_proof id0 f1 proof in
      let po2 = 
	if rpo then no1 else -1 in
      let pu2 = 
	if rpu then nu1 else -1 in
      let no2 = 
	if rno then po1 else -1 in
      let nu2 = 
	if rnu then pu1 else -1 in
      (po2, pu2, no2, nu2, proof)

    | _ -> failwith "formula not handled"
  )
;;


let new_proof_rules f = 
  let splt = true in
  let rpo = true in
  let rpu = true in
  let rno = false in
  let rnu = false in
  reset_lblcount();
  let id0 = 0 in
  let proof = "" in
  Hashtbl.clear rpo_cache;
  Hashtbl.clear rpu_cache;
  Hashtbl.clear rno_cache;
  Hashtbl.clear rnu_cache;
  let (po,pu,no,nu, proof) = wrapper_proof splt rpo rpu rno rnu id0 f proof in

  let proof = proof ^ (if rpo then "\n// PosOver@ "^ (lstr po) else "") in
  let proof = proof ^ (if rpu then "\n// PosUnder@ "^(lstr pu) else "") in
  let proof = proof ^ (if rno then "\n// NegOver@ "^ (lstr no) else "") in
  let proof = proof ^ (if rnu then "\n// NegUnder@ "^(lstr nu) else "") in

  let before_locs = "\n\n%% \n\n///////////////////////////////////////////////////////////////////////// \n// Display only structures that arise at the following program locations.\n" in

  let locs = "" in
  let locs = locs ^ (if rpo then (lstr po)^" " else "") in
  let locs = locs ^ (if rpu then (lstr pu)^" " else "") in
  let locs = locs ^ (if rno then (lstr no)^" " else "") in
  let locs = locs ^ (if rnu then (lstr nu)^" " else "") in

  proof ^ before_locs ^ locs
;;







  (* let numbered_prooflist = List.map (fun (i,x) -> "L"^(string_of_int (i+1))^" =  " ^x^ "(L"^(string_of_int i)^")" ) temp in *)
  (* let proof_listing = List.fold_left (fun acc x -> acc ^ "\n" ^ x) "// transition system for the proof --------------------------" numbered_prooflist in *)

(* print the individuals for tvs structure *)
let individuals f = 
  let ind_desc = "// individuals that model the Boolean vars for the formula nodes\n" in
  let pinds    = (findividuals f)^",\n" in
  let sh_desc  = "// a summary heap individual\n" in
  let sh       = sum_heap()^"\n" in
  ind_desc^pinds^sh_desc^sh
;;



let rec gen_nlink_ id0 f = 
  match f with
      True | False | Emp -> ""

    | And(f1, f2) | Or(f1, f2) | Iff(f1, f2) -> 
      let id1, id2 = id0+1, id0+1+formula_size f1 in
      gen_nlink_ id1 f1 ^ "\n" ^ gen_nlink_ id2 f2 

    | Ite(f1, f2, f3) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      let id3 = id2+formula_size f2 in
      gen_nlink_ id1 f1 ^ "\n" ^ gen_nlink_ id2 f2 ^ "\n" ^ gen_nlink_ id3 f3 

    | Not(f1) ->
      let id1 = id0+1 in
      gen_nlink_ id1 f1

    | SepAnd(f1, f2) | Septract(f1, f2) ->
      let id1 = id0+1 in
      let id2 = id1+formula_size f1 in
      gen_nlink_ id1 f1 ^ "\n" ^ gen_nlink_ id2 f2 
    | SingletonHeap(v1, v2) ->
      ""
    | Ls(v1, v2) ->
      "nlink"^ (args2str_sq [hstr id0; var_to_str v2])^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n"
    | Eq(v1, v2) -> ""
      
;;


(* Serialized integrity constraints for the formula *)
let gen_nlink = gen_nlink_ 0 


let predicates f = 
  let pids = (nodes_ 0 f) in

  let p_i = List.fold_left ( fun acc x -> acc^(pstr x)^" = {" ^ (pind_str x)^"}\n") "// Predicates that model the Boolean vars for the formula nodes\n" pids in

  let bval_str = "bval = " ^ args2str_curly (List.map (fun x -> (pind_str x)^":1/2") pids)^"\n" in

  let gen_equiv_val x y = if x=y then "" else ":1/2" in
  let gen_equiv_row x lids = 
    List.map (fun y -> (if y=0 then "\n" else "")^(pind_str x)^"->"^(pind_str y)^(gen_equiv_val x y) ) lids  in
  let equiv_str = "equiv = " ^ args2str_curly (List.flatten ((List.map ( fun x -> (gen_equiv_row x pids)) pids))) ^ "\n\n" in

  let bsort_str = "boolSort = " ^ args2str_curly (List.map (fun x -> (pind_str x)) pids)^"\n" in

  let vsort_str = "valSort = " ^ args2str_curly [sum_heap()] ^ "\n"in

  let hsort_str = "heapSort = " ^ args2str_curly [sum_heap()] ^ "\n\n" in 

  let heap_preds_str = 
     "// Predicates that model an arbitrary heap that has at least one cell\n" ^
     "sm = " ^ args2str_curly [sum_heap()^":1/2"]^ "\n" ^
     "n = " ^ args2str_curly [sum_heap()^"->"^sum_heap()^":1/2"]^" //unknown connections\n" ^
     "t[n] =" ^ args2str_curly[sum_heap()^"->"^sum_heap()^":1/2"]  ^ "// unknown transitive connections\n" ^
     "c[n] = " ^ args2str_curly[sum_heap()^":0"]^     "// but acyclic\n" ^
      "relevant =" ^ args2str_curly [sum_heap()^":1/2"]^"\n"^
      "irrelevant =" ^ args2str_curly [sum_heap()^":1/2"]^"\n"
  in
  let svars = (StringSet.elements (stackvars_ f))in

  let r_n_str = List.fold_left ( fun acc v -> acc^"r[n,"^ v^"] = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "" svars in

  let nxt_contains_str = List.fold_left ( fun acc v -> acc^"nxt_contains["^v^"] = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "" svars in
  let nlink_str = gen_nlink f in

  let svars_str =  List.fold_left ( fun acc v -> acc^v^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "\n// Predicates to model the two stack variables\n" svars in

  let hvars_str =  List.fold_left ( fun acc v -> acc^(hstr v)^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "\n// Predicates to model a hierarchical partition of the heap following the nodes of the formul\n" pids in

  "formula=1\n" ^ p_i ^ bval_str ^equiv_str ^ bsort_str ^ vsort_str ^ hsort_str ^ heap_preds_str ^ r_n_str ^nxt_contains_str^nlink_str ^
    svars_str ^ hvars_str
    




(* print the individuals for tvs structure *)
let new_individuals f = 
  let sh_desc  = "// A summary heap individual\n" in
  let sh       = sum_heap() in
  sh_desc^sh



let gen_all_nlink pids svars =
  let selector = "n" in
  let nlink_gen list =
    match list with
	[id;v] ->
	  "link"^ (args2str_sq [id; selector; v])^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n"
      | _ -> failwith "Incorrect length of list " in
  List.fold_left (fun acc list -> acc^(nlink_gen list) ) ("\n// "^selector^"-edge stays in heap-domain or "^selector ^" field contains stack variable\n") (cartesianProduct [pids;svars]) 

let gen_all_rthru pids svars =
  let selector = "n" in
  let rthru_gen list =
    match list with
	[id;v] ->
	  "r"^ (args2str_sq [id;selector;v;])^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n"
      | _ -> failwith "Incorrect length of list " in
  List.fold_left (fun acc list -> acc^(rthru_gen list) ) "\n// Reachability from stack variables through a heap-domain\n" (cartesianProduct [pids;svars]) 


let gen_all_is_eq svars =
  let is_eq_gen list =
    match list with
	[v1;v2] ->
	  "is_eq"^ (args2str_sq [v1; v2])^ (if v1=v2 then "= 1\n" else " = 1/2\n")
      | _ -> failwith "Incorrect length of list " in
  List.fold_left (fun acc list -> acc^(is_eq_gen list) ) "\n//Equality between stack variables\n" (cartesianProduct [svars;svars]) 


(* Due to the caching and renaming we only need d0, d1, d2 *)
let rec new_hids id0 f = 
  (StringSet.add (hstr 0)(StringSet.add (hstr 1) (StringSet.add (hstr 2) StringSet.empty)))
  (* match f with *)
  (*     True | False | Emp | SingletonHeap _ | Ls _ | Eq _  *)
  (* 	-> StringSet.add (hstr id0) StringSet.empty *)
  (*   | And(f1, f2) | Or(f1, f2)  *)
  (*     -> StringSet.union (new_hids id0 f1 ) (new_hids id0 f2 ) *)
  (*   | SepAnd(f1, f2) | Septract(f1, f2)  *)
  (*     -> StringSet.add (hstr id0) (StringSet.union (new_hids (id0+1) f1 ) (new_hids (id0+2) f2 )) *)
  (*   | Not(f1) -> new_hids id0 f1 *)
  (*   |  _ -> *)
  (*     failwith "formula type not handled in new_hids" *)

;;

(* Return string of {h0, h1, ...} *)
let new_hnodes f = 
  args2str_curly (StringSet.elements (new_hids 0 f))  


let new_predicates f = 
  let selector = "n" in
  let pids = (StringSet.elements (new_hids 0 f)) in

  let vsort_str = "valSort = " ^ args2str_curly [sum_heap()] ^ "\n"in

  let hsort_str = "heapSort = " ^ args2str_curly [sum_heap()] ^ "\n\n" in 

  let heap_preds_str = 
     "// Predicates that model an arbitrary heap that has at least one cell\n" ^
     "sm = " ^ args2str_curly [sum_heap()^":1/2"]^ "\n"
  in
  let svars = (StringSet.elements (stackvars_ f))in

  let nlink_str = gen_all_nlink pids svars in

  let svars_str =  List.fold_left ( fun acc v -> acc^v^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "\n// Predicates to model the stack variables\n" svars in

  let hvars_str =  List.fold_left ( fun acc v -> acc^ v ^" = " ^ args2str_curly [sum_heap()^":1/2"]^"\n" ) "\n// Heap-domain predicates \n" pids in

  let singleton_str =  List.fold_left ( fun acc v -> acc^"singleton"^ args2str_sq [v ]^" = 1/2\n" ) "\n// Predicates indicating whether heap-domain is singleton \n" pids in

  let is_eq_str = gen_all_is_eq svars in

  let rthru_str = gen_all_rthru pids svars in 

  let c_str = List.fold_left( fun acc v ->   acc^"c"^args2str_sq[v;selector]^"= " ^ args2str_curly[sum_heap()^":0"]^"\n" ) ("\n// Acyclic heaps\n") pids in

  let nthru_str =  List.fold_left ( fun acc v -> acc^selector^ args2str_sq [v]^ "= " ^args2str_curly [sum_heap()^"->"^sum_heap()^":1/2"]^"\n" ) ("\n// "^selector^"-edge through heap-domain \n") pids in

  let tthru_str =  List.fold_left ( fun acc v -> acc^"t"^ args2str_sq [v;selector]^ "= " ^args2str_curly [sum_heap()^"->"^sum_heap()^":1/2"]^"\n" ) ("\n// Transitive "^selector^"-edge through heap-domain \n") pids in



  vsort_str ^ hsort_str ^ heap_preds_str ^
  svars_str ^ is_eq_str ^
  hvars_str ^ singleton_str ^ c_str ^ nthru_str ^tthru_str ^
   nlink_str ^ rthru_str 







