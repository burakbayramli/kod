import markdown, sys, os
#content=open("/data/data/com.termux/files/home/Documents/Dropbox/blogs/sk/2016/01/python-ile-finans-verileri.md").read()
content=open(sys.argv[1]).read()
print (markdown.markdown(content, extensions=['fenced_code']))
