open Sl
open Utils
open Printer



let rec rewrite_formula rewriter f = 
  let fmrwt = rewrite_formula rewriter in
  match f with
      True 
    | False
    | Emp 
    | Ls _ 
    | Eq _ 
    | SingletonHeap _ -> rewriter f

    | And(f1, f2) ->  rewriter (And(fmrwt f1, fmrwt f2))
    | Or(f1, f2) ->   rewriter (Or(fmrwt f1, fmrwt f2))
    | Iff(f1, f2) ->  rewriter (Iff(fmrwt f1, fmrwt f2))
    | Ite(f1, f2, f3) -> rewriter (Ite(fmrwt f1, fmrwt f2, fmrwt f3))
    | Not(f1) ->  rewriter (Not(fmrwt f1))

    | SepAnd(f1, f2) -> rewriter (SepAnd(fmrwt f1, fmrwt f2))

    | Septract(f1, f2) -> rewriter (Septract(fmrwt f1, fmrwt f2))
;;

let simplify = 

  let rec psimplify f = 
    match f with
      | Not False -> True
      | Not True  -> False
      | Not(Not p) -> p

      | And(False, f1) | And(f1,False) -> False
      | And(True, f1) | And(f1,True)  -> f1
      | Or(False, f1) | Or(f1,False) -> f1
      | Or(True, f1) | Or(f1, True) -> True  

      | Iff(False, False) -> True
      | Iff(f1,True)   | Iff(True,f1) -> f1
      | Iff(False, f1) | Iff(f1, False) -> Not f1

      | Ite(True, f2, f3) -> f2 
      | Ite(False,f2, f3) -> f3

      | SepAnd(Emp, f1) |SepAnd(f1, Emp) | Septract(Emp, f1) ->  f1

  (*| SepAnd(True, True) -> True *)
      | _ -> f in
  rewrite_formula psimplify 
;;
