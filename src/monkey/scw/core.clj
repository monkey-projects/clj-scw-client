(ns monkey.scw.core
  "Core Scaleway client namespace.  It provides functions for creating clients for
   the various Scaleway services using their provided OpenAPI definitions."
  (:require [clojure.tools.logging :as log]
            [camel-snake-kebab.core :as csk]
            [martian
             [core :as mc]
             [interceptors :as mi]]
            [monkey.martian.aleph :as mma]))

(def scw-url "https://api.scaleway.com")
(def version "v1beta1")

(defn openapi-url [api version]
  (format "https://www.scaleway.com/en/developers/static/scaleway.%s.%s.Api.yml"
          (.replaceAll (name api) "-" "_") version))

(defn add-auth-header
  "Interceptor that adds the `X-Auth-Token` header for authentication"
  [token]
  {:name ::add-auth-header
   :enter (fn [ctx]
            (assoc-in ctx [:request :headers "X-Auth-Token"] token))})

(defn make-ctx [api conf]
  (mma/bootstrap-openapi
   (openapi-url api (get conf :api-version version))
   {:server-url scw-url
    :interceptors (concat mc/default-interceptors
                          [mi/default-encode-body
                           (mi/coerce-response (mma/make-encoders csk/->kebab-case-keyword))
                           mma/perform-request
                           (add-auth-header (:secret-key conf))])}))

(def secrets-ctx
  "Creates a secret manager api client by downloading the openapi spec from Scaleway."
  (partial make-ctx :secret-manager))

(def containers-ctx
  "Creates a serverless containers api client by downloading the openapi spec from Scaleway."
  (partial make-ctx :containers))

(def registry-ctx
  "Creates context for container registry"
  (partial make-ctx :registry))

(def instance-ctx
  "Creates context for compute instances"
  (partial make-ctx :instance))

;; TODO Add more contexts
