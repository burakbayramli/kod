import os, sys

if len(sys.argv) == 1 or sys.argv[1] == 'tex':
    os.system("pdflatex -shell-escape ast_mbti.tex")
    os.system("evince ast_mbti.pdf")
    exit()
       
