find . -type f -name "$1" -exec egrep -inH "$2" {} \;
