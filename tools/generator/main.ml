open Printf
open Sl 
open Printer 
open Tvpgen
open Tvsgen
open Txtgen
open Simplify
open Utils
open Nnf

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

let null = Var("null")
;;

let not_null v = Not(Eq(v,null))
;;
let not_eq   v1 v2 = Not(Eq(v1,v2)) 
;;


let magic_wand f1 f2 =
  Not(Septract(f1, Not(f2)))
;;

(*converts the validity check of f1=>f2*)
(*into the unsatisfiability check of f1 /\ !f2 *)
let valid_implies f1 f2 =
  And(Not(f2), f1)
;;

let implies f1 f2 = Or(Not(f1), f2);;


let print_tex f fname = 
  let tex_file = fname^"_gen.tex" in
  let oc = open_out tex_file in
  fprintf oc "\\begin{comment}%s\\end{comment} %s" fname (toTex f);

  close_out oc;
  print_endline ("Generated "^tex_file);
;;



let print_all f fname = 
  new_print_tvp f fname;
  new_print_tvs f fname;
  print_txt f fname;
  print_tex f fname
;;

let print_all lst = 
  let make_cmd name = "make 'EX="^name^"_gen' 2>"^name^"_gen.out\n" in
  let oc = open_out "run_gen.sh" in
  let texf = open_out "formulasGen.tex" in

  List.iter (fun (f,fname) -> print_endline ("Formula "^ (toString f) ^ "\n"); 
                             print_all (nnf ( simplify (* ON *) f)) fname; 
                             fprintf oc "%s" (make_cmd fname);
                             ) lst;

  close_out texf; 
  close_out oc

let disjunction_formula lst = (* expects a list of (formula, formula_name) pairs *)
  simplify
  (List.fold_left (fun f1 f2 -> (let (f2f, f2name) = f2 in
                                   Or(f1,f2f)))
                  False
                  lst)
;;

let batch_print_all lst fname = 

  let simple_lst = List.map (fun (e, ename) -> (simplify e, ename)) lst in

  (* Generate tvs file using disjunction of all (simplified) formulas *)
  let disj_f = disjunction_formula simple_lst in 

  new_print_tvs disj_f fname;

  batch_print_tvp  simple_lst disj_f fname
;;
  


