
  
let args2str args =
  match args with
      [] -> "()"
    | f::ar ->  (List.fold_left ( fun acc x -> acc ^", " ^ x ) ("("^f) ar)^")"

let args2str_none args =
  match args with
      [] -> ""
    | f::ar ->  (List.fold_left ( fun acc x -> acc ^", " ^ x ) (f) ar)


let args2str_sq args =
  match args with
      [] -> "[]"
    | f::ar ->  (List.fold_left ( fun acc x -> acc ^", " ^ x ) ("["^f) ar)^"]"

let args2str_curly args =
  match args with
      [] -> "{}"
    | f::ar ->  (List.fold_left ( fun acc x -> acc ^", " ^ x ) ("{"^f) ar)^"}"

let list_max = function
    [] -> invalid_arg "empty list"
  | x::xs -> List.fold_left max x xs



let listListPrepend x ll = List.map (fun l -> x :: l) ll

let rec cartesianProduct = function
  | aList :: listList ->
    let prev = cartesianProduct listList in
      List.flatten (
	List.map (fun x -> listListPrepend x prev) aList)
  | [] -> [[]]
