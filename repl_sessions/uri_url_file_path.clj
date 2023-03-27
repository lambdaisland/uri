(ns repl-sessions.uri-url-file-path
  (:require
   [clojure.java.io :as io])
  (:import
   (java.io File)
   (java.net URI URL)
   (java.nio.file Files OpenOption Path Paths)))

;; Let's explore some JVM alternatives for dealing with URI/URL/File objects

;; There are four objects in the Java Platform for representing either file paths
;; or URI/URLs.

;; Two have been around all the way since Java 1.0. The other two came a bit later
;; - java.io.File :       java 1.0 (1996)
;; - java.net.URL :       java 1.0 (1996)
;; - java.net.URI :       java 1.4 (2002)
;; - java.nio.file.Path : java   7 (2011)

(File. "/tmp/xxx")
(io/file "/tmp/xxx")
(URL. "file:/tmp/xxx")
(URI. "file:/tmp/xxx")
(Path/of "/tmp/xxx" (into-array String []))

;; # File

;; Internally consists of
;; - String path
;; - int pathPrefixLength
;; - char pathSeparatorChar

;; private static final FileSystem fs = DefaultFileSystem.getFileSystem();
;; -> hidden but e.g. UNIXFileSystem
;; public static final char pathSeparatorChar = fs.getPathSeparator();

;; OS-dependent
(.getName (File. "C:\\hello\\world"))
(.getName (File. "/foo/bar"))
(.getParent (File. "/foo/bar"))

;; can deal with relative
(.getCanonicalFile (File. "../foo/bar"))

;; most operations are delegated to the FileSystem implementation
(.equals (io/file "hello") (io/file "HELLO"))
;; => on Windows this will return true


;;; # URL
;; Universal Resource Locator

;; Main fields: protocol / host / port / "file"
;; Many on-demand computed fields
;; - ref
;; - authority
;; - query
;; - path

;; - InetAddress
;; - URLStreamHandler

(slurp (.openStream (URL. "https://clojure.org")))
(slurp (URL. "https://clojure.org"))

;; Built-in handlers
;; - file
;; - ftp
;; - http
;; - https
;; - jar
;; - jmod
;; - jrt
;; - mailto

;; Here be dragons

(= (URL. "http://a.example.com/foo")
   (URL. "http://b.example.com/foo"))
;; -> does DNS resolution (so does .hashCode!)
(= (URL. "http://example.com:80/foo")
   (URL. "http://example.com/foo"))

;; Compare
(= (URI. "http://example.com:80/foo")
   (URI. "http://example.com/foo"))
(= (URI. "http://a.example.com/foo")
   (URI. "http://b.example.com/foo"))

;; But, still widely used, many Java APIs take URL, and probably will forever
;; take URLs.
(io/resource "clojure/core.clj")

;; # URI
;; Sensible! Simple value object!
;; Most of the code is for parsing, normalizing, relativizing (like lambdaisland.uri)
;; Can easily convert between the different forms.
;; Use this one if you can. (even when just working with files/paths)


;; # Path
;; Similar to File it relies on a FileSystemProvider (instead of a FileSystem)
;; But, not limited to one per system!
;; Can be third party!

;; Can be created with simple strings, will default to plain file. Beware:
;; varargs.

(Path/of "hello" (into-array String []))

;; Really neat that you can do this:
(Files/write
 (Paths/get (URI. "gs://does-data-prod/sessionize/7171/workshop.edn"))
 (.getBytes "hello" "UTF-8")
 (into-array OpenOption []))

;; Annoying this is making sure the filesystem is "open"
