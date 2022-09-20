import pandas as pd, datetime, numpy as np, requests
import time as timelib, urllib.request as urllib2
from io import BytesIO

def get_yahoofin(year,ticker):
    end = datetime.datetime.now()
    start = datetime.datetime(year, 1, 1)
    start = int(timelib.mktime(start.timetuple()))
    end = int(timelib.mktime(end.timetuple()))
    base_fin_url = "https://query1.finance.yahoo.com/v7/finance/download"
    url = base_fin_url + "/%s?period1=" + str(start) + "&period2=" + \
          str(end) + "&interval=1d&events=history&includeAdjustedClose=true"
    url  = url % ticker
    r = urllib2.urlopen(url).read()
    file = BytesIO(r)
    df = pd.read_csv(file,index_col='Date',parse_dates=True)['Close']
    return df

def get_garan_xch_data():
    res = []; cols = []
    df = pd.read_csv("garanpairs.csv",sep=';')    
    for idx, row in df.iterrows():
        if 'nan' not in str(row['YF']):
            p = get_yahoofin(2010,row['YF'])
            print (p)
            res.append(p)
            cols.append(row['YF'])
    
    dfall = pd.concat(res,axis=1)
    dfall.columns = cols
    dfall.to_csv('xch.csv')

if __name__ == "__main__": 
    #get_garan_xch_data()
    pass

