import pandas as pd, json
import numpy as np, datetime
import matplotlib.pyplot as plt
import pandas_datareader.data as web
import quandl, os

params = json.loads(open(os.environ['HOME'] + "/.nomadicterrain").read())

auth = params['quandl']

start=datetime.datetime(2013, 1, 1)
end=datetime.datetime(2019, 2, 20)
df = web.DataReader("SP500", 'fred', start, end)
print (df.tail(10))

# oil
df = quandl.get("EIA/PET_RWTC_D",                 
                 returns="pandas",
                 start_date='2010-01-01',
                 end_date='2019-02-20',
                 authtoken=auth)
print (df.tail(10))

# dow jones
df = quandl.get("BCB/UDJIAD1",                 
                 returns="pandas",
                 start_date='2010-01-01',
                 end_date='2019-02-20',
                 authtoken=auth)
print (df.tail(10))

# dollar
df = quandl.get("FRED/DTWEXM",                 
                 returns="pandas",
                 start_date='2010-01-01',
                 end_date='2019-02-20',
                 authtoken=auth)
print (df.tail(10))


