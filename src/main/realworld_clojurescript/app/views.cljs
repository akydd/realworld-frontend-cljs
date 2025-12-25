(ns realworld-clojurescript.app.views
  (:require
   [re-frame.core :as re-frame]))

(defn nav-link
  [{:keys [link route label] :as opts} & children]
  (let [current-route @(re-frame/subscribe [:current-route])]
    [:li.nav-item
     [:a  {:class (str "nav-link " (when (= route (-> current-route :data :name)) "active"))
           :href (str "/#" link)}
      children
      label]]))

(defn header []
  (let [token @(re-frame/subscribe [:token])
        current-user @(re-frame/subscribe [:current-user])]
    [:nav.navbar.navbar-light
     [:div.containers
      [:a.navbar-brand "Conduit"]
      [:ul.nav.navbar-nav.pull-xs-right
       [nav-link {:link "/" :route :home :label "Home"}]
       (when token [nav-link {:link "/editor"
                              :route :editor
                              :label "\u00A0New Article"}
                    ^{:key "1"} [:i.ion-compose]])
       (when token [nav-link {:link "/settings"
                              :route :settings
                              :label "\u00A0Settings"}
                    ^{:key "1"} [:i.ion-gear-a]])
       (when token [nav-link {:link (str "/profile/" (:username current-user))
                              :route :settings
                              :label (:username current-user)}])
       (when-not token [nav-link {:link "/login"
                                  :route :login
                                  :label "Sign in"}])
       (when-not token [nav-link {:link  "/register"
                                  :route :register
                                  :label "Sign up"}])
       (when token [:li.nav-item
                    [:a.nav-link.active {:href "#"
                                         :on-click #(re-frame/dispatch [:logout])} "Logout"]])]]]))

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
           [:li (:status-text "Invalid username or password")]])

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

(defn article-preview [article]
  (let [username (get-in article [:author :username])
        profile-link (str "/profile/" username)]
    [:div.article-preview

     [:div.article-meta
      [:a {:href profile-link}
       [:img {:src (get-in article [:author :image])}]]
      [::div.info
       [:a.author {:href profile-link} username]
       [:span.date (:updatedAt article)]]
      [:button.btn.btn-outline-primary.btn-sm.pull-xs-right
       [:i.ion-heart] (:favoritesCount article)]]

     [:a.preview-link {:href (str "#/article/" (:slug article))}
      [:h1 (:title article)]
      [:p (:description article)]
      [:span "Read more..."]

      (when (seq (:tag-list article))
        [:ul.tag-list
         (for [tag (:tag-list article)]
           ^{:key tag} [:li.tag-default.tag-pill.tag-outline tag])])]]))

(defn home []
  (let [token @(re-frame/subscribe [:token])
        active-tab @(re-frame/subscribe [:home-page-active-tab])
        tags @(re-frame/subscribe [:tags])
        articles @(re-frame/subscribe [:global-feed])]
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

          (when token
            [:li.nav-item
             [:a.nav-link {:href ""} "Your Feed"]])

          [:li.nav-item
           [:a.nav-link.active {:href ""} "Global Feed"]]]]

        (for [article articles]
          ^{:key (:slug article)} [article-preview article])

        [:ul.pagination]]

       [:div.col-md-3
        [:div.sidebar
         [:p "Popular Tags"]

         [:div.tag-list
          (for [tag tags]
            ^{:key tag} [:a.tag-pill.tag-default tag])]]]]]]))

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

(defn article-meta [article profile]
  (let [profile-link (str "/#/" (:username profile))]
    [:div.article-meta
     [:a {:href profile-link}
      [:img {:src (:image profile)}]]
     [:div.info
      [:a.author {:href profile-link} (:username profile)]
      [:span.date (:updatedAt article)]]

     [:button.btn.btn-sm.btn-outline-secondary
      [:i.ion-plus-round] (str " Follow " (:username profile)) [:span.counter "(10)"]]

     [:button.btn.btn-sm.btn-outline-primary
      [:i.ion-heart] " Favorite Post " [:span.counter (str "(" (:favoritesCount article) ")")]]

     [:button.btn.btn-sm.btn-outline-secondary
      [:i.ion-edit] " Edit Article"]

     [:button.btn.btn-sm.btn-outline-danger
      [:i.ion-trash-d] " Delete Article"]]))

(defn article-page []
  (let [article @(re-frame/subscribe [:current-article])
        comments @(re-frame/subscribe [:current-comments])
        profile (:author article)]
    [:div.article-page
     [:div.banner
      [:div.container
       [:h1 (:title article)]
       [article-meta article profile]]]

     [:div.container.page
      [:div.row.article-content
       [:div.col-md-12 (:body article)

        (for [tag (:tagList article)]
          ^{:key tag} [:ul.tag-list
                       [:li.tag-default.tag-pill.tag-outline tag]])]]

      [:hr]

      [:div.article-actions
       [article-meta article profile]]

      [:div.row
       [:div.col-xs-12.col-md-8.offset-md-2

        (for [comment comments]
          (let [author (:author comment)
                author-link (str "/#/profile/" (:username author))]
            ^{:key (:id comment)} [:div.card
                                   [:div.card-block
                                    [:p.card-text (:body comment)]]
                                   [:div.card-footer
                                    [:a.comment-author {:href author-link}
                                     [:img {:src (:image author)}]]
                                    [:a.comment-author {:href author-link} (:username author)]
                                    [:span.date-posted (:updatedAt comment)]
                                    [:span.mod-options
                                     [:i.ion-trash-d]]]]))]]]]))

(defn app []
  [:div
   [header]
   [panel-router]])

