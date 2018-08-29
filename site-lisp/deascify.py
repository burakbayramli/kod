# -*- coding: utf-8 -*-
"""
Emacs'de edit yaparken uzerinde oldugunuz paragrafi mumkun oldugu
kadar Turkcelestirir.
"""
from Pymacs import lisp
import re, random, itertools
from turkish.deasciifier import Deasciifier

interactions = {}

def get_block_content(start_tag, end_tag):
    remember_where = lisp.point()
    block_begin = lisp.search_backward(start_tag)
    block_end = lisp.search_forward(end_tag)
    block_end = lisp.search_forward(end_tag)
    content = lisp.buffer_substring(block_begin, block_end)
    lisp.goto_char(remember_where)
    return block_begin, block_end, content

def to_tr(s):    
    tokens = re.split("(\\$.*?\\$)",s)
    res = []
    for x in tokens:
        if x[0]=='$' and x[-1] == '$': res.append(x); continue
        dea = Deasciifier(x)
        x = dea.convert_to_turkish()
        res.append(x)
    return ''.join(res)
    
def convert():
    remember_where = lisp.point()
    block_begin, block_end, content = get_block_content("\n\n","\n\n")

    # alttaki listeye dokunulmamasi istenen kelimeler tek ogeli
    # tuple olarak yazilir, ("kelime",) gibi. eger degisim isteniyorsa
    # ("kelime","degisim") olarak bir tuple eklenir. 
    r_list = [("verisi",), ("Calculus",), ("AIC",), ("estimator",),
              ("ise",), ("kontur",), ("hacim","hacim"), ("ODE",),
              ("Oklit",u'Öklit'), ("karekok",u'karekök'),("parcacigi",u"parçacığı"),
              ("kismi", u'kısmi'), ("integral",), ("arastirmaci",u"araştırmacı"),
              ("ihtiyacimiz",u'ihtiyacımız'), ("oldugu",u'olduğu'),
              ("parcaci",u"parçacı"), ("acilimi",u'açılımı'),
              ("ihtiyaci",u'ihtiyacı'), ("acilim",u'açılım'),
              ("acisini",u'açısını'), ("acisi",u'açısı'),
              ("aci",u'açı'), ("minimize",), ("gayri",u'gayrı'),
              ("Pandas",), ("algoritma",), ("gayri",u'gayrı'),
              ("sigma",), ("volatility",), ("matris",),
              ("frac","frac"), ("sonum",u"sönüm"), ("eksen",),
              ("sonusur",u"sonuşur"), ("amaciyla",u"amacıyla"), 
              ("amaci",u"amacı"),("sarsim",u"sarsım"),
              ("Sarsim",u"Sarsım"), (u"olduğu",), ("oldu",), (" yani",),
              (" Yani ",), ("entegrali",), ("sistem",),("invertible",),
              ("sistemi",), ("araci",u"aracı"),("agaci",u"ağacı"),
              ("robust",),("lineer",),("Tensor",) ]
              
    dict = {}; r_list_coded = []
    for x in r_list:
        key = str(int(random.random() * 1000000))
        r_list_coded.append((x[0],key))
        if len(x)==1: dict[key] = x[0]
        if len(x)==2: dict[key] = x[1]

    for x in r_list_coded: content = content.replace(x[0],x[1])

    result = to_tr(content)

    for x in dict.keys(): result = result.replace(x,dict[x])
        
    lisp.delete_region(block_begin, block_end)
    lisp.insert(result)
    lisp.goto_char(remember_where)
            
interactions[convert] = ''
