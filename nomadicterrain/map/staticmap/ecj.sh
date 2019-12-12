DIR=$HOME/Documents/kod/nomadicterrain/map/staticmap
ecj -cp $DIR/build:$DIR/lib/kxml2-2.3.0.jar:$DIR/lib/mapsforge-core-0.12.0.jar:\
$DIR/lib/mapsforge-map-0.12.0.jar:$DIR/lib/mapsforge-map-awt-0.12.0.jar:\
$DIR/lib/mapsforge-map-reader-0.12.0.jar:$DIR/lib/mapsforge-themes-0.12.0.jar:\
$DIR/lib/svg-salamander-1.0.jar \
src/SaveTiles.java 
