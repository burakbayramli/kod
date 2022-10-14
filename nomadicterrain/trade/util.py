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

def simple1():
    pd.set_option('display.max_columns', None)
    y1 = np.array([1 for i in range (10)])
    y2 = np.array([0.2*i for i in range(11,20)])
    y3 = np.array([1 for i in range (21,30)])
    y = np.hstack((y1,y2,y3))
    df = pd.DataFrame(y)
    df.columns = ['y']
    df['y'].plot(ylim=[0,5])
    #print (df)
    plt.savefig('tst2_01.png')
    df.to_csv('simple1.csv',index=None)    
    
if __name__ == "__main__": 
    #get_garan_xch_data()
    pass

