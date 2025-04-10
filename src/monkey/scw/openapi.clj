(ns monkey.scw.openapi
  (:require [aleph.http :as http]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [manifold.deferred :as md]))

(defn- throw-on-error [{:keys [status] :as resp}]
  (when (>= status 400)
    (throw (ex-info "Got error response" resp)))
  resp)

(defn load-definition [url]
  (letfn [(parse [b]
            (with-open [r (io/reader b)]
              (yaml/parse-stream r)))]
    (md/chain
     (http/get url {:as :text})
     throw-on-error
     :body
     parse)))

(defn- parse-bool [{:keys [default] :as s}]
  (cond-> s
    (string? default) (update :default Boolean/parseBoolean)))

(defn- parse-int [{:keys [default] :as s}]
  (cond-> s
    (string? default) (update :default Integer/parseInt)))

(def patchers
  {"boolean" parse-bool
   "integer" parse-int})

(defn patch-schema
  "Some schema definitions provide a default value that does not match the type
   (e.g. string values instead of booleans or ints).  This functions patches those
   schemas by updating the default values."
  [s]
  (let [p (get patchers (:type s))]
    (cond-> s
      p (p))))

(defn patch-openapi-spec [spec]
  (walk/prewalk
   (fn [f]
     (cond-> f
       (and (map? f) (contains? f :type) (contains? f :default))
       patch-schema))
   spec))
