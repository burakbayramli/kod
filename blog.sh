pandoc $1 > out.html
perl -pi -e 's/<\/p>/<\/p><br\/>/g' out.html;
