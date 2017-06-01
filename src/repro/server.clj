(ns repro.server
  (:require [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn respond-hello [req]
  (log/info "Hello Logger: " log/*logger-factory*)
  (log/info "In respond-hello")
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, World!"})

(defn -main [& args]
  (log/info "Starting server on port 3000")
  (run-jetty respond-hello {:join? false :port 3000}))
