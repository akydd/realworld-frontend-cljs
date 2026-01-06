(ns realworld-clojurescript.app.events
  (:require
   [re-frame.core :as re-frame]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [reitit.frontend.easy :as rfe]
   [ajax.core :as ajax]
   [com.smxemail.re-frame-cookie-fx]))

(re-frame/reg-event-db :init-db
                       (fn-traced [_ _]
                                  {:loading false
                                   :current-route nil
                                   :current-user nil
                                   :profile nil
                                   :comments nil
                                   :articles nil
                                   :tags nil
                                   :home-page {:tab nil}
                                   :profile-page {:tab :my-articles}
                                   :current-article nil
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

(def base-url "http://localhost:8090/api")

;; --- data load events for routes ---

(defn actions-for-route [route]
  (let [route-name (get-in route [:data :name])]
    (cond
      (= route-name :home) (list [:dispatch [:get-tags]]
                                 [:dispatch [:change-home-page-tab :global]])
      (= route-name :article-page) (let [slug (get-in route [:path-params :slug])]
                                     (list [:dispatch [:get-article slug]]
                                           [:dispatch [:get-comments slug]]))
      (= route-name :login) (list [:dispatch [:clear-login-form]])
      (= route-name :register) (list [:dispatch [:clear-reg-form]])
      (= route-name :profile) (let [username (get-in route [:path-params :username])]
                                (list [:dispatch [:get-profile username]]
                                      [:dispatch [:change-profile-page-tab :my-articles username]]))
      (= route-name :profile-favorites) (let [username (get-in route [:path-params :username])]
                                          (list [:dispatch [:get-profile username]]
                                                [:dispatch [:change-profile-page-tab :favorited-articles username]]))
      (= route-name :settings) (list [:dispatch [:clear-settings-form]]))))

;; --- auth ---

(def cookie-interceptor
  (re-frame/inject-cofx :cookie/get [:token]))

(defn get-current-user-fx [{db :db cookie :cookie/get}]
  {:db (assoc db :loading true)
   :http-xhrio {:method :get
                :headers {"Authorization" (str "Token " (:token cookie))}
                :uri (str base-url "/user")
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:get-current-user-success]
                :on-failure [:get-current-user-fail]}})

(re-frame/reg-event-fx :get-current-user
                       [cookie-interceptor]
                       get-current-user-fx)

(re-frame/reg-event-db :get-current-user-success
                       (fn-traced [db [_ result]]
                                  (-> db
                                      (assoc :current-user (:user (dissoc result :token)))
                                      (assoc :loading false))))

(re-frame/reg-event-db :get-current-user-fail
                       (fn-traced [db [_ result]]
                                  (assoc db :loading false)))

(re-frame/reg-event-fx :logout
                       (fn-traced [{:keys [db]}]
                                  {:db (-> db
                                           (assoc :current-user nil))
                                   :cookie/remove {:name "token"
                                                   :on-success [:push-state :home]}}))

;; --- navigation ---

(re-frame/reg-event-fx :route-changed
                       [cookie-interceptor]
                       (fn-traced [{db :db cookie :cookie/get}]
                                  {:fx (cond->> (actions-for-route (:current-route db))
                                         (:token cookie) (cons [:dispatch [:get-current-user]]))}))

(re-frame/reg-event-fx :change-route
                       (fn-traced [{:keys [db]} [_ new-route]]
                                  {:db (assoc db :current-route new-route)
                                   :fx [[:dispatch [:route-changed]]]}))

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
                                                :uri (str base-url "/tags")
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success [:get-tags-success]
                                                :on-failure [:get-tags-fail]}}))

(re-frame/reg-event-db :get-tags-success
                       (fn-traced [db [_ result]]
                                  (-> db
                                      (assoc :tags (:tags result))
                                      (assoc :loading false))))

(re-frame/reg-event-db :get-tags-fail
                       (fn-traced [db [_ result]]
                                  db))

;; ---- sign up form ---

(re-frame/reg-event-db :update-form
                       (fn-traced [db [_ form-id form-field value]]
                                  (assoc-in db [:forms form-id :fields form-field] value)))

(re-frame/reg-event-db :clear-reg-form
                       (fn [db]
                         (assoc-in db [:forms :reg-form] {:fields {:username ""
                                                                   :email ""
                                                                   :password ""}
                                                          :error nil})))
