(ns realworld-clojurescript.app.core
  (:require [reagent.dom :as rdom]
            [realworld-clojurescript.app.views :as views]))

(defn render []
  (rdom/render [views/app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
