(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'net.totakke/more.spec)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def basis (b/create-basis {:project "deps.edn"}))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean
  "Cleans the outputs."
  [_]
  (b/delete {:path "target"}))

(defn jar
  "Builds the JAR."
  [_]
  (b/write-pom {:basis basis
                :class-dir class-dir
                :lib lib
                :version version
                :src-dirs ["src"]})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn install
  "Installs the JAR locally."
  [_]
  (clean nil)
  (jar nil)
  (b/install {:basis basis
              :lib lib
              :version version
              :jar-file jar-file
              :class-dir class-dir}))

(defn deploy
  "Deploys the JAR to Clojars."
  [_]
  (clean nil)
  (jar nil)
  (dd/deploy {:installer :remote
              :artifact (b/resolve-path jar-file)
              :pom-file (b/pom-path {:lib lib
                                     :class-dir class-dir})}))