(re-frame/reg-event-fx :post-users
                       (fn-traced [{:keys [db]}]
                                  (let [user (get-in db [:forms :reg-form :fields])]
                                    {:db (-> db
                                             (assoc :loading true)
                                             (assoc-in [:forms :reg-form :error] nil))
                                     :http-xhrio {:method :post
                                                  :uri (str base-url "/users")
                                                  :params {:user user}
                                                  :format (ajax/json-request-format)
                                                  :response-format (ajax/json-response-format {:keywords? true})
                                                  :on-success [:post-users-success]
                                                  :on-failure [:post-users-fail]}})))

(re-frame/reg-event-fx :post-users-success
                       (fn-traced [{:keys [db]}]
                                  {:db (assoc db :loading false)
                                   :fx [[:dispatch [:clear-reg-form]]
                                        [:dispatch [:push-state :login]]]}))

(re-frame/reg-event-db :post-users-fail
                       (fn-traced [db [_ result]]
                                  (-> db
                                      (assoc :loading false)
                                      (assoc-in [:forms :reg-form :error] result))))

;; --- login form ---

(re-frame/reg-event-db :clear-login-form
                       (fn-traced [db _]
                                  (assoc-in db [:forms :login-form] {:fields {:email ""
                                                                              :password ""}
                                                                     :error nil})))

(re-frame/reg-event-fx :post-users-login
                       (fn [{:keys [db]}]
                         (let [user (get-in db [:forms :login-form :fields])]
                           {:db (-> db
                                    (assoc :loading true)
                                    (assoc-in [:forms :login-form :error] nil))
                            :http-xhrio {:method :post
                                         :uri (str base-url "/users/login")
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
                       (fn-traced [{:keys [db]} [_ result]]
                                  {:db (assoc db :loading false
                                              :current-user (dissoc (:user result) :token))
                                   :cookie/set {:name "token"
                                                :value (get-in result [:user :token])
                                                :secure true
                                                :max-age  3600}
                                   :fx [[:dispatch [:clear-login-form]]
                                        [:dispatch [:push-state :home]]]}))

;; --- home page ---

(re-frame/reg-event-fx :change-home-page-tab
                       (fn [{:keys [db]} [_ tab]]
                         {:db (assoc-in db [:home-page :tab] tab)
                          :fx (list
                               (when (= tab :global)
                                 [:dispatch [:get-articles]]))}))

(re-frame/reg-event-fx :get-articles
                       (fn [{:keys [db]}]
                         {:db (assoc db :loading true)
                          :http-xhrio {:method :get
                                       :uri (str base-url "/articles")
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success [:get-articles-success]
                                       :on-failure [:get-articles-fail]}}))

(re-frame/reg-event-db :get-articles-success
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc :articles (:articles result)))))

(re-frame/reg-event-db :get-articles-fail
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false))))

;; --- articles page ---

(re-frame/reg-event-fx :get-article
                       [cookie-interceptor]
                       (fn [{db :db cookie :cookie/get} [_ slug]]
                         {:db (assoc db :loading true)
                          :http-xhrio (cond-> {:method :get
                                               :uri (str base-url "/articles/" slug)
                                               :response-format (ajax/json-response-format {:keywords? true})
                                               :on-success [:get-article-success]
                                               :on-failure [:get-article-fail]}
                                        (:token cookie) (assoc :headers {"Authorization" (str "Token " (:token cookie))}))}))

(re-frame/reg-event-db :get-article-success
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc :current-article (:article result)))))

(re-frame/reg-event-db :get-article-fail
                       (fn [db _]
                         (-> db
                             (assoc :loading false))))

;; --- comments ---

(re-frame/reg-event-fx :get-comments
                       (fn [{:keys [db]} [_ slug]]
                         {:db (assoc db :loading true)
                          :http-xhrio {:method :get
                                       :uri (str base-url "/articles/" slug "/comments")
                                       :response-format (ajax/json-response-format {:keywords? true})
                                       :on-success [:get-comments-success]
                                       :on-failure [:get-comments-fail]}}))

(re-frame/reg-event-db :get-comments-success
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc :comments (:comments result)))))

(re-frame/reg-event-db :get-comments-fail
                       (fn [db [_ result]]
                         (assoc db :loading false)))

(re-frame/reg-event-fx :delete-comment
                       [cookie-interceptor]
                       (fn [{db :db cookie :cookie/get} [_ id]]
                         (let [slug (get-in db [:current-article :slug])]
                           {:db (assoc db :loading true)
                            :http-xhrio {:method :delete
                                         :uri (str base-url "/articles/" slug "/comments/" id)
                                         :headers {"Authorization" (str "Token " (:token cookie))}
                                         :request-format (ajax/json-response-format {:keywords? true})
                                         :on-success [:get-comments slug]
                                         :on-failure [:delete-comment-fail]}})))

