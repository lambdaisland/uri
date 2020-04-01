(require '[lambdaisland.uri :as uri])

(-> (uri/uri "https://my.service/some-context/?api_key=123")
    (update :path str "some-resource"))

(defn uri-merge [& uris]
  (let [uri-maps (->> uris (map uri/uri) (map #(into {} (filter (comp some? val)) %)))
        merged-path (some->> uris (keep :path) seq (apply uri/join) :path)]
    (-> (apply merge uri-maps)
        (assoc :path merged-path)
        uri/map->URI)))

(str (uri-merge "http://localhost:3303/hello-world"
                "https://en.wikipedia.org/wiki/VT100#Variants"))
;; => "https://en.wikipedia.org:3303#Variants"

(def endpoint "https://my.service/some-context/")
(def api-key "123")

(defn api-uri [& segments]
  (-> (apply uri/join endpoint segments)
      (assoc :query (str "api-key=123"))))

(api-uri endpoint "some-resource")
;; => "https://my.service/some-context/some-resource?api-key=123"
