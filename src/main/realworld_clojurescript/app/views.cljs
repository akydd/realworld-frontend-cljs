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
  (let [current-user @(re-frame/subscribe [:current-user])
        username (:username current-user)]
    [:nav.navbar.navbar-light
     [:div.container
      [:a.navbar-brand "Conduit"]
      [:ul.nav.navbar-nav.pull-xs-right
       [nav-link {:link "/" :route :home :label "Home"}]
       (when current-user [nav-link {:link "/editor"
                                     :route :editor
                                     :label "\u00A0New Article"}
                           ^{:key "1"} [:i.ion-compose]])
       (when current-user [nav-link {:link "/settings"
                                     :route :settings
                                     :label "\u00A0Settings"}
                           ^{:key "1"} [:i.ion-gear-a]])
       (when current-user [nav-link {:link (str "/profile/" username)
                                     :route :profile
                                     :label username}
                           (when (:image current-user)
                             ^{:key "1"} [:img.user-pic {:src (:image current-user)}])])
       (when-not current-user [nav-link {:link "/login"
                                         :route :login
                                         :label "Sign in"}])
       (when-not current-user [nav-link {:link  "/register"
                                         :route :register
                                         :label "Sign up"}])]]]))

(defn form-input [label type form-id form-field]
  (let [value @(re-frame/subscribe [:form form-id form-field])]
    [:fieldset.form-group
     [:input.form-control.form-control-lg {:type type
                                           :placeholder label
                                           :value value
                                           :on-change #(re-frame/dispatch [:update-form form-id form-field (-> % .-target .-value)])}]]))

(defn login []
  (let [form-complete? @(re-frame/subscribe [:login-form-complete?])
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
         [form-input "Email" "text" :login-form :email]
         [form-input "Password" "password" :login-form :password]
         [:button.btn.btn-lg.btn-primary.pull-xs-right {:disabled (not form-complete?)
                                                        :on-click (fn [e]
                                                                    (.preventDefault e)
                                                                    (re-frame/dispatch [:post-users-login]))} "Sign in"]]]]]]))

(defn register []
  (let [form-complete? @(re-frame/subscribe [:reg-form-complete?])
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
         [form-input "Username" "text" :reg-form :username]
         [form-input "Email" "text" :reg-form :email]
         [form-input "Password" "password" :reg-form :password]
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
        profile-link (str "#/profile/" username)]
    [:div.article-preview

     [:div.article-meta
      [:a {:href profile-link}
       [:img {:src (get-in article [:author :image])}]]
      [::div.info
       [:a.author {:href profile-link} username]
       [:span.date (:updatedAt article)]]
      [:button.btn.btn-outline-primary.btn-sm.pull-xs-right
       [:i.ion-heart] (str "\u00A0" (:favoritesCount article))]]

     [:a.preview-link {:href (str "#/article/" (:slug article))}
      [:h1 (:title article)]
      [:p (:description article)]
      [:span "Read more..."]

      (when (seq (:tag-list article))
        [:ul.tag-list
         (for [tag (:tag-list article)]
           ^{:key tag} [:li.tag-default.tag-pill.tag-outline tag])])]]))

(defn home []
  (let [current-user @(re-frame/subscribe [:current-user])
        active-tab @(re-frame/subscribe [:home-page-active-tab])
        tags @(re-frame/subscribe [:tags])
        articles @(re-frame/subscribe [:articles])]
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

          (when current-user
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
  (let [image @(re-frame/subscribe [:form :settings-form :image])
        bio @(re-frame/subscribe [:form :settings-form :bio])
        form-not-empty? @(re-frame/subscribe [:settings-form-not-empty?])
        error @(re-frame/subscribe [:settings-form-error])]
    [:div.settings-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Your Settings"]

        (when error
          [:ul.error-messages
           [:li "That name is required"]])

        [:form
         [:fieldset
          [:fieldset.form-group
           [:input.form-control {:type "text"
                                 :value image
                                 :placeholder "URL of profile picture"
                                 :on-change #(re-frame/dispatch [:update-form :settings-form :image (-> % .-target .-value)])}]]
          [form-input "Your Name" "text" :settings-form :username]
          [:fieldset.form-group
           [:textarea.form-control.form-control-lg {:value bio
                                                    :placeholder "Short bio about you"
                                                    :rows 8
                                                    :on-change #(re-frame/dispatch [:update-form :settings-form :bio (-> % .-target .-value)])}]]
          [form-input "Email" "text" :settings-form :email]
          [form-input "New Password" "password" :settings-form :password]
          [:button.btn.btn-lg.btn-primary.pull-xs-right {:disabled (not form-not-empty?)
                                                         :on-click (fn [e]
                                                                     (.preventDefault e)
                                                                     (re-frame/dispatch [:put-update-user]))}
           "Update Settings"]]]
        [:hr]
        [:button.btn.btn-outline-danger {:on-click #(re-frame/dispatch [:logout])}
         "Or click here to logout"]]]]]))

(defn article-meta []
  (let [article @(re-frame/subscribe [:current-article])
        author (:author article)
        current-user @(re-frame/subscribe [:current-user])
        profile-link (str "/#/profile/" (:username author))]
    [:div.article-meta
     [:a {:href profile-link}
      [:img {:src (:image author)}]]
     [:div.info
      [:a.author {:href profile-link} (:username author)]
      [:span.date (:updatedAt article)]]

     (when current-user
       [:button.btn.btn-sm.btn-outline-secondary
        [:i.ion-plus-round]
        (str "\u00A0" " Follow " (:username author) "\u00A0")
        [:span.counter "(10)"]])

     "\u00A0\u00A0"
     [:button.btn.btn-sm.btn-outline-primary
      [:i.ion-heart]
      (str "\u00A0" " Favorite Post" "\u00A0")
      [:span.counter (str "(" (:favoritesCount article) ")")]]

     (when (= (:username current-user) (:username author))
       [:button.btn.btn-sm.btn-outline-secondary
        [:i.ion-edit] "\u00A0Edit Article"])

     (when (= (:username current-user) (:username author))
       [:button.btn.btn-sm.btn-outline-danger
        [:i.ion-trash-a] "\u00A0Delete Article"])]))

(defn article-page []
  (let [article @(re-frame/subscribe [:current-article])
        comments @(re-frame/subscribe [:comments])
        current-user @(re-frame/subscribe [:current-user])]
    [:div.article-page
     [:div.banner
      [:div.container
       [:h1 (:title article)]
       [article-meta]]]

     [:div.container.page
      [:div.row.article-content
       [:div.col-md-12 (:body article)

        (for [tag (:tagList article)]
          ^{:key tag} [:ul.tag-list
                       [:li.tag-default.tag-pill.tag-outline tag]])]]

      [:hr]

      [:div.article-actions
       [article-meta]]

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
                                     [:img.comment-author-img {:src (:image author)}]]
                                    "\u00A0"
                                    [:a.comment-author {:href author-link} (:username author)]
                                    [:span.date-posted (:updatedAt comment)]

                                    (when (= (:username current-user) (:username author))
                                      [:span.mod-options
                                       [:i.ion-trash-a
                                        {:on-click #(re-frame/dispatch [:delete-comment (:id comment)])}]])]]))]]]]))

(defn profile []
  (let [profile @(re-frame/subscribe [:profile])
        username (:username profile)
        active-tab @(re-frame/subscribe [:profile-page-tab])
        articles @(re-frame/subscribe [:articles])
        profile-is-me? @(re-frame/subscribe [:profile-is-me?])
        profile-link (str "#/profile/" (:username profile))]
    [:div.profile-page
     [:div.user-info
      [:div.container
       [:div.row
        [:div.col-xs-12.col-md-10.offset-md-1
         [:img.user-img {:src (:image profile)}]
         [:h4 username]
         [:p (:bio profile)]

         (when (contains? profile :following)
           (if (:following profile)
             [:button.btn.btn-sm.btn-outline-secondary.action-btn
              {:on-click (fn [e]
                           (.preventDefault e)
                           (re-frame/dispatch [:follow-profile username :unfollow]))}
              [:i.ion-minus-round] (str "\u00A0 Unfollow " username)]

             [:button.btn.btn-sm.btn-outline-secondary.action-btn
              {:on-click (fn [e]
                           (.preventDefault e)
                           (re-frame/dispatch [:follow-profile username :follow]))}
              [:i.ion-plus-round] (str "\u00A0 Follow " username)]))

         (when profile-is-me?
           [:button.btn.btn-sm.btn-outline-secondary.action-btn
            {:on-click (fn [e]
                         (.preventDefault e)
                         (re-frame/dispatch [:push-state :settings]))}
            [:i.ion-gear-a "\u00A0 Edit Profile Settings"]])]]]]

     [:div.container
      [:div.row
       [:div.col-xs-12.col-md-10.offset-md-1
        [:div.articles-toggle
         [:ul.nav.nav-pills.outline-active
          [:li.nav-item
           [:a {:class (str "nav-link " (when (= active-tab :my-articles) "active"))
                :href profile-link} "My Articles"]]
          [:li.nav-item
           [:a {:class (str "nav-link " (when (= active-tab :favorited-articles) "active"))
                :href (str profile-link "/favorites")} "Favorited Articles"]]]]

        (for [article articles]
          ^{:key (:slug article)} [article-preview article])]]]]))

(defn app []
  [:div
   [header]
   [panel-router]])

