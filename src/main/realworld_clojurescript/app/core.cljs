(ns realworld-clojurescript.app.core
  (:require [reagent.dom :as rdom]
            [realworld-clojurescript.app.views :as views]
            [realworld-clojurescript.app.events]
            [realworld-clojurescript.app.subscriptions]
            [re-frame.core :as re-frame]
            [day8.re-frame.http-fx]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes [["/" {:view views/home
                   :name :home}]
             ["/login" {:view views/login
                        :name :login}]
             ["/register" {:view views/register
                           :name :register}]
             ["/settings" {:view views/settings
                           :name :settings}]
             ["/article/:slug" {:view views/article-page
                                :name :article-page}]
             ["/profile/:username" {:view views/profile
                                    :name :profile}]
             ["/profile/:username/favorites" {:view views/profile
                                              :name :profile-favorites}]])

(def router
  (rf/router routes))

(defn on-navigate [new-match]
  (when new-match
    (re-frame/dispatch [:change-route new-match])))

(defn init-routes! []
  (rfe/start! router on-navigate {:use-fragment true}))

(defn render []
  (rdom/render [views/app {:router router}] (.getElementById js/document "root")))

(defn- init-app []
  (re-frame/dispatch [:init-db])
  (re-frame/dispatch [:push-state :home]))

(defn ^:export main []
  (init-routes!)
  #_(init-app)
  (render))

(defn ^:dev/after-load reload! []
  (re-frame/clear-subscription-cache!)
  (init-app)
  (render))