let main() =

  let a1, a2, a3, a4, a5, a6, a7, a8 = Var("a1"), Var("a2"), Var("a3"), Var("a4"), Var("a5"), Var("a6"), Var("a7"), Var("a8") in
  let e1, e2, e3, e4, e5, e6 = Var("e1"), Var("e2"), Var("e3"), Var("e4"), Var("e5"), Var("e6") in
  let  p, q, r =  Var("p"), Var("q"), Var("r") in


  let atoms v1 v2  = [ (SingletonHeap(v1,v2),   "PtsTo_"^(var_to_str v1) ^"_"^(var_to_str v2));     (Ls(v1,v2),              "Ls_"^(var_to_str v1) ^"_"^(var_to_str v2));
		       (Emp,                    "Emp");       (True,                   "True");
		       (*Included the "intuitionistic" singleton heap predicate*)

	           (*    (SepAnd(SingletonHeap(v1,v2),True ), "PtsToStarTrue_"^(var_to_str v1) ^"_"^(var_to_str v2)) (* REMOVED in positive fragment *) *) 
                   
                   ] in

  let literals v1 v2 = List.flatten [atoms v1 v2; List.map (fun (f,n) -> (Not(f), "Not"^n)) (atoms v1 v2)] in


  (*******************************************************************************************)
  (*                            TABLE 5  UNSAT TESTS (BENCHMARKS)                            *)
  (*******************************************************************************************)

  (*Formulae taken from Table 2 of                                                           *)
  (*"Proof Search for propositional abstract separation logics via labelled sequents",POPL'14*)

  let popl14_table2_15_gen list = 
    match list with
	[(a,n1); (b,n2); (c,n3)] -> ( (And(Emp, 
                                            And(a, 
                                                SepAnd(b,
                                                       Not(magic_wand c (implies Emp a)))))),
				     "group2N_popl14_table2_15_"^n1^n2^n3)
      | _ -> failwith "Incorrect length of list" in

  let popl14_table2_15 = List.map (fun list -> popl14_table2_15_gen list ) (cartesianProduct [(literals a1 a2);(literals a3 a4);(literals a5 a6)]) in 

  (*******************************************************************************************)
  (*                            TABLE 6  UNSAT TESTS (BENCHMARKS)                            *)
  (*******************************************************************************************)

  let popl14_table2_19_gen list =
    match list with
	[(a,n1); (b,n2)] -> (valid_implies (And(Emp,SepAnd(a,b))) a,
			     "group1N_popl14_table2_19_"^n1^n2)
      | _ -> failwith "Incorrect length of list 19" in

  let popl14_table2_19 = List.map (fun list -> popl14_table2_19_gen list ) (cartesianProduct [(literals a1 a2);(literals a3 a4)]) in

  (*end Table 2 *)

  let ls_plus v1 v2 = And(Ls(v1,v2),Not(Eq(v1,v2))) in
  let contains f v1 temp =  And(f, SepAnd(SingletonHeap(v1,temp), True)) in

  (*******************************************************************************************)
  (*                                         SAT TESTS                                       *)
  (*******************************************************************************************)

  let examples = [] in

  let examples = examples @ [And(SepAnd(Ls(a1,a3),Ls(a3,a4)),SepAnd(Ls(a1,a2),contains (Ls(a2,a4)) a3 e1)), "exp_sat_lsOverlap"] in
  let examples = examples @ [And(SingletonHeap(a1,a2),SingletonHeap(a3,a4)), "exp_sat_singleton_eq"] in
  let examples = examples @ [SepAnd(Ls(a1,a2),Ls(a2,a1)), "exp_sat_ls_xyyx"] in
  let examples = examples @ [And(SepAnd(SingletonHeap(a1,a2), SingletonHeap(a2,a3)), SepAnd(Ls(a1,a2),SingletonHeap(a2,a3))), "exp_sat_pts_star_in_ls"] in 

  (* Leaf tests *)
  (* These formulas correspond to the leaf structures for ls, not-ls, points-to, and not-points-to.
     These are designed so that, if a leaf structure were made unsatisfiable by a buggy change to
       a leaf structure or a buggy change to a constraint rule, then one of these tests would 
       become unsatisfiable.
     At the end of each formula name there is a code e.g. "NotPts2" that you can grep for in the source
       file PrefabExpression.java to see the content of the leaf structure that corresponds to that 
       formula.  In some cases, there are two formulas for one structure, e.g. leaf_sat_NotPts2_a, 
       to cover two different cases of how a separation logic formula can describe that structure.
       Note that these formulas are not exact descriptions of the leaf content; the formulas are
       more specific than the leaf structures.  *) 
  (* Note that we could add similar tests for Eq, Not-Eq, Emp, Not-Emp and True, but those are
       effectively subsumed by these tests, in the following sense: if any of those structures were 
       made UNSAT, then at least one of these tests would become UNSAT. *)

  let leaf_sat = [] in

  let leaf_sat = leaf_sat @ [And(And(Ls(a1,a2),Eq(a1,a2)),Emp), "leaf_sat_Ls1"] in
  let leaf_sat = leaf_sat @ [And(And(Ls(a1,a2),Not(Eq(a1,a2))),SingletonHeap(a1,a2)), "leaf_sat_Ls2"] in
  let leaf_sat = leaf_sat @ [And(And(Ls(a1,a2),Not(Eq(a1,a2))),SepAnd(SingletonHeap(a1,e1),Not(Emp))), "leaf_sat_Ls3"] in

  let leaf_sat = leaf_sat @ [       And(And(Not(Ls(a1,a2)),Not(Eq(a1,a2))),Emp), "leaf_sat_NotLs1"] in 
  let leaf_sat = leaf_sat @ [       And(And(Not(Ls(a1,a2)),Eq(a1,a2)),Not(Emp)), "leaf_sat_NotLs2"] in 
  let leaf_sat = leaf_sat @ [SepAnd(And(And(Not(Ls(a1,a2)),Not(Eq(a1,a2))),Not(Emp)),SingletonHeap(a1,e1)), "leaf_sat_NotLs3_a"] in (* case a *)
  let leaf_sat = leaf_sat @ [   And(And(And(Not(Ls(a1,a2)),Not(Eq(a1,a2))),Not(Emp)),Eq(a1,null)), "leaf_sat_NotLs3_b"] in (* case b *)
  let leaf_sat = leaf_sat @ [       And(And(Not(Ls(a1,a2)),SingletonHeap(a1,a3)),And(Not(Eq(a2,a3)),Not(Eq(a1,a2)))), "leaf_sat_NotLs4"] in 
  let leaf_sat = leaf_sat @ [       And(And(Not(Ls(a1,a2)),SepAnd(Ls(a1,a2),Ls(a4,a2))),And(Not(Eq(a2,a4)),Not(Eq(a1,a2)))), "leaf_sat_NotLs5"] in 
  let leaf_sat = leaf_sat @ [       And(And(Not(Ls(a1,a2)),And(And(Ls(a1,a3),Not(Emp)),Not(SingletonHeap(a1,a3)))),And(Not(Eq(a2,a3)),Not(Eq(a1,a2)))), "leaf_sat_NotLs6"] in 

  let leaf_sat = leaf_sat @ [SingletonHeap(a1,a2), "leaf_sat_Pts1"] in 

  let leaf_sat = leaf_sat @ [       And(Not(SingletonHeap(a1,a2)),Emp), "leaf_sat_NotPts1"] in 
  let leaf_sat = leaf_sat @ [SepAnd(And(Not(SingletonHeap(a1,a2)),Not(Emp)),SingletonHeap(a1,a3)), "leaf_sat_NotPts2_a"] in (* not points to #2, case a *)
  let leaf_sat = leaf_sat @ [   And(And(Not(SingletonHeap(a1,a2)),Eq(a1,null)),Not(Emp)), "leaf_sat_NotPts2_b"] in (* not points to #2, case b *)
  let leaf_sat = leaf_sat @ [   And(And(Not(SingletonHeap(a1,a2)),SingletonHeap(a1,a3)),Not(Eq(a2,a3))), "leaf_sat_NotPts3"] in
  let leaf_sat = leaf_sat @ [   And(And(Not(SingletonHeap(a1,a2)),SepAnd(SingletonHeap(a1,a3),SingletonHeap(a4,a5))),And(And(Not(Eq(a2,a3)),Not(Eq(a5,a2))),Not(Eq(a3,a4)))), "leaf_sat_NotPts4"] in
  let leaf_sat = leaf_sat @ [       And(Not(SingletonHeap(a1,a2)),SepAnd(SingletonHeap(a1,a2),Not(Emp))), "leaf_sat_NotPts5"] in 

  let mid2014_sat = [] in 
  let mid2014_sat = mid2014_sat @ 
                                [ And(    
                                      And(SepAnd(Ls(a1,a4),SepAnd(SingletonHeap(a2,e2),True)),SepAnd(Ls(a2,a4),SepAnd(SingletonHeap(a1,e1),True))) ,
                                      And(And(Not(Eq(a1,a2)),Not(Eq(a4,null))),And(Not(Eq(a1,a4)),Not(Eq(a2,a4))))
                                     ),   
                                 "mid2014_sat_ls_multi_startpoint" ] 
                                @ [ And( SepAnd(Ls(a1,a4),SepAnd(SingletonHeap(a2,e2),True)), And(And(Eq(e2,a4),Not(Eq(e2,null))),Not(Eq(a1,a4)))), "mid2014_sat_test1" ] 
                                @ [ And( SepAnd(SingletonHeap(a1,a4),SingletonHeap(a2,e2)), And(Eq(e2,a4),Not(Eq(e2,null)))), "mid2014_sat_test2" ]
                                @ [ SepAnd(SingletonHeap(a1,a4),SingletonHeap(a2,e2)), "mid2014_sat_test3" ]
                                @ [ And( And(SepAnd(Ls(a1,a3),True),SepAnd(Ls(a2,a3),True)), Not(Eq(a3,null)) ), "mid2014_sat_simple_ls2" ] 
                                in 

  let pfrag_sat = [] in
  let pfrag_sat = pfrag_sat @ [And(Ls(a1,a2),Not(SingletonHeap(a1,a2))), "pfrag_sat_LsAndNotPointsTo"] in
  let pfrag_sat = pfrag_sat @ [SepAnd(Ls(a1,a2),Ls(a2,a1)), "pfrag_sat_ls_xyyx"] in
  let pfrag_sat = pfrag_sat @ [ And(And(SepAnd(Ls(a1,a2),True),SepAnd(Ls(a1,a3),True)),And(Not(Emp), Not(Eq(a2,a3)))), "pfrag_sat_ls_multi_endpoint"] in
  
  let all_sat_tests = examples @ leaf_sat @ mid2014_sat @ pfrag_sat in

  (*******************************************************************************************)
  (*                            TABLE 4  UNSAT TESTS (BENCHMARKS)                            *)
  (*******************************************************************************************)

  let pfrag = [] in
 
  (* The P number in the name of each of the following formulas indicates the corresponding
     row in table 4 in the SPIN 2014 publication. *)

  let pfrag = pfrag @ [And(SingletonHeap(a1,a2),Not(Ls(a1,a2))), "pfrag_unsat_P01_PointsToAndNotLs"] in

  let pfrag = pfrag @ [And(Septract(SingletonHeap(a1,a2),True),SepAnd(SingletonHeap(a1,a2),True)), "pfrag_unsat_P08_septract_domain_conflict"] in

  let pfrag = pfrag @ [SepAnd(SingletonHeap(a1,a2),SingletonHeap(a2,a1)), "pfrag_unsat_P02_pts_acyclic"] in
  
  let pfrag = pfrag @ [And(Not(Emp), SepAnd(Ls(a1,a2), Ls(a2,a1))),"pfrag_unsat_P03_lsxyyx_not_emp"] in
 
  let pfrag = pfrag @ [And(Not(Eq(a1,a2)), SepAnd(Ls(a1,a2), Ls(a2,a1))),"pfrag_unsat_P04_lsxyyx_not_eq"] in

  let pfrag = pfrag @ [And(And(Ls(a1,a2), Emp), Not(Eq(a1,a2))), "pfrag_unsat_P06_lsxy_emp_not_eq"] in

  let pfrag = pfrag @ [ And(Septract(SingletonHeap(a1,a2), Ls(a1,a3)),
                            Or(Or(Not(Ls(a2,a3)), contains True a1 e1), Eq(a1,a3))),
                        "pfrag_unsat_P19_septract_basic"] in

  let pfrag = pfrag @ [ And(SepAnd(Ls(a1,a2),Ls(a2,a3)),Not(Ls(a1,a3))), "pfrag_unsat_P05_ls_transitive"] in

  let pfrag = pfrag @ [ And(SepAnd(Ls(a1,a2),Not(Ls(a2,a3))),Ls(a1,a3)), "pfrag_unsat_P09_ls_transitive_neg_down"] in

  let pfrag = pfrag @ [ And(And(Ls(a1,a2),Ls(a1,a3)),And(Not(Emp), Not(Eq(a2,a3)))), "pfrag_unsat_P10_ls_unique_endpoint"] in

  let pfrag = pfrag @ [  And(not_eq a1 a4,And(Septract(Ls(a1,a4), Ls(e1,e2)),
                             And(Eq(a4,e2),Not(Ls(e1,a1))))),
                       "pfrag_unsat_P21_star_septract_left2" ] in

  let pfrag = pfrag @ [  And(Septract(ls_plus a1 a2, Ls(e1,e2)),
                             (*Or( *)
                                And(Not(Eq(e1,a1)),And(Eq(e2,a2),Not(Ls(e1,a1))))
                                (* , False) *)
                                ),
                                (* And(Not(Eq(e2,a2)),And(Eq(e1,a1),Not(Ls(a2,e2)))))), *)
                       "pfrag_unsat_P20_septract_left_right" ] in

  let pfrag = pfrag @ [  And(Septract(
                                ls_plus a1 a2, Ls(e1,e2)
                                     ),
                             And(
                                 Not(Eq(e2,a2)),
                                 And(Eq(e1,a1),
                                     Not(Ls(a2,e2)))
                                 )
                            ),
                       "pfrag_unsat_P22_septract_right3" ] in

  let pfrag = pfrag @ [ And(And(Ls(a1,a2), SepAnd(Not(Emp),Not(Emp))),Or(Or(Eq(a1,null),SingletonHeap(a1,e1)),SepAnd(And(SingletonHeap(a1,e1),Eq(e1,null)),True))), 
                       "pfrag_unsat_P13_ls_singleton2"] in

  let pfrag = pfrag @ [ And(Septract(Septract(SingletonHeap(a2,a3),Ls(a2,a4)),Ls(a3,a1)),Or(Not(Ls(a4,a1)),Eq(a2,a4))), 
                       "pfrag_unsat_P23_double_septract_ls" ] in

  let pfrag = pfrag @ [ And(Septract(Septract(SingletonHeap(a2,a3),Ls(a2,a4)),Ls(a3,a1)),Eq(a2,a4)), 
                       "pfrag_unsat_P18_double_septract_ls_eq_only" ] in

  let pfrag = pfrag @ [ And(Septract(Septract(SingletonHeap(a2,a3),Ls(a2,a4)),Ls(a1,a4)),Not(Ls(a1,a3))), 
                       "pfrag_unsat_P17_double_septract_ls_both_sides" ] in

  let pfrag = pfrag @ [ And(Septract(SingletonHeap(a3,a4), Ls(a1,a4)), Or(Eq(a3,a4),Not(Ls(a1,a3)))), "pfrag_unsat_P16_concur07_right_reduced2" ] in

  let pfrag = pfrag @ [ And(SepAnd((ls_plus a1 a2), (ls_plus a2 a3)), SepAnd(SepAnd(ls_plus a4 a1, SingletonHeap(a1,e1)), True)), "pfrag_unsat_P14_overlap"] in 

  let pfrag = pfrag @ [ And(Septract(Ls(a1,a2), Ls(a1,a2)), Not(Emp)), "pfrag_unsat_P15_septract_ls_self_implies_emp" ] in

  let pfrag = pfrag @ [ And(SepAnd(SepAnd(Ls(a1,a2),True), SingletonHeap(a3,a4)), 
                            SepAnd(True, And(Ls(a2,a1),Not(Eq(a2,a1))))),
                        "pfrag_unsat_P11_ls_plus_two_step_cycle2" ] in

  let pfrag = pfrag @ [ And(And(SepAnd(SingletonHeap(a1,a2), True), SepAnd(SingletonHeap(a2,a3), True)), SepAnd(True, SingletonHeap(a3,a1))),
                        "pfrag_unsat_P07_pts_three_step_cycle3" ] in

  let pfrag = pfrag @ [ And(And(SepAnd(SingletonHeap(a1,a2), Ls(e1,e2)), SepAnd(SingletonHeap(a2,a3), Not(Emp))), SepAnd(SepAnd(SingletonHeap(a3,a1),Not(SingletonHeap(a5,a6))),True)),
                        "pfrag_unsat_P12_pts_three_step_cycle2" ] in

  (*******************************************************************************************) 

  (*******************************************************************************************)

  (* ADD NEW FORMULAS HERE: *)

  let other = [] in

  (* 
     Using the lines below as a model, you can add new formulas to this system. 
     To add a new formula, copy one of the lines below, uncomment it by removing 
       the (* and *) around the code, and modify the formula content 
       (such as SingletonHeap(a1,a2) ).  Give your formula a new name (instead 
       of "Example1"; formula names must be distinct).  The name will be used to 
       report the satisfiable/unsatisfiable outcome and running time of your 
       formula.
     Then, save main.ml.
     Run "make".
     Run "./main.native".
     You should see printed output confirming that files have been created for your
       new formula.  Your formulas will be represented by two files called:
          batch_other_gen.tvp
       and
          batch_other_gen.tvs
     Copy those two files to the directory "../../benchmarks/" (that directory
       contains some common TVLA input files that are required to run any formula).
     Now, you can run the semi-decision procedure on your formula by using the Python 
       script in "../.." (the directory called "SMASLTOV") called "run_table.py"
     Run it from the command line, like:

       python run_batch.py benchmarks/batch_other
       
     Some output will appear on the command line; more detailed results will also
       be recorded as text files in the "benchmarks" directory and in the current
       directory.
  *)


  (* let other = other @ [ SepAnd(SingletonHeap(a1,a2),SingletonHeap(a3,a4)), "Example1" ] in *)

  (* let other = other @ [ Septract(SingletonHeap(a1,a2),Ls(a3,a4)), "Example2" ] in *)

  (*
     Brief guide to writing formulas:

     If x and y are stack variables and PHI and PSI are formulas, then a formula can be:

     SingletonHeap(x,y)        meaning  [x |-> y]      (x points-to y)
     Ls(x,y)                   meaning  ls(x,y)        (list segment from x to y)
     Emp                       
     True                      
     Eq(x,y)                   meaning  x == y

     Not(PHI)                  
     And(PHI,PSI)
     Or(PHI,PSI)
     SepAnd(PHI,PSI)           meaning PHI * PSI       (separating conjunction)   
     Septract(PHI,PSI)         meaning PHI --( * ) PSI (septraction)

     Lines near the top of this file have declared the following stack variables that
       you may use (or you may modify those lines to add more stack variables):

       a1, a2, a3, a4, a5, a6, a7, a8, e1, e2, e3, e4, e5, e6, p, q, r

     It is possible using the above primitives to construct a formula that falls outside
       of the fragment of separation logic described in the CAV 2014 submission.  
     Attempting to generate TVLA input files for such a formula should produce an error message.
       
  *)

  (*******************************************************************************************)

   (batch_print_all pfrag "batch_table4")
   ;
   (batch_print_all popl14_table2_19 "batch_table5")
   ;
   (batch_print_all popl14_table2_15 "batch_table6")
   ;
   (batch_print_all all_sat_tests "batch_sat") 
   ;
   (if (List.length other) > 0 then (batch_print_all other "batch_other") )

;;

main();;

