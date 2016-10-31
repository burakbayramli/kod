from functools import wraps

# store the return result of parameterless functions
def memo(func):
    cache = {}                                  
    @wraps(func)                                
    def wrap(*args):
        if args not in cache:
            cache[args] = func()
        return cache[args]
    return wrap 
