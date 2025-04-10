(ns monkey.scw.openapi-test
  (:require [clojure.test :refer [deftest testing is]]
            [monkey.scw.openapi :as sut]))

(deftest patch-schema
  (testing "converts boolean default value from string"
    (is (true? (-> {:type "boolean"
                    :default "true"}
                   (sut/patch-schema)
                   :default))))

  (testing "converts int default value from string"
    (is (= 10 (-> {:type "integer"
                   :default "10"}
                  (sut/patch-schema)
                  :default))))

  (testing "leaves correct types as-is"
    (is (= 10 (-> {:type "integer"
                   :default 10}
                  (sut/patch-schema)
                  :default)))))
