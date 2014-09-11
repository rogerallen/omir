(defproject omir "0.1.0-SNAPSHOT"
  :description  "Overtone midi interaction REPL"
  :url          "http://www.github.com/rogerallen/omir"
  :license      {:name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [overtone            "0.9.1"]
                 [quil                "2.2.1"]
                 [persi               "0.2.0"]]
  :jvm-opts     ^:replace [] )
