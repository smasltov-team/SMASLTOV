open Sl
open Utils

let var_to_str v =
match v with Var(s) -> s
;;  



let rec formula_to_str f = 
match f with
  True -> "True"
| False -> "False"
| And(f1, f2) ->  "(" ^ (formula_to_str f1) ^ ") & (" ^ (formula_to_str f2) ^ ")"
| Or(f1, f2) -> "(" ^ (formula_to_str f1) ^ ") | (" ^ (formula_to_str f2) ^ ")"
| Iff(f1, f2) -> "(" ^ (formula_to_str f1) ^ ") <--> (" ^ (formula_to_str f2) ^ ")"
| Ite(f1, f2, f3) -> "(" ^ (formula_to_str f1) ^ ") ? (" ^ (formula_to_str f2) ^ ") : (" ^ (formula_to_str f3) ^ ")"
| Not(f1) -> "!(" ^ (formula_to_str f1)^")"
  (* Empty heap *)  
| Emp -> "Emp"
  (* Separating conjunction *)
| SepAnd(f1, f2) -> "(" ^ (formula_to_str f1) ^ ") * (" ^ (formula_to_str f2) ^ ")"
  (* Separating implication *)
| Septract(f1, f2) -> "(" ^ (formula_to_str f1) ^ ") --(*) (" ^ (formula_to_str f2) ^ ")"
  (* Single heap *)
| SingletonHeap(v1, v2) -> "[ " ^ (var_to_str v1) ^ " |-> " ^ (var_to_str v2) ^ "]"
  (* List segment *)
| Ls(v1, v2) -> "ls"^ (args2str [(var_to_str v1);(var_to_str v2)])
| Eq(v1, v2) ->  var_to_str v1 ^ " == " ^ var_to_str v2
  (* Existential quantifier *)
(* | Exists(v1, f1) -> "E"^ (args2str [var_to_str v1] ) ^ (formula_to_str f1) *)
;;


let toString = formula_to_str 
;;



let rec formula_to_tex f = 
match f with
  True -> "\\True "
| False -> "\\False "
  (* Empty heap *)  
| Emp -> "\\Emp "
| Not(Emp) -> "\\lnot \\Emp "
| Ls(v1, v2) -> "\\Ls"^ (args2str [(var_to_str v1);(var_to_str v2)])
| Not(Ls(v1, v2)) -> "\\lnot \\Ls"^ (args2str [(var_to_str v1);(var_to_str v2)])
| Eq(v1, v2) ->  var_to_str v1 ^ " = " ^ var_to_str v2
| Not(Eq(v1, v2)) ->  var_to_str v1 ^ " \\neq " ^ var_to_str v2
| SingletonHeap(v1, v2) -> (var_to_str v1) ^ "\\PtsTo " ^ (var_to_str v2) 
| Not(SingletonHeap(v1, v2)) -> "\\lnot "^(var_to_str v1) ^ "\\PtsTo " ^ (var_to_str v2) 
| And(f1, f2) ->  "(" ^ (formula_to_tex f1) ^ "\\land " ^ (formula_to_tex f2) ^ ")"
| Or(f1, f2) -> "(" ^ (formula_to_tex f1) ^ " \\lor " ^ (formula_to_tex f2) ^ ")"
| Iff(f1, f2) -> "(" ^ (formula_to_tex f1) ^ ") \\iff (" ^ (formula_to_tex f2) ^ ")"
| Ite(f1, f2, f3) -> "(" ^ (formula_to_tex f1) ^ ") ? (" ^ (formula_to_tex f2) ^ ") : (" ^ (formula_to_tex f3) ^ ")"

| Not(f1) -> "\\lnot(" ^ (formula_to_tex f1)^")"
  (* Separating conjunction *)
| SepAnd(f1, f2) -> "(" ^ (formula_to_tex f1) ^ " \\SepAnd " ^ (formula_to_tex f2) ^ ")"
  (* Separating implication *)
| Septract(f1, f2) -> "(" ^ (formula_to_tex f1) ^ " \\Septract " ^ (formula_to_tex f2) ^ ")"
  (* Single heap *)

  (* List segment *)
  (* Existential quantifier *)
(* | Exists(v1, f1) -> "E"^ (args2str [var_to_str v1] ) ^ (formula_to_tex f1) *)
;;


let toTex = formula_to_tex
;;
