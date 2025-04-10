(ns monkey.scw.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [martian.core :as m]
            [monkey.scw.core :as sut]))

(deftest add-auth-header
  (let [{:keys [enter] :as i} (sut/add-auth-header "test-token")]
    (is (keyword? (:name i)))
    
    (testing "adds `X-Auth-Token` header"
      (is (= "test-token"
             (-> {}
                 (enter)
                 :request
                 :headers
                 (get "X-Auth-Token")))))))

(deftest openapi-url
  (testing "builds url according to api and version"
    (is (= "https://www.scaleway.com/en/developers/static/scaleway.secret_manager.v1beta1.Api.yml"
           (sut/openapi-url :secret-manager "v1beta1"))))

  (testing "applies default api version"
    (is (= "https://www.scaleway.com/en/developers/static/scaleway.instance.v1.Api.yml"
           (sut/openapi-url :instance (sut/default-version :instance))))))

(deftest make-ctx
  (testing "creates context for given api"
    (let [ctx (sut/make-ctx :containers {:secret-key "test-token"})]
      (is (some? ctx))
      (is (not-empty (m/explore ctx))))))

(deftest instance-ctx
  (testing "processes openapi spec correctly"
    (is (some? (-> (sut/instance-ctx {})
                   (m/explore))))))
