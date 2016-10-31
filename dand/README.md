**D**ynamic **A**pplication **N**ester **D**eamon, `dand`, aims to
combine parallel job runner, monitoring utility like `supervisord` but
is easy to use, with scheduling features of cron without its
cumbersome syntax.

All config will be through yaml files; unit of execution for a group
of executables / scripts is the conf file. The usage is

`python dand.py file.conf`

In the simplest scenario there are only a list of programs to be
executed in the conf file, in this case `dand` will read them, and run
them in parallel. But in addition, through a config parameter, dand
can watch them, restart them in case of fail, as many times as
required, and notify somewhere (TBD) if the fail persists after N many
restarts.

If schedule information is attached, dand will wait until the
scheduled time, and run the program then.

Simple config:

```
process1: 
   exec: /usr/bin/some/script arg1 arg2
   restart: 1
process2: 
   exec: /usr/bin/some/script arg3
   restart: 2
process3: 
   exec: /usr/bin/another/script
   restart: forever
```

Scheduled config:

```
myprocess: 
   exec: /bin/some/program
   restart: 2
   schedule:
     hour: 10
     minute: 00
     weekday: Monday
```