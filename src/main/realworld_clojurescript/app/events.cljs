(ns realworld-clojurescript.app.events
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reitit.frontend.easy :as rfe]
   [ajax.core :as ajax]))

(re-frame/reg-event-db :init-db
                       (fn-traced [_ _]
                                  {:loading false
                                   :current-route nil
                                   :token nil
                                   :tags nil
                                   :forms {:reg-form {:fields {:username ""
                                                               :email ""
                                                               :password ""}
                                                      :error nil}
                                           :login-form {:fields {:email ""
                                                                 :password ""}
                                                        :error nil}
                                           :settings-form {:fields {:image ""
                                                                    :username ""
                                                                    :bio ""
                                                                    :email ""
                                                                    :password ""}
                                                           :error nil}}}))

;; --- data load events for routes ---
(defn load-data-for-route [route]
  (case (get-in route [:data :name])
    :home (list [:dispatch [:get-tags]])))

;; --- navigation ---

(re-frame/reg-event-fx :change-route
                       (fn-traced [{:keys [db]} [_ new-route]]
                                  {:db (assoc db :current-route new-route)
                                   :fx (load-data-for-route new-route)}))

(re-frame/reg-fx :rfe-push-state
                 (fn-traced [route]
                            (rfe/push-state route)))

(re-frame/reg-event-fx :push-state
                       (fn-traced [_ [_ route]]
                                  {:rfe-push-state route}))

(re-frame/reg-event-fx :get-tags
                       (fn-traced [{:keys [db]}]
                                  {:db (assoc db :loading true)
                                   :http-xhrio {:method :get
                                                :uri "http://localhost:8090/api/tags"
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success [:get-tags-success]
                                                :on-failure [:get-tags-fail]}}))

(re-frame/reg-event-db :get-tags-success
                       (fn-traced [db [_ result]]
                                  (assoc db :tags (:tags result)
                                         :loading false)))

(re-frame/reg-event-db :get-tags-fail
                       (fn-traced [db [_ result]]
                                  db))

;; ---- sign up form ---

(re-frame/reg-event-db :update-reg-form-name
                       (fn-traced [db [_ name]]
                                  (assoc-in db [:forms :reg-form :fields :username] name)))

(re-frame/reg-event-db :update-reg-form-email
                       (fn-traced [db [_ email]]
                                  (assoc-in db [:forms :reg-form :fields :email] email)))

(re-frame/reg-event-db :update-reg-form-password
                       (fn-traced [db [_ password]]
                                  (assoc-in db [:forms :reg-form :fields :password] password)))

(re-frame/reg-event-fx :post-users
                       (fn-traced [{:keys [db]}]
                                  (let [user (get-in db [:forms :reg-form :fields])]
                                    {:db (-> db
                                             (assoc :loading true)
                                             (assoc-in [:forms :reg-form :error] nil))
                                     :http-xhrio {:method :post
                                                  :uri "http://localhost:8090/api/users"
                                                  :params {:user user}
                                                  :format (ajax/json-request-format)
                                                  :response-format (ajax/json-response-format {:keywords? true})
                                                  :on-success [:post-users-success]
                                                  :on-failure [:post-users-fail]}})))

(re-frame/reg-event-fx :post-users-success
                       (fn [{:keys [db]}]
                         {:db (-> db
                                  (assoc-in [:forms :reg-form] {:fields {:username  ""
                                                                         :email ""
                                                                         :password ""}
                                                                :error nil})
                                  (assoc :loading false))
                          :fx [[:dispatch [:push-state :login]]]}))

(re-frame/reg-event-db :post-users-fail
                       (fn-traced [db [_ result]]
                                  (-> db
                                      (assoc :loading false)
                                      (assoc-in [:forms :reg-form :error] result))))

;; --- login form ---

(re-frame/reg-event-db :update-login-form-email
                       (fn [db [_ email]]
                         (assoc-in db [:forms :login-form :fields :email] email)))

(re-frame/reg-event-db :update-login-form-password
                       (fn [db [_ password]]
                         (assoc-in db [:forms :login-form :fields :password] password)))

(re-frame/reg-event-fx :post-users-login
                       (fn [{:keys [db]}]
                         (let [user (get-in db [:forms :login-form :fields])]
                           {:db (-> db
                                    (assoc :loading true)
                                    (assoc-in [:forms :login-form :error] nil))
                            :http-xhrio {:method :post
                                         :uri "http://localhost:8090/api/users/login"
                                         :params {:user user}
                                         :format (ajax/json-request-format)
                                         :response-format (ajax/json-response-format {:keywords? true})
                                         :on-success [:post-users-login-success]
                                         :on-failure [:post-users-login-fail]}})))

(re-frame/reg-event-db :post-users-login-fail
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc-in [:forms :login-form :error] result))))

(re-frame/reg-event-fx :post-users-login-success
                       (fn [{:keys [db]} [_ result]]
                         {:db (-> db
                                  (assoc-in [:forms :login-form] {:fields {:email ""
                                                                           :password ""}
                                                                  :error nil})
                                  (assoc :loading false))
                          :fx [[:dispatch [:push-state :home]]]}))
