# -*- coding: utf-8 -*-
#
# Simple evaluation of python code in any buffer, no markup is
# required. Function pexec calls eval() on the current line, and the
# result is displayed 2 line below. If line contains "import" or "="
# statements, eval is called differently. Another feature is selecting
# an entire region and calling M-x peval-pexec-region. The results of
# all these calls are remembered / are sticky and persists accross
# peval calls.
from Pymacs import lisp
import re, random, time
from turkish.deasciifier import Deasciifier

interactions = {}
glob = {}

def get_block_content(start_tag, end_tag):
    remember_where = lisp.point()
    block_begin = lisp.search_backward(start_tag)
    block_end = lisp.search_forward(end_tag)
    block_end = lisp.search_forward(end_tag)
    content = lisp.buffer_substring(block_begin, block_end)
    lisp.goto_char(remember_where)
    return block_begin, block_end, content

def pexec():
    global glob
    start = time.time()
    remember_where = lisp.point()
    block_begin, block_end, content = get_block_content("\n","\n")
    if "=" in content or "import" in content:
        c = compile(source=content,filename="",mode="single")
        eval(c,glob)
    else:
        c = compile(source=content,filename="",mode="eval")
        res = eval(c,glob)
        lisp.forward_line(2)
        bb1, be2, bc2 = get_block_content("\n","\n")
        lisp.delete_region(bb1,be2)
        lisp.insert("\n")
        lisp.insert(str(res))
    lisp.goto_char(remember_where)
    elapsed = (time.time() - start)
    lisp.message("Ran in " + str(elapsed) + " seconds")

def pexec_region():
    global glob
    start = time.time()
    start, end = lisp.point(), lisp.mark(lisp.t)
    content = lisp.buffer_substring(start, end)
    lines = content.split("\n")
    for line in lines:
        line = line.replace("\n","")
        if line != "":
            c = compile(source=line,filename="",mode="single")
            eval(c,glob)
    elapsed = (time.time() - start)
    lisp.message("Ran in " + str(elapsed) + " seconds")
    
interactions[pexec] = ''
interactions[pexec_region] = ''

