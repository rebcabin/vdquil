;; Ben Fry's Visualizing Data, Chapter 4 (Time Series), figure 8:
;; Continuously drawn time series using vertices
;; Converted from Processing to Quil as an exercise by Dave Liepmann

(ns vdquil.ch4_fig8
  (:use quil.core)
  (require [clojure.set :refer [union]]))

;; Color conversion function, courtesy of Jack Rusher
(defn hex-to-rgb [hex]
  (map (comp #(Integer/parseInt % 16) (partial apply str))
       (partition 2 (.replace hex "#" ""))))

;; Data (locations, names) was extracted from the supplied TSV files
;; interactively in emacs using Jack Rusher's tsv-to-sexp lisp function:
;;
;;;; (defun tsv-to-sexp (tsv)
;;;;   "Parses the string `tsv` as a tab-separated-value file,
;;;; returning a sexp containing the values with strings converted to
;;;; numbers where appropriate."
;;;;   (-map (lambda (s) (-map 'reformat-field (s-split "\t" s))) (s-lines tsv)))

(def milk-tea-coffee-data
  '(("Year" "Milk" "Tea" "Coffee")
    (1910 32.2 9.6 21.7)
    (1911 31.3 10.2 19.7)
    (1912 34.4 9.6 25.5)
    (1913 33.1 8.5 21.2)
    (1914 31.1 8.9 21.8)
    (1915 29 9.6 25)
    (1916 28 9.6 27.1)
    (1917 29.7 11.3 28.6)
    (1918 34 11.3 23.7)
    (1919 30.4 5.8 27.9)
    (1920 34 7.7 27.6)
    (1921 33.2 6.5 28.4)
    (1922 33.5 8 27.8)
    (1923 32.5 8.5 29.9)
    (1924 32.7 7.5 28.9)
    (1925 33.6 8.1 25)
    (1926 33.5 7.6 29.3)
    (1927 33.2 6.9 28.7)
    (1928 33.2 6.9 28.2)
    (1929 33.4 6.8 28.7)
    (1930 33.2 6.4 29.5)
    (1931 33.2 6.5 30.7)
    (1932 33.8 7.1 29.4)
    (1933 33.7 7.2 30.1)
    (1934 32.5 5.5 29.1)
    (1935 33 6 31.7)
    (1936 33.3 6 32.4)
    (1937 33.5 6.4 31.4)
    (1938 33.5 6.3 35.2)
    (1939 34 6.6 35.2)
    (1940 34 6.2 36.6)
    (1941 34.4 7.3 38)
    (1942 37 5.2 36.2)
    (1943 41 5.7 33.1)
    (1944 43.6 5.1 41.8)
    (1945 44.7 5.2 44.4)
    (1946 42.1 5.2 46.4)
    (1947 39.9 5.4 40.8)
    (1948 38.1 5.4 43.5)
    (1949 37.5 5.7 45.1)
    (1950 37.2 5.7 38.6)
    (1951 37.5 6.1 39.5)
    (1952 37.6 5.9 38)
    (1953 37 6.2 37.3)
    (1954 36.2 6.4 30.5)
    (1955 36.2 6 32)
    (1956 36.3 5.9 31.6)
    (1957 35.9 5.5 30.6)
    (1958 35.2 5.5 30.4)
    (1959 34.4 5.5 30.9)
    (1960 33.9 5.6 30.7)
    (1961 33 5.8 31)
    (1962 32.9 6 31)
    (1963 33 6.2 30.8)
    (1964 33 6.3 30.5)
    (1965 32.9 6.4 29.4)
    (1966 33 6.5 28.9)
    (1967 31.4 6.6 29)
    (1968 31.3 6.8 29.1)
    (1969 31.1 6.8 27.6)
    (1970 31.3 6.8 27.4)
    (1971 31.3 7.2 25.7)
    (1972 31 7.3 26.8)
    (1973 30.5 7.4 25.8)
    (1974 29.5 7.5 24.2)
    (1975 29.5 7.5 23.3)
    (1976 29.3 7.7 23.7)
    (1977 29 7.5 17.2)
    (1978 28.6 7.2 19.9)
    (1979 28.2 6.9 21.7)
    (1980 27.6 7.3 19.2)
    (1981 27.1 7.2 18.7)
    (1982 26.4 6.9 18.3)
    (1983 26.3 7 18.5)
    (1984 26.4 7.1 18.9)
    (1985 26.7 7.1 19.3)
    (1986 26.5 7.1 19.4)
    (1987 26.1 6.9 18.8)
    (1988 26.1 7 18.2)
    (1989 26 6.9 18.8)
    (1990 25.7 6.9 19.4)
    (1991 25.5 7.4 19.5)
    (1992 25.1 8 18.9)
    (1993 24.4 8.3 17.2)
    (1994 24.3 8.1 15.6)
    (1995 23.9 7.9 15.3)
    (1996 23.8 7.6 16.8)
    (1997 23.4 7.2 17.9)
    (1998 23 8.3 18.3)
    (1999 22.9 8.2 19.3)
    (2000 22.5 7.8 20)
    (2001 22 8.2 18.5)
    (2002 21.9 7.8 18.1)
    (2003 21.6 7.5 18.5)
    (2004 21.2 7.3 18.8)))

(def current-column (atom 1))

(defn setup []
  (set-state!
   :ymtc (atom (rest milk-tea-coffee-data))))

(defn draw []
  (background 224)
  (smooth)
  
  ;; Show the plot area as a white box
  (fill 255)
  (no-stroke)
  (rect-mode :corners)
  ;; ... define corners of the plotted time series
  (let [plotx1 120
        plotx2 (- (width) 80)
        ploty1 60
        ploty2 (- (height) 70)
        year-min (apply min (map #(first %) @(state :ymtc))) 
        year-max (apply max (map #(first %) @(state :ymtc)))
        year-interval 10
        volume-interval 5
        data-min (apply min (union (map #(second %) @(state :ymtc)) (map #(nth % 2) @(state :ymtc)) (map #(nth % 3) @(state :ymtc))))
        data-max (* volume-interval (ceil (/ (apply max (union (map #(second %) @(state :ymtc)) (map #(nth % 2) @(state :ymtc)) (map #(nth % 3) @(state :ymtc)))) volume-interval)))
        data-first 0]
    (rect plotx1 ploty1 plotx2 ploty2)

    ;; Draw title
    (fill 0)
    (text-size 20)
    (text-align :left :baseline)
    (text-font (create-font "Sans-Serif" 20))
    (text (nth (first milk-tea-coffee-data) @current-column) plotx1 (- ploty1 10))

    ;; Draw year labels
    (text-size 10)
    (text-align :center :top)
    (stroke-weight 1)
    ;; ...use thin, gray lines to draw the grid
    (stroke 224)
    (doseq [year (range year-min year-max year-interval)]
      (let [x (map-range year year-min year-max plotx1 plotx2)]
        (text (str year) x (+ 10 ploty2) )
        (line x ploty1 x ploty2 )
        ))

    ;; Draw volume labels
    ;; (Since we're not drawing the minor ticks, we would ideally
    ;; increase volume-interval to 10 and remove the modulo-10 check.
    (text-align :right :center)
    (doseq [volume (range data-first data-max volume-interval)]
      (let [y (map-range volume data-first data-max ploty2 ploty1)]
        ;; Commented out; the minor tick marks are too visually distracting
        ;; (stroke 128)
        ;; (line plotx1 y (- plotx1 2) y) ;; Draw minor tick
        (if (= 0 (mod volume 10)) ;; Draw major tick mark
          (do
            (stroke 0)
            (line plotx1 y (- plotx1 4) y)
            (text-align :right :center) ;; Center vertically
            (if (= volume data-first) (text-align :right :bottom)) ;; Align the "0" label by the bottom
            (text (str (ceil volume)) (- plotx1 10) y)))))
    ;; Clojure's range function is exclusive on the upper bound,
    ;; so we have to manually append to the above loop
    (text-align :right :top) ;; Align the "50" by the top
    (stroke 0)
    (line plotx1 ploty1 (- plotx1 4) ploty1)
    (text (str data-max) (- plotx1 10) ploty1)

    ;; Draw axis labels
    (text-size 13)
    (text-leading 15)
    (text-align :center :center)
    (text "Gallons\nconsumer\nper capita" 50 (/ (+ ploty1 ploty2) 2))
    ;; (Data referred to directly, rather than by state, in order to get the labels.)
    (text (str (first (first milk-tea-coffee-data))) (/ (+ plotx1 plotx2) 2) (- (height) 25))
    
    ;; Draw data points
    (stroke-weight 5)
    (stroke (color (first (hex-to-rgb "#5679C1")) (second (hex-to-rgb "#5679C1")) (nth (hex-to-rgb "#5679C1") 2)))
    (no-fill)
    (begin-shape)
    (doseq [row @(state :ymtc)]
      (let [year (first row)
            milk (second row)
            tea (nth row 2)
            coffee (nth row 3)]
        (vertex (map-range year year-min year-max plotx1 plotx2) (map-range (nth row @current-column) data-min data-max ploty2 ploty1))))
    (end-shape)))

(defn switch-data-set []
  (if (= (str (raw-key)) "[")
    (do (swap! current-column inc)
        (if (>= @current-column (count (first @(state :ymtc))))
          (reset! current-column 1)))
    (if (= (str (raw-key)) "]")
      (do (swap! current-column dec)
          (if (= @current-column 0)
            (reset! current-column (- (count (first @(state :ymtc))) 1)))))))

(defsketch mtc
  :title "Milk, Tea, Coffee"
  :setup setup
  :draw draw
  :size [720, 405]
  :key-pressed switch-data-set)
