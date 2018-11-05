




```python
(43000 * 2) / 60. / 24.
```

```text
Out[1]: 59.72222222222222
```


Scrape

python -u console.py bing dog --limit 10 --json

Dand

python ../../dand/dand.py dand.conf 

Plant edibility data came from a combination of sources. First did a dump on

https://plants.sc.egov.usda.gov/adv_search.html

By enabling as many as edibility parameters, including scientific name etc.

Then scraped "Food", "Cuisine", "Culinary" headings on Wikipedia, by passing the scientific name.

Then scraped PFAF by using

https://pfaf.org/user/Plant.aspx?LatinName=__name__

for scientific name.

The combined results is below:

https://www.dropbox.com/s/9xk33ruzvpmq57f/edible_plants.csv?dl=1

