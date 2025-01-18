import markdown, sys, os
head = '''
<html>
<head>
<script type="text/x-mathjax-config">MathJax.Hub.Config({  tex2jax: {inlineMath: [["$","$"]  ]}});</script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-AMS_HTML-full">
</script>
<meta charset='utf-8'>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
</head>
<body>
'''
content=open(sys.argv[1]).read()
#content=content.replace("\\","\\\\")
print (head)
print (markdown.markdown(content, extensions=['fenced_code','tables']))
print ("""
</body>
</html>
""")
