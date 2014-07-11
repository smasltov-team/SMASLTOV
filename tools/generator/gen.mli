val generate_integrity : Sl.formula -> string
val triplets : Sl.formula -> string
val hnodes : Sl.formula -> string
val fnodes : Sl.formula -> string
val stackvars : Sl.formula -> string
val garbage_rule : Sl.formula -> string
val proof_rules: Sl.formula -> string


val new_hnodes : Sl.formula -> string 

val new_proof_rules: Sl.formula -> string
val batch_element_proof_rules: Sl.formula -> string -> bool -> (string * string)

val reset_lblcount : unit -> unit

val individuals: Sl.formula -> string
val predicates: Sl.formula ->string

val new_individuals: Sl.formula -> string
val new_predicates: Sl.formula ->string
