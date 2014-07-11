open Sl
open Utils
open Printer

let rec nnf f = 
  match f with 
      True 
    | False 
    | SingletonHeap _ 
    | Ls  _ 
    | Eq _ 
    | Emp 

    | Not(True)
    | Not(False)
    | Not(SingletonHeap _)
    | Not(Ls  _)
    | Not(Eq _)
    | Not(Emp) -> f


    | And(f1, f2) -> And(nnf f1, nnf f2)
    | Or(f1, f2) -> Or(nnf f1, nnf f2)
      
    | SepAnd(f1, f2) -> SepAnd(nnf f1, nnf f2)
    | Septract(f1, f2) -> Septract(nnf f1, nnf f2) 

    | Not(Not p) -> nnf p
    | Not(And(f1, f2)) -> Or(nnf (Not f1), nnf (Not f2))
    | Not(Or(f1, f2)) -> And(nnf (Not f1), nnf (Not f2))
      
    | Not(SepAnd(f1, f2)) -> Not(SepAnd(nnf f1, nnf f2))
    | Not(Septract(f1, f2)) -> Not(Septract(nnf f1, nnf f2))
    | _ -> failwith "Formula not supported in nnf"
  
