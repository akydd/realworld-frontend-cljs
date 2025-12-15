(ns realworld-clojurescript.app.views
  (:require
   [re-frame.core :as re-frame]))

(defn nav-link [link route label]
  (let [current-route @(re-frame/subscribe [:current-route])]
    [:li.nav-item
     [:a  {:class (str "nav-link " (when (= route (-> current-route :data :name)) "active"))
           :href (str "/#" link)
           :on-click #(re-frame/dispatch [:push-state route])} label]]))

(defn header []
  [:nav.navbar.navbar-light
   [:div.containers
    [:a.navbar-brand "Conduit"]
    [:ul.nav.navbar-nav.pull-xs-right
     [nav-link "/" :home "Home"]
     [nav-link "/login" :login "Sign in"]
     [nav-link "/register" :register "Sign up"]]]])

(defn login []
  (let [email @(re-frame/subscribe [:login-form-email])
        password @(re-frame/subscribe [:login-form-password])
        form-complete? @(re-frame/subscribe [:login-form-complete?])
        error @(re-frame/subscribe [:login-form-error])]
    [:div.auth-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Sign in"]
        [:p.text-xs-center
         [:a {:href "/register"} "Need an account"]]

        (when error
          [:ul.error-messages
           [:li (:status-text error)]])

        [:form
         [:fieldset.form-group
          [:input.form-control.form-control-lg {:type "text"
                                                :placeholder "Email"
                                                :value email
                                                :on-change #(re-frame/dispatch [:update-login-form-email (-> % .-target .-value)])}]]
         [:fieldset.form-group
          [:input.form-control.form-control-lg {:type "password"
                                                :placeholder "Password"
                                                :value password
                                                :on-change #(re-frame/dispatch [:update-login-form-password (-> % .-target .-value)])}]]
         [:button.btn.btn-lg.btn-primary.pull-xs-right {:disabled (not form-complete?)
                                                        :on-click (fn [e]
                                                                    (.preventDefault e)
                                                                    (re-frame/dispatch [:post-users-login]))} "Sign in"]]]]]]))

(defn register []
  (let [name @(re-frame/subscribe [:reg-form-name])
        email @(re-frame/subscribe [:reg-form-email])
        password @(re-frame/subscribe [:reg-form-password])
        form-complete? @(re-frame/subscribe [:reg-form-complete?])
        error @(re-frame/subscribe [:reg-form-error])]
    [:div.auth-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Sign up"]
        [:p.text-xs-center
         [:a {:href "/#/login"} "Have an account?"]]

        (when error
          [:ul.error-messages
           [:li (:status-text error)]])

        [:form
         [:fieldset.form-group
          [:input.form-control.form-control-lg {:type "text"
                                                :value name
                                                :placeholder "Username"
                                                :on-change #(re-frame/dispatch [:update-reg-form-name (-> % .-target .-value)])}]]
         [:fieldset.form-group
          [:input.form-control.form-control-lg {:type "text"
                                                :value email
                                                :placeholder "Email"
                                                :on-change #(re-frame/dispatch [:update-reg-form-email (-> % .-target .-value)])}]]
         [:fieldset.form-group
          [:input.form-control.form-control-lg {:type "password"
                                                :value password
                                                :placeholder "Password"
                                                :on-change #(re-frame/dispatch [:update-reg-form-password (-> % .-target .-value)])}]]
         [:button.btn.btn-lg.btn-primary.pull-xs-right {:disabled (not form-complete?)
                                                        :on-click (fn [e]
                                                                    (.preventDefault e)
                                                                    (re-frame/dispatch [:post-users]))} "Sign up"]]]]]]))

(defn panel-router []
  (let [current-route @(re-frame/subscribe [:current-route])]
    (if (and current-route (-> current-route :data :view))
      [(-> current-route :data :view)]
      [:p "No component found"])))

(defn home []
  [:div.home-page
   [:div.banner
    [:div.container
     [:h1.logo-font "conduit"]
     [:p "A place to share your knowledge"]]]

   [:div.container.page
    [:div.row
     [:div.col-md-9
      [:div.feed-toggle
       [:ul.nav.nav-pills.outline-active
        [:li.new-item
         [:a.nav-link {:href ""} "Your Feed"]]
        [:li.nav-item
         [:a.nav-link.active {:href ""} "Global Feed"]]]]]]]])

(defn settings []
  [:div.settings-page
   [:div.container.page
    [:div.row
     [:div.col-md-6.offset-md-3.col-xs-12
      [:h1.text-xs-center "Your Settings"]

      [:ul.error-messages
       [:li "That name is required"]]

      [:form
       [:fieldset
        [:fieldset.form-group
         [:input.form-control {:type "text"
                               :placeholder "URL of profile picture"}]]
        [:fieldset.form-group
         [:input.form-control.form-control-lg {:type "text"
                                               :placeholder "Your Name"}]]
        [:fieldset.form-group
         [:textarea.form-control.form-control-lg {:placeholder "Short bio about you"
                                                  :rows 8}]]
        [:fieldset.form-group
         [:input.form-control.form-control-lg {:type "text"
                                               :placeholder "Email"}]]
        [:fieldset.form-group
         [:input.form-control.form-control-lg {:type "password"
                                               :placeholder "New Password"}]]
        [:button.btn.btn-lg.btn-primary.pull-xs-right "Update Settings"]]]
      [:hr]
      [:button.btn.btn-outline-danger "Or click here to logout"]]]]])

(defn app []
  [:div
   [header]
   [panel-router]])

