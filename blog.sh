filename=$1
filename="${filename%.*}"
/usr/bin/pandoc --template=/home/burak/Documents/classnotes/template.html -M title="test" --mathjax=https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-AMS_HTML-full -f markdown -t html $filename.md -o `pwd`/$filename.html
echo $filename
perl -pi -e 's/<title>test<\/title>/<title><\/title>/sg'  `pwd`/$filename.html
wc -l  $filename.md
sort $filename.md | uniq -cd | sort -nr
