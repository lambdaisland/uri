(ns lambdaisland.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [lambdaisland.uri-test]
            [lambdaisland.uri.normalize-test]))

(doo-tests 'lambdaisland.uri-test
           'lambdaisland.uri.normalize-test)
