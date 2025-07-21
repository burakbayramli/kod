/usr/bin/pandoc --template=/home/burak/Documents/classnotes/template.html -M title="test" --mathjax=https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-AMS_HTML-full -f markdown -t html $1 -o `pwd`/out.html
#python $HOME/Documents/kod/blog.py $1 > `pwd`/out.html
wc -l  $1
sort $1 | uniq -cd | sort -nr
