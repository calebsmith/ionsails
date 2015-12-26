rm -rf resources/public/js/compiled/
lein cljsbuild once min
cp -R resources/public/ build/
