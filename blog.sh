python $HOME/Documents/kod/blog.py $1 > `pwd`/out.html
xvkbd -window *Firefox* -text "\Cr"
xvkbd -window *emacs* -text "\Ce"
sort $1 | uniq -cd | sort -nr
