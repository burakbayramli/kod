(set-language-environment "UTF-8")

(autoload 'c++-mode "cc-mode" "C++ Editing Mode" t) 
(autoload 'c-mode "c-mode" "C mode" t)

(setq initial-scratch-message nil) ;; empty scratch buffer
(setq max-specpdl-size 50000)
(setq max-lisp-eval-depth 50000)
(setq tool-bar-mode -1)
(setq auto-resize-tool-bars -1) 
(setq compile-command "python -u build.py ")
(setq x-select-enable-clipboard t)
(setq my-python-command "python") ;; pymacs uses this

(set-variable (quote latex-run-command) "pdflatex")
(set-variable (quote tex-dvi-view-command) "xpdf")

(setenv "PATH"
  (concat
   ;; Change this with your path to MSYS bin directory
   "c:\\Program Files\\Git\\usr\\bin;"
   (getenv "PATH")))

(defun move-back-one-char ()  
  (interactive)                  
  (backward-char 1)
  (backward-delete-char)
  )

(defun move-back-three-chars ()  
  (interactive)                  
  (backward-char 3)              
  (backward-delete-char)
  )

(defun move-back-four-chars ()  
  (interactive)                  
  (backward-char 4)              
  (backward-delete-char)
  )

(defun move-back-seven-chars ()  
  (interactive)                  
  (backward-char 7)              
  (backward-delete-char)
  )

;; set name of abbrev file with .el extension
(setq abbrev-file-name "c:/Users/burak/Documents/kod/site-lisp/abbrevs.el")
(setq-default abbrev-mode t)
(setq save-abbrevs nil)
(setq ev-exe "C:\\Users\\burak\\AppData\\Local\\Apps\\Evince-2.32.0.145\\bin\\evince.exe")
(setq img-viewer-exe "c:\\Program Files\\FastStone Image Viewer\\FSViewer.exe") 
(setq chrome-exe "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe") 

(defun open-file-ext ()
  "In dired, open the file named on this line."
  ;;file-name-extension 
  (interactive)
  (let* ((file (buffer-file-name (current-buffer)) ))
    (when (equal (file-name-extension file) "png")
      (call-process img-viewer-exe nil 0 nil file))
    (when (equal (file-name-extension file) "jpg")
      (call-process img-viewer-exe nil 0 nil file))
    (when (equal (file-name-extension file) "pdf")
      (call-process ev-exe nil 0 nil file))
    (when (equal (file-name-extension file) "djvu")
      (call-process ev-exe nil 0 nil file))
    (when (equal (file-name-extension file) "html")
      (call-process chrome-exe nil 0 nil file))
    (when (equal (file-name-extension file) "htm")
      (call-process chrome-exe nil 0 nil file))
    ))

;; Make all yes-or-no questions as y-or-n
;; tired of typing yes when I simply want a buffer deleted
(fset 'yes-or-no-p 'y-or-n-p)

(defun my-delete-word (arg)
  "Delete characters forward until encountering the end of a word.
With argument, do this that many times.
This command does not push erased text to kill-ring."
  (interactive "p")
  (delete-region (point) (progn (forward-word arg) (point))))

(defun my-backward-delete-word (arg)
  "Delete characters backward until encountering the beginning of a word.
With argument, do this that many times.
This command does not push erased text to kill-ring."
  (interactive "p")
  (my-delete-word (- arg)))


(modify-coding-system-alist 'file "*compilation*" 'utf-8)
(modify-coding-system-alist 'file "\\.pl\\'" 'latin-5)
(modify-coding-system-alist 'file "\\.tex\\'" 'latin-5)
(modify-coding-system-alist 'file "\\.htm\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.xml\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.html\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.csv\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.sql\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.xhtml\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.jsp\\'" 'latin-5)
(modify-coding-system-alist 'file "\\.inc\\'" 'latin-5)
(modify-coding-system-alist 'file "\\.el\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.txt\\'" 'utf-8)
(modify-coding-system-alist 'file "\\.md\\'" 'utf-8)

(require 'nxml-mode)
(load-file "c:/Users/burak/Documents/kod/site-lisp/_latin_post_ek.el")
(require 'tempo)

(setq bell-volume 0)
;;(set-message-beep 'silent)
(setq indent-tabs-mode nil)

(setq TeX-master-file-ask nil)

(add-to-list 'load-path "c:/Users/burak/Documents/kod/site-lisp/python-mode.el-6.0.10") 
(setq py-install-directory "c:/Users/burak/Documents/kod/site-lisp/python-mode.el-6.0.10")
(autoload 'autopair-global-mode "autopair" nil t)
;;(autopair-global-mode)
(add-hook 'lisp-mode-hook
          #'(lambda () (setq autopair-dont-activate t)))

(custom-set-variables
  ;; custom-set-variables was added by Custom -- don't edit or cut/paste it!
  ;; Your init file should contain only one such instance.
 '(archive-zip-extract (quote ("unzip" "-qq" "-c")))
 '(case-fold-search t)
 '(blink-cursor-mode nil)
 '(compilation-scroll-output t)
 '(current-language-environment "Turkish")
 '(default-input-method "turkish-postfix")
 '(ecb-options-version "2.27")
 '(dired-recursive-copies t)
 '(global-font-lock-mode t)
 '(inhibit-iso-escape-detection t t)
 '(inhibit-startup-message t)
 '(scroll-conservatively 1)
 '(scroll-step 1)
 '(cua-mode t)
 '(text-mode-hook (quote (turn-on-auto-fill text-mode-hook-identify)))
 '(transient-mark-mode t)
 '(truncate-lines t)
 '(which-func-mode-global t nil (which-func))
 )


(defun benim-list-buffers()
  ;; if I am viewing one buffer, in one screen, then this func divides
  ;; screen, and buffer list is in the other window, the cursor goes
  ;; there. If screen is already divided, then buffer list is
  ;; brought up in whatever window I happen to be in.
  (interactive)
  (if (one-window-p)
      (progn
        (split-window)(other-window 1)(buffer-menu))
    (progn 
      (buffer-menu)
      ))
  )
(define-key global-map "\C-x\C-b" 'benim-list-buffers)
(global-set-key "\M-b" 'benim-list-buffers)

;;
;; editing settings
(setq column-number-mode t)
;;(pc-selection-mode) ;; Ctrl-C Ctrl-V Windows'daki gibi calisiyor
;;(load "cua-mode")
;;(CUA-mode t)
(add-hook 'text-mode-hook 'turn-on-auto-fill)
(add-hook 'nxml-mode-hook 'turn-on-auto-fill)
(setq fill-column 80) 

;; backups
(setq make-backup-files nil)
(setq vc-make-backup-files nil)

;; loads the _emacs file with one keystroke
(defun find-dotemacs() (interactive)
  (find-file "c:/Users/burak/Documents/kod/site-lisp/emacs-win.el"))
(define-key global-map "\C-c\C-f" 'find-dotemacs)

(defun kill-current-buffer ()
  "Kill the current buffer, without confirmation."
  (interactive)
  (kill-buffer (current-buffer)))

(defun my-dired ()
  "Do Quick Dired Without Key Confirmation ."
  (interactive)
  (dired nil))

(defun cleanup ()
  (interactive)
  (if (buffer-live-p (get-buffer "*Compile-Log*" ))
      (kill-buffer (get-buffer
		    "*Compile-Log*")))
  (if (buffer-live-p (get-buffer "*vc-diff*"     ))
      (kill-buffer (get-buffer
		    "*vc-diff*")))
  (if (buffer-live-p (get-buffer "*Completions*" ))
      (kill-buffer (get-buffer
		    "*Completions*")))
  (if (buffer-live-p (get-buffer "*Buffer List*" ))
      (kill-buffer (get-buffer
		    "*Buffer List*")))
  )


(defun use-python-3 ()
  (interactive)
  (setq compile-command "C:\\Users\\burak\\Anaconda2\\envs\\py3k\\python.exe -u build.py ")
  )


;;
;;create my menu
;;
(easy-menu-define
  my-jde-mode-menu
  global-map
  "Custom"
  (list
   "Custom"
   "--"
   ["Open Dired In Current Dir..." my-dired]   
   ["Open Shell In Current Dir..." open-cmd-in-current-dir]   
   ["Open Explorer In Current Dir..." open-explorer-in-current-dir]   
   ["Refresh..." revert-buffer]
   ["Emacs Derle" byte-me]
   ["Ready for Blog" ready-for-blog]   
   ["Git Show Older Version" githist-do-show-version]
   ["Use Python 3" use-python-3]   
   ["Repeat Last Command..." repeat-complex-command]   
   ["News..." news-retrieve]   
   ))
(easy-menu-add my-jde-mode-menu)

(defun news-retrieve()
  (interactive)
  (compile "python c:/Users/burak/Documents/kod/news.py")
  (delete-other-windows)
  (switch-to-buffer "*compilation*")
  (org-mode)
  )

;;
;; bu bir ziplama fonksiyonu cunku tempo-template-tex-itemize
;; her nedense alt taraftaki goruntuyu yokediyor (dosyaya bir sey
;; olmuyor) fakat elle recenter yapmak gerekiyor; Bu ek hareketi otomatik
;; olarak burada yaptiriyoruz
;;
(defun x-tex-itemize()
  (interactive)
  (tempo-template-tex-itemize)
  (recenter)
  )

;;create my menu
(easy-menu-define
  my-book-mode-menu
  global-map
  "Other"
  (list
   "Other"
   "--"
   ["Tex New Equation Page" tempo-template-new-equation-page]
   ["Html Code" tempo-template-cdata]
   ["Python Main" tempo-template-python-main]
   ["Pandas" tempo-template-pandas]
   ["All Imports" tempo-template-pyall]
   ["Regression" tempo-template-reg]
   ["Tex Minted Python (File)" tempo-template-tex-listings-python-file]
   ["Tex Minted Python" tempo-template-tex-listings-python]
   ["Tex Itemize" x-tex-itemize]
   ["Tex Verbatim" tempo-template-tex-verbatim-big]
   ["Tex Enumerate" tempo-template-tex-enumerate]
   ["Tex Vector" tempo-template-tex-vector]
   ["Tex Matrix" tempo-template-tex-matrix]
   ["Tex Partial Derivative" tempo-template-tex-partial-derivative]
   ["Tex Function In Pieces" tempo-template-tex-function-in-pieces]
   ["Tex Multiline Equation" tempo-template-equation-multiline]
   ["Tex Numbered Equation" tempo-template-tex-eqnarray]
   ["Tex New Page" x-tex-new-page]
   ["Tex Itemize Wrap" tex-itemize]
   ["Tex Matlab Listing" tempo-template-verb-block]
   ["Tex Graphics Lec Notes" tempo-template-tex-graphics-2]
   ))
(easy-menu-add my-book-mode-menu)


;;
;; Benim yeni tanimladigim C-j'yi ve C-o'yu tanimamakta direnen
;; mod'larin a***sini burada belliyoruz. Resistance is futile. 
;;
(defun my-nxml-mode-hook ()
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key "\C-w" 'backward-word)  
  (local-set-key [?\M-h] 'dabbrev-expand)
  (setq tab-width 2
	indent-tabs-mode nil
	truncate-lines t
        )
  )
(add-hook 'nxml-mode-hook 'my-nxml-mode-hook)  

(defun open-file-evince()
  (interactive)
  (setq fev (buffer-file-name (current-buffer)))
  (setq fev (replace-in-string fev ".tex" ".pdf"))
  (call-process ev-exe nil 0 nil fev)
  )
  
(defun my-tex-mode-hook ()
  (local-set-key "\C-j" 'backward-char) 
  (local-set-key "\C-s" 'isearch-forward) 
  (local-unset-key "\M-s") 
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key "\M-\r" 'compile)
  (local-set-key "\M-s" 'isearch-forward) 
  (local-set-key "\C-v" 'scroll-up)
  (local-set-key "\C-c\C-t" 'open-file-evince)
  (setq fill-column 80)
  (electric-indent-mode -1)
  )
(add-hook 'tex-mode-hook 'my-tex-mode-hook)
(defun my-text-mode-hook ()
  (local-unset-key "\M-s") 
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key "\M-s" 'isearch-forward) 
  )
(add-hook 'text-mode-hook 'my-text-mode-hook)
(add-hook 'LaTeX-mode-hook '(lambda ()
                              (set-fill-column 75)
                              ))
(defun my-sql-mode-common-hook () 
  (local-set-key "\C-j" 'backward-char) 
  )
(add-hook 'sql-mode-common-hook 'my-sql-mode-common-hook)
(defun my-lisp-interaction-mode-common-hook () 
  (local-set-key "\C-j" 'backward-char) 
  )
(add-hook 'lisp-interaction-mode-common-hook 'my-lisp-interaction-mode-common-hook)
(defun my-dired-mode-hook () 
  (local-set-key "\C-o" 'other-window)
  (local-set-key "r" 'dired-up-directory)
  (local-set-key [?\M-g] 'keyboard-quit)  
  (local-unset-key "\M-s") 
  (local-set-key "\M-s" 'isearch-forward) 
  )
(add-hook 'dired-mode-hook 'my-dired-mode-hook)
(defun my-buffer-mode-hook ()
  (local-set-key "\C-o" 'other-window)	
  (local-set-key [mouse-1] 'Buffer-menu-mouse-select)
  (local-set-key [?\M-g] 'keyboard-quit)
  )
(add-hook 'buffer-menu-mode-hook 'my-buffer-mode-hook)
(defun my-c-mode-common-hook11 () 
  (local-unset-key "\C-d")
  (local-set-key "\C-d" 'forward-word)
  (local-set-key "\M-q" 'scroll-down)
  (local-set-key [?\M-j] 'backward-char)
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key [?\M-a] 'move-beginning-of-line)
  (local-set-key [?\M-e] 'move-end-of-line)
  (local-set-key [?\M-g] 'keyboard-quit)
  )
(add-hook 'c-mode-common-hook 'my-c-mode-common-hook11)

;; compilation mode buffer should wrap lines
(defun my-compilation-mode-hook ()
  (setq truncate-lines nil)
  (local-set-key [?\M-n] 'next-line)
  (local-set-key [?\M-p] 'previous-line)
  (local-set-key [?\M-g] 'keyboard-quit)
  )
(add-hook 'compilation-mode-hook 'my-compilation-mode-hook)

(defun my-shell-mode-common-hook () 
  (local-unset-key "\C-d")
  (local-unset-key (kbd "M-p"))
  (local-unset-key (kbd "M-n"))
  (local-set-key "\M-\r" 'compile)
  (local-set-key "\C-d" 'forward-word)
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key "\C-j" 'backward-char) 
  (local-set-key (kbd "M-p") 'previous-line) 
  (local-set-key (kbd "M-n") 'next-line) 
  )
(add-hook 'shell-mode-hook 'my-shell-mode-common-hook)

;;
;; Byte compiles the _emacs file, saves it as _emacs.elc, and re-loads
;; it too.
;;
(defun byte-me()
  "byte compile _emacs file"
  (interactive)
  (byte-compile-file "c:/Users/burak/Documents/kod/site-lisp/emacs-win.el")
  (load-file "c:/Users/burak/Documents/kod/site-lisp/emacs-win.elc")
  (message "Byte compiling _emacs...Done")
  )

(defun ready-for-blog()
  (interactive)
  (my-untabify)
  (save-excursion (replace-string "<" "&lt;" nil))
  (save-excursion (replace-string ">" "&gt;" nil))
  )

;; Yardimci metot. Bir string icindeki bir karakteri ya da
;; string'i bir baskasi ile degistirir
(defun replace-in-string (string regexp newtext)
  (let ((skip (length newtext))
       (start 0))
    (while (string-match regexp string start)
      (setq string (replace-match newtext t t string)
           start (+ skip (match-beginning 0)))))
  string)

;; Dired icinde iken, o anda baktigimiz dizini explorer
;; icinde acar.
(defun open-explorer-in-current-dir()
  (interactive)
  (defvar komut)
  (setq komut "explorer ")  
  (setq komut (concat komut (dired-current-directory)))
  (setq komut (replace-in-string komut "/" "\\"))
  (message komut)
  (shell-command komut))
  
;; Dired icinde iken, o anda baktigimiz dizinde cmd
;; icinde acar.
(defun open-cmd-in-current-dir()
  (interactive)
  (defvar komut)
  (setq komut "'C:/Program Files/Git/git-bash.exe' --cd=")
  (setq komut (concat komut (dired-current-directory)))
  (setq komut (replace-in-string komut "/" "\\"))
  (setq komut (replace-in-string komut "'" "\""))
  (message komut)
  (async-shell-command komut))

;;
;; UI
;;

(set-frame-position (selected-frame) 33 0) ;emacs, ekranin "neresinde" baslamali

;; butun fontlarin listesi icin, asagidaki komutu *scratch* icinde islet.
;;(insert (prin1-to-string (x-list-fonts "*")))

;; screen, display settings

(set-default-font "-outline-Consolas-normal-normal-normal-mono-*-*-*-*-c-*-iso10646-1")
;;(set-default-font "-outline-Consolas-bold-normal-normal-mono-*-*-*-*-c-*-iso10646-1")
;;(set-default-font "-outline-Consolas-normal-normal-normal-mono-*-*-*-*-c-*-windows-1258")
;;(set-default-font "-outline-Courier New-bold-normal-normal-mono-*-*-*-*-c-*-iso10646-1")
;;(set-default-font "-raster-Courier-normal-normal-normal-mono-25-*-*-*-c-*-iso8859-1")


(set-face-attribute 'default nil :height 115)

(setq default-frame-alist
      '((top . 0) (left . 500)
        (width . 85) (height . 37)
))

(tool-bar-add-item "fwd-arrow" 'revert-buffer 'revert-buffer :help "Refresh" )

;;/usr/share/emacs/23.3/etc/images/diropen.xpm
(tool-bar-add-item "diropen" 'my-dired 'my-dired :help "Open Dired" )

;; when enter is pressed, it automatically indents.
(setq next-line-add-newlines nil)

(require 'font-lock)
;; Maximum colors
(setq font-lock-maximum-decoration t)

;; Turn on font-lock in all modes that support it
(global-font-lock-mode t)

;;
;; File A lists.
;;
(setq auto-mode-alist
      (append '(("\\.C$"   . c++-mode)
		("\\.cc$"  . c++-mode)
		("\\.cpp$" . c++-mode)	
		("\\.m$" . octave-mode)	
		("\\.log$" . hscroll-mode)
		("\\.cxx$" . c++-mode)
		("\\.hxx$" . c++-mode)
		("\\.h$"   . c++-mode)
		("\\.hh$"  . c++-mode)
		("\\.c$"   . c++-mode)
                ("\\.pl$"  . perl-mode)
                ("\\.txt$"  . text-mode)
                ("\\.csv$"  . text-mode)
                ("\\.js" . c++-mode)
                ("\\.htm$" . nxml-mode)
                ("\\.html$" . nxml-mode)
                ("\\.xhtml$" . nxml-mode)
                ("\\.jsp$" . nxml-mode)
                ("\\.xsl$" . nxml-mode)
                ("Makefile" . text-mode)
                ("\\.inc$" . nxml-mode)
                ("\\.xml$" . nxml-mode)
                ("\\.pyx$" . python-mode)
                ("\\.tld$" . nxml-mode)
                ("\\.tld$" . nxml-mode)
		("\\.djvu\\'" . fundamental-mode)
		)
	      auto-mode-alist))

(set-cursor-color "Red")
(setq CUA-mode-normal-cursor-color "Red")
(setq CUA-mode-overwrite-cursor-color "Red")
(setq CUA-mode-read-only-cursor-color "Red")

(setq ring-bell-function 'ignore)

(custom-set-variables
  ;; custom-set-variables was added by Custom.
  ;; If you edit it by hand, you could mess it up, so be careful.
  ;; Your init file should contain only one such instance.
  ;; If there is more than one, they won't work right.
 '(archive-zip-extract (quote ("unzip" "-qq" "-c")))
 '(blink-cursor-mode nil)
 '(case-fold-search t)
 '(compilation-scroll-output t)
 '(cua-mode t nil (cua-base))
 '(current-language-environment "Turkish")
 '(default-input-method "turkish-postfix")
 '(dired-recursive-copies t)
 '(ecb-options-version "2.27")
 '(global-font-lock-mode t)
 '(inhibit-iso-escape-detection t t)
 '(inhibit-startup-screen t)
 '(scroll-conservatively 1)
 '(scroll-step 1)
 '(text-mode-hook (quote (turn-on-auto-fill text-mode-hook-identify)))
 '(transient-mark-mode t)
 '(truncate-lines t)
 '(which-func-mode-global t nil (which-func))
 )

(custom-set-faces
 ;; custom-set-faces was added by Custom -- don't edit or cut/paste it!
 ;; Your init file should contain only one such instance.
 '(font-lock-builtin-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-lock-builtin-name-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-lock-comment-face ((((class color) (background light)) (:foreground "DarkGreen"))))
 '(font-lock-constant-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-lock-keyword-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-lock-string-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-lock-type-face ((((class color) (background light)) (:foreground "Black"))))
 '(font-lock-doc-face  ((((class color) (background light)) (:foreground "DarkGreen"))))
 '(font-lock-variable-name-face ((((class color) (background light)) (:foreground "NavyBlue"))))
 '(font-latex-verbatim-face  ((t (:family "courier" :foreground "Black" :weight bold))))
 )

(set-face-foreground 'font-lock-comment-face "DarkGreen")

;;turn on interactive prompting for code generation
(setq tempo-interactive t)

(tempo-define-template  "py-debug-var" 
 '("print (\"" (p "Enter debug message: " deg) 
   "=\" + str(" (s deg) "))" ) "d" ) 

(tempo-define-template "verb"
 '("\\verb!" (s) "!") "d" "") 

(tempo-define-template  "tex-equation"
 '("%\n$$ " (s) " $$\n%{{x.png}}")  "d"  "") 

(tempo-define-template "tex-verbatim-big"
 '("\\begin{verbatim}\n" (s) "\n\\end{verbatim}") "d"  "") 

(tempo-define-template  "pandas"
 '("import pandas as pd\n\n" "df = pd.read_csv('" (s) "')") "d"     "") 

(tempo-define-template  "pyall"
 '("import pandas as pd\nimport numpy as np\nimport matplotlib.pyplot as plt\n\n" (s) ) "")
 
(tempo-define-template  "reg"
 '("import statsmodels.formula.api as smf\n" "results = smf.ols('"
  (s) "', data=df).fit()\n" "print results.summary()") "d"  "") 

(tempo-define-template  "tex-eqnarray"
 '("\\begin{equation}\\label{}\n" (s) "\n\\end{equation}\n") "d"    "") 

(tempo-define-template  "tex-itemize"
 '("\\begin{itemize}\n   \\item " (s) "\n\\end{itemize}\n") "d"     "") 

(tempo-define-template  "tex-enumerate"
 '("\\begin{enumerate}\n  \\item " (s) "\n\\end{enumerate}\n") "d"  "") 

(tempo-define-template  "tex-vector"
 '("\\left[\\begin{array}{ccc} " (s) " \\end{array}\\right]^T") "d" "") 

(tempo-define-template  "tex-graphics-2"
 '("\\includegraphics[height=4cm]{" (s) ".png}" ) "")

(tempo-define-template  "tex-listings-python-file" 
 '("\\inputminted[fontsize=\\footnotesize]{python}{"  (s) ".py}"  ) "")

(tempo-define-template  "tex-matrix" 
 '("\\left[\\begin{array}{rrr}\n" (s) "\n\\end{array}\\right]" )    "")

(tempo-define-template  "tex-function-in-pieces" 
 '("\\left\\{ \\begin{array}{ll}\n" (s) "\n\\end{array} \\right." ) "")

(tempo-define-template  "tex-partial-derivative" 
 '("\\frac{\\partial "   (s)   "}{\\partial }"   ) "")

(tempo-define-template  "equation-multiline" 
 '("\$$ \n \\begin{array}{lll}\n "   (s)   "\n\\end{array}" "$$"  ) "")

(tempo-define-template  "new-equation-page"	
 '("\\documentclass[12pt,fleqn]{article}\\usepackage{../../common}\n"
   "\\begin{document}\n"   (s)   "\n\\end{document}\n"   )          "")

(tempo-define-template  "python-main" 
 '("if __name__ == \"__main__\": \n "   (s)   ""   )                "")

(set-default 'truncate-lines t)


;; Bu fonksiyon, uzerinde oldugunuz kelimeyi otomatik olarak
;; metin icinde aramaya baslar. "Bu kelimeden baska nerede var"
;; seklindeki aramalar icin birebir bir fonksiyon. Kod gezerken
;; iyi
(defun my-isearch-yank-word ()  
  (interactive)
  (isearch-mode t nil nil nil)
  (if (= (length isearch-string) 0)
      (progn (forward-word 1)
	     (backward-word 1)))
  (isearch-yank-word))


(defun tex-itemize (start end)
  "put begin and end itemize tex commands."
  (interactive "r")
  (goto-char start)
  (previous-line 1)
  (insert "\n\\begin\{itemize\}")
  (goto-char end)
  (next-line 1)
  (insert "\\end\{itemize\}\n")  
  )

;;
;; Key mappings
;;
(define-key global-map "\C-m" 'newline-and-indent)
(global-unset-key "\C-x\C-e")
(global-unset-key "\C-f")
(global-unset-key "\C-w")
(global-unset-key "\C-d")
(global-unset-key "\C-b")
(global-unset-key "\C-l")
(global-unset-key "\C-p")
(global-unset-key "\C-j")
(global-unset-key "\C-k")
(global-unset-key "\C-o")
(global-unset-key [(alt f4)])
(global-set-key "\C-x\C-i" 'tempo-template-py-debug-var)
(global-set-key "\C-o" 'other-window)
(global-set-key "\M-p" 'previous-line)
(global-set-key "\C-p" 'previous-line)
(global-set-key "\C-k" 'backward-delete-char-untabify)
(global-set-key "\M-k" 'backward-delete-char-untabify)
(global-set-key "\C-n" 'next-line)
(global-set-key "\M-n" 'next-line)
(global-set-key "\C-j" 'backward-char)
(global-set-key [?\M-j] 'backward-char)
(global-set-key [?\M-l] 'backward-char)
(global-set-key "\C-f" 'my-backward-delete-word)
(global-set-key "\M--" 'undo)
(global-set-key "\M-f" 'my-backward-delete-word)
(global-set-key "\C-w" 'backward-word)
(global-set-key "\C-d" 'forward-word)
(global-set-key "\C-l" 'forward-char)
(global-set-key [?\M-d] 'forward-word)
(global-set-key [?\M-w] 'backward-word)
(global-set-key "\C-p" 'previous-line)
(global-set-key "\C-t" 'kill-line)
(global-set-key "\C-x\c" 'compile)
(global-set-key "\M-\r" 'compile)
(global-set-key [?\C-=] 'indent-region)
(global-set-key [?\M-=] 'indent-region)
(global-set-key [?\C--] 'undo)
(global-set-key "\C-c\C-b" 'byte-me)
(global-set-key "\C-c\k" 'kill-region)
(global-set-key "\M-l" 'forward-char)
(global-set-key "\C-x\C-r" 'scroll-cursor-to-top)
(global-set-key "\M-u" 'universal-argument)
(global-set-key "\C-x\C-p" nil)
(global-set-key "\C-x\C-n" nil)
(global-set-key "\C-x\q" 'query-replace)
(global-set-key "\C-c\C-g" 'grep-find)
(global-set-key "\C-x\g" 'goto-line)
(global-set-key [?\M-m] 'scroll-up)
(global-set-key [?\C-,] 'scroll-up)
(global-set-key [?\M-q] 'scroll-down)
(global-set-key [?\M-o] 'other-window)
(global-set-key [?\M-a] 'move-beginning-of-line)
(global-set-key [?\M-e] 'move-end-of-line)
(global-set-key [?\C-1] 'scroll-down)
(global-set-key [?\C-q] 'scroll-down)
(global-set-key [?\C-0] 'delete-window)
(global-set-key "\C-v" 'scroll-up)
(global-set-key "\M-v" 'scroll-up)
(global-set-key [mouse-3] 'kill-current-buffer)
(global-set-key "\M-1" 'delete-other-windows)
(global-set-key "\M-2" 'scroll-cursor-to-top)
(global-set-key "\M-3" 'save-buffer)
(global-set-key "\M-5" 'kill-rectangle)
(global-set-key "\M-8" 'kill-current-buffer)
(global-set-key "\M-9" 'kill-current-buffer)
(global-set-key "\M-r" 'isearch-backward)
(global-set-key "\M-s" 'isearch-forward)
(global-set-key "\M-[" 'fill-paragraph)
(global-set-key "\M-'" 'comment-region)
;;(global-set-key "\M-"  'beginning-of-buffer)
(global-set-key "\M-]" 'recenter)
(global-set-key "\C-c\C-c" 'comment-region)
(global-set-key "\M-t" 'kill-line)
(global-set-key "\M-y" 'cua-paste)
(global-set-key "\M-c" 'cua-copy-region)
(global-set-key "\M-'" 'cua-cut-region)
(global-set-key "\M-\\" 'cua-cut-region)
(global-set-key "\C-c\C-t" 'open-file-ext)
(define-key isearch-mode-map [?\M-s] 'isearch-repeat-forward)
(define-key isearch-mode-map [?\M-r] 'isearch-repeat-backward)
(define-key universal-argument-map [?\M-u] 'universal-argument-more)
(global-set-key [?\M-h] 'dabbrev-expand)
(global-set-key [?\M-g] 'keyboard-escape-quit)
(global-set-key "\M- " 'cua-set-mark)
(global-set-key "\C-h" 'dired)
(global-set-key "\C-\M-y" 'yank-pop)

(defun scroll-cursor-to-top()
  (interactive)
  (recenter 3)
  (next-line 3)
  )

(defun my-untabify ()
  (save-excursion
    (goto-char (point-min))
    (while (re-search-forward "[ \t]+$" nil t)
      (delete-region (match-beginning 0) (match-end 0)))
    (goto-char (point-min))
    (if (search-forward "\t" nil t)
	(untabify (1- (point)) (point-max))))
  nil)

;;
;; define F keys
;;
(global-set-key [f1] 'tempo-template-tex-listings-python)
(global-set-key [f2] 'tempo-template-verb)
(global-set-key [\M-f4] 'tempo-template-tex-equation)
(global-set-key [f8] 'mark-whole-buffer)
(global-set-key [f10] 'open-explorer-in-current-dir)
(global-set-key [f12] 'next-error)

;; go to iimage.el and comment out the code that sets
;; iimage-mode-image-regex-alist. Instead use this
;; 
;;(defcustom iimage-mode-image-regex-alist 
;;  `((,(concat "\\(%\n$$[[:ascii:][:nonascii:]]*?$$\n%{{\\)"
;;	      "\\(" iimage-mode-image-filename-regex "\\)"
;;	      "\\(}}\\)") . 2))
;;
;; do byte-compile-file, ki so {{dosya.png}} is rendered
;;
;; formulas need to be like this:
;; %
;; $$ stuff, x^2, \cos $$
;; the % sign above $$ sign is key

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CUA mod
;;
;; [Control v] needs to be canceled in any new installation of Emacs
;; in cua-base.el comment out this
;; 
;; (define-key cua--cua-keys-keymap [(control v)] 'yank)
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Dired
;;
;; get sources from ftp.gnu.org
;;
;; (defun dired-other-window degisecek - switch-to-buffer-other-window
;; becomes switch-to-buffer
;;
;; and
;;
;; in dired.el, function dired-mouse-find-file-other-window() change the line
;; find-file-other-window call to find-file. 
;;
;;
(load-file "c:/Users/burak/Documents/kod/site-lisp/dired.el")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Tex mode icin not: change tex-simple.el dosyasinda \M-\r mapping'ini
;; iptal et boylece bizim tanimladigimiz aktif olsun. 
;;

(put 'upcase-region 'disabled nil)
(put 'downcase-region 'disabled nil)

;; ;; Pymacs
(load-file "c:/Users/burak/Documents/kod/site-lisp/pymacs/pymacs.el")

(autoload 'pymacs-apply "pymacs")
(autoload 'pymacs-call "pymacs")
(autoload 'pymacs-eval "pymacs" nil t)
(autoload 'pymacs-exec "pymacs" nil t)
(autoload 'pymacs-load "pymacs" nil t)

(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/peval")
(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/deascify")
(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/news")
(global-set-key "\M-," 'peval-pexec)
(global-unset-key "\M-]")
(global-set-key "\C-x\]" 'deascify-convert)
(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/preview")
(global-set-key "\M-4" 'iimage-mode)
(global-set-key "\M-$" 'preview-show)
(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/githist")
(defun githist-do-show-version(num) 
 (interactive "nHow many commits back: ")
  (githist-show-version num)
  )

;;(require 'auto-complete)
;;(global-auto-complete-mode t)

(setq inferior-lisp-program "sbcli")

(add-to-list 'compile-history compile-command)
(add-to-list 'compile-history "python ../build.py tex")

(defun reload-pymacs()
  (interactive)
  (if (buffer-live-p (get-buffer "*Pymacs*" ))
      (kill-buffer (get-buffer "*Pymacs*")))
  (message (buffer-file-name (current-buffer)))
  ;;
  ;; load tex or md mode based on the extension
  (if (equal (file-name-extension (buffer-file-name (current-buffer))) "tex")
      (progn 
	(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/ipython-tex")
	(global-set-key "\M-," 'ipython-tex-run-py-code)
	(global-set-key [f5] 'ipython-tex-complete-py)
	(tempo-define-template 
	 "tex-listings-python" 
	 '("\\begin{minted}[fontsize=\\footnotesize]{python}\n"
	   (s)
	   "\n\\end{minted}\n"
	   )
	 "")	
	))
  (if (equal (file-name-extension (buffer-file-name (current-buffer))) "md")
      (progn 
	(pymacs-load "c:/Users/burak/Documents/kod/site-lisp/ipython-md")
	(global-set-key "\M-," 'ipython-md-run-py-code)
	(global-set-key [f5] 'ipython-md-complete-py)
	(tempo-define-template 
	 "tex-listings-python" 
	 '("```python\n"
	   (s)
	   "\n```\n"
	   )
	 "")	
	))

  )

(global-set-key [f11] 'reload-pymacs)

(fset 'tex-font-lock-suscript 'ignore)

(when (or (eq system-type 'windows-nt) (eq system-type 'msdos))
  (setenv "PATH" (concat "C:\\GNU\\gnuwin32\\bin;" (getenv "PATH")))
  (setq grep-find-command "c:\\Users\\burak\\Documents\\kod\\find.bat *.* "
	grep-program ""
	))

;; ;; open files / directories beforehand so they are already in the buffer
;;
(find-file-other-window "C:/Users/burak/AppData/Local/Temp")
(find-file-other-window "c:/Users/burak/Documents/kod/guide")
(find-file-other-window "c:/Users/burak/Documents/classnotes/app_math/dict")
(find-file-other-window "c:/Users/burak/Documents/Dropbox")
(find-file-other-window "c:/Users/burak/Documents/Dropbox/TODO.txt")
(find-file-other-window "c:/Users/burak/Documents/classnotes")
(find-file-other-window "c:/Users/burak/Documents/classnotes/vision")
(find-file-other-window "c:/Users/burak/Documents/classnotes/chaos")
(find-file-other-window "c:/Users/burak/Documents")
(find-file-other-window "c:/Users/burak/Documents/kod")
(find-file-other-window "c:/Users/burak/Downloads")
(find-file-other-window "c:/Users/burak/Documents/bass")

(switch-to-buffer "*scratch*")
(delete-other-windows)

