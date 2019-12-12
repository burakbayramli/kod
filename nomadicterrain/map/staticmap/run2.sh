DIR=$HOME/Documents/kod/nomadicterrain/map/staticmap
dalvikvm -cp $DIR/build/build.dex:$DIR/lib/kxml2-2.3.0.dex:$DIR/lib/mapsforge-core-0.12.0.dex:\
$DIR/lib/mapsforge-map-0.12.0.dex:$DIR/lib/mapsforge-map-awt-0.12.0.dex:\
$DIR/lib/mapsforge-map-reader-0.12.0.dex:$DIR/lib/mapsforge-themes-0.12.0.dex:\
$DIR/lib/svg-salamander-1.0.dex \
SaveTiles $1 $2 $3 $4
