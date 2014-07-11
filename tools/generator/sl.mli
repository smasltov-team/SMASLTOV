(* The formula language *)

type variable =
  Var of string

type formula = 
  True
| False
| And of formula * formula
| Or of formula * formula
| Iff of formula * formula
| Ite of formula * formula * formula
| Not of formula
  (* Empty heap *)  
| Emp
  (* Separating conjunction *)
| SepAnd of formula * formula 
  (* Existential magic wand*)
| Septract of formula * formula
  (* Single heap *)
| SingletonHeap of variable * variable 
  (* List segment *)
| Ls of variable * variable  
  (* Existential quantifier *)
| Eq of variable * variable
(*| Exists of variable * formula *)

