(ns lambdaisland.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [lambdaisland.uri-test]))

(doo-tests 'lambdaisland.uri-test)
