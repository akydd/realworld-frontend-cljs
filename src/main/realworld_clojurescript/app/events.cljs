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
                                   :forms {:reg-form {:fields {:username ""
                                                               :email ""
                                                               :password ""}
                                                      :error nil}
                                           :login-form {:fields {:email ""
                                                                 :password ""}
                                                        :error nil}}}))

(re-frame/reg-event-db :change-route
                       (fn-traced [db [_ new-route]]
                                  (assoc db :current-route new-route)))

(re-frame/reg-fx :rfe-push-state
                 (fn [route]
                   (rfe/push-state route)))

(re-frame/reg-event-fx :push-state
                       (fn-traced [_ [_ route]]
                                  {:rfe-push-state route}))
(re-frame/reg-event-fx :get-tags
                       (fn [{:keys [db]}]
                         {:db (assoc db :loading true)
                          :http-xhrio {:method :get
                                       :uri "http://localhost:8090/api/tags"
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success [:get-tags-success]
                                       :on-failure [:get-tags-fail]}}))

(re-frame/reg-event-db :get-tags-success
                       (fn [db [_ result]]
                         (assoc db :tags result
                                :loading false)))

(re-frame/reg-event-db :get-tags-fail
                       (fn [db [_ result]]
                         (assoc db :tags result
                                :loading false)))

(re-frame/reg-event-db :update-reg-form-name
                       (fn [db [_ name]]
                         (assoc-in db [:forms :reg-form :fields :username] name)))

(re-frame/reg-event-db :update-reg-form-email
                       (fn [db [_ email]]
                         (assoc-in db [:forms :reg-form :fields :email] email)))

(re-frame/reg-event-db :update-reg-form-password
                       (fn [db [_ password]]
                         (assoc-in db [:forms :reg-form :fields :password] password)))

(re-frame/reg-event-fx :post-users
                       (fn [{:keys [db]}]
                         (let [user (get-in db [:forms :reg-form :fields])]

                           {:db (assoc db :loading true)
                            :http-xhrio {:method :post
                                         :uri "http://localhost:8090/api/users"
                                         :params {:user user}
                                         :format (ajax/json-request-format)
                                         :response-format (ajax/json-response-format {:keywords? true})
                                         :on-success [:post-users-success]
                                         :on-failure [:post-users-fail]}})))

(re-frame/reg-event-db :post-users-success
                       (fn [db [_ result]]
                         (-> db
                             (assoc-in [:forms :reg-form :fields :username] "")
                             (assoc-in [:forms :reg-form :fields :email] "")
                             (assoc-in [:forms :reg-form :fields :password] "")
                             (assoc-in [:forms :reg-form :error] nil)
                             (assoc :loading false))))

(re-frame/reg-event-db :post-users-fail
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc-in [:forms :reg-form :error] result))))
