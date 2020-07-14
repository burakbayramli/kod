import urllib.request as req, re

URL = "https://www.random.org/integers/?num=30&min=1&max=10" + \
      "&col=1&base=10&format=html&rnd=newb" 
r = req.urlopen(URL).read().decode('utf-8')
dd = re.findall(r'<pre class=\"data\">(.*?)</pre>', r, re.DOTALL)[0]
res = [int(x) for x in dd.split("\n") if len(x) > 0]
print (res)
