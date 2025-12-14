(ns realworld-clojurescript.app.subscriptions
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub :current-route
                  (fn [db _]
                    (:current-route db)))

(re-frame/reg-sub :reg-form-name
                  (fn [db _]
                    (get-in db [:forms :reg-form :name])))

(re-frame/reg-sub :reg-form-email
                  (fn [db _]
                    (get-in db [:forms :reg-form :email])))

(re-frame/reg-sub :reg-form-password
                  (fn [db _]
                    (get-in db [:forms :reg-form :password])))

(re-frame/reg-sub :reg-form-complete?
                  (fn [db _]
                    (every? not-empty (vals (get-in db [:forms :reg-form])))))
