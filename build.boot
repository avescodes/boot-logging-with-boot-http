(set-env! :resource-paths #{"src"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [adzerk/boot-test "RELEASE" :scope "test"]

                            ;; For boot-logservice
                            [adzerk/boot-logservice "1.2.0"]
                            [org.clojure/tools.logging "0.3.1"]

                            ;; for serve-pod
                            [org.clojure/tools.nrepl "0.2.12"]
                            [pandeiro/boot-http "0.7.6" :scope "test"]

                            ;; for serve-main
                            [ring/ring-core "1.6.1"]
                            [ring/ring-jetty-adapter "1.6.1"]])

(require '[boot.core :refer [with-pre-wrap]]
         '[pandeiro.boot-http :refer [serve]]
         '[repro.server :as server]
         '[clojure.tools.logging :as log]
         '[adzerk.boot-logservice :as log-service])

;; Configure logging as per boot-logservice README
(def log-config
  [:configuration {:scan true, :scanPeriod "10 seconds"}
   [:appender {:name "FILE" :class "ch.qos.logback.core.rolling.RollingFileAppender"}
    [:encoder [:pattern "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"]]
    [:rollingPolicy {:class "ch.qos.logback.core.rolling.TimeBasedRollingPolicy"}
     [:fileNamePattern "logs/%d{yyyy-MM-dd}.%i.log"]
     [:timeBasedFileNamingAndTriggeringPolicy {:class "ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP"}
      [:maxFileSize "64 MB"]]]
    [:prudent true]]
   [:appender {:name "STDOUT" :class "ch.qos.logback.core.ConsoleAppender"}
    [:encoder [:pattern "%-5level %logger{36} - %msg%n"]]
    [:filter {:class "ch.qos.logback.classic.filter.ThresholdFilter"}
     [:level "INFO"]]]
   [:root {:level "INFO"}
    [:appender-ref {:ref "FILE"}]
    [:appender-ref {:ref "STDOUT"}]]
   [:logger {:name "user" :level "ALL"}]
   [:logger {:name "boot.user" :level "ALL"}]])


(deftask with-logging
  "Initialize the clojure.utils.logging logger factory."
  []
  (with-pre-wrap fileset
    (alter-var-root #'log/*logger-factory* (constantly (log-service/make-factory log-config)))
    (log/info "Base Logger: " log/*logger-factory*)
    fileset))

(deftask run-jetty
  "Simple wrapper over repro.server/-main. Non-joining, use in conjunction with `wait`"
  []
  (with-pre-wrap fileset
    (server/-main)
    fileset))

;; Test cases

(deftask serve-pod
  "Serve the app using boot-http in a separate worker pod"
  []
  (comp
    (with-logging)
    (serve :handler 'repro.server/respond-hello)
    (wait)))

(deftask serve-pod-init
  "Serve the app using boot-http in a separate worker pod, but try initializing
  logging via init."
  []
  (comp
    (serve :handler 'repro.server/respond-hello
           :init 'repro.logging/init-logging!)
    (wait)))

(deftask serve-main
  "Serve the app using run-jetty in the main pod"
  []
  (comp
    (with-logging)
    (run-jetty)
    (wait)))
