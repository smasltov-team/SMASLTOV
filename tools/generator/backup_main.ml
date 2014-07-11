open Printf
open Sl 
open Printer 
open Tvpgen
open Tvsgen
open Txtgen
open Simplify

let listListPrepend x ll = List.map (fun l -> x :: l) ll

let rec cartesianProduct = function
  | aList :: listList ->
    let prev = cartesianProduct listList in
      List.flatten (
	List.map (fun x -> listListPrepend x prev) aList)
  | [] -> [[]]

(*k combinations*)
let extract k list =
  let rec aux k acc emit = function
    | [] -> acc
    | h :: t ->
        if k = 1 then aux k (emit [h] acc) emit t else
          let new_emit x = emit (h :: x) in
          aux k (aux (k-1) acc new_emit t) emit t
  in
  let emit x acc = x :: acc in
    aux k [] emit list;;



let magic_wand f1 f2 =
  Not(Septract(f1, Not(f2)))
;;

(*converts the validity check of f1=>f2*)
(*into the unsatisfiability check of f1 /\ !f2 *)
let valid_implies f1 f2 =
  And(Not(f2), f1)
;;

let print_all f fname = 
  print_tvp f fname;
  print_tvs f fname;
  print_txt f fname
;;

let print_all lst = 
  let make_cmd name = "make 'EX="^name^"_gen' 2>"^name^"_gen.out\n" in
  let oc = open_out "run_gen.sh" in

  List.iter (fun (f,fname) -> print_all (simplify f) fname; fprintf oc "%s" (make_cmd fname);) lst;

  close_out oc

