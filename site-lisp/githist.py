# -*- coding: utf-8 -*-
#
# Starting from any file, goes up in directory finds the .git
# directory location, from there executes the git show command. 
# Comes pretty handy to compare two or more versions of the same file 
# by naked eye. 
#

from Pymacs import lisp
import re, sys, os, subprocess
import glob, string

def run_command(command):
    result = []
    print 'Running:', command
    f = os.popen(command, "r")
    sys.stdout.flush()
    for l in f.xreadlines():
        result.append(l)
    return result

def branch():
    '''
    run shell command and return the output as list
    '''
    f = os.popen("git branch", "r")
    return re.findall('\\*\s(\w+)',f.read().strip())[0]

def show_version(num):
    print "Getting this version ago:" + str(num)
    curr_dir = os.getcwd()
    dot_git_dir = find_dot_git()    
    os.chdir(dot_git_dir)
    
    fname=lisp.buffer_file_name()
    fdir=os.path.dirname(fname)
    
    # subtract the .git location from the beginning part of the 
    # full path because git show does not like it
    suitable_file_for_git_show = re.sub(dot_git_dir, "", fname)

    # also get rid of the first / 
    dot_git_dir = dot_git_dir.replace("\\","/")
    print dot_git_dir
    print "suitable_file_for_git_show", suitable_file_for_git_show
    suitable_file_for_git_show = suitable_file_for_git_show.replace(dot_git_dir,"")
    print "suitable_file_for_git_show", suitable_file_for_git_show
    
    os.chdir(dot_git_dir)
    cmd = "git show %s~%d:%s > %s/githist-%d.dat" % (branch(),
                                                     num,
                                                     suitable_file_for_git_show[1:],
                                                     os.environ['TEMP'],
                                                     num)
    res = run_command(cmd)
    #lisp.switch_to_buffer_other_window(cmd)
    lisp.find_file_other_window("%s/githist-%d.dat" % (os.environ['TEMP'],num))
    os.chdir(curr_dir)
            
def find_dot_git() :     
    fname=lisp.buffer_file_name()
    dirname = re.sub("\/\w*?\.*\w*?$", "", fname)
    print "Dir:"+dirname

    found = False
    os.chdir(dirname)
    while (True) :
        dirname = os.getcwd()
        print "Trying " + dirname + "/.git"
        if (os.path.isdir(dirname + "/.git")): return dirname
        if (os.getcwd() == "/"): 
            raise Exception("no .git found")
        os.chdir(os.pardir) 

