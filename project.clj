(defproject ionsails-web "0.1.0-SNAPSHOT"
  :description "Ionsails"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [
                 ;; Core libs
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"]
                 ;; Data/algorithm libs
                 [amalloy/ring-buffer "1.2"]
                 ;; UI libs
                 [quiescent "0.2.0-RC2"
                  :exclusions [cljsjs/react cljsjs/react-with-addons]]
                 [cljsjs/react-with-addons "0.13.3-0"]
                 [cljsjs/vis "4.9.0-2"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-figwheel "0.5.0-1"]]

  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.0-1"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]

                :figwheel {:on-jsload "ionsails-web.core/on-js-reload"}

                :compiler {:main ionsails-web.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/ionsails_web.js"
                           :output-dir "resources/public/js/compiled/out"
                           :optimizations :none
                           :source-map-timestamp true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/ionsails_web.js"
                           :main ionsails-web.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:server-port 3449
             :server-ip "127.0.0.1"
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             })
