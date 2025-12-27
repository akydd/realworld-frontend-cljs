(ns realworld-clojurescript.app.subscriptions
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub :current-route
                  (fn [db _]
                    (:current-route db)))

;; --- sign up form ---

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

;; --- login form ---

(re-frame/reg-sub :login-form-email
                  (fn [db _]
                    (get-in db [:forms :login-form :fields :email])))

(re-frame/reg-sub :login-form-password
                  (fn [db _]
                    (get-in db [:forms :login-form :fields :password])))

(re-frame/reg-sub :login-form-complete?
                  (fn [db _]
                    (every? not-empty (vals (get-in db [:forms :login-form :fields])))))

(re-frame/reg-sub :login-form-error
                  (fn [db _]
                    (get-in db [:forms :login-form :error])))

;; -- home page --

(re-frame/reg-sub :tags
                  (fn [db _]
                    (:tags db)))

(re-frame/reg-sub :home-page-active-tab
                  (fn [db _]
                    (get-in db [:home-page :tab])))

;; --- authentication ---

(re-frame/reg-sub :token
                  (fn [db _]
                    (:token db)))

(re-frame/reg-sub :current-user
                  (fn [db _]
                    (:current-user db)))

;; --- articles ---

(re-frame/reg-sub :current-article
                  (fn [db _]
                    (:current-article db)))

(re-frame/reg-sub :articles
                  (fn [db _]
                    (:articles db)))

(re-frame/reg-sub :comments
                  (fn [db _]
                    (:comments db)))

;; --- profile page ---

(re-frame/reg-sub :profile
                  (fn [db _]
                    (:profile db)))

(re-frame/reg-sub :profile-page-tab
                  (fn [db _]
                    (get-in db [:profile-page :tab])))
