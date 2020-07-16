python $HOME/Documents/kod/blog.py $1 > `pwd`/out.html
#xvkbd -window *Firefox* -text "\Cr" > /dev/null 2>&1
#xvkbd -window *emacs* -text "\Ce" > /dev/null 2>&1
wc -l  $1
sort $1 | uniq -cd | sort -nr
