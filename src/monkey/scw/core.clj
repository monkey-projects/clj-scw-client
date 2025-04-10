(ns monkey.scw.core
  "Core Scaleway client namespace.  It provides functions for creating clients for
   the various Scaleway services using their provided OpenAPI definitions."
  (:require [camel-snake-kebab.core :as csk]
            [martian
             [core :as mc]
             [interceptors :as mi]
             [openapi :as mo]]
            [monkey.martian.aleph :as mma]
            [monkey.scw.openapi :as openapi]))

(def scw-url "https://api.scaleway.com")

(def stable-apis #{:instance})

(defn default-version [api]
  (if (stable-apis api)
    "v1"
    "v1beta1"))

(defn openapi-url [api version]
  (format "https://www.scaleway.com/en/developers/static/scaleway.%s.%s.Api.yml"
          (.replaceAll (name api) "-" "_") version))

(defn add-auth-header
  "Interceptor that adds the `X-Auth-Token` header for authentication"
  [token]
  {:name ::add-auth-header
   :enter (fn [ctx]
            (assoc-in ctx [:request :headers "X-Auth-Token"] token))})

(defn default-interceptors
  "Creates a list of default interceptors that are used by the context.  You can use
   this to build your own interceptor list."
  [conf]
  (concat mc/default-interceptors
          [mi/default-encode-body
           (mi/coerce-response (mma/make-encoders csk/->kebab-case-keyword))
           mma/perform-request
           (add-auth-header (:secret-key conf))]))

(defn make-ctx [api conf]
  (mma/bootstrap-openapi
   (openapi-url api (get conf :api-version (default-version api)))
   {:server-url scw-url
    :interceptors (or (:interceptors conf) (default-interceptors conf))}))

(def secrets-ctx
  "Creates a secret manager api client by downloading the openapi spec from Scaleway."
  (partial make-ctx :secret-manager))

(def containers-ctx
  "Creates a serverless containers api client by downloading the openapi spec from Scaleway."
  (partial make-ctx :containers))

(def registry-ctx
  "Creates context for container registry"
  (partial make-ctx :registry))

(defn instance-ctx
  "Creates context for compute instances"
  [conf]
  ;; Here we need to override the default functionality because the spec contains some
  ;; errors regarding defaults, which we need to patch before we can proceed.
  (let [api :instance
        defs (-> (openapi/load-definition (openapi-url api (default-version api)))
                 deref
                 (openapi/patch-openapi-spec))
        interceptors (or (:interceptors conf) (default-interceptors conf))
        handlers (mo/openapi->handlers defs (mi/supported-content-types interceptors))]
    (mma/bootstrap scw-url handlers {:interceptors interceptors})))

;; TODO Add more contexts