(re-frame/reg-event-db :delete-comment-fail
                       (fn [db [_ result]]
                         (assoc db :loading false)))

;; --- profile ---

(re-frame/reg-event-fx :get-profile
                       [cookie-interceptor]
                       (fn [{db :db cookie :cookie/get} [_ username]]
                         {:db (assoc db :loading true)
                          :http-xhrio (cond-> {:method :get
                                               :uri (str base-url "/profiles/" username)
                                               :response-format (ajax/json-response-format {:keywords? true})
                                               :on-success [:get-profile-success]
                                               :on-failure [:get-profile-fail]}
                                        (:token cookie) (assoc :headers {"Authorization" (str "Token " (:token cookie))}))}))

(re-frame/reg-event-db :get-profile-success
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc :profile (:profile result)))))

(re-frame/reg-event-db :get-profile-fail
                       (fn [db [_ result]]
                         (assoc db :loading false)))

;; --- profile page ---

(re-frame/reg-event-fx :change-profile-page-tab
                       (fn-traced [{:keys [db]} [_ tab username]]
                                  {:db (assoc-in db [:profile-page :tab] tab)
                                   :fx (list
                                        (case tab
                                          :my-articles [:dispatch [:get-articles-for-user username]]
                                          :favorited-articles [:dispatch [:get-user-favorited-articles username]]))}))

(re-frame/reg-event-fx :get-articles-for-user
                       (fn-traced [{:keys [db]} [_ username]]
                                  {:db (assoc db :loading true)
                                   :http-xhrio {:method :get
                                                :uri (str base-url "/articles?author=" username)
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success [:get-articles-success]
                                                :on-failure [:get-article-fail]}}))

(re-frame/reg-event-fx :get-user-favorited-articles
                       (fn-traced [{:keys [db]} [_ username]]
                                  {:db (assoc db :loading true)
                                   :http-xhrio {:method :get
                                                :uri (str base-url "/articles?favorited=" username)
                                                :response-format (ajax/json-response-format {:keywords? true})
                                                :on-success [:get-articles-success]
                                                :on-failure [:get-article-fail]}}))

;; --- settings form ---

(re-frame/reg-event-db :clear-settings-form
                       (fn [db]
                         (assoc-in db [:forms :settings-form :fields] {:image ""
                                                                       :username ""
                                                                       :bio ""
                                                                       :email ""
                                                                       :password ""})))

(re-frame/reg-event-fx :put-update-user
                       [cookie-interceptor]
                       (fn [{db :db cookie :cookie/get}]
                         (let [user (get-in db [:forms :settings-form :fields])
                               user (into {} (filter (fn [[_ v]] (not= "" v)) user))]
                           {:db (assoc db :loading true)
                            :http-xhrio {:method :put
                                         :uri (str base-url "/user")
                                         :params {:user user}
                                         :headers {"Authorization" (str "Token " (:token cookie))}
                                         :format (ajax/json-request-format)
                                         :response-format (ajax/json-response-format {:keywords? true})
                                         :on-success [:put-user-success]
                                         :on-failure [:put-user-fail]}})))

(re-frame/reg-event-fx :put-user-success
                       (fn [{:keys [db]} [_ result]]
                         {:db (-> db
                                  (assoc :loading false)
                                  (assoc :current-user (dissoc (:user result) :token)))
                          :fx [[:dispatch [:clear-settings-form]]
                               [:dispatch [:push-state :home]]]}))

(re-frame/reg-event-db :put-user-fail
                       (fn [db [_ result]]
                         (-> db
                             (assoc :loading false)
                             (assoc-in [:forms :settings-form :error] result))))

;; --- follow user ---

(re-frame/reg-event-fx :follow-profile
                       [cookie-interceptor]
                       (fn [{db :db cookie :cookie/get} [_ username action]]
                         (let [method (if (= action :follow) :post :delete)]
                           {:db (assoc db :loading true)
                            :http-xhrio {:method method
                                         :uri (str base-url "/profiles/" username "/follow")
                                         :headers {"Authorization" (str "Token " (:token cookie))}
                                         :format (ajax/json-request-format)
                                         :response-format (ajax/json-response-format {:keywords? true})
                                         :on-success [:get-profile-success]
                                         :on-failure [:post-profile-fail]}})))

(re-frame/reg-event-db :post-profile-fail
                       (fn [db [_ result]]
                         (assoc db :loading false)))
