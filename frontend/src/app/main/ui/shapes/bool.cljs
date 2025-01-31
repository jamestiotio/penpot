;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS INC

(ns app.main.ui.shapes.bool
  (:require
   [app.common.data.macros :as dm]
   [app.common.geom.shapes :as gsh]
   [app.main.ui.hooks :as h]
   [app.main.ui.shapes.export :as use]
   [app.main.ui.shapes.path :refer [path-shape]]
   [rumext.v2 :as mf]))

(defn bool-shape
  [shape-wrapper]
  (mf/fnc bool-shape
    {::mf/wrap-props false}
    [props]
    (let [shape     (unchecked-get props "shape")
          children  (unchecked-get props "childs")
          children  (h/use-equal-memo children)

          metadata? (mf/use-ctx use/include-metadata-ctx)
          content   (mf/with-memo [shape children]
                      (let [content (:bool-content shape)]
                        (cond
                          (some? content)
                          content

                          (some? children)
                          (gsh/calc-bool-content shape children))))

          shape     (mf/with-memo [shape content]
                      (assoc shape :content content))]

      [:*
       (when (some? content)
         [:& path-shape {:shape shape}])

       (when metadata?
         ;; FIXME: get children looks wrong
         [:> "penpot:bool" {}
          (for [item (map #(get children %) (:shapes shape))]
            [:& shape-wrapper
             {:shape item
              :key (dm/str (dm/get-prop item :id))}])])])))
