cd $HOME/Downloads/UrbanTerror
while true
do
    ./ioUrTded.i386 \
	+set fs_game q3ut4 \
	+set dedicated 1 \
	+set net_port 27960 \
	+set com_hunkmegs 128 \
	+exec server.cfg \
	+exec bots.cfg
    

echo "server crashed on `date`" > last_crash.txt
done
