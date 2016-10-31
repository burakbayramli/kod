from Pymacs import lisp
import re, time, os

interactions = {}

from IPython.testing.globalipapp import get_ipython
from IPython.utils.io import capture_output
from memo import *

@memo
def get_ip():
    ip = get_ipython()
    ip.run_cell('%load_ext autoreload')        
    ip.run_cell('%autoreload 2')    
    ip.run_cell('import numpy as np')
    ip.run_cell('import matplotlib.pylab as plt')
    return ip

def run_cell(cmd):
    with capture_output() as io:
        res = get_ip().run_cell(cmd)
    res_out = io.stdout
    return res_out

# make digits into length two - i.e. 1 into 01
def two_digit(i): return "0"+str(i) if i < 10 else str(i)

def get_block_content(start_tag, end_tag):
    remember_where = lisp.point()
    block_end = lisp.search_forward(end_tag)
    block_begin = lisp.search_backward(start_tag)
    content = lisp.buffer_substring(block_begin, block_end)
    content = re.sub(start_tag, "", content)
    content = re.sub(end_tag, "", content)
    lisp.goto_char(remember_where)
    return block_begin, block_end, content

def get_buffer_content_prev(bend):
    where_am_i = lisp.point()
    lisp.beginning_of_buffer(); st = lisp.point()
    s = lisp.buffer_substring(st,bend)
    lisp.goto_char(where_am_i)
    return s

def run_py_code():
    remember_where = lisp.point()
    # check if the line contains \inputminted
    lisp.beginning_of_line()
    l1 = lisp.point()
    lisp.end_of_line()
    l2 = lisp.point()
    line = lisp.buffer_substring(l1,l2)
    # if code comes from file
    # get code content from latex
    block_begin,block_end,content = get_block_content("```python","```\n")

    # we have code content at this point

    # scan content to find plt.plot(). if there is, scan buffer
    # previous to *here* to determine order of _this_ plt.plot(), and
    # give it an appropiate index that will be appended to the end of
    # the .png image file, i.e. [buffer name]_[index].png. plt.plot()
    # commands will be replaced by the corresponding plt.savefig
    # command.

    # generate savefig for execution code (no output in emacs yet)
    bc = lisp.buffer_string()
    plt_count_before = len(re.findall('plt\.savefig\(',bc))
    base = os.path.splitext(lisp.buffer_name())[0]
    f = '%s_%s.png' % (base, two_digit(plt_count_before+1))
    rpl = "plt.savefig('%s')" % f
    show_replaced = True if "plt.show()" in content else False
    content=content.replace("plt.show()",rpl)
    content="plt.figure();\n"+content    
    include_graphics_command = "![](%s)" % f
    
    # we have code content at this point
    start = time.time()
    
    with capture_output() as io:
        res_code = get_ip().run_cell(content)
    res = io.stdout

    elapsed = (time.time() - start)
    if len(res) > 0: 
        display_results(block_end, res) # display it

    if show_replaced:
        lisp.goto_char(block_end)
        lisp.forward_line(2) # skip over end verbatim, leave one line emtpy
        lisp.insert('\n' + include_graphics_command + '\n')
        lisp.scroll_up(1) # skip over end verbatim, leave one line emtpy        
        lisp.goto_char(remember_where)
        lisp.replace_string("plt.show()",rpl,None,block_begin,block_end)

    
    lisp.goto_char(remember_where)
    
    lisp.message("Ran in " + str(elapsed) + " seconds")

def verb_exists():
    remem = lisp.point()
    lisp.forward_line(1)
    lisp.beginning_of_line()
    verb_line_b = lisp.point()
    lisp.end_of_line()
    verb_line_e = lisp.point()
    verb_line = lisp.buffer_substring(verb_line_b, verb_line_e)
    lisp.goto_char(remem)
    if "```text" in verb_line: return True
    else: return False
    
def display_results(end_block, res):
    remem = lisp.point()
    res=res.replace("\r","")
    lisp.goto_char(end_block)
    verb_begin = None
    # if there is output block, remove it whether there output or not
    # because it will be replaced anyway if something exists
    if verb_exists():
        verb_begin,verb_end,content = get_block_content("```text","```\n")
        lisp.delete_region(verb_begin, verb_end)
        lisp.goto_char(remem)

    # now if there _is_ output, then go to beginning of old verbatim
    # output (if removed), if not, this is brand new output, move
    # down 2 lines, insert the output 
    if verb_begin:
        lisp.goto_char(verb_begin)
    else:
        lisp.forward_line(1)
    lisp.insert("```text\n")
    lisp.insert(res)
    lisp.insert("```\n")

def thing_at_point():
    right_set = left_set = set(['\n',' '])
    curridx = lisp.point()
    curr=''
    while (curr in right_set) == False:
        curr = lisp.buffer_substring(curridx, curridx+1)
        curridx += 1
    start = curridx-1
        
    curridx = lisp.point()
    curr=''
    while (curr in left_set) == False:
        curr = lisp.buffer_substring(curridx-1, curridx)
        curridx -= 1
    end = curridx+1
        
    s = lisp.buffer_substring(start, end)
    return s, end
        
        
def complete_py():
    thing, start = thing_at_point()
    lisp.message(thing)
    text, matches = get_ip().complete(thing)
    lisp.switch_to_buffer("*pytexipy*")
    lisp.kill_buffer(lisp.get_buffer("*pytexipy*"))
    lisp.switch_to_buffer_other_window("*pytexipy*")
    lisp.insert(thing)
    for item in matches:        
        lisp.insert(item)
        lisp.insert("\n")
            
interactions[run_py_code] = ''
interactions[complete_py] = ''
