import urllib.request, re
from bs4 import BeautifulSoup

ps = ['Achillea millefolium','Cinna latifolia','Chenopodium album']

for p in ps:
    print (p)
    plant = p.replace(" ","_")
    #with urllib.request.urlopen('https://en.wikipedia.org/wiki/Achillea_millefolium') as response:
    with urllib.request.urlopen('https://en.wikipedia.org/wiki/' + plant) as response:
       html = response.read()
       res = re.findall('<h3><span class="mw-headline" id="Food".*?<p>(.*?)</p>', str(html))
       if len(res) > 0:
           t = res[0]
           soup = BeautifulSoup(t, "html.parser")
           for a in soup("a"): a.replace_with(a.text)
           for a in soup("sup"): a.replace_with(a.text)
           print(str(soup))
       
