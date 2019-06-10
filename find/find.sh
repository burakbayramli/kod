find . -type f -name "$1" -exec grep -inH "$2" {} \;
