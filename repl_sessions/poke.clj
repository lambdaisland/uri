(ns poke
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as norm]))

(:fragment (norm/normalize (uri/map->URI {:fragment "'#'schön"})))
"'%23'sch%C3%B6n"

(norm/normalize-fragment "schön")
;; => "sch%C3%B6n"
