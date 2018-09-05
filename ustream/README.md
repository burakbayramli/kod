
Simple distributed map/reduce based on open_stream. Mappers divide up
files(s) (based on # of machines in shared simple conf), write them to
reducers, over socket. Reducers read data over socket (stream read
over open_stream), each reducer is a seperate python function, process
<key,line>, pass it over to next stage.

Unit of distribution is key, obviously, based on mod(hash(key), N)
where N is # of processes - S servers per H hosts, N = S*H.

Same mod(hash(key), N) goes to the same machine, where reducing is
done.

We have different host:port for each "def reduce_" method in a "work
file", where work file is a class which captures a self-contained unit
of work.

Final output(s) goes to file(s). 

Deployment: send same conf file to each machine. run python
http://batchstreamr.py <conf>. It reads file, finds self in host list,
starts S servers. Ports start from a base, like 12000. Construct
host:server:port list, mod takes u to the right one.

Some work unit files (classes) can become standardized, one for
KMeans, one for PCA, one for SQLJoins, etc.

# Installation

Requires Python 3

virtualenv -p /usr/bin/python3 py3b

source py3b/bin/activate

python setup.py install