let main() =

  let a1, a2, a3, a4, a5, a6, a7, a8, null = Var("a1"), Var("a2"), Var("a3"), Var("a4"), Var("a5"), Var("a6"), Var("a7"), Var("a8"), Var("null") in
  let e1, e2, e3, e4, e5, e6 = Var("e1"), Var("e2"), Var("e3"), Var("e4"), Var("e5"), Var("e6") in
  let  p, q, r =  Var("p"), Var("q"), Var("r") in


  let atoms v1 v2  = [ (SingletonHeap(v1,v2),   "PtsTo");     (Ls(v1,v2),              "Ls");
		       (SingletonHeap(v1,null), "PtsToNull"); (Ls(v1,null),            "LsNull");
		       (Emp,                    "Emp");       (True,                   "True");
		       (*Included the "intuitionistic" singleton heap predicate*)
	               (SepAnd(SingletonHeap(v1,v2),True ), "PtsToStarTrue")] in

  let neg_atms = List.map (fun (f,n) -> (Not(f), "Not"^n)) (atoms a1 a2) in
  let lits     = List.flatten [atoms a1 a2;neg_atms] in


  let formulas = [] in


  (* All literals *)
  let formulas = formulas @ lits in
  
  (* A & !A, where A is an atom *)
  let a_not_a = List.map (fun (a,n) -> (And(a, Not(a)), n^"AndNot"^n)) 
                         (atoms a1 a2)@ [(Eq(a1,a2),"Eq")]  in
  let formulas = formulas @ a_not_a in



  let simp_formulas = [
                        (valid_implies (SepAnd(SingletonHeap(a1,a2), True))  
                                       (SepAnd(Ls(a1,a2)           , True)), 
			  	       "PointsToImpliesLs")
		     ] in
  let formulas = formulas @ simp_formulas in

  let jstar_bench = [
             (*fun17 :: lseg(x1, nil) ==> lseg(x1, x1) * lseg(x1, nil). clones-01.sl*)
	     (And( Ls(a1,null), Not(SepAnd(Ls(a1,a1) ,Ls(a1,null) )  )), "funOneSeven");

             (*fun39 :: emp ==> lseg(nil, nil). clones-01.sl*)
	     (And(Emp, Not(Ls(null,null))), "funThreeNine")
   ] in
  let formulas = formulas @ jstar_bench in



  (*Formulae taken from Table 3 of "Proof Search for propositional abstract separation logics via labelled sequents", POPL'14 *)
  let popl14_table3 = [	  
	     ( SepAnd(SingletonHeap(e1,e2), SingletonHeap(e1,e3)), "popl14_table3_1");

	     (valid_implies  (And(SepAnd(SingletonHeap(e1,e2),SingletonHeap(e3,e4)),SepAnd(SingletonHeap(e1,e2), SingletonHeap(e5,e6)))  )  
		             (SepAnd(SingletonHeap(e3,e6),True)), 
	      "popl14_table3_2");  

	     (valid_implies (Septract(SingletonHeap(e1,e2), SingletonHeap(e3,e4)))
                            (And(Eq(e1,e3), And(Eq(e2,e4), Emp))),
	      "popl14_table3_4");
	     

	     (valid_implies (Septract(SepAnd(SingletonHeap(e1,p), SingletonHeap(e2,q)),
                                      SingletonHeap(e3,r)))
                            (Septract(SingletonHeap(e1,p),
                                      Septract(SingletonHeap(e2,q),SingletonHeap(e3,r)) )),
	      "popl14_table3_5");


	     (valid_implies (Septract(SingletonHeap(e1,p), 
                                      SingletonHeap(e2,q)))
                            (Septract(SingletonHeap(e1,p),
                                      And(SingletonHeap(e2,q), 
                                          SepAnd(SingletonHeap(e1,p), 
                                                 True)))),
              "popl14_table3_6")
                 ] in
  let formulas = formulas @ popl14_table3 in
  (*end Table 3 *)

  (*Formulae taken from Table 2 of                                                             *)
  (*"Proof Search for propositional abstract separation logics via labelled sequents", POPL'14 *)

  let popl14_table2_1_gen list =  
    match list with 
	[(a,n1); (b,n2)] -> (valid_implies (And(magic_wand a b,SepAnd(True, And(Emp,a)))) b,
			     "popl14_table2_1_"^n1^n2) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_1 = List.map (fun list -> popl14_table2_1_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4)]) in
  let formulas = formulas @ popl14_table2_1 in


  let popl14_table2_2_gen (a,n) = 
    (valid_implies ( magic_wand Emp (Not(SepAnd(Not(a),Emp)))) a,
     "popl14_table2_2_"^n) in
  let popl14_table2_2 = List.map (fun a -> popl14_table2_2_gen a) (atoms a1 a2) in
  let formulas = formulas @ popl14_table2_2 in
    
  let popl14_table2_3_gen list =  
    match list with 
	[(a,n1); (b,n2)] -> (And(magic_wand a (Not(SepAnd(a,b))),
	                         And(magic_wand (Not(a)) (Not(b)), b)),
			     "popl14_table2_3_"^n1^n2) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_3 = List.map (fun list -> popl14_table2_3_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4)]) in
  let formulas = formulas @ popl14_table2_3 in

  let popl14_table2_4_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3)] -> (valid_implies Emp 
                                           (magic_wand (magic_wand a (magic_wand b c) )
                                                       (magic_wand (SepAnd(a,b)) c )),
				     "popl14_table2_4_"^n1^n2^n3) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_4 = List.map (fun list -> popl14_table2_4_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4);(atoms a5 a6)]) in
  let formulas = formulas @ popl14_table2_4 in

  let popl14_table2_5_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3)] -> (valid_implies Emp 
                                           (magic_wand (SepAnd(a, SepAnd(b,c))) 
                                                       (SepAnd(SepAnd(a,b),c))),
				     "popl14_table2_5_"^n1^n2^n3) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_5 = List.map (fun list -> popl14_table2_5_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4);(atoms a5 a6)]) in
  let formulas = formulas @ popl14_table2_5 in


  let popl14_table2_6_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3); (e,n4)] -> (valid_implies Emp
                                                   (magic_wand (SepAnd(a,SepAnd(magic_wand b e,c)))
                                                               (SepAnd(SepAnd(a,magic_wand b e),c))),
					     "popl14_table2_6_"^n1^n2^n3^n4) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_6 = List.map (fun list -> popl14_table2_6_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4);(atoms a5 a6);(atoms a7 a8)]) in
  let formulas = formulas @ popl14_table2_6 in


  let popl14_table2_7_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3); (d,n4)] -> (And(magic_wand a (Not(SepAnd(Septract(d,SepAnd(a,SepAnd(c,b))),a))),
                                                SepAnd(c, And(d, SepAnd(a,b)))),
					     "popl14_table2_7_"^n1^n2^n3^n4) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_7 = List.map (fun list -> popl14_table2_7_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4);(atoms a5 a6);(atoms a7 a8)]) in
  let formulas = formulas @ popl14_table2_7 in




  (****************)
  (*MISSING 8,9 *)
  (****************)

  let popl14_table2_10_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3); (d,n4)] -> (valid_implies (SepAnd(a,SepAnd(b,SepAnd(c,d))))
                                                           (SepAnd(d,SepAnd(c,SepAnd(b,a)))),
					     "popl14_table2_10_"^n1^n2^n3^n4) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_10 = List.map (fun list -> popl14_table2_10_gen list ) (extract 4 (atoms a1 a2)) in
  let formulas = formulas @ popl14_table2_10 in


  let popl14_table2_11_gen list =  
    match list with 
	[(a,n1); (b,n2); (c,n3); (d,n4)] -> (valid_implies (SepAnd(a,SepAnd(b,SepAnd(c,d))))
                                                           (SepAnd(d,SepAnd(b,SepAnd(c,a)))),
					     "popl14_table2_11_"^n1^n2^n3^n4) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_11 = List.map (fun list -> popl14_table2_11_gen list ) (extract 4 (atoms a1 a2)) in
  let formulas = formulas @ popl14_table2_11 in

  let popl14_table2_12_gen list =  
    match list with 
	[(a,n1);(b,n2);(c,n3);(d,n4);(e,n5)] -> (valid_implies (SepAnd(a,SepAnd(b,SepAnd(c,SepAnd(d,e)))))
                                                               (SepAnd(e,SepAnd(d,SepAnd(a,SepAnd(b,c))))),
						 "popl14_table2_12_"^n1^n2^n3^n4^n5) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_12 = List.map (fun list -> popl14_table2_12_gen list ) 
                                  (extract 5 (atoms a1 a2)) in
  let formulas = formulas @ popl14_table2_12 in
  
  let popl14_table2_13_gen list =  
    match list with 
	[(a,n1);(b,n2);(c,n3);(d,n4);(e,n5)] -> (valid_implies (SepAnd(a,SepAnd(b,SepAnd(c,SepAnd(d,e)))))
                                                               (SepAnd(e,SepAnd(b,SepAnd(a,SepAnd(c,d))))),
						 "popl14_table2_13_"^n1^n2^n3^n4^n5) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_13 = List.map (fun list -> popl14_table2_13_gen list ) 
                                  (extract 5 (atoms a1 a2)) in
  let formulas = formulas @ popl14_table2_13 in

  (***                 **)
  (*MISSING 14,15,16,17 *)
  (***                 **)

  let f18 = Septract(True, Emp) in
  let popl14_table2_18 = [(valid_implies (SepAnd(f18,f18)) f18, "popl14_table2_18")] in
  let formulas = formulas @ popl14_table2_18 in


  let popl14_table2_19_gen list =  
    match list with 
	[(a,n1); (b,n2)] -> (valid_implies (And(Emp,SepAnd(a,b))) a,
			     "popl14_table2_19_"^n1^n2) 
      | _ -> failwith "Incorrect length of list " in

  let popl14_table2_19 = List.map (fun list -> popl14_table2_19_gen list ) (cartesianProduct [(atoms a1 a2);(atoms a3 a4)]) in
  let formulas = formulas @ popl14_table2_19 in
  
  (* end Table 2 *)

  (* Additional formulas not from the paper *)

  let nPPP = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6)))), "nPPP"] in
  
  let nPPP_noMeet = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6)))), "nPPP_noMeet"] in

  (*let tSANPTS = [And(SepAnd(SingletonHeap(a5,a6),SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4))),
                 Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6))))),
                 "twoStarAndNotPermutedTwoStar"] in*)
  
  let pPanPP = [And(SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4)),
                 Not(SepAnd(SingletonHeap(a3,a4),SingletonHeap(a1,a2)))),
                 "PPanPP_noMeet"] in

  let tSANPTS_noMeet = [And(SepAnd(SingletonHeap(a5,a6),SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4))),
                 Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6))))),
                 "twoStarAndNotPermutedTwoStar_noMeet"] in

  let tSANPTSwt = [And(SepAnd(True,SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4))),
                   Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),True)))),
                   "twoStarAndNotPermutedTwoStarWithTrue"] in

  let tSANPTSmm = [And(SepAnd(SingletonHeap(a5,a6),SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4))),
                   Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a7,a8))))),
                   "twoStarAndNotPermutedTwoStarMismatched"] in

  let tEP_nPET = [And(SepAnd(True,SepAnd(Emp, SingletonHeap(a1,a2))),
                  Not(SepAnd(SingletonHeap(a1,a2),SepAnd(Emp,True)))),
                  "TEP_nPET"] in

  let tPE_nEPT = [And(
                     SepAnd(True,SepAnd(SingletonHeap(a1,a2), Emp)),
                   Not(
                     SepAnd(Emp, SepAnd(SingletonHeap(a1,a2),True))
                   )
                 ),
                 "TPE_nEPT"] in

  let nPTTT = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(True,SepAnd(True,True)))), "nPTTT"] in
  let nPTT = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(True,True))), "nPTT"] in
  let nPT = [Not(SepAnd(SingletonHeap(a1,a2),True)), "nPT"] in

  (*let formulas = nPT @ nPTT @ nPTTT in*)
  (*let formulas = tSANPTS @ tSANPTSwt @ tSANPTSmm in*)
  (*let formulas = tEP_nPET @ tPE_nEPT in*)

  let plugin_lits = List.map (fun (f,n) -> (f, "Plugin_"^n)) (lits) in

  (*let formulas = pPanPP in*)

  let formulas = nPPP in

  (* Some regression tests: *)

  let reg_pointsToAndNotLs = [And(SingletonHeap(a1,a2),Not(Ls(a1,a2))), "regression_PointsToAndNotLs"] in
  let reg_lsAndNotPointsTo = [And(Ls(a1,a2),Not(SingletonHeap(a1,a2))), "regression_LsAndNotPointsTo"] in
  let reg_lsAndNotLs       = [And(Ls(a1,a2),Not(Ls(a1,a2))), "regression_LsAndNotLs"] in
  let reg_empStarEmp       = [SepAnd(Emp, Emp), "regression_EmpStarEmp"] in
  let reg_notEmpStarEmp    = [Not(SepAnd(Emp, Emp)), "regression_NotEmpStarEmp"] in
  let reg_nPPP             = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6)))), "regression_nPPP"] in
  let reg_TSANPTS          = [And(SepAnd(SingletonHeap(a5,a6),SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4))),
                              Not(SepAnd(SingletonHeap(a1,a2),SepAnd(SingletonHeap(a3,a4),SingletonHeap(a5,a6))))),
                              "regression_twoStarAndNotPermutedTwoStar"] in
  let reg_nPTT             = [Not(SepAnd(SingletonHeap(a1,a2),SepAnd(True,True))), "regression_nPTT"] in
  let reg_nPT              = [Not(SepAnd(SingletonHeap(a1,a2),True)), "regression_nPT"] in
  let reg_septractSimple   = [Septract(SingletonHeap(a1,a2),Ls(a1,a3)), "regression_septractSimple"] in
  let reg_septractList     = [And(Not(Ls(a1,a3)),Septract(SingletonHeap(a1,a2),Ls(a1,a3))), "regression_septractList"] in
  let reg_septractConflict = [And(And(Not(Ls(a1,a3)),Not(Ls(a1,a5))),
                                  And(Septract(SingletonHeap(a1,a2),Ls(a1,a3)),
                                      Septract(SingletonHeap(a1,a4),Ls(a1,a5)))),
                              "regression_septractConflict"] in
  let reg_septractOverlap  = [And(SingletonHeap(a1,a2),Septract(SingletonHeap(a1,a2),Ls(a1,a3))), "regression_septractOverlap"] in

  let regressions = reg_pointsToAndNotLs @ reg_lsAndNotPointsTo @
                    reg_lsAndNotLs @ reg_empStarEmp @ reg_notEmpStarEmp @ reg_nPPP @ 
                    reg_TSANPTS @ reg_nPTT @ reg_nPT @ reg_septractSimple @ 
                    reg_septractList @ reg_septractConflict @ regression_septractOverlap in

  (*let formulas = tSANPTS_noMeet in *)

  print_all formulas
  (*print_all regressions*)
;;

main();;

