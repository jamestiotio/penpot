;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.util.code-gen.markup-html
  (:require
   ["react-dom/server" :as rds]
   [app.common.data :as d]
   [app.common.data.macros :as dm]
   [app.common.pages.helpers :as cph]
   [app.common.types.shape.layout :as ctl]
   [app.config :as cfg]
   [app.main.ui.shapes.text.html-text :as text]
   [app.util.code-gen.common :as cgc]
   [app.util.code-gen.markup-svg :refer [generate-svg]]
   [cuerdas.core :as str]
   [rumext.v2 :as mf]))

(defn generate-html
  ([objects shape]
   (generate-html objects shape 0))

  ([objects shape level]
   (let [indent (str/repeat "  " level)
         maybe-reverse (if (ctl/any-layout? shape) reverse identity)

         shape-html
         (cond
           (cgc/svg-markup? shape)
           (let [svg-markup (generate-svg objects shape)]
             (dm/fmt "%<div class=\"%\">\n%\n%</div>"
                     indent
                     (dm/str "shape " (d/name (:type shape)) " "
                             (cgc/shape->selector shape))
                     svg-markup
                     indent))

           (cph/text-shape? shape)
           (let [text-shape-html (rds/renderToStaticMarkup (mf/element text/text-shape #js {:shape shape :code? true}))]
             (dm/fmt "%<div class=\"%\">\n%\n%</div>"
                     indent
                     (dm/str "shape " (d/name (:type shape)) " "
                             (cgc/shape->selector shape))
                     text-shape-html
                     indent))

           (cph/image-shape? shape)
           (let [data (or (:metadata shape) (:fill-image shape))
                 image-url (cfg/resolve-file-media data)]
             (dm/fmt "%<img src=\"%\" class=\"%\">\n%</img>"
                     indent
                     image-url
                     (dm/str "shape " (d/name (:type shape)) " "
                             (cgc/shape->selector shape))
                     indent))

           (empty? (:shapes shape))
           (dm/fmt "%<div class=\"%\">\n%</div>"
                   indent
                   (dm/str "shape " (d/name (:type shape)) " "
                           (cgc/shape->selector shape))
                   indent)

           :else
           (dm/fmt "%<div class=\"%\">\n%\n%</div>"
                   indent
                   (dm/str (d/name (:type shape)) " "
                           (cgc/shape->selector shape))
                   (->> (:shapes shape)
                        (maybe-reverse)
                        (map #(generate-html objects (get objects %) (inc level)))
                        (str/join "\n"))
                   indent))

         shape-html
         (if (cgc/has-wrapper? objects shape)
           (dm/fmt  "<div class=\"%\">%</div>"
                    (dm/str (cgc/shape->selector shape) "-wrapper")
                    shape-html)

           shape-html)]
     (dm/fmt "%<!-- % -->\n%" indent (dm/str (d/name (:type shape)) ": " (:name shape)) shape-html))))

(defn generate-markup
  [objects shapes]
  (->> shapes
       (map #(generate-html objects %))
       (str/join "\n")))
