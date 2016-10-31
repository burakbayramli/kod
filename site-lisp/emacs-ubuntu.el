(set-language-environment "UTF-8")

(autoload 'c++-mode "cc-mode" "C++ Editing Mode" t) 
(autoload 'c-mode "c-mode" "C mode" t)
(load "preview-latex.el" nil t t)

(setq initial-scratch-message nil) ;; empty scratch buffer
(setq max-specpdl-size 50000)
(setq max-lisp-eval-depth 50000)
(setq tool-bar-mode -1)
(setq auto-resize-tool-bars -1) 
(setq compile-command "python build.py ")
(setq x-select-enable-clipboard t)
(setq interprogram-paste-function 'x-cut-buffer-or-selection-value)
(setq shell-file-name "bash")
(setq shell-command-switch "-ic")
(setq my-python-command "/home/burak/anaconda/bin/python")

(set-variable (quote latex-run-command) "pdflatex")
(set-variable (quote tex-dvi-view-command) "xpdf")

(defun move-back-one-char ()  
  (interactive)                  
  (backward-char 1)              
  )

(defun move-back-three-chars ()  
  (interactive)                  
  (backward-char 3)              
  )

(defun move-back-four-chars ()  
  (interactive)                  
  (backward-char 4)              
  )

(defun move-back-seven-chars ()  
  (interactive)                  
  (backward-char 7)              
  )

;; set name of abbrev file with .el extension
(setq abbrev-file-name "/usr/share/emacs/site-lisp/abbrevs.el")
(setq-default abbrev-mode t)
(setq save-abbrevs nil)

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

;;(load-file "/usr/share/emacs/site-lisp/weblogger.el")
(require 'java-mode-indent-annotations)
(require 'nxml-mode)
(load-file "/usr/share/emacs/site-lisp/_latin_post_ek.elc")
(require 'tempo)
(require 'julia-mode)

(setq bell-volume 0)
;;(set-message-beep 'silent)
(setq indent-tabs-mode nil)

(setq TeX-master-file-ask nil)

(add-to-list 'load-path "/usr/share/emacs/site-lisp/python-mode.el-6.0.10") 
(setq py-install-directory "/usr/share/emacs/site-lisp/python-mode.el-6.0.10")
(autoload 'autopair-global-mode "autopair" nil t)
;;(autopair-global-mode)
(add-hook 'lisp-mode-hook
          #'(lambda () (setq autopair-dont-activate t)))

(custom-set-variables
  ;; custom-set-variables was added by Custom -- don't edit or cut/paste it!
  ;; Your init file should contain only one such instance.
 '(archive-zip-extract (quote ("unzip" "-qq" "-c")))
 '(case-fold-search t)
 '(preview-scale-function 1.3)
 '(preview-auto-cache-preamble nil)
 '(preview-image-type (quote dvipng)) 
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
  (interactive)
  (other-window 1)
  (list-buffers)
  (other-window 1)
  )
(define-key global-map "\C-x\C-b" 'benim-list-buffers)
(global-set-key "\M-b" 'benim-list-buffers)



;;
;; PRINTING
;;
;;
;; enscript -r -C -2 -j --pretty-print=java -r -v --lines-per-page=120 -pout.ps CLASS.java
;;

;;
;; Python kodlara pretty print yapar
;; "sudo apt-get install enscript" gerekiyor
;; landscape mod iki sayfa yanyana, satir numaralarini da hesaplayarak basar
;;
(require 'ps-print)
(setq ps-paper-type 'a4)
(setq ps-lpr-command "enscript ")
(setq ps-lpr-switches '("-r -c -C -2 -j --pretty-print=python -r -v --lines-per-page=120")) 
(setq ps-lpr-buffer "-p/tmp/out.ps ")	; a tmp spool file

(defun enscript-print ()
  (interactive)
  (global-font-lock-mode nil)  
  (shell-command
   (apply 'concat (append (list ps-lpr-command " ")
			  ps-lpr-switches
			  (list " " ps-lpr-buffer (buffer-file-name (current-buffer)))
			  ))
   )
  (global-font-lock-mode t)
  )

(define-key global-map "\C-cp" 'enscript-print)


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
  (find-file "/usr/share/emacs/site-lisp/emacs-ubuntu.el"))
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
  "Kill the current buffer, without confirmation."
  (interactive)
  (setq compile-command "/home/burak/anaconda/envs/py3k/bin/python build.py ")
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
   ))
(easy-menu-add my-jde-mode-menu)

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
(defun x-tex-new-page()
  (interactive)
  (tempo-template-tex-sr)
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
   ["Tex Graphics" tempo-template-tex-graphics]
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
	;;ispell-skip-html t
        )
  )
(add-hook 'nxml-mode-hook 'my-nxml-mode-hook)  

(defun my-tex-mode-hook ()
  (local-set-key "\C-j" 'backward-char) 
  (local-set-key "\C-s" 'isearch-forward) 
  (local-unset-key "\M-s") 
  (local-set-key [?\M-g] 'keyboard-quit)
  (local-set-key "\M-\r" 'compile)
  (local-set-key "\M-s" 'isearch-forward) 
  (local-set-key "\C-v" 'scroll-up)  
  (setq fill-column 80) 
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
(defun my-dired-mode-hook ()  "Bind some private functions."
  (local-set-key "\C-o" 'other-window)
  (local-set-key "r" 'dired-up-directory)
  (local-set-key [?\M-g] 'keyboard-quit)  
  (local-unset-key "\M-s") 
  (local-set-key "\M-s" 'isearch-forward) 
  )
(add-hook 'dired-mode-hook 'my-dired-mode-hook)
(defun my-buffer-mode-hook ()  "Bind some private functions."
  (local-set-key "\C-o" 'other-window)	
  (local-set-key [mouse-1] 'Buffer-menu-mouse-select)
  (local-set-key [?\M-g] 'keyboard-quit)
;;  (local-set-key "\M-\d" 'save-buffers-kill-emacs)
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
;;
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
  (byte-compile-file "/usr/share/emacs/site-lisp/emacs-ubuntu.el")
  (load-file "/usr/share/emacs/site-lisp/emacs-ubuntu.elc")
  (message "Byte compiling _emacs...Done")
  )

(defun reload-all-buffers()
  (interactive)
  (cleanup)
  (defvar b)
  (mapcar (function
	   (lambda (buf)
	     (setq b (buffer-file-name buf))
	     (kill-buffer buf)
	     (message b)
	     (find-file (file-name-sans-versions b))
	     ))
	  (buffer-list "*.java"))  
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
  (setq komut "nautilus ")  
  (setq komut (concat komut "'" ))
  (setq komut (concat komut (dired-current-directory)))
  (setq komut (concat komut "'" ))
  (setq komut (concat komut " 2> /dev/null "))
  (message komut)
  (shell-command komut))
  
;; Dired icinde iken, o anda baktigimiz dizinde cmd
;; icinde acar.
(defun open-cmd-in-current-dir()
  (interactive)
  (defvar komut)
  (setq komut "gnome-terminal --working-directory=")
  (setq komut (concat komut (dired-current-directory)))
  (setq komut (concat komut " &"))
  (shell-command komut))

;;
;; UI
;;

(set-frame-position (selected-frame) 40 0) ;emacs, ekranin "neresinde" baslamali

;; butun fontlarin listesi icin, asagidaki komutu *scratch* icinde islet.
;;(insert (prin1-to-string (x-list-fonts "*")))

;; screen, display settings

;;(set-default-font "-unknown-LMMono10-normal-normal-normal-*-*-*-*-*-*-0-iso10646-1" )
;;(set-default-font "-unknown-LMMonoLt10-bold-normal-normal-*-*-*-*-*-*-0-iso10646-1")
;;(set-default-font "-unknown-Liberation Mono-bold-normal-normal-*-*-*-*-*-m-0-iso10646-1" )
;;(set-default-font "-unknown-DejaVu Sans Mono-normal-normal-normal-*-*-*-*-*-m-0-iso10646-1" )
;;(set-default-font "-unknown-FreeMono-bold-bold-normal-*-*-*-*-*-m-0-iso10646-1"  )
;;(set-default-font "-unknown-FreeMono-bold-normal-normal-*-*-*-*-*-m-0-iso10646-1"  )
;;(set-default-font "-misc-fixed-medium-r-normal--6-*-75-75-c-40-iso8859-7")
(set-default-font "-bitstream-Courier 10 Pitch-normal-normal-normal-*-*-*-*-*-m-0-iso10646-1")

(set-face-attribute 'default nil :height 110)

(setq default-frame-alist
      '((top . 0) (left . 500)
        (width . 80) (height . 40)
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
                ("\\.jl\\'" . julia-mode)
                ("\\.tld$" . nxml-mode)
                ("\\.pdf$" . doc-view-mode)
                ("\\.?[Ff][Aa][Qq]$" . faq-mode)
		)
	      auto-mode-alist))

(set-cursor-color "Red")
(setq CUA-mode-normal-cursor-color "Red")
(setq CUA-mode-overwrite-cursor-color "Red")
(setq CUA-mode-read-only-cursor-color "Red")

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
 '(preview-LaTeX-command (quote ("%`%l -shell-escape \"\\nonstopmode\\nofiles\\PassOptionsToPackage{" ("," . preview-required-option-list) "}{preview}\\AtBeginDocument{\\ifx\\ifPreview\\undefined" preview-default-preamble "\\fi}\"%' %t")))
 '(preview-auto-cache-preamble nil)
 '(preview-image-type (quote dvipng))
 '(preview-scale-function 1.3)
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

;;(add-to-list 'LaTeX-verbatim-macros-with-braces-local "url")  

(set-face-foreground 'font-lock-comment-face "DarkGreen")
;;(set-face-foreground 'modeline "black") 

;;turn on interactive prompting for code generation
(setq tempo-interactive t)

(tempo-define-template 
 "py-debug-var" 
 '("print \""         
   (p "Enter debug message: " deg) 
   "=\" + str("
   (s deg)
   ")"					
   )
 "d" 
 "Inserts a print debug message") 

(tempo-define-template 
 "verb" 
 '("\\verb!"
   (s)
   "!") 
 "d" 
 "") 

(tempo-define-template 
 "tex-equation" ;; 
 '("$$ "
   (s)
   " $$") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-equation-numbered" ;; 
 '("\\begin{equation}\\label{}\n"
   (s)
   "\n\\end{equation}") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-verbatim-big" ;; 
 '("\\begin{verbatim}\n"
   (s)
   "\n\\end{verbatim}") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "pandas" ;; 
 '("import pandas as pd\n\n"
   "df = pd.read_csv('"
   (s)
   "')") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "reg" ;; 
 '("import statsmodels.formula.api as smf\n"
   "results = smf.ols('"
   (s)
   "', data=df).fit()\n"
   "print results.summary()")
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "figure-ref" 
 '("Figure~\\ref{"         
   (p "Enter figure: ") 
   "}") 
 "d" 
 "Inserts a print debug message") 

(tempo-define-template 
 "tex-eqnarray" ;; 
 '("\\begin{equation}\\label{}\n"
   (s)
   "\n\\end{equation}\n") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-itemize" ;; 
 '("\\begin{itemize}\n   \\item "
   (s)
   "\n\\end{itemize}\n") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-enumerate" ;; 
 '("\\begin{enumerate}\n   \\item "
   (s)
   "\n\\end{enumerate}\n") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-sr" ;; 
 '("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\\sr{"
   (s)
   "}") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-vector" ;; 
 '("\\left[\\begin{array}{ccc} "
   (s)
   " \\end{array}\\right]^T") 
 "d" ;; 
 "") ;; 

(tempo-define-template 
 "tex-graphics"	;; 
 '("\\begin{figure}[!hbp]\n"
   "\\center{\n"
   "  \\scalebox{0.55}{\n"
   "  \\includegraphics{"
   (s)
   "}\n"
   "  }\n"
   "}\n"
   "\\caption{}\n"
   "\\end{figure}\n"
   )
 "d"
 "")

(tempo-define-template 
 "tex-graphics-2" 
 '("\\includegraphics[height=4cm]{"
   (s)
   ".png}"
   )
 "")

(tempo-define-template 
 "tex-listings-python-file" 
 '("\\inputminted[fontsize=\\footnotesize]{python}{"
   (s)
   ".py}"
   )
 "")


(tempo-define-template 
 "tex-matrix" 
 '("\\left[\\begin{array}{rrr}\n"
   (s)
   "\n\\end{array}\\right]"
   )
 "")

(tempo-define-template 
 "tex-function-in-pieces" 
 '("\\left\\{ \\begin{array}{ll}\n"
   (s)
   "\n\\end{array} \\right."
   )
 "")

(tempo-define-template 
 "tex-partial-derivative" 
 '("\\frac{\\partial "
   (s)
   "}{\\partial }"
   )
 "")

(tempo-define-template 
 "equation-multiline" 
 '("\$$ \n \\begin{array}{lll}\n "
   (s)
   "\n\\end{array}"
   "$$"
   )
 "")

(tempo-define-template 
 "new-equation-page"	;; 
 '("\\documentclass[12pt,fleqn]{article}\\usepackage{../common}\n"
   "\\begin{document}\n"
   (s)
   "\n\\end{document}\n"
   )
 "")

(tempo-define-template 
 "python-main" 
 '("if __name__ == \"__main__\": \n "
   (s)
   ""
   )
 "")

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
(global-set-key "\M-4" 'preview-at-point)
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
(global-set-key "\C-x\C-i" 'tempo-template-py-debug-var)
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
;;(global-set-key "\M-\d" 'save-buffers-kill-emacs)
(global-set-key [f1] 'tempo-template-tex-listings-python)
(global-set-key [f2] 'tempo-template-verb)
(global-set-key [f4] 'tempo-template-tex-equation)
(global-set-key [f6] 'reload-all-buffers)
(global-set-key [f8] 'mark-whole-buffer)
(global-set-key [f10] 'open-explorer-in-current-dir)
(global-set-key [f12] 'next-error)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CUA mod'u icin not
;;
;; [Control v] needs to be canceled in any new installation of Emacs
;; in cua-base.el comment out this
;; 
;; (define-key cua--cua-keys-keymap [(control v)] 'yank)
;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Dired icin not: change dired.el
;;
;; get sources from ftp.gnu.org
;;
;; (defun dired-other-window degisecek - switch-to-buffer-other-window
;; becomes switch-to-buffer
;;
;; and
;;
;; in function dired-mouse-find-file-other-window() change the line
;; find-file-other-window call to find-file. 
;;
;; recompile using byte-compile-file, and load it like this
;;
(load-file "/usr/share/emacs/site-lisp/dired.elc")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; Tex mode icin not: change tex-simple.el dosyasinda \M-\r mapping'ini
;; iptal et boylece bizim tanimladigimiz aktif olsun. 
;;

(put 'upcase-region 'disabled nil)
(put 'downcase-region 'disabled nil)

;; ;; Pymacs

(autoload 'pymacs-apply "pymacs")
(autoload 'pymacs-call "pymacs")
(autoload 'pymacs-eval "pymacs" nil t)
(autoload 'pymacs-exec "pymacs" nil t)
(autoload 'pymacs-load "pymacs" nil t)

;; ;; surekli gereken dizinler onceden acilip buffer'a konuyor.
;; ;; boylece ikidebir oraya dired'den gezerek gitmek gerekmez
;; ;;
(find-file-other-window "/tmp")
(find-file-other-window "/home/burak/Desktop")
(find-file-other-window "/home/burak/Dropbox/TODO.txt")
(find-file-other-window "/home/burak/Dropbox")
(find-file-other-window "/home/burak/kod/books")
(find-file-other-window "/home/skorsky/mindmeld/doc/details")
(find-file-other-window "/home/skorsky/mindmeld/data")
(find-file-other-window "/home/burak/Downloads")
(find-file-other-window "/home/burak/Documents")
(find-file-other-window "/home/burak/Documents/fin/foam")
(find-file-other-window "/home/burak/Documents/fin/foam/notebooks/systematic")
(find-file-other-window "/home/burak/Documents/classnotes")
(find-file-other-window "/home/burak/Documents/classnotes/app_math/dict")
(find-file-other-window "/home/burak/Downloads/pysystemtrade")

(switch-to-buffer "*scratch*")
(delete-other-windows)

(pymacs-load "/usr/share/emacs/site-lisp/githist")
(defun githist-do-show-version(num) 
 (interactive "nHow many commits back: ")
  (githist-show-version num)
)

;;(require 'auto-complete)
;;(global-auto-complete-mode t)

(setq inferior-lisp-program "sbcli")

(add-to-list 'compile-history compile-command)

(defvar previewable-environments
  "List of environments that should be previewed."
  '("tabular" "tabular*" "tikzpicture" "..."))

(defadvice preview-region (around preview-at-point-no-long-pauses activate)
  "Make `preview-at-point' a no-op if mark is inactive and point is not on a preview."
  (message "preview-region")
  (if (or (not (eq this-command 'preview-at-point))
            (TeX-active-mark)
            (texmathp)
            (member (LaTeX-current-environment) previewable-environments))
    ad-do-it
    (preview-section)
    )
  )

(defun reload-pymacs()
  (interactive)
  (if (buffer-live-p (get-buffer "*Pymacs*" ))
      (kill-buffer (get-buffer "*Pymacs*")))
  (message (buffer-file-name (current-buffer)))
  (message (file-name-extension (buffer-file-name (current-buffer))))
  (pymacs-load "/usr/share/emacs/site-lisp/deascify")
  (global-set-key "\M-]" 'deascify-convert)
  ;;
  ;; load tex or md mode based on the extension
  (if (equal (file-name-extension (buffer-file-name (current-buffer))) "tex")
      (progn 
	(pymacs-load "/usr/share/emacs/site-lisp/ipython-tex")
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
	(pymacs-load "/usr/share/emacs/site-lisp/ipython-md")
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

