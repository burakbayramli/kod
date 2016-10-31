import scrape_bt

con = open("test1.html").read()
line = scrape_bt.extract(con)
assert len(line.split(";")) == 9

con = open("test2.html").read()
line = scrape_bt.extract(con)
assert len(line.split(";")) == 9
