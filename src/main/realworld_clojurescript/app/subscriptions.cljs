(ns realworld-clojurescript.app.subscriptions
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub :current-route
                  (fn [db _]
                    (:current-route db)))

(re-frame/reg-sub :reg-form-name
                  (fn [db _]
                    (get-in db [:forms :reg-form :fields :username])))

(re-frame/reg-sub :reg-form-email
                  (fn [db _]
                    (get-in db [:forms :reg-form :fields :email])))

(re-frame/reg-sub :reg-form-password
                  (fn [db _]
                    (get-in db [:forms :reg-form :fields :password])))

(re-frame/reg-sub :reg-form-complete?
                  (fn [db _]
                    (every? not-empty (vals (get-in db [:forms :reg-form :fields])))))

(re-frame/reg-sub :reg-form-error
                  (fn [db _]
                    (get-in db [:forms :reg-form :error])))
