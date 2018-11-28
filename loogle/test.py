import loogle2

index_dir = "/tmp/idx"    
loogle2.index("/home/burak/Documents/kod/loogle/sub", index_dir, new_index=True)
loogle2.index("/home/burak/Documents/kod/loogle/sub", index_dir)
res = loogle2.search("scientist", index_dir)
print (res)

